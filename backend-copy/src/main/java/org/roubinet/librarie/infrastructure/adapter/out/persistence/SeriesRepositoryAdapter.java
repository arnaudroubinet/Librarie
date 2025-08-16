package org.roubinet.librarie.infrastructure.adapter.out.persistence;

import org.roubinet.librarie.application.port.out.SeriesRepository;
import org.roubinet.librarie.domain.entity.Series;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorUtils;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of the SeriesRepository port.
 * This adapter translates domain repository operations to Panache ORM calls.
 */
@ApplicationScoped
public class SeriesRepositoryAdapter implements SeriesRepository {
    
    private final CursorUtils cursorUtils;
    
    @Inject
    public SeriesRepositoryAdapter(CursorUtils cursorUtils) {
        this.cursorUtils = cursorUtils;
    }
    
    @Override
    public CursorPageResult<Series> getAllSeries(String cursor, int limit) {
        // For cursor pagination, we'll use ID and createdAt timestamp
        PanacheQuery<Series> query;
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                CursorUtils.CursorData cursorData = cursorUtils.parseCursor(cursor);
                UUID lastId = UUID.fromString(cursorData.getId());
                OffsetDateTime lastTimestamp = OffsetDateTime.parse(cursorData.getTimestamp());
                
                // Use composite cursor: first by timestamp, then by ID for consistency
                query = Series.find("(createdAt > ?1) OR (createdAt = ?1 AND id > ?2)", 
                                  lastTimestamp, lastId)
                             .page(Page.ofSize(limit + 1)); // Get one extra to check for next page
            } catch (Exception e) {
                // If cursor is invalid, start from beginning
                query = Series.findAll(Sort.by("createdAt", "id"))
                             .page(Page.ofSize(limit + 1));
            }
        } else {
            // No cursor, start from beginning
            query = Series.findAll(Sort.by("createdAt", "id"))
                         .page(Page.ofSize(limit + 1));
        }
        
        List<Series> items = query.list();
        boolean hasNext = items.size() > limit;
        
        // Remove the extra item if present
        if (hasNext) {
            items = items.subList(0, limit);
        }
        
        // Generate cursors
        String nextCursor = null;
        String previousCursor = null;
        
        if (hasNext && !items.isEmpty()) {
            Series lastItem = items.get(items.size() - 1);
            nextCursor = cursorUtils.createCursor(lastItem.getId(), lastItem.getCreatedAt());
        }
        
        // For previous cursor, we'd need to implement reverse pagination
        // This is a simplified implementation
        boolean hasPrevious = cursor != null && !cursor.trim().isEmpty();
        
        return CursorPageResult.<Series>builder()
            .items(items)
            .nextCursor(nextCursor)
            .previousCursor(previousCursor)
            .hasNext(hasNext)
            .hasPrevious(hasPrevious)
            .limit(limit)
            .totalCount(Series.count())
            .build();
    }
    
    @Override
    public Optional<Series> findById(UUID id) {
        return Series.findByIdOptional(id);
    }
    
    @Override
    public long getTotalCount() {
        return Series.count();
    }
    
    @Override
    public List<Series> findByNameContainingIgnoreCase(String name) {
        return Series.find("LOWER(name) LIKE LOWER(?1) OR LOWER(sortName) LIKE LOWER(?1)", "%" + name + "%").list();
    }
}