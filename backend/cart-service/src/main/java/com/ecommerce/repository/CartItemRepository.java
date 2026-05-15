package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndBookId(Long cartId, Long bookId);
    List<CartItem> findByCartId(Long cartId);
    
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.bookId = :bookId")
    void deleteByCartIdAndBookId(@Param("cartId") Long cartId, @Param("bookId") Long bookId);
    
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);
    
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    int countItemsByCartId(@Param("cartId") Long cartId);
    
    @Query("SELECT SUM(ci.totalPrice) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Double sumTotalPriceByCartId(@Param("cartId") Long cartId);
}