package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.BookSearchCriteria;
import org.motpassants.domain.core.model.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port defining book-related use cases.
 * Represents the primary ports (driving ports) for book operations.
 * Pure domain interface without infrastructure dependencies.
 */
public interface BookUseCase {
    
    /**
     * Retrieve all books with cursor-based pagination support.
     * 
     * @param cursor the pagination cursor (null for first page)
     * @param limit the maximum number of books to return
     * @return cursor-paginated result containing books and navigation info
     */
    PageResult<Book> getAllBooks(String cursor, int limit);

    /**
     * Retrieve books in a given series using cursor-based pagination.
     *
     * @param seriesId the series UUID
     * @param cursor pagination cursor (null for first page)
     * @param limit max items per page
     * @return cursor-paginated result of books in the series
     */
    PageResult<Book> getBooksBySeries(UUID seriesId, String cursor, int limit);
    
    /**
     * Retrieve a book by its unique identifier.
     * 
     * @param id the book's UUID
     * @return optional containing the book if found
     */
    Optional<Book> getBookById(UUID id);

    /**
     * Create a new book.
     * 
     * @param book the book to create
     * @return the created book with assigned ID
     */
    Book createBook(Book book);

    /**
     * Update an existing book.
     * 
     * @param book the book to update
     * @return the updated book
     */
    Book updateBook(Book book);

    /**
     * Delete a book by its ID.
     * 
     * @param id the book's UUID
     */
    void deleteBook(UUID id);

    /**
     * Search books by query.
     * 
     * @param query the search query
     * @return list of matching books
     */
    List<Book> searchBooks(String query);

    /**
     * Search books with criteria.
     * 
     * @param criteria the search criteria
     * @return cursor-paginated result containing matching books
     */
    PageResult<Book> searchBooks(BookSearchCriteria criteria);

    /**
     * Search books with criteria returning a list.
     * 
     * @param criteria the search criteria
     * @return list of matching books
     */
    List<Book> searchBooksByCriteria(BookSearchCriteria criteria);

    /**
     * Count total number of books.
     * 
     * @return total book count
     */
    long getTotalBooksCount();
}