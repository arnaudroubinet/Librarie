package org.roubinet.librarie.application.port.in;

import org.roubinet.librarie.domain.entity.Series;
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
     * @return Paginated result of series
     */
    CursorPageResult<Series> getAllSeries(String cursor, int limit);
    
    /**
     * Get a series by its ID.
     * 
     * @param id Series ID
     * @return Optional containing the series if found
     */
    Optional<Series> getSeriesById(UUID id);
}