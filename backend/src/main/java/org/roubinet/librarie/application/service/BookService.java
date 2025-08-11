package org.roubinet.librarie.application.service;

import org.roubinet.librarie.application.port.in.BookUseCase;
import org.roubinet.librarie.application.port.in.BookSearchCriteria;
import org.roubinet.librarie.application.port.out.BookRepository;
import org.roubinet.librarie.application.service.title.TitleSortingService;
import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;
import org.roubinet.librarie.infrastructure.config.LibrarieConfigProperties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Application service implementing book management use cases.
 * This is the core business logic layer in the hexagonal architecture.
 */
@ApplicationScoped
public class BookService implements BookUseCase {
    
    private final BookRepository bookRepository;
    private final TitleSortingService titleSortingService;
    private final LibrarieConfigProperties config;
    
    @Inject
    public BookService(BookRepository bookRepository, 
                      TitleSortingService titleSortingService,
                      LibrarieConfigProperties config) {
        this.bookRepository = bookRepository;
        this.titleSortingService = titleSortingService;
        this.config = config;
    }
    
    @Override
    public CursorPageResult<Book> getAllBooks(String cursor, int limit) {
        // Validate pagination parameters using configuration
        if (limit <= 0) {
            limit = config.pagination().defaultPageSize();
        }
        if (limit > config.pagination().maxPageSize()) {
            limit = config.pagination().maxPageSize();
        }
        
        return bookRepository.findAll(cursor, limit);
    }
    
    @Override
    public Optional<Book> getBookById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return bookRepository.findById(id);
    }
    
    @Override
    public CursorPageResult<Book> searchBooks(String query, String cursor, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks(cursor, limit);
        }
        
        // Validate pagination parameters
        if (limit <= 0) {
            limit = config.pagination().defaultPageSize();
        }
        if (limit > config.pagination().maxPageSize()) {
            limit = config.pagination().maxPageSize();
        }
        
        return bookRepository.searchBooks(query.trim(), cursor, limit);
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
    public CursorPageResult<Book> getBooksByCriteria(BookSearchCriteria criteria, String cursor, int limit) {
        if (criteria == null) {
            return getAllBooks(cursor, limit);
        }
        
        // Validate pagination parameters
        if (limit <= 0) {
            limit = config.pagination().defaultPageSize();
        }
        if (limit > config.pagination().maxPageSize()) {
            limit = config.pagination().maxPageSize();
        }
        
        return bookRepository.findByCriteria(criteria, cursor, limit);
    }

    
    /**
     * Generate a sortable title using language-specific strategies.
     * Based on Calibre's title sorting logic with internationalization support.
     */
    private String generateTitleSort(String title) {
        return titleSortingService.generateSortableTitle(title);
    }
    
    /**
     * Generate a sortable title for a specific language.
     * 
     * @param title the original title
     * @param languageCode ISO 639-1 language code
     * @return the sortable title
     */
    private String generateTitleSort(String title, String languageCode) {
        return titleSortingService.generateSortableTitle(title, languageCode);
    }
}