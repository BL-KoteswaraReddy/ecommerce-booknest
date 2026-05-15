package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Cart c SET c.totalItems = :totalItems, c.totalPrice = :totalPrice WHERE c.id = :cartId")
    void updateCartTotals(@Param("cartId") Long cartId,
                          @Param("totalItems") Integer totalItems,
                          @Param("totalPrice") Double totalPrice);
}
