package org.roubinet.librarie.infrastructure.adapter.out.persistence;

import org.roubinet.librarie.application.port.in.BookSearchCriteria;
import org.roubinet.librarie.application.port.out.BookRepository;
import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorUtils;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of the BookRepository port.
 * This adapter translates domain repository operations to Panache ORM calls.
 */
@ApplicationScoped
public class BookRepositoryAdapter implements BookRepository {
    
    private final CursorUtils cursorUtils;
    
    @Inject
    public BookRepositoryAdapter(CursorUtils cursorUtils) {
        this.cursorUtils = cursorUtils;
    }
    
    @Override
    public CursorPageResult<Book> findAll(String cursor, int limit) {
        // For cursor pagination, we'll use ID and createdAt timestamp
        PanacheQuery<Book> query;
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                CursorUtils.CursorData cursorData = cursorUtils.parseCursor(cursor);
                UUID lastId = UUID.fromString(cursorData.getId());
                OffsetDateTime lastTimestamp = OffsetDateTime.parse(cursorData.getTimestamp());
                
                // Use composite cursor: first by timestamp, then by ID for consistency
                query = Book.find("(createdAt > ?1) OR (createdAt = ?1 AND id > ?2)", 
                                lastTimestamp, lastId)
                           .page(Page.ofSize(limit + 1)); // Get one extra to check for next page
            } catch (Exception e) {
                // Invalid cursor, start from beginning
                query = Book.findAll(Sort.by("createdAt").and("id"))
                           .page(Page.ofSize(limit + 1));
            }
        } else {
            // First page
            query = Book.findAll(Sort.by("createdAt").and("id"))
                       .page(Page.ofSize(limit + 1));
        }
        
        List<Book> books = query.list();
        return buildCursorPageResult(books, limit, cursor);
    }
    

    @Override
    public Optional<Book> findById(UUID id) {
        return Book.findByIdOptional(id);
    }
    

    @Override
    public Optional<Book> findByPath(String path) {
        return Book.find("path", path).firstResultOptional();
    }
    
    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return Book.find("isbn", isbn).firstResultOptional();
    }
    
    @Override
    public Book save(Book book) {
        book.persist();
        return book;
    }
    
    @Override
    public void deleteById(UUID id) {
        Book.deleteById(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return Book.findById(id) != null;
    }
    
    @Override
    public long count() {
        return Book.count();
    }
    



    // New cursor-based pagination methods
    
    @Override
    public CursorPageResult<Book> findByTitleContainingIgnoreCase(String title, String cursor, int limit) {
        PanacheQuery<Book> query;
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                CursorUtils.CursorData cursorData = cursorUtils.parseCursor(cursor);
                UUID lastId = UUID.fromString(cursorData.getId());
                OffsetDateTime lastTimestamp = OffsetDateTime.parse(cursorData.getTimestamp());
                
                query = Book.find("(LOWER(title) LIKE LOWER(?1)) AND ((createdAt > ?2) OR (createdAt = ?2 AND id > ?3))", 
                               "%" + title + "%", lastTimestamp, lastId)
                           .page(Page.ofSize(limit + 1));
            } catch (Exception e) {
                query = Book.find("LOWER(title) LIKE LOWER(?1)", "%" + title + "%")
                           .page(Page.ofSize(limit + 1));
            }
        } else {
            query = Book.find("LOWER(title) LIKE LOWER(?1)", "%" + title + "%")
                       .page(Page.ofSize(limit + 1));
        }
        
        List<Book> books = query.list();
        return buildCursorPageResult(books, limit, cursor);
    }

    
    @Override
    public CursorPageResult<Book> searchBooks(String searchQuery, String cursor, int limit) {
        PanacheQuery<Book> query;
        String searchPattern = "%" + searchQuery + "%";
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                CursorUtils.CursorData cursorData = cursorUtils.parseCursor(cursor);
                UUID lastId = UUID.fromString(cursorData.getId());
                OffsetDateTime lastTimestamp = OffsetDateTime.parse(cursorData.getTimestamp());
                
                query = Book.find("(LOWER(title) LIKE LOWER(?1) OR LOWER(isbn) LIKE LOWER(?1)) AND ((createdAt > ?2) OR (createdAt = ?2 AND id > ?3))", 
                               searchPattern, lastTimestamp, lastId)
                           .page(Page.ofSize(limit + 1));
            } catch (Exception e) {
                query = Book.find("LOWER(title) LIKE LOWER(?1) OR LOWER(isbn) LIKE LOWER(?1)", searchPattern)
                           .page(Page.ofSize(limit + 1));
            }
        } else {
            query = Book.find("LOWER(title) LIKE LOWER(?1) OR LOWER(isbn) LIKE LOWER(?1)", searchPattern)
                       .page(Page.ofSize(limit + 1));
        }
        
        List<Book> books = query.list();
        return buildCursorPageResult(books, limit, cursor);
    }
    
    @Override
    public CursorPageResult<Book> findByCriteria(BookSearchCriteria criteria, String cursor, int limit) {
        StringBuilder queryBuilder = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        int paramIndex = 1;
        
        // Build WHERE clause based on criteria
        List<String> conditions = new ArrayList<>();
        
        // Handle contributor search using proper JPA joins
        if (criteria.getContributorsContain() != null && !criteria.getContributorsContain().isEmpty()) {
            StringBuilder contributorCondition = new StringBuilder();
            contributorCondition.append("id IN (SELECT DISTINCT b.id FROM Book b ");
            contributorCondition.append("JOIN b.originalWorks bow ");
            contributorCondition.append("JOIN bow.originalWork ow ");
            contributorCondition.append("JOIN ow.authors owa ");
            contributorCondition.append("JOIN owa.author a WHERE ");
            
            List<String> authorConditions = new ArrayList<>();
            for (String contributorSearch : criteria.getContributorsContain()) {
                authorConditions.add("LOWER(a.name) LIKE LOWER(?" + paramIndex + ")");
                parameters.add("%" + contributorSearch + "%");
                paramIndex++;
            }
            contributorCondition.append(String.join(" OR ", authorConditions));
            contributorCondition.append(")");
            conditions.add(contributorCondition.toString());
        }
        
        // Handle title search
        if (criteria.getTitleContains() != null && !criteria.getTitleContains().trim().isEmpty()) {
            conditions.add("LOWER(title) LIKE LOWER(?" + paramIndex + ")");
            parameters.add("%" + criteria.getTitleContains() + "%");
            paramIndex++;
        }
        
        // Handle language filter
        if (criteria.getLanguageEquals() != null && !criteria.getLanguageEquals().trim().isEmpty()) {
            conditions.add("language.name = ?" + paramIndex);
            parameters.add(criteria.getLanguageEquals());
            paramIndex++;
        }
        
        // Handle publisher filter
        if (criteria.getPublisherContains() != null && !criteria.getPublisherContains().trim().isEmpty()) {
            conditions.add("LOWER(publisher.name) LIKE LOWER(?" + paramIndex + ")");
            parameters.add("%" + criteria.getPublisherContains() + "%");
            paramIndex++;
        }
        
        // Handle series filter
        if (criteria.getSeriesContains() != null && !criteria.getSeriesContains().trim().isEmpty()) {
            conditions.add("id IN (SELECT DISTINCT b.id FROM Book b JOIN b.series bs JOIN bs.series s WHERE LOWER(s.name) LIKE LOWER(?" + paramIndex + "))");
            parameters.add("%" + criteria.getSeriesContains() + "%");
            paramIndex++;
        }
        
        // Handle cursor pagination
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                CursorUtils.CursorData cursorData = cursorUtils.parseCursor(cursor);
                UUID lastId = UUID.fromString(cursorData.getId());
                OffsetDateTime lastTimestamp = OffsetDateTime.parse(cursorData.getTimestamp());
                
                conditions.add("((createdAt > ?" + paramIndex + ") OR (createdAt = ?" + paramIndex + " AND id > ?" + (paramIndex + 1) + "))");
                parameters.add(lastTimestamp);
                parameters.add(lastTimestamp);
                parameters.add(lastId);
                paramIndex += 3;
            } catch (Exception e) {
                // Invalid cursor, ignore
            }
        }
        
        // Build the final query
        if (!conditions.isEmpty()) {
            queryBuilder.append(String.join(" AND ", conditions));
        } else {
            queryBuilder.append("1=1");
        }
        
        // Add sorting
        String sortField = criteria.getSortBy() != null ? criteria.getSortBy() : "createdAt";
        String sortDirection = criteria.getSortDirection() != null ? criteria.getSortDirection() : "desc";
        queryBuilder.append(" ORDER BY ").append(sortField).append(" ").append(sortDirection);
        if (!"id".equals(sortField)) {
            queryBuilder.append(", id ").append(sortDirection);
        }
        
        // Execute the query
        PanacheQuery<Book> query = Book.find(queryBuilder.toString(), parameters.toArray())
                                      .page(Page.ofSize(limit + 1)); // Get one extra to check for next page
        
        List<Book> books = query.list();
        return buildCursorPageResult(books, limit, cursor);
    }
    
    /**
     * Helper method to build cursor page result from list of books.
     */
    private CursorPageResult<Book> buildCursorPageResult(List<Book> books, int limit, String currentCursor) {
        boolean hasNext = books.size() > limit;
        boolean hasPrevious = currentCursor != null && !currentCursor.trim().isEmpty();
        
        // Remove the extra item if we have more than requested
        List<Book> resultBooks = hasNext ? books.subList(0, limit) : books;
        
        String nextCursor = null;
        if (hasNext && !resultBooks.isEmpty()) {
            Book lastBook = resultBooks.get(resultBooks.size() - 1);
            if (lastBook.getCreatedAt() != null && lastBook.getId() != null) {
                nextCursor = cursorUtils.createCursor(lastBook.getId(), lastBook.getCreatedAt());
            }
        }
        
        return CursorPageResult.<Book>builder()
            .items(resultBooks)
            .nextCursor(nextCursor)
            .previousCursor(null) // Previous cursor logic would need additional implementation
            .hasNext(hasNext)
            .hasPrevious(hasPrevious)
            .limit(limit)
            .totalCount(null) // Count would require separate query for performance
            .build();
    }
}