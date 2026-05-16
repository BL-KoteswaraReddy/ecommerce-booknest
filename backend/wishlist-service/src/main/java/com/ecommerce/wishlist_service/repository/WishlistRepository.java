package com.ecommerce.wishlist_service.repository;

import com.ecommerce.wishlist_service.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(w.items) FROM Wishlist w WHERE w.userId = :userId")
    Integer getWishlistItemCount(@Param("userId") Long userId);
}
