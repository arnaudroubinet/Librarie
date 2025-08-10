package org.roubinet.librarie.application.service;

import org.roubinet.librarie.application.port.in.BookUseCase;
import org.roubinet.librarie.application.port.out.BookRepository;
import org.roubinet.librarie.domain.entity.Book;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service implementing book management use cases.
 * This is the core business logic layer in the hexagonal architecture.
 */
@ApplicationScoped
public class BookService implements BookUseCase {
    
    private final BookRepository bookRepository;
    
    @Inject
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    @Override
    public List<Book> getAllBooks(int page, int size) {
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0) size = 20; // Default page size
        if (size > 100) size = 100; // Maximum page size
        
        return bookRepository.findAll(page, size);
    }
    
    @Override
    public Optional<Book> getBookById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return bookRepository.findById(id);
    }
    
    @Override
    public List<Book> searchBooks(String query, int page, int size) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks(page, size);
        }
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        return bookRepository.searchBooks(query.trim(), page, size);
    }
    
    @Override
    @Transactional
    public Book createBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }
        
        // Validate required fields
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title is required");
        }
        
        if (book.getPath() == null || book.getPath().trim().isEmpty()) {
            throw new IllegalArgumentException("Book path is required");
        }
        
        // Check for duplicate path
        Optional<Book> existingBook = bookRepository.findByPath(book.getPath());
        if (existingBook.isPresent()) {
            throw new IllegalArgumentException("A book with this path already exists");
        }
        
        // Set title sort if not provided
        if (book.getTitleSort() == null || book.getTitleSort().trim().isEmpty()) {
            book.setTitleSort(generateTitleSort(book.getTitle()));
        }
        
        return bookRepository.save(book);
    }
    
    @Override
    @Transactional
    public Book updateBook(Book book) {
        if (book == null || book.getId() == null) {
            throw new IllegalArgumentException("Book and book ID cannot be null");
        }
        
        // Verify book exists
        if (!bookRepository.existsById(book.getId())) {
            throw new IllegalArgumentException("Book not found with ID: " + book.getId());
        }
        
        // Validate required fields
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title is required");
        }
        
        // Update title sort if title changed
        if (book.getTitleSort() == null || book.getTitleSort().trim().isEmpty()) {
            book.setTitleSort(generateTitleSort(book.getTitle()));
        }
        
        return bookRepository.save(book);
    }
    
    @Override
    @Transactional
    public void deleteBook(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Book not found with ID: " + id);
        }
        
        bookRepository.deleteById(id);
    }
    
    @Override
    public long getTotalBooksCount() {
        return bookRepository.count();
    }
    
    @Override
    public List<Book> getBooksByAuthor(String authorName, int page, int size) {
        if (authorName == null || authorName.trim().isEmpty()) {
            return List.of();
        }
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        return bookRepository.findByAuthorName(authorName.trim(), page, size);
    }
    
    @Override
    public List<Book> getBooksBySeries(String seriesName, int page, int size) {
        if (seriesName == null || seriesName.trim().isEmpty()) {
            return List.of();
        }
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        return bookRepository.findBySeriesName(seriesName.trim(), page, size);
    }
    
    /**
     * Generate a sortable title by removing common articles.
     * Based on Calibre's title sorting logic.
     */
    private String generateTitleSort(String title) {
        if (title == null) {
            return "";
        }
        
        String cleaned = title.trim();
        
        // Remove common articles for sorting (English)
        if (cleaned.toLowerCase().startsWith("the ")) {
            cleaned = cleaned.substring(4).trim() + ", The";
        } else if (cleaned.toLowerCase().startsWith("a ")) {
            cleaned = cleaned.substring(2).trim() + ", A";
        } else if (cleaned.toLowerCase().startsWith("an ")) {
            cleaned = cleaned.substring(3).trim() + ", An";
        }
        
        return cleaned;
    }
}