package org.roubinet.librarie.infrastructure.adapter.in.rest.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for Author response data.
 */
public class AuthorResponseDto {
    
    private UUID id;
    private String name;
    private String sortName;
    private Map<String, String> bio;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String websiteUrl;
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    public AuthorResponseDto() {}
    
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
    
    public Map<String, String> getBio() {
        return bio;
    }
    
    public void setBio(Map<String, String> bio) {
        this.bio = bio;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public LocalDate getDeathDate() {
        return deathDate;
    }
    
    public void setDeathDate(LocalDate deathDate) {
        this.deathDate = deathDate;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
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
}