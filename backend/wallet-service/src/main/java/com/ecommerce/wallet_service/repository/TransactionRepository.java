package com.ecommerce.wallet_service.repository;

import com.ecommerce.wallet_service.entity.Transaction;
import com.ecommerce.wallet_service.enums.TransactionStatus;
import com.ecommerce.wallet_service.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    
    Page<Transaction> findByWalletUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);
    
    List<Transaction> findByWalletUserIdAndType(Long userId, TransactionType type);
    
    Page<Transaction> findByWalletUserIdAndType(Long userId, TransactionType type, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.wallet.userId = :userId AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findTransactionsBetweenDates(@Param("userId") Long userId, 
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.wallet.userId = :userId AND t.type = :type AND t.status = 'SUCCESS'")
    Double getTotalAmountByType(@Param("userId") Long userId, @Param("type") TransactionType type);
    
    Optional<Transaction> findByOrderNumber(String orderNumber);
}