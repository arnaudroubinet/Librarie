package org.motpassants.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for book data.
 */
@Schema(description = "Book response data")
public class BookResponseDto {
    
    @Schema(description = "Book ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    
    @Schema(description = "Book title", example = "The Great Gatsby")
    private String title;
    
    @Schema(description = "Book title for sorting", example = "Great Gatsby, The")
    private String titleSort;
    
    @Schema(description = "Book ISBN", example = "978-0-7432-7356-5")
    private String isbn;
    
    @Schema(description = "Book description or summary")
    private String description;
    
    @Schema(description = "Number of pages", example = "180")
    private Integer pageCount;
    
    @Schema(description = "Publication year", example = "1925")
    private Integer publicationYear;
    
    @Schema(description = "Language code", example = "en")
    private String language;
    
    @Schema(description = "Cover image URL")
    private String coverUrl;
    
    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private OffsetDateTime updatedAt;
    
    // Default constructor
    public BookResponseDto() {}
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTitleSort() {
        return titleSort;
    }
    
    public void setTitleSort(String titleSort) {
        this.titleSort = titleSort;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getPageCount() {
        return pageCount;
    }
    
    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }
    
    public Integer getPublicationYear() {
        return publicationYear;
    }
    
    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
    
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
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
}