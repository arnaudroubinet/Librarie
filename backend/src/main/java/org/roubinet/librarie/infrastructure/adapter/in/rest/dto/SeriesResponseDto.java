package org.roubinet.librarie.infrastructure.adapter.in.rest.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for Series response data.
 */
public class SeriesResponseDto {
    
    private UUID id;
    private String name;
    private String sortName;
    private String description;
    private String imagePath;
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private int bookCount;
    private String fallbackImagePath; // From the book with lowest index
    
    public SeriesResponseDto() {}
    
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
    
    public int getBookCount() {
        return bookCount;
    }
    
    public void setBookCount(int bookCount) {
        this.bookCount = bookCount;
    }
    
    public String getFallbackImagePath() {
        return fallbackImagePath;
    }
    
    public void setFallbackImagePath(String fallbackImagePath) {
        this.fallbackImagePath = fallbackImagePath;
    }
    
    /**
     * Get the effective image path - series image if available, otherwise fallback
     */
    public String getEffectiveImagePath() {
        return imagePath != null ? imagePath : fallbackImagePath;
    }
}