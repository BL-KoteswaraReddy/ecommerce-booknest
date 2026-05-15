package com.ecommerce.book_service.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//@Entity
//@Table(name = "books")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class Book
//{
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long bookId;
//
//    private String title;
//    private String author;
//
//    @Column(unique = true)
//    private String isbn;
//
//    private String genre;
//    private String publisher;
//    private BigDecimal price;
//    private Integer stock;
//
//    @Column(columnDefinition = "TEXT")
//    private String description;
//
//    private String coverImageUrl;
//
//    private LocalDate publishedDate;
//    private Boolean featured;
//
//}

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 50)
    private String genre;

    @Column(length = 100)
    private String publisher;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(precision = 3, scale = 1)
    private BigDecimal rating;

    @Column(length = 2000)
    private String description;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_bestseller")
    private Boolean isBestseller = false;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (rating == null) rating = BigDecimal.ZERO;
        if (reviewCount == null) reviewCount = 0;
        if (isFeatured == null) isFeatured = false;
        if (isBestseller == null) isBestseller = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
