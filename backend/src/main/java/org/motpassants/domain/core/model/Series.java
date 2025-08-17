package org.motpassants.domain.core.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Series domain model - rich domain entity with business logic.
 * Following DDD principles with proper encapsulation and validation.
 */
public class Series {
    
    private UUID id;
    private String name;
    private String sortName;
    private String description;
    // imagePath is no longer stored in DB; kept for backward-compat metadata/use only
    private String imagePath;
    private int totalBooks;
    private boolean isCompleted;
    private Boolean hasPicture; // persisted flag
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Default constructor for frameworks
    public Series() {}
    
    // Factory method with validation
    public static Series create(String name, String sortName) {
        validateRequired(name, "Series name cannot be null or empty");
        
        Series series = new Series();
        series.name = name.trim();
        series.sortName = (sortName != null && !sortName.trim().isEmpty()) ? sortName.trim() : name.trim();
        series.totalBooks = 0;
        series.isCompleted = false;
        series.createdAt = OffsetDateTime.now();
        series.updatedAt = OffsetDateTime.now();
        return series;
    }
    
    // Business logic methods
    public void updateDetails(String name, String sortName, String description, 
                             String imagePath, Integer totalBooks, Boolean isCompleted, 
                             Map<String, Object> metadata) {
        if (name != null) {
            validateRequired(name, "Series name cannot be null or empty");
            this.name = name.trim();
        }
        if (sortName != null) {
            this.sortName = sortName.trim().isEmpty() ? this.name : sortName.trim();
        }
        this.description = description;
        this.imagePath = imagePath;
        if (totalBooks != null) {
            this.totalBooks = Math.max(0, totalBooks); // Ensure non-negative
        }
        if (isCompleted != null) {
            this.isCompleted = isCompleted;
        }
        this.metadata = metadata;
        this.updatedAt = OffsetDateTime.now();
    }
    
    public void incrementBookCount() {
        this.totalBooks++;
        this.updatedAt = OffsetDateTime.now();
    }
    
    public void decrementBookCount() {
        if (this.totalBooks > 0) {
            this.totalBooks--;
            this.updatedAt = OffsetDateTime.now();
        }
    }
    
    public String getEffectiveImagePath() {
        return imagePath != null && !imagePath.trim().isEmpty() 
            ? imagePath 
            : "/api/v1/books/series/" + id + "/picture";
    }
    
    // Validation helper
    private static void validateRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSortName() {
        return sortName;
    }
    
    public void setSortName(String sortName) {
        this.sortName = sortName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public int getTotalBooks() {
        return totalBooks;
    }
    
    public void setTotalBooks(int totalBooks) {
        this.totalBooks = totalBooks;
    }
    
    public boolean getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getHasPicture() { return hasPicture; }
    public void setHasPicture(Boolean hasPicture) { this.hasPicture = hasPicture; }
    
    // Legacy compatibility methods for bookCount
    public int getBookCount() {
        return totalBooks;
    }
    
    public void setBookCount(int bookCount) {
        this.totalBooks = bookCount;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Series)) return false;
        Series series = (Series) o;
        return id != null && id.equals(series.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Series{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", totalBooks=" + totalBooks +
                '}';
    }

    // Builder for Series domain model
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Series s = new Series();
        public Builder id(UUID id) { s.setId(id); return this; }
        public Builder name(String name) { s.setName(name); return this; }
        public Builder sortName(String sortName) { s.setSortName(sortName); return this; }
        public Builder description(String description) { s.setDescription(description); return this; }
        public Builder imagePath(String imagePath) { s.setImagePath(imagePath); return this; }
        public Builder totalBooks(int totalBooks) { s.setTotalBooks(totalBooks); return this; }
        public Builder isCompleted(boolean isCompleted) { s.setIsCompleted(isCompleted); return this; }
        public Builder hasPicture(Boolean hasPicture) { s.setHasPicture(hasPicture); return this; }
        public Builder metadata(Map<String, Object> metadata) { s.setMetadata(metadata); return this; }
        public Builder createdAt(OffsetDateTime createdAt) { s.setCreatedAt(createdAt); return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { s.setUpdatedAt(updatedAt); return this; }
        public Series build() {
            if (s.getCreatedAt() == null) s.setCreatedAt(OffsetDateTime.now());
            if (s.getUpdatedAt() == null) s.setUpdatedAt(OffsetDateTime.now());
            if (s.getSortName() == null || s.getSortName().isBlank()) s.setSortName(s.getName());
            return s;
        }
    }
}