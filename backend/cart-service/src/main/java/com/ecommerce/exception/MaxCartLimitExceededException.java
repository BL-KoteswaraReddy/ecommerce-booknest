package com.ecommerce.exception;

public class MaxCartLimitExceededException extends RuntimeException {
    public MaxCartLimitExceededException(String message) {
        super(message);
    }
}
