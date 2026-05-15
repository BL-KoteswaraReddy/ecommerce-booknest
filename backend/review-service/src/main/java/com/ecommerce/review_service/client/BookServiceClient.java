package com.ecommerce.review_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.book.url}")
    private String bookServiceUrl;
    
    public boolean bookExists(Long bookId) {
        try {
            webClient.get()
                .uri(bookServiceUrl + "/api/books/{id}", bookId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void updateBookRating(Long bookId, Double averageRating, Integer reviewCount) {
        try {
            webClient.patch()
                .uri(bookServiceUrl + "/api/books/{id}/rating", bookId)
                .bodyValue(new RatingUpdateRequest(averageRating, reviewCount))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            log.error("Error updating book rating: {}", e.getMessage());
        }
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class RatingUpdateRequest {
        private Double averageRating;
        private Integer reviewCount;
    }
}