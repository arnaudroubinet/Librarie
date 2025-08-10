package org.roubinet.librarie.application.port.in;

import org.roubinet.librarie.domain.entity.Book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface defining book-related use cases.
 * Represents the primary ports (driving ports) for book operations.
 */
public interface BookUseCase {
    
    /**
     * Retrieve all books with pagination support.
     * 
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of books for the requested page
     */
    List<Book> getAllBooks(int page, int size);
    
    /**
     * Retrieve a book by its unique identifier.
     * 
     * @param id the book's UUID
     * @return optional containing the book if found
     */
    Optional<Book> getBookById(UUID id);
    
    /**
     * Search books by title, author, or other metadata.
     * 
     * @param query the search query
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of books matching the search criteria
     */
    List<Book> searchBooks(String query, int page, int size);
    
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
     * Get books by author name.
     * 
     * @param authorName the author's name
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of books by the specified author
     */
    List<Book> getBooksByAuthor(String authorName, int page, int size);
    
    /**
     * Get books by series name.
     * 
     * @param seriesName the series name
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of books in the specified series
     */
    List<Book> getBooksBySeries(String seriesName, int page, int size);
}