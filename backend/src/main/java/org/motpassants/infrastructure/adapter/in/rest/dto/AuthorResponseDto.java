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

    // Default constructor
    public AuthorResponseDto() {}

    // Constructor for easy creation
    public AuthorResponseDto(UUID id, String name, String sortName, Map<String, String> bio,
                            LocalDate birthDate, LocalDate deathDate, String websiteUrl,
                            Map<String, Object> metadata, OffsetDateTime createdAt,
                            OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.sortName = sortName;
        this.bio = bio;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.websiteUrl = websiteUrl;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    @Override
    public String toString() {
        return "AuthorResponseDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sortName='" + sortName + '\'' +
                '}';
    }
}