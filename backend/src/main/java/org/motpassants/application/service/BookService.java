package org.motpassants.application.service;

import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.domain.core.model.BookSearchCriteria;
import org.motpassants.domain.port.in.BookUseCase;
import org.motpassants.domain.port.out.BookRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

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
    public Optional<Book> getBookById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        return bookRepository.findById(id);
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
        
        if (book.getPath() == null || book.getPath().trim().isEmpty()) {
            throw new IllegalArgumentException("Book path cannot be empty");
        }
        
        // Check if book with same path already exists
        Optional<Book> existing = bookRepository.findByPath(book.getPath());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Book with path '" + book.getPath() + "' already exists");
        }
        
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
    public long getTotalBooksCount() {
        return bookRepository.count();
    }
}