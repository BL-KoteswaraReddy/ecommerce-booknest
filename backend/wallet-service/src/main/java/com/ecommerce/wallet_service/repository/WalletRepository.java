package com.ecommerce.wallet_service.repository;

// WalletRepository.java
import com.ecommerce.wallet_service.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ecommerce.wallet_service.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    @Modifying
    @Query("UPDATE Wallet w SET w.currentBalance = w.currentBalance + :amount, w.totalCredited = w.totalCredited + :amount WHERE w.userId = :userId")
    int addMoney(@Param("userId") Long userId, @Param("amount") Double amount);

    @Modifying
    @Query("UPDATE Wallet w SET w.currentBalance = w.currentBalance - :amount, w.totalDebited = w.totalDebited + :amount WHERE w.userId = :userId AND w.currentBalance >= :amount")
    int deductMoney(@Param("userId") Long userId, @Param("amount") Double amount);

    @Query("SELECT w.currentBalance FROM Wallet w WHERE w.userId = :userId")
    Double getBalance(@Param("userId") Long userId);
}
