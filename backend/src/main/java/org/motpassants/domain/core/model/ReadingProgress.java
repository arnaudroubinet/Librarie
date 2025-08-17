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
        this.lastReadAt = lastReadAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReadingProgress create(UUID userId, UUID bookId) {
        return new ReadingProgress(
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
    }

    public void updateProgress(Double progress, Integer currentPage, Integer totalPages) {
        this.progress = progress;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.isCompleted = progress >= 1.0;
        this.lastReadAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public LocalDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(LocalDateTime lastReadAt) { this.lastReadAt = lastReadAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}