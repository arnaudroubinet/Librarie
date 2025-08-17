package org.motpassants.infrastructure.adapter.in.rest.dto;

import java.util.Map;

/**
 * Data Transfer Object for Series creation and update requests.
 */
public class SeriesRequestDto {
    
    private String name;
    private String sortName; // Optional for backward compatibility
    private String description;
    private String imagePath;
    private Integer totalBooks;
    private Boolean isCompleted;
    private Map<String, Object> metadata;
    
    // Default constructor
    public SeriesRequestDto() {}
    
    // Constructor with required fields
    public SeriesRequestDto(String name) {
        this.name = name;
        // Set default sortName to name if not provided
        this.sortName = name;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSortName() {
        return sortName != null ? sortName : name; // Default to name if not set
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
    
    public Integer getTotalBooks() {
        return totalBooks;
    }
    
    public void setTotalBooks(Integer totalBooks) {
        this.totalBooks = totalBooks;
    }
    
    public Boolean getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return "SeriesRequestDto{" +
                "name='" + name + '\'' +
                ", totalBooks=" + totalBooks +
                '}';
    }
}