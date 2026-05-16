package com.ecommerce.wallet_service.entity;

import com.ecommerce.wallet_service.enums.TransactionStatus;
import com.ecommerce.wallet_service.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(name = "balance_before")
    private Double balanceBefore;
    
    @Column(name = "balance_after")
    private Double balanceAfter;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "reference_id")
    private String referenceId;
    
    @Column(name = "order_number")
    private String orderNumber;
    
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    
    @Column(name = "remarks", length = 500)
    private String remarks;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    
    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
        if (transactionId == null) {
            transactionId = generateTransactionId();
        }
    }
    
    private String generateTransactionId() {
        String prefix = type == TransactionType.CREDIT ? "CR" : "DR";
        return prefix + System.currentTimeMillis() + String.format("%06d", (int)(Math.random() * 1000000));
    }
}