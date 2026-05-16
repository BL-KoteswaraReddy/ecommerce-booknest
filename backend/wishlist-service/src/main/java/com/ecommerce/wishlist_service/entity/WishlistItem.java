package com.ecommerce.wishlist_service.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"wishlist_id", "book_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "book_title", nullable = false)
    private String bookTitle;

    @Column(name = "book_author")
    private String bookAuthor;

    @Column(name = "book_cover_image")
    private String bookCoverImage;

    @Column(name = "book_price")
    private Double bookPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}