package com.ecommerce.wishlist_service.exception;

public class DuplicateWishlistItemException extends RuntimeException {
    public DuplicateWishlistItemException(String message) {
        super(message);
    }
}