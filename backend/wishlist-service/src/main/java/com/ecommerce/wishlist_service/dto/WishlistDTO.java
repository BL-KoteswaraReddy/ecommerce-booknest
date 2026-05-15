package com.ecommerce.wishlist_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class WishlistDTO {
    private Long id;
    private Long userId;
    private Integer totalItems;
    private List<WishlistItemDTO> items;
    private String createdAt;
    private String updatedAt;
}