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
    // Raw Readium locator JSON (optional)
    private String progressLocator;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime lastReadAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long syncVersion;
    private String notes;

    // Default constructor
    public ReadingProgress() {
        this.status = ReadingStatus.READING;
        this.syncVersion = 1L;
    }

    public ReadingProgress(UUID id, UUID userId, UUID bookId, Double progress, Integer currentPage, 
                          Integer totalPages, Boolean isCompleted, ReadingStatus status, 
                          LocalDateTime startedAt, LocalDateTime finishedAt, LocalDateTime lastReadAt, 
                          LocalDateTime createdAt, LocalDateTime updatedAt, Long syncVersion, String notes) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.progress = progress;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.isCompleted = isCompleted;
        this.status = status != null ? status : ReadingStatus.READING;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.lastReadAt = lastReadAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.syncVersion = syncVersion != null ? syncVersion : 1L;
        this.notes = notes;
    }

    public static ReadingProgress create(UUID userId, UUID bookId) {
        LocalDateTime now = LocalDateTime.now();
        return new ReadingProgress(
            UUID.randomUUID(),
            userId,
            bookId,
            0.0,
            0,
            null,
            false,
            ReadingStatus.UNREAD,
            null,
            null,
            now,
            now,
            now,
            1L,
            null
        );
    }

    public void updateProgress(Double progress, Integer currentPage, Integer totalPages) {
        LocalDateTime now = LocalDateTime.now();
        
        // If this is the first progress update, mark as started
        if (this.startedAt == null && progress != null && progress > 0.0) {
            this.startedAt = now;
            this.status = ReadingStatus.READING;
        }
        
        this.progress = progress;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        
        // Update completion status
        if (progress != null && progress >= 1.0) {
            this.isCompleted = true;
            this.status = ReadingStatus.FINISHED;
            this.finishedAt = now;
            
            // Set current page to total pages if both are available
            if (this.totalPages != null && this.currentPage == null) {
                this.currentPage = this.totalPages;
            }
        } else {
            this.isCompleted = false;
        }
        
        this.lastReadAt = now;
        this.updatedAt = now;
        this.syncVersion = (this.syncVersion != null ? this.syncVersion : 0L) + 1L;
    }

    /**
     * Mark this book as finished.
     */
    public void markAsFinished() {
        LocalDateTime now = LocalDateTime.now();
        this.status = ReadingStatus.FINISHED;
        this.finishedAt = now;
        this.progress = 1.0;
        this.isCompleted = true;
        this.lastReadAt = now;
        this.updatedAt = now;
        this.syncVersion = (this.syncVersion != null ? this.syncVersion : 0L) + 1L;
        
        // Set current page to total pages if both are available
        if (this.totalPages != null) {
            this.currentPage = this.totalPages;
        }
    }

    /**
     * Mark this book as started (explicitly).
     */
    public void markAsStarted() {
        LocalDateTime now = LocalDateTime.now();
        if (this.startedAt == null) {
            this.startedAt = now;
        }
        this.status = ReadingStatus.READING;
        this.lastReadAt = now;
        this.updatedAt = now;
        this.syncVersion = (this.syncVersion != null ? this.syncVersion : 0L) + 1L;
    }

    /**
     * Mark this book as DNF (Did Not Finish).
     */
    public void markAsDNF() {
        LocalDateTime now = LocalDateTime.now();
        this.status = ReadingStatus.DNF;
        this.isCompleted = false;
        this.lastReadAt = now;
        this.updatedAt = now;
        this.syncVersion = (this.syncVersion != null ? this.syncVersion : 0L) + 1L;
    }

    /**
     * Get progress as a percentage (0.0 to 100.0).
     */
    public double getProgressPercentage() {
        if (this.progress == null) {
            return 0.0;
        }
        return this.progress * 100.0;
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

    public String getProgressLocator() { return progressLocator; }
    public void setProgressLocator(String progressLocator) { this.progressLocator = progressLocator; }

    public ReadingStatus getStatus() { return status; }
    public void setStatus(ReadingStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    public LocalDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(LocalDateTime lastReadAt) { this.lastReadAt = lastReadAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getSyncVersion() { return syncVersion; }
    public void setSyncVersion(Long syncVersion) { this.syncVersion = syncVersion; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}