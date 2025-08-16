package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.Author;
import org.motpassants.domain.core.model.PageResult;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port for Author operations.
 * Defines the use cases that can be performed on Author entities.
 * Pure domain interface with no framework dependencies.
 */
public interface AuthorUseCase {
    
    /**
     * Create a new author.
     */
    Author createAuthor(String name, String sortName, Map<String, String> bio,
                       LocalDate birthDate, LocalDate deathDate, String websiteUrl,
                       Map<String, Object> metadata);
    
    /**
     * Get author by ID.
     */
    Optional<Author> getAuthorById(UUID id);
    
    /**
     * Update an existing author.
     */
    Author updateAuthor(UUID id, String name, String sortName, Map<String, String> bio,
                       LocalDate birthDate, LocalDate deathDate, String websiteUrl,
                       Map<String, Object> metadata);
    
    /**
     * Delete an author.
     */
    void deleteAuthor(UUID id);
    
    /**
     * Get all authors with pagination.
     */
    PageResult<Author> getAllAuthors(String cursor, int limit);
    
    /**
     * Search authors by name with pagination.
     */
    PageResult<Author> searchAuthors(String query, String cursor, int limit);
    
    /**
     * Check if an author exists by name.
     */
    boolean existsByName(String name);
}