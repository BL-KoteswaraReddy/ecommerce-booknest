package com.ecommerce.review_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"book_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "book_id", nullable = false)
    private Long bookId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(nullable = false)
    private Integer rating;  // 1-5 stars
    
    @Column(length = 2000)
    private String comment;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "is_verified_purchase")
    private Boolean isVerifiedPurchase = false;
    
    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;
    
    @Column(name = "review_date")
    private LocalDateTime reviewDate;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_approved")
    private Boolean isApproved = true;  // For admin moderation
    
    @PrePersist
    protected void onCreate() {
        reviewDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (helpfulCount == null) helpfulCount = 0;
        if (isVerifiedPurchase == null) isVerifiedPurchase = false;
        if (isApproved == null) isApproved = true;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}