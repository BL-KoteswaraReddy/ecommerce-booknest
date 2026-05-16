package com.ecommerce.wishlist_service.controller;

import com.ecommerce.wishlist_service.dto.*;
import com.ecommerce.wishlist_service.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<WishlistDTO>> getWishlist(@PathVariable Long userId) {
        WishlistDTO wishlist = wishlistService.getWishlistByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist retrieved successfully", wishlist));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<WishlistDTO>> addToWishlist(@Valid @RequestBody AddToWishlistRequest request) {
        System.out.println("reaching request wishlist controller method");
        WishlistDTO updatedWishlist = wishlistService.addToWishlist(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Book added to wishlist successfully", updatedWishlist));
    }

    @DeleteMapping("/{userId}/item/{wishlistItemId}")
    public ResponseEntity<ApiResponse<WishlistDTO>> removeFromWishlist(
            @PathVariable Long userId,
            @PathVariable Long wishlistItemId) {
        WishlistDTO updatedWishlist = wishlistService.removeFromWishlist(userId, wishlistItemId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item removed from wishlist successfully", updatedWishlist));
    }

    @DeleteMapping("/{userId}/book/{bookId}")
    public ResponseEntity<ApiResponse<WishlistDTO>> removeBookFromWishlist(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        WishlistDTO updatedWishlist = wishlistService.removeBookFromWishlist(userId, bookId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Book removed from wishlist successfully", updatedWishlist));
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<WishlistDTO>> clearWishlist(@PathVariable Long userId) {
        WishlistDTO clearedWishlist = wishlistService.clearWishlist(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist cleared successfully", clearedWishlist));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteWishlist(@PathVariable Long userId) {
        wishlistService.deleteWishlist(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist deleted successfully", null));
    }

    @PostMapping("/move-to-cart")
    public ResponseEntity<ApiResponse<WishlistDTO>> moveToCart(@Valid @RequestBody MoveToCartRequest request) {
        WishlistDTO updatedWishlist = wishlistService.moveToCart(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item moved to cart successfully", updatedWishlist));
    }

    @GetMapping("/{userId}/check/{bookId}")
    public ResponseEntity<ApiResponse<Boolean>> isBookInWishlist(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        boolean isInWishlist = wishlistService.isBookInWishlist(userId, bookId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist check completed", isInWishlist));
    }

    @GetMapping("/{userId}/count")
    public ResponseEntity<ApiResponse<Integer>> getWishlistItemCount(@PathVariable Long userId) {
        Integer count = wishlistService.getWishlistItemCount(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist item count retrieved", count));
    }
}