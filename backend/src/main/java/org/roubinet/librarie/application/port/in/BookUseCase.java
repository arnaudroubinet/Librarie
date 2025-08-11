package org.roubinet.librarie.application.port.in;

import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;

import java.util.Optional;
import java.util.UUID;

/**
 * Port interface defining book-related use cases.
 * Represents the primary ports (driving ports) for book operations.
 */
public interface BookUseCase {
    
    /**
     * Retrieve all books with cursor-based pagination support.
     * 
     * @param cursor the pagination cursor (null for first page)
     * @param limit the maximum number of books to return
     * @return cursor-paginated result containing books and navigation info
     */
    CursorPageResult<Book> getAllBooks(String cursor, int limit);
    
    /**
     * Retrieve a book by its unique identifier.
     * 
     * @param id the book's UUID
     * @return optional containing the book if found
     */
    Optional<Book> getBookById(UUID id);
    
    /**
     * Search books by title, author, or other metadata with cursor pagination.
     * 
     * @param query the search query
     * @param cursor the pagination cursor (null for first page)
     * @param limit the maximum number of books to return
     * @return cursor-paginated result containing matching books and navigation info
     */
    CursorPageResult<Book> searchBooks(String query, String cursor, int limit);
    
    /**
     * Create a new book in the library.
     * 
     * @param book the book to create
     * @return the created book with generated ID
     */
    Book createBook(Book book);
    
    /**
     * Update an existing book's metadata.
     * 
     * @param book the book with updated information
     * @return the updated book
     */
    Book updateBook(Book book);
    
    /**
     * Delete a book from the library.
     * 
     * @param id the book's UUID
     */
    void deleteBook(UUID id);
    
    /**
     * Get total count of books for pagination.
     * 
     * @return total number of books
     */
    long getTotalBooksCount();
    
    /**
     * Get books by complex criteria using a fluent DSL for queries.
     * Supports filtering by multiple fields, sorting, and cursor pagination.
     * 
     * @param criteria the search criteria using DSL
     * @param cursor the pagination cursor (null for first page)
     * @param limit the maximum number of books to return
     * @return cursor-paginated result containing matching books
     */
    CursorPageResult<Book> getBooksByCriteria(BookSearchCriteria criteria, String cursor, int limit);
    
    /**
     * Get books by series name.
     * 
     * @param seriesName the series name
     * @param cursor the pagination cursor (null for first page)
     * @param limit the maximum number of books to return
     * @return cursor-paginated result containing books in the specified series
     */
    CursorPageResult<Book> getBooksBySeries(String seriesName, String cursor, int limit);
}