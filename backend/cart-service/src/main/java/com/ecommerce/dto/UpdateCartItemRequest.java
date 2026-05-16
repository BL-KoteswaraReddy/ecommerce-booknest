package com.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Cart Item ID is required")
    private Long cartItemId;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}