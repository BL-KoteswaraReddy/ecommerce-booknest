package com.ecommerce.controller;
import com.ecommerce.dto.*;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@PathVariable Long userId) {
        CartDTO cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart retrieved successfully", cart));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartDTO>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        CartDTO updatedCart = cartService.addToCart(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item added to cart successfully", updatedCart));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(@Valid @RequestBody UpdateCartItemRequest request) {
        CartDTO updatedCart = cartService.updateCartItem(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart item updated successfully", updatedCart));
    }

    @DeleteMapping("/{userId}/item/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeCartItem(
            @PathVariable Long userId,
            @PathVariable Long cartItemId) {
        CartDTO updatedCart = cartService.removeCartItem(userId, cartItemId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item removed from cart successfully", updatedCart));
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<CartDTO>> clearCart(@PathVariable Long userId) {
        CartDTO clearedCart = cartService.clearCart(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart cleared successfully", clearedCart));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteCart(@PathVariable Long userId) {
        cartService.deleteCart(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart deleted successfully", null));
    }

    @GetMapping("/{userId}/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(@PathVariable Long userId) {
        Integer count = cartService.getCartItemCount(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart item count retrieved", count));
    }

    @GetMapping("/{userId}/total")
    public ResponseEntity<ApiResponse<Double>> getCartTotal(@PathVariable Long userId) {
        Double total = cartService.getCartTotal(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart total retrieved", total));
    }
}
