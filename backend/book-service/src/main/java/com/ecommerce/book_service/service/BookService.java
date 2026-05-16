package com.ecommerce.book_service.service;
import com.ecommerce.book_service.dto.BookDTO;
import com.ecommerce.book_service.dto.BookRequestDTO;
import com.ecommerce.book_service.dto.BookResponseDTO;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookService {
    BookDTO createBook(BookRequestDTO bookRequest);
    BookDTO updateBook(Long id, BookRequestDTO bookRequest);
    void deleteBook(Long id);
    BookDTO getBookById(Long id);
    BookResponseDTO getAllBooks(Pageable pageable);
    BookResponseDTO searchBooks(String keyword, Pageable pageable);
    long reindexSearch();
    BookResponseDTO getBooksByGenre(String genre, Pageable pageable);
    BookResponseDTO getBooksByAuthor(String author, Pageable pageable);
    BookResponseDTO getFeaturedBooks(Pageable pageable);
    BookResponseDTO getBestsellers(Pageable pageable);
    BookResponseDTO getNewArrivals(Pageable pageable);
    BookResponseDTO getBooksByPriceRange(Double minPrice, Double maxPrice, Pageable pageable);
    void updateStock(Long id, Integer quantity);
    boolean checkStock(Long id, Integer requestedQuantity);
    List<String> getAllGenres();
}
