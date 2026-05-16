package com.ecommerce.service;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderResponseDTO;
import com.ecommerce.dto.OrderStatusUpdateRequest;
import com.ecommerce.dto.PlaceOrderRequest;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDTO placeOrder(PlaceOrderRequest request);
    OrderDTO getOrderById(Long orderId);
    OrderDTO getOrderByNumber(String orderNumber);
    OrderResponseDTO getUserOrders(Long userId, Pageable pageable);
    OrderResponseDTO getAllOrders(Pageable pageable);
    OrderResponseDTO getOrdersByStatus(String status, Pageable pageable);
    OrderDTO updateOrderStatus(OrderStatusUpdateRequest request);
    void cancelOrder(Long orderId, String reason);
    Double getUserOrderTotal(Long userId);
    Double getTotalSales();
}
