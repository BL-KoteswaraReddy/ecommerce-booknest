package com.ecommerce.review_service.controller;

import com.ecommerce.review_service.dto.*;
import com.ecommerce.review_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ReviewController {

    private final ReviewService reviewService;

    // Create review
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDTO>> addReview(@Valid @RequestBody ReviewRequestDTO request) {
        ReviewDTO review = reviewService.addReview(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Review added successfully", review));
    }

    // Update review
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDTO>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequestDTO request) {
        ReviewDTO review = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review updated successfully", review));
    }

    // Delete review
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review deleted successfully", null));
    }

    // Get review by ID
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDTO>> getReviewById(@PathVariable Long reviewId) {
        ReviewDTO review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review retrieved successfully", review));
    }

    // Get reviews for a book
    @GetMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getReviewsByBookId(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reviewDate").descending());
        ReviewResponseDTO reviews = reviewService.getReviewsByBookId(bookId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reviews retrieved successfully", reviews));
    }

    // Get reviews by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getReviewsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reviewDate").descending());
        ReviewResponseDTO reviews = reviewService.getReviewsByUserId(userId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Your reviews retrieved successfully", reviews));
    }

    // Get rating summary for a book
    @GetMapping("/rating/{bookId}")
    public ResponseEntity<ApiResponse<RatingSummaryDTO>> getRatingSummary(@PathVariable Long bookId) {
        RatingSummaryDTO summary = reviewService.getRatingSummary(bookId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rating summary retrieved", summary));
    }

    // Mark review as helpful
    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<ApiResponse<Void>> markReviewHelpful(
            @PathVariable Long reviewId,
            @RequestParam Long userId) {
        reviewService.markReviewHelpful(reviewId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review marked as helpful", null));
    }

    // Admin: Get pending reviews
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        ReviewResponseDTO reviews = reviewService.getPendingReviews(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending reviews retrieved", reviews));
    }

    // Admin: Moderate review
    @PutMapping("/{reviewId}/moderate")
    public ResponseEntity<ApiResponse<Void>> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam Boolean approve) {
        reviewService.moderateReview(reviewId, approve);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review moderated successfully", null));
    }

    // Check if user has reviewed
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> hasUserReviewed(
            @RequestParam Long bookId,
            @RequestParam Long userId) {
        boolean hasReviewed = reviewService.hasUserReviewed(bookId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Check completed", hasReviewed));
    }
}