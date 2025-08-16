package org.roubinet.librarie.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;


public class SeriesData {
    
    private final UUID id;
    private final String name;
    private final String sortName;
    private final String description;
    private final String imagePath;
    private final Map<String, Object> metadata;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final int bookCount;
    private final String effectiveImagePath;
    
    public SeriesData(UUID id, String name, String sortName, String description, 
                     String imagePath, Map<String, Object> metadata, 
                     OffsetDateTime createdAt, OffsetDateTime updatedAt,
                     int bookCount, String effectiveImagePath) {
        this.id = id;
        this.name = name;
        this.sortName = sortName;
        this.description = description;
        this.imagePath = imagePath;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.bookCount = bookCount;
        this.effectiveImagePath = effectiveImagePath;
    }
    
    // Getters
    public UUID getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSortName() {
        return sortName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public int getBookCount() {
        return bookCount;
    }
    
    public String getEffectiveImagePath() {
        return effectiveImagePath;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeriesData)) return false;
        SeriesData that = (SeriesData) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "SeriesData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bookCount=" + bookCount +
                '}';
    }
}