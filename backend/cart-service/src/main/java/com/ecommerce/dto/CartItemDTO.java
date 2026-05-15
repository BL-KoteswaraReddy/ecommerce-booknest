package com.ecommerce.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookCoverImage;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private String addedAt;
}