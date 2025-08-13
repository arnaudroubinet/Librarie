package org.roubinet.librarie.application.port.out;

import org.roubinet.librarie.domain.entity.Author;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary port (driven port) for author persistence operations.
 * This interface will be implemented by JPA repository adapters.
 */
public interface AuthorRepository {
    
    /**
     * Find all authors with cursor-based pagination.
     * 
     * @param cursor the pagination cursor (null for first page)
     * @param limit the maximum number of authors to return
     * @return cursor-paginated result containing authors
     */
    CursorPageResult<Author> findAll(String cursor, int limit);
    
    /**
     * Find an author by its ID.
     * 
     * @param id the author's UUID
     * @return optional containing the author if found
     */
    Optional<Author> findById(UUID id);
    
    /**
     * Find authors by name containing the search term (case-insensitive) with cursor pagination.
     * 
     * @param name the name search term
     * @param cursor the pagination cursor (null for first page)
     * @param limit the maximum number of authors to return
     * @return cursor-paginated result containing matching authors
     */
    CursorPageResult<Author> findByNameContainingIgnoreCase(String name, String cursor, int limit);
    
    /**
     * Save an author (create or update).
     * 
     * @param author the author to save
     * @return the saved author
     */
    Author save(Author author);
    
    /**
     * Delete an author by ID.
     * 
     * @param id the author's UUID
     */
    void deleteById(UUID id);
    
    /**
     * Check if an author exists by ID.
     * 
     * @param id the author's UUID
     * @return true if the author exists
     */
    boolean existsById(UUID id);
    
    /**
     * Count total number of authors.
     * 
     * @return total count
     */
    long count();
}