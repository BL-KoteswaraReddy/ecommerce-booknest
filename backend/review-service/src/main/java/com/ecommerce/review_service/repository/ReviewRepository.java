package com.ecommerce.review_service.repository;

import com.ecommerce.review_service.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Page<Review> findByBookIdOrderByReviewDateDesc(Long bookId, Pageable pageable);
    
    Page<Review> findByUserIdOrderByReviewDateDesc(Long userId, Pageable pageable);
    
    Optional<Review> findByBookIdAndUserId(Long bookId, Long userId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.bookId = :bookId AND r.isApproved = true")
    Double getAverageRatingByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.bookId = :bookId AND r.isApproved = true")
    Integer getReviewCountByBookId(@Param("bookId") Long bookId);
    
    @Modifying
    @Query("UPDATE Review r SET r.helpfulCount = r.helpfulCount + 1 WHERE r.id = :reviewId")
    void incrementHelpfulCount(@Param("reviewId") Long reviewId);
    
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.bookId = :bookId AND r.isApproved = true GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("bookId") Long bookId);
    
    Page<Review> findByIsApprovedFalse(Pageable pageable);
    
    @Modifying
    @Query("UPDATE Review r SET r.isApproved = :approved WHERE r.id = :reviewId")
    void updateApprovalStatus(@Param("reviewId") Long reviewId, @Param("approved") Boolean approved);
    
    boolean existsByBookIdAndUserId(Long bookId, Long userId);
}
