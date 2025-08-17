package org.motpassants.domain.core.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Rating {
    private UUID id;
    private UUID userId;
    private UUID bookId;
    private Integer rating; // 1-5 stars
    private String review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Rating() {}

    public Rating(UUID id, UUID userId, UUID bookId, Integer rating, String review, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.review = review;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Rating create(UUID userId, UUID bookId, Integer rating, String review) {
        return new Rating(
            UUID.randomUUID(),
            userId,
            bookId,
            rating,
            review,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}