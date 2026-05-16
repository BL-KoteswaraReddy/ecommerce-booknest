package com.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Book ID is required")
    private Long bookId;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;
}
