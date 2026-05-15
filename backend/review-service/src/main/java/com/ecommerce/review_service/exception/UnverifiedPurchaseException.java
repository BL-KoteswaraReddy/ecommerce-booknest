package com.ecommerce.review_service.exception;

public class UnverifiedPurchaseException extends RuntimeException {
    public UnverifiedPurchaseException(String message) {
        super(message);
    }
}
