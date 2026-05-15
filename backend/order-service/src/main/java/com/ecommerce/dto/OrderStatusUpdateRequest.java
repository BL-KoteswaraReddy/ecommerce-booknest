package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private String cancellationReason;
}