package org.motpassants.domain.core.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Publisher domain model representing book publishers.
 * Pure domain object without any infrastructure dependencies.
 */
public class Publisher {

    private UUID id;
    private String name;
    private String website;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Default constructor
    public Publisher() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    // Constructor for creating new publishers
    public Publisher(String name) {
        this();
        this.name = name;
    }

    public Publisher(String name, String website) {
        this(name);
        this.website = website;
    }

    // Business methods
    public void updateName(String newName) {
        this.name = newName;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateWebsite(String newWebsite) {
        this.website = newWebsite;
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean hasWebsite() {
        return website != null && !website.trim().isEmpty();
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Publisher)) return false;
        Publisher publisher = (Publisher) o;
        return id != null && id.equals(publisher.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Publisher{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", website='" + website + '\'' +
                '}';
    }
}