package com.ecommerce.book_service.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String publisher;
    private BigDecimal price;
    private Integer stock;
    private Double rating;
    private String description;
    private String coverImageUrl;
    private LocalDateTime publishedDate;
    private Boolean isFeatured;
    private Boolean isBestseller;
    private Integer reviewCount;
}