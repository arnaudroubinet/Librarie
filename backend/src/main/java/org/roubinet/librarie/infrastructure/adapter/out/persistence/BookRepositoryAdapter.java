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
        // For now, implement basic version that returns empty result
        // In a full implementation, this would use the DSL criteria to build dynamic queries
        return CursorPageResult.<Book>builder()
            .items(List.of())
            .limit(limit)
            .hasNext(false)
            .hasPrevious(cursor != null)
            .build();
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