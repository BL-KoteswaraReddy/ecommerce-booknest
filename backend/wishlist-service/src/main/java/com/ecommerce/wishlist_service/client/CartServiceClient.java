package com.ecommerce.wishlist_service.client;

import com.ecommerce.wishlist_service.exception.WishlistServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.cart.url}")
    private String cartServiceUrl;
    
    public void addToCart(Long userId, Long bookId, Integer quantity) {
        try {
            AddToCartRequest request = new AddToCartRequest(userId, bookId, quantity);
            
            webClient.post()
                .uri(cartServiceUrl + "/api/cart/add")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            log.error("Error adding item to cart: {}", e.getMessage());
            throw new WishlistServiceException("Failed to add item to cart");
        }
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class AddToCartRequest {
        private Long userId;
        private Long bookId;
        private Integer quantity;
    }
}