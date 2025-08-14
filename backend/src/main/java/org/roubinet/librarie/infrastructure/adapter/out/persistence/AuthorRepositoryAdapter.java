package org.roubinet.librarie.infrastructure.adapter.out.persistence;

import org.roubinet.librarie.application.port.out.AuthorRepository;
import org.roubinet.librarie.domain.entity.Author;
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
 * JPA implementation of the AuthorRepository port.
 * This adapter translates domain repository operations to Panache ORM calls.
 */
@ApplicationScoped
public class AuthorRepositoryAdapter implements AuthorRepository {
    
    private final CursorUtils cursorUtils;
    
    @Inject
    public AuthorRepositoryAdapter(CursorUtils cursorUtils) {
        this.cursorUtils = cursorUtils;
    }
    
    @Override
    public CursorPageResult<Author> findAll(String cursor, int limit) {
        // For cursor pagination, we'll use ID and createdAt timestamp
        PanacheQuery<Author> query;
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                CursorUtils.CursorData cursorData = cursorUtils.parseCursor(cursor);
                UUID lastId = UUID.fromString(cursorData.getId());
                OffsetDateTime lastTimestamp = OffsetDateTime.parse(cursorData.getTimestamp());
                
                // Use composite cursor: first by timestamp, then by ID for consistency
                query = Author.find("(createdAt > ?1) OR (createdAt = ?1 AND id > ?2)", 
                                lastTimestamp, lastId)
                           .page(Page.ofSize(limit + 1)); // Get one extra to check for next page
            } catch (Exception e) {
                // Invalid cursor, start from beginning
                query = Author.findAll(Sort.by("createdAt").and("id"))
                           .page(Page.ofSize(limit + 1));
            }
        } else {
            // First page
            query = Author.findAll(Sort.by("createdAt").and("id"))
                       .page(Page.ofSize(limit + 1));
        }
        
        List<Author> authors = query.list();
        return buildCursorPageResult(authors, limit, cursor);
    }
    
    @Override
    public Optional<Author> findById(UUID id) {
        return Author.findByIdOptional(id);
    }
    
    @Override
    public CursorPageResult<Author> findByNameContainingIgnoreCase(String name, String cursor, int limit) {
        // For cursor pagination with name search, we'll use ID and createdAt timestamp
        PanacheQuery<Author> query;
        String searchPattern = "%" + name.toLowerCase() + "%";
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                CursorUtils.CursorData cursorData = cursorUtils.parseCursor(cursor);
                UUID lastId = UUID.fromString(cursorData.getId());
                OffsetDateTime lastTimestamp = OffsetDateTime.parse(cursorData.getTimestamp());
                
                // Use composite cursor with name search: first by timestamp, then by ID for consistency
                query = Author.find("LOWER(name) LIKE ?1 AND ((createdAt > ?2) OR (createdAt = ?2 AND id > ?3))", 
                                searchPattern, lastTimestamp, lastId)
                           .page(Page.ofSize(limit + 1)); // Get one extra to check for next page
            } catch (Exception e) {
                // Invalid cursor, start from beginning
                query = Author.find("LOWER(name) LIKE ?1", searchPattern)
                           .page(Page.ofSize(limit + 1));
            }
        } else {
            // First page
            query = Author.find("LOWER(name) LIKE ?1", searchPattern)
                       .page(Page.ofSize(limit + 1));
        }
        
        List<Author> authors = query.list();
        return buildCursorPageResult(authors, limit, cursor);
    }
    
    @Override
    public List<Author> findByNameContainingIgnoreCase(String name) {
        String searchPattern = "%" + name.toLowerCase() + "%";
        // Limit results for performance - typically used in unified search
        return Author.find("LOWER(name) LIKE ?1", searchPattern)
                    .page(Page.ofSize(50)) // Reasonable limit for unified search
                    .list();
    }
    
    @Override
    public Author save(Author author) {
        Author.persist(author);
        return author;
    }
    
    @Override
    public void deleteById(UUID id) {
        Author.deleteById(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return Author.findByIdOptional(id).isPresent();
    }
    
    @Override
    public long count() {
        return Author.count();
    }
    
    /**
     * Helper method to build cursor page result from list of authors.
     */
    private CursorPageResult<Author> buildCursorPageResult(List<Author> authors, int limit, String currentCursor) {
        boolean hasNext = authors.size() > limit;
        boolean hasPrevious = currentCursor != null && !currentCursor.trim().isEmpty();
        
        // Remove the extra item if we have more than requested
        List<Author> resultAuthors = hasNext ? authors.subList(0, limit) : authors;
        
        String nextCursor = null;
        if (hasNext && !resultAuthors.isEmpty()) {
            Author lastAuthor = resultAuthors.get(resultAuthors.size() - 1);
            if (lastAuthor.getCreatedAt() != null && lastAuthor.getId() != null) {
                nextCursor = cursorUtils.createCursor(lastAuthor.getId(), lastAuthor.getCreatedAt());
            }
        }
        
        return CursorPageResult.<Author>builder()
            .items(resultAuthors)
            .nextCursor(nextCursor)
            .previousCursor(null) // Previous cursor logic would need additional implementation
            .hasNext(hasNext)
            .hasPrevious(hasPrevious)
            .limit(limit)
            .totalCount(null) // Count would require separate query for performance
            .build();
    }
}