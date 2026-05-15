package com.ecommerce.review_service.service.impl;

import com.ecommerce.review_service.client.BookServiceClient;
import com.ecommerce.review_service.client.OrderServiceClient;
import com.ecommerce.review_service.dto.*;
import com.ecommerce.review_service.entity.Review;
import com.ecommerce.review_service.entity.ReviewHelpful;
import com.ecommerce.review_service.exception.*;
import com.ecommerce.review_service.repository.ReviewHelpfulRepository;
import com.ecommerce.review_service.repository.ReviewRepository;
import com.ecommerce.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ReviewHelpfulRepository reviewHelpfulRepository;
    private final BookServiceClient bookServiceClient;
    private final OrderServiceClient orderServiceClient;
    
    @Override
    public ReviewDTO addReview(ReviewRequestDTO request) {
        log.info("Adding review for book {} by user {}", request.getBookId(), request.getUserId());
        
        // Check if book exists
        if (!bookServiceClient.bookExists(request.getBookId())) {
            throw new ReviewServiceException("Book not found with id: " + request.getBookId());
        }
        
        // Check if user already reviewed this book
        if (reviewRepository.existsByBookIdAndUserId(request.getBookId(), request.getUserId())) {
            throw new DuplicateReviewException("You have already reviewed this book");
        }
        
        // Check if user has purchased this book
        boolean hasPurchased = orderServiceClient.hasUserPurchasedBook(request.getUserId(), request.getBookId());
        
        Review review = new Review();
        review.setBookId(request.getBookId());
        review.setUserId(request.getUserId());
        review.setUserName(request.getUserName());
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setIsVerifiedPurchase(hasPurchased);
        
        Review savedReview = reviewRepository.save(review);
        
        // Update book rating
        updateBookRating(request.getBookId());
        
        return convertToDTO(savedReview);
    }
    
    @Override
    public ReviewDTO updateReview(Long reviewId, UpdateReviewRequestDTO request) {
        log.info("Updating review {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
        
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
        
        Review updatedReview = reviewRepository.save(review);
        
        // Update book rating
        updateBookRating(review.getBookId());
        
        return convertToDTO(updatedReview);
    }
    
    @Override
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Deleting review {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
        
        // Only review owner or admin can delete
        if (!review.getUserId().equals(userId)) {
            throw new ReviewServiceException("You are not authorized to delete this review");
        }
        
        Long bookId = review.getBookId();
        reviewRepository.delete(review);
        
        // Update book rating
        updateBookRating(bookId);
    }
    
    @Override
    public ReviewDTO getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
        return convertToDTO(review);
    }
    
    @Override
    public ReviewResponseDTO getReviewsByBookId(Long bookId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByBookIdOrderByReviewDateDesc(bookId, pageable);
        return convertToResponseDTO(reviews);
    }
    
    @Override
    public ReviewResponseDTO getReviewsByUserId(Long userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByUserIdOrderByReviewDateDesc(userId, pageable);
        return convertToResponseDTO(reviews);
    }
    
    @Override
    public RatingSummaryDTO getRatingSummary(Long bookId) {
        Double averageRating = reviewRepository.getAverageRatingByBookId(bookId);
        Integer totalReviews = reviewRepository.getReviewCountByBookId(bookId);
        
        // Get rating distribution
        List<Object[]> distribution = reviewRepository.getRatingDistribution(bookId);
        int fiveStar = 0, fourStar = 0, threeStar = 0, twoStar = 0, oneStar = 0;
        
        for (Object[] row : distribution) {
            int rating = ((Number) row[0]).intValue();
            int count = ((Number) row[1]).intValue();

            switch (rating) {
                case 5 -> fiveStar = count;
                case 4 -> fourStar = count;
                case 3 -> threeStar = count;
                case 2 -> twoStar = count;
                case 1 -> oneStar = count;
                default -> {
                }
            }
        }
        
        return RatingSummaryDTO.builder()
            .bookId(bookId)
            .averageRating(averageRating != null ? averageRating : 0.0)
            .totalReviews(totalReviews != null ? totalReviews : 0)
            .fiveStarCount(fiveStar)
            .fourStarCount(fourStar)
            .threeStarCount(threeStar)
            .twoStarCount(twoStar)
            .oneStarCount(oneStar)
            .build();
    }
    
    @Override
    public void markReviewHelpful(Long reviewId, Long userId) {
        // Check if user already marked as helpful
        if (reviewHelpfulRepository.existsByReviewIdAndUserId(reviewId, userId)) {
            throw new ReviewServiceException("You have already marked this review as helpful");
        }
        
        ReviewHelpful helpful = new ReviewHelpful();
        helpful.setReviewId(reviewId);
        helpful.setUserId(userId);
        reviewHelpfulRepository.save(helpful);
        
        reviewRepository.incrementHelpfulCount(reviewId);
    }
    
    @Override
    public ReviewResponseDTO getPendingReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByIsApprovedFalse(pageable);
        return convertToResponseDTO(reviews);
    }
    
    @Override
    public void moderateReview(Long reviewId, Boolean approve) {
        reviewRepository.updateApprovalStatus(reviewId, approve);
    }
    
    @Override
    public Double getAverageRating(Long bookId) {
        Double rating = reviewRepository.getAverageRatingByBookId(bookId);
        return rating != null ? rating : 0.0;
    }
    
    @Override
    public Integer getReviewCount(Long bookId) {
        Integer count = reviewRepository.getReviewCountByBookId(bookId);
        return count != null ? count : 0;
    }
    
    @Override
    public boolean hasUserReviewed(Long bookId, Long userId) {
        return reviewRepository.existsByBookIdAndUserId(bookId, userId);
    }
    
    private void updateBookRating(Long bookId) {
        Double averageRating = getAverageRating(bookId);
        Integer reviewCount = getReviewCount(bookId);
        bookServiceClient.updateBookRating(bookId, averageRating, reviewCount);
    }
    
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setBookId(review.getBookId());
        dto.setUserId(review.getUserId());
        dto.setUserName(review.getUserName());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setComment(review.getComment());
        dto.setIsVerifiedPurchase(review.getIsVerifiedPurchase());
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setReviewDate(review.getReviewDate());
        dto.setUpdatedAt(review.getUpdatedAt());
        dto.setIsApproved(review.getIsApproved());
        return dto;
    }
    
    private ReviewResponseDTO convertToResponseDTO(Page<Review> page) {
        java.util.List<ReviewDTO> content = page.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return ReviewResponseDTO.builder()
            .content(content)
            .pageNo(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }
}
