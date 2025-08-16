package org.motpassants.domain.core.model;

import java.util.UUID;

/**
 * OriginalWork, Rating, ReadingProgress domain models.
 * Placeholders for now - will be detailed later.
 */
public class OriginalWork {
    private UUID id;
    private String title;
    
    public OriginalWork() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}

class Rating {
    private UUID id;
    private Integer rating;
    
    public Rating() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}

class ReadingProgress {
    private UUID id;
    private Integer currentPage;
    
    public ReadingProgress() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }
}