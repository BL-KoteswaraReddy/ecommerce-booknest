package com.ecommerce.client;

import com.ecommerce.dto.CartCheckoutDTO;
import com.ecommerce.exception.OrderServiceException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartServiceClient {

    private final WebClient webClient;

    @Value("${services.cart.url}")
    private String cartServiceUrl;

    @Value("${app.internal.secret}")
    private String internalSecret;

    @Data
    static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
    }

    public CartCheckoutDTO getCartForCheckout(Long userId) {
        try {
            // Step 1: Log raw response
            String raw = webClient.get()
                    .uri(cartServiceUrl + "/api/cart/{userId}", userId)
                    .header("X-Internal-Secret", internalSecret)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Raw cart response: {}", raw); // ← shows exact JSON

            // Step 2: Unwrap ApiResponse
            ApiResponse<CartCheckoutDTO> response = webClient.get()
                    .uri(cartServiceUrl + "/api/cart/{userId}", userId)
                    .header("X-Internal-Secret", internalSecret)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<CartCheckoutDTO>>() {})
                    .block();

            log.info("Parsed cart: {}", response != null ? response.getData() : null);

            if (response == null || response.getData() == null) {
                throw new OrderServiceException("Cart not found for user: " + userId);
            }

            return response.getData();

        } catch (OrderServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching cart for checkout: {}", e.getMessage());
            throw new OrderServiceException("Failed to fetch cart details");
        }
    }

    public void clearCart(Long userId) {
        try {
            webClient.delete()
                    .uri(cartServiceUrl + "/api/cart/{userId}/clear", userId)
                    .header("X-Internal-Secret", internalSecret)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Error clearing cart: {}", e.getMessage());
        }
    }
}