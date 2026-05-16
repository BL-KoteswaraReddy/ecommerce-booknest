package com.ecommerce.wishlist_service.service;
import com.ecommerce.wishlist_service.dto.AddToWishlistRequest;
import com.ecommerce.wishlist_service.dto.MoveToCartRequest;
import com.ecommerce.wishlist_service.dto.WishlistDTO;

public interface WishlistService {
    WishlistDTO getWishlistByUserId(Long userId);
    WishlistDTO addToWishlist(AddToWishlistRequest request);
    WishlistDTO removeFromWishlist(Long userId, Long wishlistItemId);
    WishlistDTO removeBookFromWishlist(Long userId, Long bookId);
    WishlistDTO clearWishlist(Long userId);
    void deleteWishlist(Long userId);
    WishlistDTO moveToCart(MoveToCartRequest request);
    boolean isBookInWishlist(Long userId, Long bookId);
    Integer getWishlistItemCount(Long userId);
}