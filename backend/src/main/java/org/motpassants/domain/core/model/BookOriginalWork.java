package org.motpassants.domain.core.model;

import java.util.UUID;

/**
 * BookOriginalWork domain model representing book-original work relationships.
 * Placeholder for now - will be detailed later.
 */
public class BookOriginalWork {
    private UUID id;
    private Book book;
    private OriginalWork originalWork;
    
    public BookOriginalWork() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public OriginalWork getOriginalWork() { return originalWork; }
    public void setOriginalWork(OriginalWork originalWork) { this.originalWork = originalWork; }
}