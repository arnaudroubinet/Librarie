package org.roubinet.librarie.application.port.in;

import org.roubinet.librarie.domain.entity.Author;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Primary port (driving port) for author operations.
 * This interface defines the application business logic for author management.
 */
public interface AuthorUseCase {
    
    /**
     * Get all authors with cursor-based pagination.
     * 
     * @param cursor the pagination cursor
     * @param limit the maximum number of items to return
     * @return paginated result containing authors
     */
    CursorPageResult<Author> getAllAuthors(String cursor, int limit);
    
    /**
     * Get an author by ID.
     * 
     * @param id the author's UUID
     * @return optional containing the author if found
     */
    Optional<Author> getAuthorById(UUID id);
    
    /**
     * Search authors by name.
     * 
     * @param name the name search term
     * @param cursor the pagination cursor
     * @param limit the maximum number of items to return
     * @return paginated result containing matching authors
     */
    CursorPageResult<Author> searchAuthors(String name, String cursor, int limit);
    
    /**
     * Search authors by name (simple version for unified search).
     * 
     * @param query Search query for author name
     * @return List of matching authors
     */
    List<Author> searchAuthors(String query);
    
    /**
     * Create a new author.
     * 
     * @param author the author to create
     * @return the created author
     */
    Author createAuthor(Author author);
    
    /**
     * Update an existing author.
     * 
     * @param author the author to update
     * @return the updated author
     */
    Author updateAuthor(Author author);
    
    /**
     * Delete an author by ID.
     * 
     * @param id the author's UUID
     */
    void deleteAuthor(UUID id);
}