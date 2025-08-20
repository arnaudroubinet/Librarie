package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.Page;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.domain.core.model.Series;
import org.motpassants.domain.core.model.SeriesSortCriteria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case interface for Series operations.
 * Defines the business capabilities for series management.
 */
public interface SeriesUseCase {
    
    /**
     * Get all series with pagination.
     * 
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return Paginated result of series
     */
    Page<Series> getAllSeries(int page, int size);

    /**
     * Offset-based pagination for series with sorting support.
     */
    Page<Series> getAllSeries(int page, int size, SeriesSortCriteria sortCriteria);

    /**
     * Cursor-based pagination for series.
     * @param cursor base64("<epochMicros>|<uuid>") or legacy millis
     * @param limit page size
     */
    PageResult<Series> getAllSeries(String cursor, int limit);

    /**
     * Cursor-based pagination for series with sorting support.
     */
    PageResult<Series> getAllSeries(String cursor, int limit, SeriesSortCriteria sortCriteria);
    
    /**
     * Get a series by its ID.
     * 
     * @param id Series ID
     * @return Optional containing the series if found
     */
    Optional<Series> getSeriesById(UUID id);
    
    /**
     * Create a new series.
     * 
     * @param name Series name
     * @param sortName Series sort name (optional, defaults to name)
     * @param description Series description
     * @param imagePath Series image path
     * @param totalBooks Total number of books in series
     * @param isCompleted Whether the series is completed
     * @param metadata Series metadata
     * @return Created series
     */
    Series createSeries(String name, String sortName, String description, 
                       String imagePath, Integer totalBooks, Boolean isCompleted,
                       java.util.Map<String, Object> metadata);
    
    /**
     * Update an existing series.
     * 
     * @param id Series ID
     * @param name Series name
     * @param sortName Series sort name
     * @param description Series description
     * @param imagePath Series image path
     * @param totalBooks Total number of books in series
     * @param isCompleted Whether the series is completed
     * @param metadata Series metadata
     * @return Updated series
     */
    Optional<Series> updateSeries(UUID id, String name, String sortName, String description,
                                 String imagePath, Integer totalBooks, Boolean isCompleted,
                                 java.util.Map<String, Object> metadata);
    
    /**
     * Delete a series by ID.
     * 
     * @param id Series ID
     * @return true if deleted, false if not found
     */
    boolean deleteSeries(UUID id);
    
    /**
     * Search series by name.
     * 
     * @param query Search query for series name
     * @return List of matching series
     */
    List<Series> searchSeries(String query);
    
    /**
     * Get books in a series.
     * 
     * @param seriesId Series ID
     * @return List of books in the series
     */
    List<org.motpassants.domain.core.model.Book> getSeriesBooks(UUID seriesId);
}