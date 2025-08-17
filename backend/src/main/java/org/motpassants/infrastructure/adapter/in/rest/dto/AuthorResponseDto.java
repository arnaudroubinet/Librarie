package org.motpassants.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for Author responses.
 * Provides clean API contract for client consumption.
 */
public class AuthorResponseDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("sortName")
    private String sortName;

    @JsonProperty("bio")
    private Map<String, String> bio;

    @JsonProperty("birthDate")
    private LocalDate birthDate;

    @JsonProperty("deathDate")
    private LocalDate deathDate;

    @JsonProperty("websiteUrl")
    private String websiteUrl;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @JsonProperty("updatedAt")
    private OffsetDateTime updatedAt;
    
    @JsonProperty("hasPicture")
    private Boolean hasPicture;

    // Default constructor
    public AuthorResponseDto() {}

    // Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final AuthorResponseDto dto = new AuthorResponseDto();
        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder name(String name) { dto.name = name; return this; }
        public Builder sortName(String sortName) { dto.sortName = sortName; return this; }
        public Builder bio(Map<String, String> bio) { dto.bio = bio; return this; }
        public Builder birthDate(LocalDate birthDate) { dto.birthDate = birthDate; return this; }
        public Builder deathDate(LocalDate deathDate) { dto.deathDate = deathDate; return this; }
        public Builder websiteUrl(String websiteUrl) { dto.websiteUrl = websiteUrl; return this; }
        public Builder metadata(Map<String, Object> metadata) { dto.metadata = metadata; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { dto.updatedAt = updatedAt; return this; }
        public Builder hasPicture(Boolean hasPicture) { dto.hasPicture = hasPicture; return this; }
        public AuthorResponseDto build() { return dto; }
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
    
    public Boolean getHasPicture() { return hasPicture; }
    public void setHasPicture(Boolean hasPicture) { this.hasPicture = hasPicture; }

    @Override
    public String toString() {
        return "AuthorResponseDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sortName='" + sortName + '\'' +
                '}';
    }
}