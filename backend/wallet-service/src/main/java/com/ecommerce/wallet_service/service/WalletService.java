package com.ecommerce.wallet_service.service;
// WalletService.java (interface — in case you don't have it)
import com.ecommerce.wallet_service.dto.*;
import org.springframework.data.domain.Pageable;

public interface WalletService {
    WalletDTO getWalletByUserId(Long userId);
    WalletDTO addMoney(AddMoneyRequest request);
    WalletPaymentResponse processPayment(WalletPaymentRequest request);
    TransactionDTO getTransactionById(Long transactionId);
    TransactionDTO getTransactionByTransactionId(String transactionId);
    TransactionResponseDTO getUserTransactions(Long userId, Pageable pageable);
    TransactionResponseDTO getUserTransactionsByType(Long userId, String type, Pageable pageable);
    Double getWalletBalance(Long userId);
    WalletDTO createWallet(Long userId);
    void deactivateWallet(Long userId);
    void activateWallet(Long userId);
}
