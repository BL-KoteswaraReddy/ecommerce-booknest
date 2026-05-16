package com.ecommerce.wishlist_service.client;

import com.ecommerce.wishlist_service.dto.BookApiResponse;
import com.ecommerce.wishlist_service.dto.BookInfoDTO;
import com.ecommerce.wishlist_service.exception.WishlistServiceException;
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
    
//    public BookInfoDTO getBookInfo(Long bookId) {
//        try {
//            return webClient.get()
//                .uri(bookServiceUrl + "/api/books/{id}", bookId)
//                .retrieve()
//                .bodyToMono(BookInfoDTO.class)
//                .block();
//        } catch (Exception e) {
//            log.error("Error fetching book info: {}", e.getMessage());
//            throw new WishlistServiceException("Failed to fetch book information");
//        }
//    }
//

    public BookInfoDTO getBookInfo(Long bookId) {
        try {
            // First, see the raw response
            BookApiResponse response = webClient.get()
                    .uri(bookServiceUrl + "/api/books/{id}", bookId)
                    .retrieve()
                    .bodyToMono(BookApiResponse.class)
                    .block();
            log.info("RAW book service response: {}", response);  // <-- look at this log
            if (response == null || response.getData() == null) {
                throw new WishlistServiceException("Book not found with id: " + bookId);
            }
            return response.getData();

            // Then deserialize
//            return webClient.get()
//                    .uri(bookServiceUrl + "/api/books/{id}", bookId)
//                    .retrieve()
//                    .bodyToMono(BookInfoDTO.class)
//                    .block();
        } catch (Exception e) {
            log.error("Error fetching book info: {}", e.getMessage());
            throw new WishlistServiceException("Failed to fetch book information");
        }
    }
    public boolean bookExists(Long bookId) {
        try {
            BookInfoDTO book = getBookInfo(bookId);
            return book != null;
        } catch (Exception e) {
            return false;
        }
    }
}