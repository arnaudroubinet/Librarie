package org.motpassants.domain.core.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Author domain entity - pure business logic with no framework dependencies.
 * Represents an author, editor, translator, or other contributor to works.
 * Following DDD principles with rich domain model.
 */
public class Author {

    private final UUID id;
    private String name;
    private String sortName;
    private Map<String, String> bio;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String websiteUrl;
    private Map<String, Object> metadata;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Boolean hasPicture;

    private Author(UUID id, String name, String sortName, Map<String, String> bio,
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

    public static Author create(String name, String sortName, Map<String, String> bio,
                               LocalDate birthDate, LocalDate deathDate, String websiteUrl,
                               Map<String, Object> metadata) {
        validateName(name);
        validateSortName(sortName);
        
        return new Author(
            UUID.randomUUID(),
            name,
            sortName,
            bio,
            birthDate,
            deathDate,
            websiteUrl,
            metadata,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
    }

    // Factory method for reconstituting from repository
    public static Author reconstitute(UUID id, String name, String sortName, Map<String, String> bio,
                                     LocalDate birthDate, LocalDate deathDate, String websiteUrl,
                                     Map<String, Object> metadata, OffsetDateTime createdAt,
                                     OffsetDateTime updatedAt) {
        return new Author(id, name, sortName, bio, birthDate, deathDate, websiteUrl, 
                         metadata, createdAt, updatedAt);
    }

    // Business methods with validation
    public void updateName(String newName) {
        validateName(newName);
        this.name = newName;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateSortName(String newSortName) {
        validateSortName(newSortName);
        this.sortName = newSortName;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateBio(Map<String, String> newBio) {
        this.bio = newBio;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateBirthDate(LocalDate newBirthDate) {
        validateDates(newBirthDate, this.deathDate);
        this.birthDate = newBirthDate;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateDeathDate(LocalDate newDeathDate) {
        validateDates(this.birthDate, newDeathDate);
        this.deathDate = newDeathDate;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateWebsiteUrl(String newWebsiteUrl) {
        this.websiteUrl = newWebsiteUrl;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateMetadata(Map<String, Object> newMetadata) {
        this.metadata = newMetadata;
        this.updatedAt = OffsetDateTime.now();
    }

    // Business validation rules
    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Author name cannot be null or empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Author name cannot exceed 255 characters");
        }
    }

    private static void validateSortName(String sortName) {
        if (sortName == null || sortName.trim().isEmpty()) {
            throw new IllegalArgumentException("Author sort name cannot be null or empty");
        }
        if (sortName.length() > 255) {
            throw new IllegalArgumentException("Author sort name cannot exceed 255 characters");
        }
    }

    private static void validateDates(LocalDate birthDate, LocalDate deathDate) {
        if (birthDate != null && deathDate != null && birthDate.isAfter(deathDate)) {
            throw new IllegalArgumentException("Birth date cannot be after death date");
        }
    }

    // Getters (no setters to maintain immutability where appropriate)
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSortName() {
        return sortName;
    }

    public Map<String, String> getBio() {
        return bio;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
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

    public Boolean getHasPicture() { return hasPicture; }
    public void setHasPicture(Boolean hasPicture) { this.hasPicture = hasPicture; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author author)) return false;
        return Objects.equals(id, author.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sortName='" + sortName + '\'' +
                '}';
    }

    // Builder for Author domain model
    public static Builder builder() { return new Builder(); }
    public static class Builder {
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
        private Boolean hasPicture;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder sortName(String sortName) { this.sortName = sortName; return this; }
        public Builder bio(Map<String, String> bio) { this.bio = bio; return this; }
        public Builder birthDate(LocalDate birthDate) { this.birthDate = birthDate; return this; }
        public Builder deathDate(LocalDate deathDate) { this.deathDate = deathDate; return this; }
        public Builder websiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; return this; }
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder hasPicture(Boolean hasPicture) { this.hasPicture = hasPicture; return this; }

        public Author build() {
            Author a;
            if (id != null || createdAt != null || updatedAt != null) {
                UUID effId = (id != null) ? id : java.util.UUID.randomUUID();
                OffsetDateTime effCreated = (createdAt != null) ? createdAt : OffsetDateTime.now();
                OffsetDateTime effUpdated = (updatedAt != null) ? updatedAt : OffsetDateTime.now();
                a = Author.reconstitute(effId, name, sortName, bio, birthDate, deathDate, websiteUrl, metadata, effCreated, effUpdated);
            } else {
                a = Author.create(name, sortName, bio, birthDate, deathDate, websiteUrl, metadata);
            }
            if (hasPicture != null) a.setHasPicture(hasPicture);
            return a;
        }
    }
}