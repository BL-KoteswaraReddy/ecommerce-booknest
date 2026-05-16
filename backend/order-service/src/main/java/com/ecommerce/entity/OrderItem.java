package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "book_id", nullable = false)
    private Long bookId;
    
    @Column(name = "book_title", nullable = false)
    private String bookTitle;
    
    @Column(name = "book_author")
    private String bookAuthor;
    
    @Column(name = "book_isbn")
    private String bookIsbn;
    
    @Column(name = "book_cover_image")
    private String bookCoverImage;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;
    
    @Column(name = "total_price", nullable = false)
    private Double totalPrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}