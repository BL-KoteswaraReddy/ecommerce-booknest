package com.ecommerce.review_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long bookId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private LocalDateTime reviewDate;
    private LocalDateTime updatedAt;
    private Boolean isApproved;
}