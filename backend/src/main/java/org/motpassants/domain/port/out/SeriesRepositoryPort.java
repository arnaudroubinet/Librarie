package org.motpassants.domain.port.out;

import org.motpassants.domain.core.model.Series;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Series entity.
 * Defines the persistence contract for series management.
 */
public interface SeriesRepositoryPort {
    
    /**
     * Find all series with pagination.
     * 
     * @param offset Offset for pagination
     * @param limit Maximum number of items to return
     * @return List of series
     */
    List<Series> findAll(int offset, int limit);
    
    /**
     * Count total number of series.
     * 
     * @return Total count of series
     */
    long count();
    
    /**
     * Find a series by its ID.
     * 
     * @param id Series ID
     * @return Optional containing the series if found
     */
    Optional<Series> findById(UUID id);
    
    /**
     * Save a series (create or update).
     * 
     * @param series Series to save
     * @return Saved series
     */
    Series save(Series series);
    
    /**
     * Delete a series by ID.
     * 
     * @param id Series ID
     * @return true if deleted, false if not found
     */
    boolean deleteById(UUID id);
    
    /**
     * Search series by name (case-insensitive).
     * 
     * @param query Search query
     * @return List of matching series
     */
    List<Series> searchByName(String query);
    
    /**
     * Check if a series exists by name.
     * 
     * @param name Series name
     * @return true if exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if a series exists by name excluding a specific ID.
     * 
     * @param name Series name
     * @param excludeId ID to exclude from the check
     * @return true if exists
     */
    boolean existsByNameAndIdNot(String name, UUID excludeId);
}