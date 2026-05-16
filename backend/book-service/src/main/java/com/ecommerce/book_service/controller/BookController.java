package com.ecommerce.book_service.controller;

import com.ecommerce.book_service.dto.*;
import com.ecommerce.book_service.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookDTO>> createBook(@Valid @RequestBody BookRequestDTO bookRequest) {
        log.info("reached creating book controller method");
        BookDTO createdBook = bookService.createBook(bookRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Book created successfully", createdBook));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDTO bookRequest) {
        BookDTO updatedBook = bookService.updateBook(id, bookRequest);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Book updated successfully", updatedBook));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Book deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Book retrieved successfully", book));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BookResponseDTO>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        BookResponseDTO books = bookService.getAllBooks(pageable);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Books retrieved successfully", books));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<BookResponseDTO>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        BookResponseDTO books = bookService.searchBooks(keyword, pageable);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Search results retrieved successfully", books));
    }

    @PostMapping("/search/reindex")
    public ResponseEntity<ApiResponse<Map<String, Long>>> reindexSearch() {
        long indexedBooks = bookService.reindexSearch();
        Map<String, Long> response = new HashMap<>();
        response.put("indexedBooks", indexedBooks);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Book search index rebuilt successfully", response));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<ApiResponse<BookResponseDTO>> getBooksByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        BookResponseDTO books = bookService.getBooksByGenre(genre, pageable);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Books by genre retrieved successfully", books));
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<ApiResponse<BookResponseDTO>> getBooksByAuthor(
            @PathVariable String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        BookResponseDTO books = bookService.getBooksByAuthor(author, pageable);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Books by author retrieved successfully", books));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<BookResponseDTO>> getFeaturedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        BookResponseDTO books = bookService.getFeaturedBooks(pageable);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Featured books retrieved successfully", books));
    }

    @GetMapping("/bestsellers")
    public ResponseEntity<ApiResponse<BookResponseDTO>> getBestsellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        BookResponseDTO books = bookService.getBestsellers(pageable);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Bestsellers retrieved successfully", books));
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<ApiResponse<BookResponseDTO>> getNewArrivals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        BookResponseDTO books = bookService.getNewArrivals(pageable);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "New arrivals retrieved successfully", books));
    }

    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<BookResponseDTO>> getBooksByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        BookResponseDTO books = bookService.getBooksByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Books by price range retrieved successfully", books));
    }

    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<String>>> getAllGenres() {
        List<String> genres = bookService.getAllGenres();
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Genres retrieved successfully", genres));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Void>> updateStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {

        Integer quantity = request.get("quantity");
        bookService.updateStock(id, quantity);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Stock updated successfully", null));
    }

    @GetMapping("/{id}/check-stock")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {

        boolean available = bookService.checkStock(id, quantity);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);

        return ResponseEntity
                .ok(new ApiResponse<>(true, "Stock check completed", response));
    }
}
