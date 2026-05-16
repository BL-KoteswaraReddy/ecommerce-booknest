package com.ecommerce.wallet_service.exception;

public class WalletTransactionException extends RuntimeException {
    public WalletTransactionException(String message) {
        super(message);
    }
}