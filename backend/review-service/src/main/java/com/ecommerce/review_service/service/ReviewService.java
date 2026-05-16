package com.ecommerce.review_service.service;

import com.ecommerce.review_service.dto.*;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewDTO addReview(ReviewRequestDTO request);
    ReviewDTO updateReview(Long reviewId, UpdateReviewRequestDTO request);
    void deleteReview(Long reviewId, Long userId);
    ReviewDTO getReviewById(Long reviewId);
    ReviewResponseDTO getReviewsByBookId(Long bookId, Pageable pageable);
    ReviewResponseDTO getReviewsByUserId(Long userId, Pageable pageable);
    RatingSummaryDTO getRatingSummary(Long bookId);
    void markReviewHelpful(Long reviewId, Long userId);
    ReviewResponseDTO getPendingReviews(Pageable pageable);
    void moderateReview(Long reviewId, Boolean approve);
    Double getAverageRating(Long bookId);
    Integer getReviewCount(Long bookId);
    boolean hasUserReviewed(Long bookId, Long userId);
}