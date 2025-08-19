package org.motpassants.application.service;

import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.domain.core.model.BookSearchCriteria;
import org.motpassants.domain.port.in.BookUseCase;
import org.motpassants.domain.port.out.BookRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service implementing book management use cases.
 * This is the core business logic layer in the hexagonal architecture.
 * Orchestrates domain objects and outbound ports.
 */
@ApplicationScoped
public class BookService implements BookUseCase {
    
    private final BookRepository bookRepository;
    
    @Inject
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    @Override
    public PageResult<Book> getAllBooks(String cursor, int limit) {
        // Validate pagination parameters
        if (limit <= 0) {
            limit = 20; // Default page size
        }
        if (limit > 100) {
            limit = 100; // Max page size
        }
        
        return bookRepository.findAll(cursor, limit);
    }

    @Override
    public PageResult<Book> getBooksBySeries(UUID seriesId, String cursor, int limit) {
        if (seriesId == null) {
            return new PageResult<>(java.util.List.of(), null, null, false, false, 0);
        }
        if (limit <= 0) limit = 20;
        if (limit > 100) limit = 100;
        return bookRepository.findBySeries(seriesId, cursor, limit);
    }
    
    @Override
    public Optional<Book> getBookById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        return bookRepository.findById(id);
    }

    /** Get contributors grouped by role for a book. */
    public java.util.Map<String, java.util.List<org.motpassants.domain.core.model.Author>> getContributors(UUID bookId) {
        if (bookId == null) return java.util.Map.of();
        return bookRepository.findContributorsByBook(bookId);
    }

    /**
     * Helper for series image fallback: fetch a few books of a series ordered by series index.
     */
    public java.util.List<Book> getBooksBySeriesOrderedByIndex(UUID seriesId, int limit) {
        if (seriesId == null) return java.util.List.of();
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        return bookRepository.findBySeriesOrderByIndex(seriesId, limit);
    }

    @Override
    @Transactional
    public Book createBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }
        
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty");
        }
        
        // Generate path if not provided
        if (book.getPath() == null || book.getPath().trim().isEmpty()) {
            book.setPath("/books/" + UUID.randomUUID().toString());
        }
        
        // Check if book with same path already exists
        Optional<Book> existing = bookRepository.findByPath(book.getPath());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Book with path '" + book.getPath() + "' already exists");
        }
        
        // Set ID if not present
        if (book.getId() == null) {
            book.setId(UUID.randomUUID());
        }
        
        // Set timestamps
        OffsetDateTime now = OffsetDateTime.now();
        book.setCreatedAt(now);
        book.setUpdatedAt(now);
        
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public Book updateBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }
        
        if (book.getId() == null) {
            throw new IllegalArgumentException("Book ID cannot be null for update");
        }
        
        if (!bookRepository.existsById(book.getId())) {
            throw new IllegalArgumentException("Book with ID " + book.getId() + " does not exist");
        }
        
        book.markAsUpdated();
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Book with ID " + id + " does not exist");
        }
        
        bookRepository.deleteById(id);
    }

    @Override
    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        
        return bookRepository.findByTitleOrAuthorContaining(query.trim());
    }

    @Override
    public PageResult<Book> searchBooks(BookSearchCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }
        
        return bookRepository.search(criteria);
    }

    @Override
    public List<Book> searchBooksByCriteria(BookSearchCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }
        
        return bookRepository.findByCriteria(criteria);
    }

    @Override
    public long getTotalBooksCount() {
        return bookRepository.count();
    }
}