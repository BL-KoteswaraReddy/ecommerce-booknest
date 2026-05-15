package com.ecommerce.wishlist_service.dto;

import lombok.Data;

@Data
public class BookInfoDTO {
    private Long id;
    private String title;
    private String author;
    private Double price;
    private Integer stock;
    private String coverImageUrl;
    private Boolean available;
}
