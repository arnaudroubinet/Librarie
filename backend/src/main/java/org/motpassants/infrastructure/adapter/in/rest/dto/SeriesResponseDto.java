package org.motpassants.infrastructure.adapter.in.rest.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for Series responses.
 */
public class SeriesResponseDto {
    
    private UUID id;
    private String name;
    private String sortName;
    private String description;
    private String imagePath;
    private int totalBooks; // Using totalBooks to match integration test expectations
    private boolean isCompleted; // Added for integration test compatibility
    private Boolean hasPicture; // persisted flag
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Default constructor
    public SeriesResponseDto() {}

    // Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final SeriesResponseDto dto = new SeriesResponseDto();
        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder name(String name) { dto.name = name; return this; }
        public Builder sortName(String sortName) { dto.sortName = sortName; return this; }
        public Builder description(String description) { dto.description = description; return this; }
        public Builder imagePath(String imagePath) { dto.imagePath = imagePath; return this; }
        public Builder totalBooks(int totalBooks) { dto.totalBooks = totalBooks; return this; }
        public Builder isCompleted(boolean isCompleted) { dto.isCompleted = isCompleted; return this; }
        public Builder hasPicture(Boolean hasPicture) { dto.hasPicture = hasPicture; return this; }
        public Builder metadata(Map<String, Object> metadata) { dto.metadata = metadata; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { dto.updatedAt = updatedAt; return this; }
        public SeriesResponseDto build() { return dto; }
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

    /**
     * Frontend expects "bookCount"; keep "totalBooks" for tests and provide this alias for FE.
     */
    public int getBookCount() { return totalBooks; }
    public void setBookCount(int bookCount) { this.totalBooks = bookCount; }
    
    @Override
    public String toString() {
        return "SeriesResponseDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", totalBooks=" + totalBooks +
                '}';
    }
}