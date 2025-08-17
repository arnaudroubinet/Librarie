package org.motpassants.domain.core.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DownloadHistory domain model for tracking download events.
 */
public class DownloadHistory {
    
    private UUID id;
    private UUID formatId;
    private UUID userId;
    private String ipAddress;
    private String userAgent;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Factory method
    public static DownloadHistory create(UUID formatId, UUID userId, String ipAddress, String userAgent) {
        DownloadHistory history = new DownloadHistory();
        history.id = UUID.randomUUID();
        history.formatId = formatId;
        history.userId = userId;
        history.ipAddress = ipAddress;
        history.userAgent = userAgent;
        history.createdAt = OffsetDateTime.now();
        history.updatedAt = OffsetDateTime.now();
        return history;
    }
    
    // Business methods
    public void updateUserAgent(String newUserAgent) {
        this.userAgent = newUserAgent;
        this.updatedAt = OffsetDateTime.now();
    }
    
    public void updateIpAddress(String newIpAddress) {
        this.ipAddress = newIpAddress;
        this.updatedAt = OffsetDateTime.now();
    }
    
    public boolean isRecentDownload() {
        return createdAt != null && 
               createdAt.isAfter(OffsetDateTime.now().minusHours(24));
    }
    
    public boolean hasValidFormat() {
        return formatId != null;
    }
    
    public boolean hasValidUser() {
        return userId != null;
    }
    
    // Getters
    public UUID getId() { return id; }
    public UUID getFormatId() { return formatId; }
    public UUID getUserId() { return userId; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters for persistence
    public void setId(UUID id) { this.id = id; }
    public void setFormatId(UUID formatId) { this.formatId = formatId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}