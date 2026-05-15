package com.ecommerce.book_service.repository;

import org.springframework.data.jpa.repository.Modifying;
import com.ecommerce.book_service.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Find by title (case-insensitive)
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Find by author (case-insensitive)
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    // Find by genre
    Page<Book> findByGenreIgnoreCase(String genre, Pageable pageable);

    // Find by ISBN
    Book findByIsbn(String isbn);

    // Find featured books
    Page<Book> findByIsFeaturedTrue(Pageable pageable);

    // Find bestsellers
    Page<Book> findByIsBestsellerTrue(Pageable pageable);

    // Find books by price range
    Page<Book> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Search by keyword in title, author, or description
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Find books with low stock
    List<Book> findByStockLessThan(Integer threshold);

    // Count by genre
    @Query("SELECT b.genre, COUNT(b) FROM Book b GROUP BY b.genre")
    List<Object[]> countBooksByGenre();

    // Find new arrivals (last 30 days)
    @Query("SELECT b FROM Book b WHERE b.createdAt >= :date")
    Page<Book> findNewArrivals(@Param("date") LocalDateTime date, Pageable pageable);
    // Update stock
    @Modifying
    @Query("UPDATE Book b SET b.stock = :stock WHERE b.id = :id")
    int updateStock(@Param("id") Long id, @Param("stock") Integer stock);
}