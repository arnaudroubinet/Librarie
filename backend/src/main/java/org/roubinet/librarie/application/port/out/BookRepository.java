package org.roubinet.librarie.application.port.out;

import org.roubinet.librarie.domain.entity.Book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary port (driven port) for book persistence operations.
 * This interface will be implemented by JPA repository adapters.
 */
public interface BookRepository {
    
    /**
     * Find all books with pagination.
     * 
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of books
     */
    List<Book> findAll(int page, int size);
    
    /**
     * Find a book by its ID.
     * 
     * @param id the book's UUID
     * @return optional containing the book if found
     */
    Optional<Book> findById(UUID id);
    
    /**
     * Find books by title containing the search term (case-insensitive).
     * 
     * @param title the title search term
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of matching books
     */
    List<Book> findByTitleContainingIgnoreCase(String title, int page, int size);
    
    /**
     * Find books by path (useful for preventing duplicates during ingest).
     * 
     * @param path the file path
     * @return optional containing the book if found
     */
    Optional<Book> findByPath(String path);
    
    /**
     * Find books by ISBN.
     * 
     * @param isbn the ISBN
     * @return optional containing the book if found
     */
    Optional<Book> findByIsbn(String isbn);
    
    /**
     * Save a book (create or update).
     * 
     * @param book the book to save
     * @return the saved book
     */
    Book save(Book book);
    
    /**
     * Delete a book by ID.
     * 
     * @param id the book's UUID
     */
    void deleteById(UUID id);
    
    /**
     * Check if a book exists by ID.
     * 
     * @param id the book's UUID
     * @return true if the book exists
     */
    boolean existsById(UUID id);
    
    /**
     * Count total number of books.
     * 
     * @return total count
     */
    long count();
    
    /**
     * Find books by author name (joining through relationships).
     * 
     * @param authorName the author's name
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of books by the author
     */
    List<Book> findByAuthorName(String authorName, int page, int size);
    
    /**
     * Find books by series name (joining through relationships).
     * 
     * @param seriesName the series name
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of books in the series
     */
    List<Book> findBySeriesName(String seriesName, int page, int size);
    
    /**
     * Search books by multiple criteria (title, author, ISBN, etc.).
     * 
     * @param searchQuery the search query
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of matching books
     */
    List<Book> searchBooks(String searchQuery, int page, int size);
}