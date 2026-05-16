package com.ecommerce.review_service.repository;

import com.ecommerce.review_service.entity.ReviewHelpful;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReviewHelpfulRepository extends JpaRepository<ReviewHelpful, Long> {
    
    Optional<ReviewHelpful> findByReviewIdAndUserId(Long reviewId, Long userId);
    
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
    
    long countByReviewId(Long reviewId);
}