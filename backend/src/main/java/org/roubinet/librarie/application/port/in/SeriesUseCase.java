package org.roubinet.librarie.application.port.in;

import org.roubinet.librarie.domain.model.SeriesData;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;

import java.util.Optional;
import java.util.UUID;

/**
 * Use case interface for Series operations.
 */
public interface SeriesUseCase {
    
    /**
     * Get all series with cursor-based pagination.
     * 
     * @param cursor Cursor for pagination
     * @param limit Maximum number of items to return
     * @return Paginated result of series data
     */
    CursorPageResult<SeriesData> getAllSeries(String cursor, int limit);
    
    /**
     * Get a series by its ID.
     * 
     * @param id Series ID
     * @return Optional containing the series data if found
     */
    Optional<SeriesData> getSeriesById(UUID id);
}