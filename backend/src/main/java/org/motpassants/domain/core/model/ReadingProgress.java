package org.motpassants.domain.core.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReadingProgress {
    private UUID id;
    private UUID userId;
    private UUID bookId;
    private Double progress; // 0.0 to 1.0 (0% to 100%)
    private Integer currentPage;
    private Integer totalPages;
    private Boolean isCompleted;
    private ReadingStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String deviceId;
    private Long syncVersion;
    private String notes;
    // Raw Readium locator JSON (optional)
    private String progressLocator;
    private LocalDateTime lastReadAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public ReadingProgress() {}

    public ReadingProgress(UUID id, UUID userId, UUID bookId, Double progress, Integer currentPage, 
                          Integer totalPages, Boolean isCompleted, LocalDateTime lastReadAt, 
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.progress = progress;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.isCompleted = isCompleted;
        this.status = ReadingStatus.UNREAD;
        this.lastReadAt = lastReadAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.syncVersion = 1L;
    }

    public static ReadingProgress create(UUID userId, UUID bookId) {
        ReadingProgress progress = new ReadingProgress(
            UUID.randomUUID(),
            userId,
            bookId,
            0.0,
            0,
            null,
            false,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        progress.setStatus(ReadingStatus.UNREAD);
        progress.setSyncVersion(1L);
        return progress;
    }

    public void updateProgress(Double progress, Integer currentPage, Integer totalPages) {
        this.progress = progress;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.isCompleted = progress >= 1.0;
        this.lastReadAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.syncVersion = this.syncVersion != null ? this.syncVersion + 1 : 1L;
        
        // Update status based on progress
        if (progress >= 1.0 && this.status != ReadingStatus.FINISHED) {
            this.status = ReadingStatus.FINISHED;
            this.finishedAt = LocalDateTime.now();
        } else if (progress > 0.0 && this.status == ReadingStatus.UNREAD) {
            this.status = ReadingStatus.READING;
            if (this.startedAt == null) {
                this.startedAt = LocalDateTime.now();
            }
        }
    }
    
    /**
     * Calculate progress percentage (0-100).
     * 
     * @return progress percentage or 0.0 if not available
     */
    public double getProgressPercentage() {
        if (progress != null) {
            return progress * 100.0;
        }
        if (currentPage != null && totalPages != null && totalPages > 0) {
            return (currentPage * 100.0) / totalPages;
        }
        return 0.0;
    }
    
    /**
     * Mark the book as finished.
     */
    public void markAsFinished() {
        this.status = ReadingStatus.FINISHED;
        this.isCompleted = true;
        this.progress = 1.0;
        this.finishedAt = LocalDateTime.now();
        this.lastReadAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.syncVersion = this.syncVersion != null ? this.syncVersion + 1 : 1L;
        
        if (this.totalPages != null && this.totalPages > 0) {
            this.currentPage = this.totalPages;
        }
    }
    
    /**
     * Mark the book as started.
     */
    public void markAsStarted() {
        if (this.status == ReadingStatus.UNREAD) {
            this.status = ReadingStatus.READING;
            this.startedAt = LocalDateTime.now();
            this.lastReadAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.syncVersion = this.syncVersion != null ? this.syncVersion + 1 : 1L;
        }
    }
    
    /**
     * Mark the book as DNF (Did Not Finish).
     */
    public void markAsDnf() {
        this.status = ReadingStatus.DNF;
        this.isCompleted = false;
        this.lastReadAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.syncVersion = this.syncVersion != null ? this.syncVersion + 1 : 1L;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }

    public Double getProgress() { return progress; }
    public void setProgress(Double progress) { this.progress = progress; }

    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public ReadingStatus getStatus() { return status; }
    public void setStatus(ReadingStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Long getSyncVersion() { return syncVersion; }
    public void setSyncVersion(Long syncVersion) { this.syncVersion = syncVersion; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getProgressLocator() { return progressLocator; }
    public void setProgressLocator(String progressLocator) { this.progressLocator = progressLocator; }

    public LocalDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(LocalDateTime lastReadAt) { this.lastReadAt = lastReadAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}