package com.ecommerce.book_service.service.impl;
import com.ecommerce.book_service.dto.BookDTO;
import com.ecommerce.book_service.dto.BookRequestDTO;
import com.ecommerce.book_service.dto.BookResponseDTO;
import com.ecommerce.book_service.entity.Book;
import com.ecommerce.book_service.exception.BookNotFoundException;
import com.ecommerce.book_service.exception.DuplicateIsbnException;
import com.ecommerce.book_service.exception.InsufficientStockException;
import com.ecommerce.book_service.repository.BookRepository;
import com.ecommerce.book_service.search.BookSearchIndexService;
import com.ecommerce.book_service.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookSearchIndexService bookSearchIndexService;

    @Override
    @CacheEvict(value = {"booksById", "bookLists", "bookGenres"}, allEntries = true)
    public BookDTO createBook(BookRequestDTO bookRequest) {
        // Check for duplicate ISBN
        if (bookRequest.getIsbn() != null && bookRepository.findByIsbn(bookRequest.getIsbn()) != null) {
            throw new DuplicateIsbnException("Book with ISBN " + bookRequest.getIsbn() + " already exists");
        }

        Book book = convertToEntity(bookRequest);
        Book savedBook = bookRepository.save(book);
        bookSearchIndexService.indexBook(savedBook);
        return convertToDTO(savedBook);
    }

    @Override
    @CacheEvict(value = {"booksById", "bookLists", "bookGenres"}, allEntries = true)
    public BookDTO updateBook(Long id, BookRequestDTO bookRequest) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        // Check ISBN uniqueness if changed
        if (bookRequest.getIsbn() != null && !bookRequest.getIsbn().equals(existingBook.getIsbn())) {
            Book bookWithIsbn = bookRepository.findByIsbn(bookRequest.getIsbn());
            if (bookWithIsbn != null && !bookWithIsbn.getId().equals(id)) {
                throw new DuplicateIsbnException("Book with ISBN " + bookRequest.getIsbn() + " already exists");
            }
        }

        updateEntity(existingBook, bookRequest);
        Book updatedBook = bookRepository.save(existingBook);
        bookSearchIndexService.indexBook(updatedBook);
        return convertToDTO(updatedBook);
    }

    @Override
    @CacheEvict(value = {"booksById", "bookLists", "bookGenres"}, allEntries = true)
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
        bookSearchIndexService.deleteBook(id);
    }

    @Override
    @Cacheable(value = "booksById", key = "#id")
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        return convertToDTO(book);
    }

    @Override
    @Cacheable(value = "bookLists", key = "'all:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
    public BookResponseDTO getAllBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findAll(pageable);
        return convertToResponseDTO(books);
    }

    @Override
    @Cacheable(value = "bookLists", key = "'search:' + #keyword + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
    public BookResponseDTO searchBooks(String keyword, Pageable pageable) {
        try {
            return bookSearchIndexService.search(keyword, pageable);
        } catch (Exception ex) {
            Page<Book> books = bookRepository.searchByKeyword(keyword, pageable);
            return convertToResponseDTO(books);
        }
    }

    @Override
    @CacheEvict(value = "bookLists", allEntries = true)
    public long reindexSearch() {
        return bookSearchIndexService.reindexAllBooks();
    }

    @Override
    @Cacheable(value = "bookLists", key = "'genre:' + #genre + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
    public BookResponseDTO getBooksByGenre(String genre, Pageable pageable) {
        Page<Book> books = bookRepository.findByGenreIgnoreCase(genre, pageable);
        return convertToResponseDTO(books);
    }

    @Override
    @Cacheable(value = "bookLists", key = "'author:' + #author + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
    public BookResponseDTO getBooksByAuthor(String author, Pageable pageable) {
        Page<Book> books = bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
        return convertToResponseDTO(books);
    }

    @Override
    @Cacheable(value = "bookLists", key = "'featured:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
    public BookResponseDTO getFeaturedBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findByIsFeaturedTrue(pageable);
        return convertToResponseDTO(books);
    }

    @Override
    @Cacheable(value = "bookLists", key = "'bestsellers:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
    public BookResponseDTO getBestsellers(Pageable pageable) {
        Page<Book> books = bookRepository.findByIsBestsellerTrue(pageable);
        return convertToResponseDTO(books);
    }

    @Override
    @Cacheable(value = "bookLists", key = "'new-arrivals:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
    public BookResponseDTO getNewArrivals(Pageable pageable) {
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        Page<Book> books = bookRepository.findNewArrivals(last30Days, pageable);
        return convertToResponseDTO(books);
    }

    @Override
    @Cacheable(value = "bookLists", key = "'price:' + #minPrice + ':' + #maxPrice + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
    public BookResponseDTO getBooksByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Book> books = bookRepository.findByPriceBetween(
                BigDecimal.valueOf(minPrice),
                BigDecimal.valueOf(maxPrice),
                pageable
        );
        return convertToResponseDTO(books);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"booksById", "bookLists"}, allEntries = true)
    public void updateStock(Long id, Integer quantity) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        int newStock = book.getStock() - quantity;
        if (newStock < 0) {
            throw new InsufficientStockException("Insufficient stock. Available: " + book.getStock());
        }

        book.setStock(newStock);
        Book savedBook = bookRepository.save(book);
        bookSearchIndexService.indexBook(savedBook);
    }

    @Override
    public boolean checkStock(Long id, Integer requestedQuantity) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        return book.getStock() >= requestedQuantity;
    }

    @Override
    @Cacheable(value = "bookGenres", key = "'all'")
    public List<String> getAllGenres() {
        return List.of("Fiction", "Non-Fiction", "Mystery", "Thriller",
                "Romance", "Science Fiction", "Fantasy", "Biography",
                "History", "Self-Help", "Business", "Children");
    }

    // Helper methods for conversion
    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setGenre(book.getGenre());
        dto.setPublisher(book.getPublisher());
        dto.setPrice(book.getPrice());
        dto.setStock(book.getStock());
        dto.setRating(book.getRating().doubleValue());
        dto.setDescription(book.getDescription());
        dto.setCoverImageUrl(book.getCoverImageUrl());
        dto.setPublishedDate(book.getPublishedDate());
        dto.setIsFeatured(book.getIsFeatured());
        dto.setIsBestseller(book.getIsBestseller());
        dto.setReviewCount(book.getReviewCount());
        return dto;
    }

    private Book convertToEntity(BookRequestDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setGenre(dto.getGenre());
        book.setPublisher(dto.getPublisher());
        book.setPrice(dto.getPrice());
        book.setStock(dto.getStock());
        book.setDescription(dto.getDescription());
        book.setCoverImageUrl(dto.getCoverImageUrl());
        book.setPublishedDate(dto.getPublishedDate());
        book.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
        book.setIsBestseller(dto.getIsBestseller() != null ? dto.getIsBestseller() : false);
        return book;
    }

    private void updateEntity(Book book, BookRequestDTO dto) {
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setGenre(dto.getGenre());
        book.setPublisher(dto.getPublisher());
        book.setPrice(dto.getPrice());
        book.setStock(dto.getStock());
        book.setDescription(dto.getDescription());
        book.setCoverImageUrl(dto.getCoverImageUrl());
        book.setPublishedDate(dto.getPublishedDate());
        book.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : book.getIsFeatured());
        book.setIsBestseller(dto.getIsBestseller() != null ? dto.getIsBestseller() : book.getIsBestseller());
    }

    private BookResponseDTO convertToResponseDTO(Page<Book> page) {
        List<BookDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return BookResponseDTO.builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
