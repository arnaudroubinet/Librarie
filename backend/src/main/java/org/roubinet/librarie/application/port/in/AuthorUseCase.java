package org.roubinet.librarie.application.port.in;

import org.roubinet.librarie.domain.entity.Author;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case interface for Author operations.
 */
public interface AuthorUseCase {
    
    /**
     * Get all authors.
     * 
     * @return List of all authors
     */
    List<Author> getAllAuthors();
    
    /**
     * Get an author by its ID.
     * 
     * @param id Author ID
     * @return Optional containing the author if found
     */
    Optional<Author> getAuthorById(UUID id);
    
    /**
     * Search authors by name.
     * 
     * @param query Search query for author name
     * @return List of matching authors
     */
    List<Author> searchAuthors(String query);
}