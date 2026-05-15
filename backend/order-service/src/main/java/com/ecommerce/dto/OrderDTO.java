package com.ecommerce.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long userId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private Double shippingCharge;
    private Double discountAmount;
    private Double finalAmount;
    private String status;
    private String paymentMode;
    private String paymentStatus;
    private String paymentId;
    private String orderNotes;
    private List<OrderItemDTO> items;
    private AddressDTO shippingAddress;
    private LocalDateTime createdAt;
}