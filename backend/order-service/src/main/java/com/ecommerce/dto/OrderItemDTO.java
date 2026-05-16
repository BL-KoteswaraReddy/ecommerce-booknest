package com.ecommerce.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;
    private String bookCoverImage;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
}