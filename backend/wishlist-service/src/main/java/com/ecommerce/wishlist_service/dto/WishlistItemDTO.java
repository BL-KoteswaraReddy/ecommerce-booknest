package com.ecommerce.wishlist_service.dto;

import lombok.Data;

@Data
public class WishlistItemDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookCoverImage;
    private Double bookPrice;
    private String addedAt;
}