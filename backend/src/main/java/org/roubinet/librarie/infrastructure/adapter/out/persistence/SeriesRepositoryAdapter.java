package org.roubinet.librarie.infrastructure.adapter.out.persistence;

import org.roubinet.librarie.application.port.out.SeriesRepository;
import org.roubinet.librarie.domain.entity.Series;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorUtils;
import org.roubinet.librarie.infrastructure.config.LibrarieConfigProperties;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

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
    private final EntityManager entityManager;
    private final LibrarieConfigProperties config;
    
    @Inject
    public SeriesRepositoryAdapter(CursorUtils cursorUtils, EntityManager entityManager, 
                                 LibrarieConfigProperties config) {
        this.cursorUtils = cursorUtils;
        this.entityManager = entityManager;
        this.config = config;
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
            .totalCount(null) // Count would require separate query for performance
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
    public int getBookCountForSeries(UUID seriesId) {
        try {
            Query query = entityManager.createQuery(
                "SELECT COUNT(bs) FROM BookSeries bs WHERE bs.series.id = :seriesId"
            );
            query.setParameter("seriesId", seriesId);
            Long count = (Long) query.getSingleResult();
            return count.intValue();
        } catch (Exception e) {
            return 0;
        }
    }
    
    @Override
    public Optional<String> getFallbackImageForSeries(UUID seriesId) {
        try {
            // First, try to get books with covers ordered by series index
            Query query = entityManager.createQuery(
                "SELECT b.path FROM BookSeries bs " +
                "JOIN bs.book b " +
                "WHERE bs.series.id = :seriesId " +
                "AND b.hasCover = true " +
                "ORDER BY bs.seriesIndex ASC"
            );
            query.setParameter("seriesId", seriesId);
            
            @SuppressWarnings("unchecked")
            List<String> results = query.getResultList();
            
            // Try each book until we find one with a cover
            for (String bookPath : results) {
                if (bookPath != null && !bookPath.trim().isEmpty()) {
                    // Assuming cover path follows a pattern like /books/{id}/cover
                    return Optional.of(bookPath + "/cover");
                }
            }
            
            // If no book has a cover, return the default cover
            return Optional.of(config.fileProcessing().defaultCoverPath());
        } catch (Exception e) {
            // In case of error, return the default cover
            return Optional.of(config.fileProcessing().defaultCoverPath());
        }
    }
}