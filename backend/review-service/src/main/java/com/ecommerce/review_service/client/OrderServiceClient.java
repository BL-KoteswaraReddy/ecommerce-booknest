package com.ecommerce.review_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.order.url}")
    private String orderServiceUrl;
    
    public boolean hasUserPurchasedBook(Long userId, Long bookId) {
        try {
            OrdersResponse response = webClient.get()
                .uri(orderServiceUrl + "/api/orders/user/{userId}/purchased-books", userId)
                .retrieve()
                .bodyToMono(OrdersResponse.class)
                .block();
            
            return response != null && response.getBookIds().contains(bookId);
        } catch (Exception e) {
            log.error("Error checking purchase history: {}", e.getMessage());
            return false;
        }
    }
    
    @lombok.Data
    private static class OrdersResponse {
        private List<Long> bookIds;
    }
}