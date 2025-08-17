package org.motpassants.domain.core.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * User domain model representing system users with OIDC integration.
 */
public class User {
    
    private UUID id;
    private String oidcOriginName;
    private String oidcSubject;
    private String publicName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Factory method
    public static User create(String oidcOriginName, String oidcSubject, String publicName) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.oidcOriginName = oidcOriginName;
        user.oidcSubject = oidcSubject;
        user.publicName = publicName;
        user.createdAt = OffsetDateTime.now();
        user.updatedAt = OffsetDateTime.now();
        return user;
    }
    
    // Business methods
    public void updatePublicName(String newPublicName) {
        if (newPublicName == null || newPublicName.trim().isEmpty()) {
            throw new IllegalArgumentException("Public name cannot be null or empty");
        }
        this.publicName = newPublicName.trim();
        this.updatedAt = OffsetDateTime.now();
    }
    
    public boolean isValidOidcUser() {
        return oidcOriginName != null && !oidcOriginName.trim().isEmpty() &&
               oidcSubject != null && !oidcSubject.trim().isEmpty();
    }
    
    // Getters
    public UUID getId() { return id; }
    public String getOidcOriginName() { return oidcOriginName; }
    public String getOidcSubject() { return oidcSubject; }
    public String getPublicName() { return publicName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters for persistence
    public void setId(UUID id) { this.id = id; }
    public void setOidcOriginName(String oidcOriginName) { this.oidcOriginName = oidcOriginName; }
    public void setOidcSubject(String oidcSubject) { this.oidcSubject = oidcSubject; }
    public void setPublicName(String publicName) { this.publicName = publicName; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}