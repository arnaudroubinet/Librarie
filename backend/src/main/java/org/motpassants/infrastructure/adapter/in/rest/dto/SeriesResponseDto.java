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
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String fallbackImagePath;
    
    // Default constructor
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
    
    public String getFallbackImagePath() {
        return fallbackImagePath;
    }
    
    public void setFallbackImagePath(String fallbackImagePath) {
        this.fallbackImagePath = fallbackImagePath;
    }
    
    @Override
    public String toString() {
        return "SeriesResponseDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", totalBooks=" + totalBooks +
                '}';
    }
}