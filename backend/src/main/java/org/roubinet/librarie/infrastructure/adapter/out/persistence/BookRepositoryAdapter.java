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
        // For now, let's implement a simpler approach by getting all books and filtering in memory
        // This is not optimal for large datasets, but works for demo purposes
        
        PanacheQuery<Book> baseQuery;
        List<Object> parameters = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        
        // Start with basic query
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                CursorUtils.CursorData cursorData = cursorUtils.parseCursor(cursor);
                UUID lastId = UUID.fromString(cursorData.getId());
                OffsetDateTime lastTimestamp = OffsetDateTime.parse(cursorData.getTimestamp());
                
                queryBuilder.append("(createdAt > ?1) OR (createdAt = ?1 AND id > ?2)");
                parameters.add(lastTimestamp);
                parameters.add(lastId);
            } catch (Exception e) {
                // Invalid cursor, ignore and start from beginning
                queryBuilder.append("1=1");
            }
        } else {
            queryBuilder.append("1=1");
        }
        
        // Add sorting
        String sortField = criteria.getSortBy() != null ? criteria.getSortBy() : "createdAt";
        String sortDirection = criteria.getSortDirection() != null ? criteria.getSortDirection() : "desc";
        queryBuilder.append(" ORDER BY ").append(sortField).append(" ").append(sortDirection).append(", id ").append(sortDirection);
        
        // Get more books than needed to allow for filtering
        baseQuery = Book.find(queryBuilder.toString(), parameters.toArray())
                        .page(Page.ofSize(Math.min(limit * 10, 1000))); // Get more for filtering
        
        List<Book> allBooks = baseQuery.list();
        List<Book> filteredBooks = new ArrayList<>();
        
        // Apply filtering
        for (Book book : allBooks) {
            boolean matches = true;
            
            // Filter by contributors if specified
            if (matches && criteria.getContributorsContain() != null && !criteria.getContributorsContain().isEmpty()) {
                matches = false; // Start with false, need to find a match
                
                // Check each contributor criteria
                for (String contributorSearch : criteria.getContributorsContain()) {
                    // Use the extractContributorsFromBook logic or check title for now
                    if (book.getTitle() != null && book.getTitle().toLowerCase().contains(contributorSearch.toLowerCase())) {
                        matches = true;
                        break;
                    }
                    // For demo purposes, also check if it's in the title sort field which might contain author info
                    if (book.getTitleSort() != null && book.getTitleSort().toLowerCase().contains(contributorSearch.toLowerCase())) {
                        matches = true;
                        break;
                    }
                }
            }
            
            // Filter by title if specified
            if (matches && criteria.getTitleContains() != null && !criteria.getTitleContains().trim().isEmpty()) {
                matches = book.getTitle() != null && 
                         book.getTitle().toLowerCase().contains(criteria.getTitleContains().toLowerCase());
            }
            
            // Filter by language if specified
            if (matches && criteria.getLanguageEquals() != null && !criteria.getLanguageEquals().trim().isEmpty()) {
                matches = book.getLanguage() != null && book.getLanguage().getName() != null &&
                         criteria.getLanguageEquals().equals(book.getLanguage().getName());
            }
            
            // Filter by publisher if specified
            if (matches && criteria.getPublisherContains() != null && !criteria.getPublisherContains().trim().isEmpty()) {
                matches = book.getPublisher() != null && book.getPublisher().getName() != null &&
                         book.getPublisher().getName().toLowerCase().contains(criteria.getPublisherContains().toLowerCase());
            }
            
            if (matches) {
                filteredBooks.add(book);
                if (filteredBooks.size() >= limit + 1) { // Get one extra for hasNext check
                    break;
                }
            }
        }
        
        return buildCursorPageResult(filteredBooks, limit, cursor);
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