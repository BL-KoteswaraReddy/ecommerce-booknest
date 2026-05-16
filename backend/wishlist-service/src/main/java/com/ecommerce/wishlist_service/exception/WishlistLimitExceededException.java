package com.ecommerce.wishlist_service.exception;

public class WishlistLimitExceededException extends RuntimeException {
    public WishlistLimitExceededException(String message) {
        super(message);
    }
}