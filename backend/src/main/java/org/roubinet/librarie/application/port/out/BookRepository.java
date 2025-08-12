package org.roubinet.librarie.application.port.out;

import org.roubinet.librarie.application.port.in.BookSearchCriteria;
import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;

import java.util.Optional;
import java.util.UUID;

/**
 * Secondary port (driven port) for book persistence operations.
 * This interface will be implemented by JPA repository adapters.
 */
public interface BookRepository {
    
    /**
     * Find all books with cursor-based pagination.
     * 
     * @param cursor the pagination cursor (null for first page)
     * @param limit the maximum number of books to return
     * @return cursor-paginated result containing books
     */
    CursorPageResult<Book> findAll(String cursor, int limit);
    
    /**
     * Find a book by its ID.
     * 
     * @param id the book's UUID
     * @return optional containing the book if found
     */
    Optional<Book> findById(UUID id);
    
    /**
     * Find books by title containing the search term (case-insensitive) with cursor pagination.
     * 
     * @param title the title search term
     * @param cursor the pagination cursor (null for first page)
     * @param limit the maximum number of books to return
     * @return cursor-paginated result containing matching books
     */
    CursorPageResult<Book> findByTitleContainingIgnoreCase(String title, String cursor, int limit);
    
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
     * Search books by multiple criteria (title, author, ISBN, etc.) with cursor pagination.
     * 
     * @param searchQuery the search query
     * @param cursor the pagination cursor (null for first page)
     * @param limit the maximum number of books to return
     * @return cursor-paginated result containing matching books
     */
    CursorPageResult<Book> searchBooks(String searchQuery, String cursor, int limit);
    
    /**
     * Find books by complex criteria using DSL with cursor pagination.
     * 
     * @param criteria the search criteria
     * @param cursor the pagination cursor (null for first page)
     * @param limit the maximum number of books to return
     * @return cursor-paginated result containing matching books
     */
    CursorPageResult<Book> findByCriteria(BookSearchCriteria criteria, String cursor, int limit);

}