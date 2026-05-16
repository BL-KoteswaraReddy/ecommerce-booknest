package com.ecommerce.service;
import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.UpdateCartItemRequest;

public interface CartService {
    CartDTO getCartByUserId(Long userId);
    CartDTO addToCart(AddToCartRequest request);
    CartDTO updateCartItem(UpdateCartItemRequest request);
    CartDTO removeCartItem(Long userId, Long cartItemId);
    CartDTO clearCart(Long userId);
    void deleteCart(Long userId);
    Integer getCartItemCount(Long userId);
    Double getCartTotal(Long userId);
}