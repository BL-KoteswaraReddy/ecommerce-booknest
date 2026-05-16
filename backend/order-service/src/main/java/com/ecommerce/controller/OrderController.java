package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<OrderDTO>> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderDTO order = orderService.placeOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Order placed successfully", order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long orderId) {
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order retrieved successfully", order));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByNumber(@PathVariable String orderNumber) {
        OrderDTO order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order retrieved successfully", order));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        OrderResponseDTO orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        OrderResponseDTO orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "All orders retrieved successfully", orders));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        OrderResponseDTO orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders by status retrieved successfully", orders));
    }

    @PutMapping("/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(@Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderDTO order = orderService.updateOrderStatus(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order status updated successfully", order));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "Customer requested cancellation") String reason) {
        orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order cancelled successfully", null));
    }

    @GetMapping("/user/{userId}/total")
    public ResponseEntity<ApiResponse<Double>> getUserOrderTotal(@PathVariable Long userId) {
        Double total = orderService.getUserOrderTotal(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "User order total retrieved", total));
    }

    @GetMapping("/sales/total")
    public ResponseEntity<ApiResponse<Double>> getTotalSales() {
        Double total = orderService.getTotalSales();
        return ResponseEntity.ok(new ApiResponse<>(true, "Total sales retrieved", total));
    }
}