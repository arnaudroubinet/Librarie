package org.motpassants.domain.port.out;

import org.motpassants.domain.core.model.Author;
import org.motpassants.domain.core.model.PageResult;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for Author persistence operations.
 * Defines the contract that must be implemented by persistence adapters.
 * Pure domain interface with no framework dependencies.
 */
public interface AuthorRepositoryPort {
    
    /**
     * Save a new author.
     */
    Author save(Author author);
    
    /**
     * Update an existing author.
     */
    Author update(Author author);
    
    /**
     * Find author by ID.
     */
    Optional<Author> findById(UUID id);
    
    /**
     * Find author by name.
     */
    Optional<Author> findByName(String name);
    
    /**
     * Delete author by ID.
     */
    void deleteById(UUID id);
    
    /**
     * Check if author exists by ID.
     */
    boolean existsById(UUID id);
    
    /**
     * Check if author exists by name.
     */
    boolean existsByName(String name);
    
    /**
     * Find all authors with cursor-based pagination.
     */
    PageResult<Author> findAll(String cursor, int limit);

    /**
     * Find all authors with cursor-based pagination and sorting support.
     */
    PageResult<Author> findAll(String cursor, int limit, org.motpassants.domain.core.model.AuthorSortCriteria sortCriteria);
    
    /**
     * Search authors by name with cursor-based pagination.
     */
    PageResult<Author> searchByName(String query, String cursor, int limit);
    
    /**
     * Count total number of authors.
     */
    long count();
}