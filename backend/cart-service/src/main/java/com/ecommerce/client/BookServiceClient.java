//package com.ecommerce.client;
//
//import com.ecommerce.dto.BookInfoDTO;
//import com.ecommerce.exception.CartOperationException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//import reactor.core.publisher.Mono;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class BookServiceClient {
//
//    private final WebClient webClient;
//
//    @Value("${book.service.url}")
//    private String bookServiceUrl;
//
//    public BookInfoDTO getBookInfo(Long bookId) {
//        try {
//            return webClient.get()
//                .uri(bookServiceUrl + "/api/books/{id}", bookId)
//                .retrieve()
//                .bodyToMono(BookInfoDTO.class)
//                .block();
//        } catch (WebClientResponseException.NotFound e) {
//            log.error("Book not found with id: {}", bookId);
//            throw new CartOperationException("Book not found with id: " + bookId);
//        } catch (Exception e) {
//            log.error("Error fetching book info: {}", e.getMessage());
//            throw new CartOperationException("Failed to fetch book information");
//        }
//    }
//
//    public boolean checkStock(Long bookId, Integer quantity) {
//        try {
//            // ✅ FIXED - build full URL as plain string, no uriBuilder
//            String url = bookServiceUrl + "/api/books/" + bookId + "/check-stock?quantity=" + quantity;
//            String response = webClient.get()
//                    .uri(url)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//            log.info("Book info received: {}", response); // ← Add this
//            return response != null && response.contains("\"available\":true");
//        } catch (Exception e) {
//            log.error("Error checking stock: {}", e.getMessage());
//            return false;
//        }
//    }
//}

package com.ecommerce.client;

import com.ecommerce.dto.BookInfoDTO;
import com.ecommerce.exception.CartOperationException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookServiceClient {

    private final WebClient webClient;

    @Value("${book.service.url}")
    private String bookServiceUrl;

    // ✅ Inner wrapper class to match ApiResponse<T>
    @Data
    static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private String timestamp;
    }

    @Data
    static class StockData {
        private Boolean available;
    }

    public BookInfoDTO getBookInfo(Long bookId) {
        try {
            ApiResponse<BookInfoDTO> response = webClient.get()
                    .uri(bookServiceUrl + "/api/books/" + bookId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<BookInfoDTO>>() {})
                    .block(Duration.ofSeconds(4));

            if (response == null || response.getData() == null) {
                throw new CartOperationException("Book not found with id: " + bookId);
            }
            return response.getData();

        } catch (WebClientResponseException.NotFound e) {
            log.error("Book not found with id: {}", bookId);
            throw new CartOperationException("Book not found with id: " + bookId);
        } catch (CartOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching book info: {}", e.getMessage());
            throw new CartOperationException("Failed to fetch book information");
        }
    }

    public boolean checkStock(Long bookId, Integer quantity) {
        try {
            String url = bookServiceUrl + "/api/books/" + bookId + "/check-stock?quantity=" + quantity;

            ApiResponse<StockData> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<StockData>>() {})
                    .block(Duration.ofSeconds(4));

            log.info("Stock check response: {}", response);

            return response != null
                    && response.getData() != null
                    && Boolean.TRUE.equals(response.getData().getAvailable());

        } catch (Exception e) {
            log.error("Error checking stock: {}", e.getMessage());
            return false;
        }
    }
}
