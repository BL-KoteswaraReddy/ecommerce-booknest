package com.ecommerce.wishlist_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveToCartRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Wishlist Item ID is required")
    private Long wishlistItemId;
    
    private Integer quantity = 1;
}