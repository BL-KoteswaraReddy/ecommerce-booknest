package com.ecommerce.client;

import com.ecommerce.exception.OrderServiceException;
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


    @Value("${app.internal.secret}")   // ← Add this
    private String internalSecret;


    public void updateStock(Long bookId, Integer quantity) {
        try {
            webClient.patch()
                .uri(bookServiceUrl + "/api/books/{id}/stock", bookId)
                    .header("X-Internal-Secret", internalSecret)  //
                .bodyValue(java.util.Map.of("quantity", quantity))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            log.error("Error updating stock for book {}: {}", bookId, e.getMessage());
            throw new OrderServiceException("Failed to update book stock");
        }
    }
}
