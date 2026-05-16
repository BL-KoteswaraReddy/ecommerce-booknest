package com.ecommerce.wallet_service.exception;

public class WalletLimitExceededException extends RuntimeException {
    public WalletLimitExceededException(String message) {
        super(message);
    }
}