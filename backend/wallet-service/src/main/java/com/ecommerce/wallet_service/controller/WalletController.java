package com.ecommerce.wallet_service.controller;

import com.ecommerce.wallet_service.dto.*;
import com.ecommerce.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<WalletDTO>> getWallet(@PathVariable Long userId) {
        WalletDTO wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet retrieved successfully", wallet));
    }

    @PostMapping("/add-money")
    public ResponseEntity<ApiResponse<WalletDTO>> addMoney(@Valid @RequestBody AddMoneyRequest request) {
        WalletDTO updatedWallet = walletService.addMoney(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Money added to wallet successfully", updatedWallet));
    }

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<WalletPaymentResponse>> processPayment(@Valid @RequestBody WalletPaymentRequest request) {
        WalletPaymentResponse response = walletService.processPayment(request);
        return ResponseEntity.ok(new ApiResponse<>(true, response.getMessage(), response));
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        TransactionResponseDTO transactions = walletService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transactions retrieved successfully", transactions));
    }

    @GetMapping("/{userId}/transactions/type/{type}")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> getUserTransactionsByType(
            @PathVariable Long userId,
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        TransactionResponseDTO transactions = walletService.getUserTransactionsByType(userId, type, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transactions retrieved successfully", transactions));
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<Double>> getWalletBalance(@PathVariable Long userId) {
        Double balance = walletService.getWalletBalance(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet balance retrieved", balance));
    }

    @PostMapping("/{userId}/create")
    public ResponseEntity<ApiResponse<WalletDTO>> createWallet(@PathVariable Long userId) {
        WalletDTO wallet = walletService.createWallet(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Wallet created successfully", wallet));
    }

    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateWallet(@PathVariable Long userId) {
        walletService.deactivateWallet(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet deactivated successfully", null));
    }

    @PutMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateWallet(@PathVariable Long userId) {
        walletService.activateWallet(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet activated successfully", null));
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionById(@PathVariable Long transactionId) {
        TransactionDTO transaction = walletService.getTransactionById(transactionId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transaction retrieved successfully", transaction));
    }

    @GetMapping("/transactions/reference/{referenceId}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionByReferenceId(@PathVariable String referenceId) {
        TransactionDTO transaction = walletService.getTransactionByTransactionId(referenceId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transaction retrieved successfully", transaction));
    }
}