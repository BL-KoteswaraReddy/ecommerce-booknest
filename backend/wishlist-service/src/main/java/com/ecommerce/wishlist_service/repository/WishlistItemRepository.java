package com.ecommerce.wishlist_service.repository;

import com.ecommerce.wishlist_service.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    
    Optional<WishlistItem> findByWishlistIdAndBookId(Long wishlistId, Long bookId);
    
    List<WishlistItem> findByWishlistId(Long wishlistId);
    
    @Modifying
    @Query("DELETE FROM WishlistItem wi WHERE wi.wishlist.id = :wishlistId AND wi.bookId = :bookId")
    void deleteByWishlistIdAndBookId(@Param("wishlistId") Long wishlistId, @Param("bookId") Long bookId);
    
    @Modifying
    @Query("DELETE FROM WishlistItem wi WHERE wi.wishlist.id = :wishlistId")
    void deleteAllByWishlistId(@Param("wishlistId") Long wishlistId);
    
    @Query("SELECT COUNT(wi) FROM WishlistItem wi WHERE wi.wishlist.id = :wishlistId")
    int countItemsByWishlistId(@Param("wishlistId") Long wishlistId);
    
    boolean existsByWishlistIdAndBookId(Long wishlistId, Long bookId);
}