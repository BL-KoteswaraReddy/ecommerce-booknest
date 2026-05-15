package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);
    
    List<Order> findByUserId(Long userId);
    
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Modifying
    @Query("UPDATE Order o SET o.status = :status, o.cancelledAt = :cancelledAt, o.cancellationReason = :reason WHERE o.id = :orderId")
    int updateOrderStatus(@Param("orderId") Long orderId, 
                          @Param("status") OrderStatus status,
                          @Param("cancelledAt") LocalDateTime cancelledAt,
                          @Param("reason") String reason);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);
    
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Double getTotalSalesBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
}