package org.roubinet.librarie.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for author API responses.
 * Represents the author data structure exposed via REST API.
 */
@Schema(description = "Author information")
public class AuthorResponseDto {
    
    @Schema(description = "Unique identifier of the author", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID id;
    
    @Schema(description = "Name of the author", example = "J.R.R. Tolkien")
    private String name;
    
    @Schema(description = "Sortable name (last name first)", example = "Tolkien, J.R.R.")
    private String sortName;
    
    @Schema(description = "Author biography in multiple languages")
    private Map<String, String> bio;
    
    @Schema(description = "Birth date of the author", example = "1892-01-03")
    private LocalDate birthDate;
    
    @Schema(description = "Death date of the author", example = "1973-09-02")
    private LocalDate deathDate;
    
    @Schema(description = "Author's official website URL", example = "https://www.tolkienestate.com/")
    private String websiteUrl;
    
    @Schema(description = "Additional metadata about the author")
    private Map<String, Object> metadata;
    
    @Schema(description = "When the author was added to the library")
    private OffsetDateTime createdAt;
    
    @Schema(description = "When the author was last updated")
    private OffsetDateTime updatedAt;
    
    // Constructors
    public AuthorResponseDto() {}
    
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