package org.motpassants.domain.port.out;

import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.domain.core.model.BookSearchCriteria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for book repository operations.
 * Defines the contract for book persistence without coupling to specific technologies.
 * Pure domain interface without infrastructure dependencies.
 */
public interface BookRepository {

    /**
     * Find all books with pagination.
     * 
     * @param cursor pagination cursor
     * @param limit number of items per page
     * @return paginated books
     */
    PageResult<Book> findAll(String cursor, int limit);

    /**
     * Find books that belong to a given series, using the same cursor-based pagination
     * contract as findAll.
     * The ordering must match the global listing (created_at DESC, id DESC) to keep
     * cursor semantics consistent across lists.
     *
     * @param seriesId the series ID to filter on
     * @param cursor pagination cursor, base64("<epochMicros>|<uuid>") or null for first page
     * @param limit max items per page
     * @return paginated result of books in the series
     */
    PageResult<Book> findBySeries(UUID seriesId, String cursor, int limit);

    /**
     * Find books that belong to a given series ordered by their series index (ascending).
     * Null indexes are ordered last. This is intended for selecting a representative cover
     * for a series from its earliest-indexed book.
     *
     * @param seriesId the series ID to filter on
     * @param limit maximum number of books to return (use a small cap like 50)
     * @return list of books in the series ordered by index
     */
    java.util.List<Book> findBySeriesOrderByIndex(UUID seriesId, int limit);

    /**
     * Find a book by its ID.
     * 
     * @param id the book ID
     * @return optional containing the book if found
     */
    Optional<Book> findById(UUID id);

    /**
     * Find a book by its file path.
     * 
     * @param path the file path
     * @return optional containing the book if found
     */
    Optional<Book> findByPath(String path);

    /**
     * Find a book by its ISBN.
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
     * Delete a book by its ID.
     * 
     * @param id the book ID
     */
    void deleteById(UUID id);

    /**
     * Check if a book exists by ID.
     * 
     * @param id the book ID
     * @return true if book exists
     */
    boolean existsById(UUID id);

    /**
     * Count total number of books.
     * 
     * @return total count
     */
    long count();

    /**
     * Search books by criteria.
     * 
     * @param criteria search criteria
     * @return paginated search results
     */
    PageResult<Book> search(BookSearchCriteria criteria);

    /**
     * Find books by title or author containing the query string.
     * 
     * @param query search query
     * @return list of matching books
     */
    List<Book> findByTitleOrAuthorContaining(String query);

    /**
     * Find books by criteria.
     * 
     * @param criteria search criteria
     * @return list of matching books
     */
    List<Book> findByCriteria(BookSearchCriteria criteria);

    /**
     * Link a book to a series with an optional index (order within the series).
     * If the link already exists, the index will be updated.
     */
    void linkBookToSeries(UUID bookId, UUID seriesId, Double seriesIndex);
}