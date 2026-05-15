package com.ecommerce.book_service.search;

import com.ecommerce.book_service.dto.BookDTO;
import com.ecommerce.book_service.dto.BookResponseDTO;
import com.ecommerce.book_service.entity.Book;
import com.ecommerce.book_service.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookSearchIndexService {

    private static final IndexCoordinates BOOK_INDEX = IndexCoordinates.of("books");

    private final ElasticsearchOperations elasticsearchOperations;
    private final BookRepository bookRepository;

    public void indexBook(Book book) {
        try {
            elasticsearchOperations.save(toDocument(book), BOOK_INDEX);
        } catch (Exception ex) {
            log.error("Failed to index book {} in Elasticsearch: {}", book.getId(), ex.getMessage());
        }
    }

    public void deleteBook(Long bookId) {
        try {
            elasticsearchOperations.delete(String.valueOf(bookId), BOOK_INDEX);
        } catch (Exception ex) {
            log.error("Failed to delete book {} from Elasticsearch: {}", bookId, ex.getMessage());
        }
    }

    public BookResponseDTO search(String keyword, Pageable pageable) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m
                        .query(keyword)
                        .fields("title^4", "author^3", "genre^2", "isbn^2", "publisher", "description")
                        .fuzziness("AUTO")
                ))
                .withPageable(pageable)
                .build();

        SearchHits<BookSearchDocument> hits = elasticsearchOperations.search(query, BookSearchDocument.class, BOOK_INDEX);
        List<BookDTO> content = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toDTO)
                .toList();
        long totalElements = hits.getTotalHits();
        int totalPages = pageable.getPageSize() == 0
                ? 0
                : (int) Math.ceil((double) totalElements / pageable.getPageSize());

        return BookResponseDTO.builder()
                .content(content)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(pageable.getPageNumber() + 1 >= totalPages)
                .build();
    }

    public long reindexAllBooks() {
        List<BookSearchDocument> documents = bookRepository.findAll().stream()
                .map(this::toDocument)
                .toList();
        elasticsearchOperations.save(documents, BOOK_INDEX);
        return documents.size();
    }

    private BookSearchDocument toDocument(Book book) {
        BookSearchDocument document = new BookSearchDocument();
        document.setId(String.valueOf(book.getId()));
        document.setTitle(book.getTitle());
        document.setAuthor(book.getAuthor());
        document.setIsbn(book.getIsbn());
        document.setGenre(book.getGenre());
        document.setPublisher(book.getPublisher());
        document.setPrice(book.getPrice());
        document.setStock(book.getStock());
        document.setRating(book.getRating() != null ? book.getRating().doubleValue() : 0.0);
        document.setDescription(book.getDescription());
        document.setCoverImageUrl(book.getCoverImageUrl());
        document.setPublishedDate(book.getPublishedDate());
        document.setIsFeatured(book.getIsFeatured());
        document.setIsBestseller(book.getIsBestseller());
        document.setReviewCount(book.getReviewCount());
        return document;
    }

    private BookDTO toDTO(BookSearchDocument document) {
        BookDTO dto = new BookDTO();
        dto.setId(Long.valueOf(document.getId()));
        dto.setTitle(document.getTitle());
        dto.setAuthor(document.getAuthor());
        dto.setIsbn(document.getIsbn());
        dto.setGenre(document.getGenre());
        dto.setPublisher(document.getPublisher());
        dto.setPrice(document.getPrice());
        dto.setStock(document.getStock());
        dto.setRating(document.getRating());
        dto.setDescription(document.getDescription());
        dto.setCoverImageUrl(document.getCoverImageUrl());
        dto.setPublishedDate(document.getPublishedDate());
        dto.setIsFeatured(document.getIsFeatured());
        dto.setIsBestseller(document.getIsBestseller());
        dto.setReviewCount(document.getReviewCount());
        return dto;
    }
}
