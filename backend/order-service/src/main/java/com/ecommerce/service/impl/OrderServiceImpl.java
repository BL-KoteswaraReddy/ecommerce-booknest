// OrderService.java
package com.ecommerce.service.impl;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderResponseDTO;
import com.ecommerce.dto.OrderStatusUpdateRequest;
import com.ecommerce.dto.PlaceOrderRequest;
import com.ecommerce.service.OrderService;
import org.springframework.data.domain.Pageable;

// OrderServiceImpl.java
import com.ecommerce.client.BookServiceClient;
import com.ecommerce.client.CartServiceClient;
import com.ecommerce.client.WalletServiceClient;
import com.ecommerce.dto.*;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMode;
import com.ecommerce.dto.event.NotificationEvent;
import com.ecommerce.exception.InvalidOrderStatusException;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.exception.OrderServiceException;
import com.ecommerce.exception.PaymentFailedException;
import com.ecommerce.messaging.NotificationEventPublisher;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final CartServiceClient cartServiceClient;
    private final BookServiceClient bookServiceClient;
    private final WalletServiceClient walletServiceClient;
    private final NotificationEventPublisher notificationEventPublisher;
    
    @Override
    public OrderDTO placeOrder(PlaceOrderRequest request) {
        log.info("Placing order for user: {}", request.getUserId());
        
        // Fetch cart details
        CartCheckoutDTO cart = cartServiceClient.getCartForCheckout(request.getUserId());
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new OrderServiceException("Cart is empty");
        }
        
        // Create order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(cart.getTotalAmount());
        order.setFinalAmount(cart.getTotalAmount()); // Add discount logic if needed
        order.setShippingCharge(0.0);
        order.setDiscountAmount(0.0);
        order.setOrderNotes(request.getOrderNotes());
        
        // Set payment mode
        if ("CASH_ON_DELIVERY".equalsIgnoreCase(request.getPaymentMode())) {
            order.setPaymentMode(PaymentMode.CASH_ON_DELIVERY);
            order.setPaymentStatus("PENDING");
            order.setStatus(OrderStatus.PENDING);
        } else if ("WALLET".equalsIgnoreCase(request.getPaymentMode())) {
            order.setPaymentMode(PaymentMode.WALLET);
            // Process wallet payment
            boolean paymentSuccess = walletServiceClient.processWalletPayment(
                request.getUserId(), 
                order.getFinalAmount(), 
                order.getOrderNumber(),
                request.getUserEmail()
            );
            if (!paymentSuccess) {
                throw new PaymentFailedException("Wallet payment failed");
            }
            order.setPaymentStatus("COMPLETED");
            order.setStatus(OrderStatus.CONFIRMED);
        } else {
            throw new OrderServiceException("Invalid payment mode");
        }
        
        // Save address
        Address address = convertToAddressEntity(request.getShippingAddress());
        address = addressRepository.save(address);
        order.setShippingAddress(address);
        
        // Create order items from cart items
        for (CartItemInfoDTO cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setBookId(cartItem.getBookId());
            orderItem.setBookTitle(cartItem.getBookTitle());
            orderItem.setBookAuthor(cartItem.getBookAuthor());
            orderItem.setBookCoverImage(cartItem.getBookCoverImage());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setTotalPrice(cartItem.getTotalPrice());
            orderItem.setOrder(order);
            order.getItems().add(orderItem);
            
            // Update stock in book service
            bookServiceClient.updateStock(cartItem.getBookId(), cartItem.getQuantity());
        }
        
        // Save order
        order = orderRepository.save(order);
        
        // Clear cart after successful order
        cartServiceClient.clearCart(request.getUserId());
        
        log.info("Order placed successfully: {}", order.getOrderNumber());
        publishOrderPlacedEvent(order, request.getUserEmail());
        return convertToDTO(order);
    }
    
    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return convertToDTO(order);
    }
    
    @Override
    public OrderDTO getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
        return convertToDTO(order);
    }
    
    @Override
    public OrderResponseDTO getUserOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable);
        return convertToResponseDTO(orders);
    }
    
    @Override
    public OrderResponseDTO getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return convertToResponseDTO(orders);
    }
    
    @Override
    public OrderResponseDTO getOrdersByStatus(String status, Pageable pageable) {
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusException("Invalid order status: " + status);
        }
        Page<Order> orders = orderRepository.findByStatus(orderStatus, pageable);
        return convertToResponseDTO(orders);
    }
    
    @Override
    public OrderDTO updateOrderStatus(OrderStatusUpdateRequest request) {
        log.info("Updating order status: {}", request);
        
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + request.getOrderId()));
        
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusException("Invalid order status: " + request.getStatus());
        }
        
        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);
        
        order.setStatus(newStatus);
        
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        } else if (newStatus == OrderStatus.CANCELLED) {
            order.setCancelledAt(LocalDateTime.now());
            order.setCancellationReason(request.getCancellationReason());
            // TODO: Reverse stock update for cancelled orders
        }
        
        order = orderRepository.save(order);
        publishOrderStatusEvent(order, request.getStatus(), null);
        return convertToDTO(order);
    }
    
    @Override
    public void cancelOrder(Long orderId, String reason) {
        log.info("Cancelling order: {} with reason: {}", orderId, reason);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException("Cannot cancel delivered order");
        }
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException("Order already cancelled");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);
        
        // TODO: Refund if payment was made via wallet
        // TODO: Reverse stock update
        
        orderRepository.save(order);
        publishOrderStatusEvent(order, "CANCELLED", null);
    }
    
    @Override
    public Double getUserOrderTotal(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
            .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
            .mapToDouble(Order::getFinalAmount)
            .sum();
    }
    
    @Override
    public Double getTotalSales() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        Double sales = orderRepository.getTotalSalesBetweenDates(startOfMonth, LocalDateTime.now());
        return sales != null ? sales : 0.0;
    }
    
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException("Cannot update cancelled order");
        }
        
        if (current == OrderStatus.DELIVERED && next != OrderStatus.RETURNED) {
            throw new InvalidOrderStatusException("Delivered order can only be returned");
        }
        
        // Add more transition rules as needed
    }

    private void publishOrderPlacedEvent(Order order, String userEmail) {
        notificationEventPublisher.publish(new NotificationEvent(
                order.getUserId(),
                userEmail,
                "ORDER_PLACED",
                "Order Placed Successfully",
                "Your order #" + order.getOrderNumber() + " has been placed successfully.",
                order.getOrderNumber(),
                userEmail != null
        ));

        if ("COMPLETED".equalsIgnoreCase(order.getPaymentStatus())) {
            notificationEventPublisher.publish(new NotificationEvent(
                    order.getUserId(),
                    userEmail,
                    "PAYMENT_SUCCESS",
                    "Payment Successful",
                    "Your payment of " + order.getFinalAmount() + " for order #" + order.getOrderNumber() + " has been successfully processed.",
                    order.getOrderNumber(),
                    userEmail != null
            ));
        }
    }

    private void publishOrderStatusEvent(Order order, String status, String userEmail) {
        notificationEventPublisher.publish(new NotificationEvent(
                order.getUserId(),
                userEmail,
                notificationTypeForOrderStatus(status),
                "Order " + status,
                "Your order #" + order.getOrderNumber() + " has been " + status.toLowerCase() + ".",
                order.getOrderNumber(),
                userEmail != null
        ));
    }

    private String notificationTypeForOrderStatus(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> "ORDER_PENDING";
            case "CONFIRMED" -> "ORDER_CONFIRMED";
            case "PROCESSING" -> "ORDER_PROCESSING";
            case "DISPATCHED" -> "ORDER_DISPATCHED";
            case "OUT_FOR_DELIVERY" -> "ORDER_OUT_FOR_DELIVERY";
            case "DELIVERED" -> "ORDER_DELIVERED";
            case "CANCELLED" -> "ORDER_CANCELLED";
            case "RETURNED" -> "ORDER_RETURNED";
            default -> "ORDER_CONFIRMED";
        };
    }
    
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUserId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingCharge(order.getShippingCharge());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setPaymentMode(order.getPaymentMode().name());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentId(order.getPaymentId());
        dto.setOrderNotes(order.getOrderNotes());
        dto.setCreatedAt(order.getCreatedAt());
        
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList()));
        }
        
        if (order.getShippingAddress() != null) {
            dto.setShippingAddress(convertToAddressDTO(order.getShippingAddress()));
        }
        
        return dto;
    }
    
    private OrderItemDTO convertToItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setBookId(item.getBookId());
        dto.setBookTitle(item.getBookTitle());
        dto.setBookAuthor(item.getBookAuthor());
        dto.setBookIsbn(item.getBookIsbn());
        dto.setBookCoverImage(item.getBookCoverImage());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }
    
    private AddressDTO convertToAddressDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setFullName(address.getFullName());
        dto.setMobile(address.getMobile());
        dto.setAlternateMobile(address.getAlternateMobile());
        dto.setAddress(address.getAddress());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setAddressType(address.getAddressType());
        dto.setIsDefault(address.getIsDefault());
        return dto;
    }
    
    private Address convertToAddressEntity(AddressDTO dto) {
        Address address = new Address();
        address.setFullName(dto.getFullName());
        address.setMobile(dto.getMobile());
        address.setAlternateMobile(dto.getAlternateMobile());
        address.setAddress(dto.getAddress());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry() != null ? dto.getCountry() : "India");
        address.setAddressType(dto.getAddressType());
        address.setIsDefault(dto.getIsDefault());
        return address;
    }
    
    private OrderResponseDTO convertToResponseDTO(Page<Order> page) {
        List<OrderDTO> content = page.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return OrderResponseDTO.builder()
            .content(content)
            .pageNo(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }
}
