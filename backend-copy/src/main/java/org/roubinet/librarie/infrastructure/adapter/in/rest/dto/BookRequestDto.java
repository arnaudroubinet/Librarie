package org.roubinet.librarie.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Request DTO for book creation and updates.
 */
@Schema(description = "Book request data")
public class BookRequestDto {
    
    @Schema(description = "Book title", required = true, example = "The Great Gatsby")
    private String title;
    
    @Schema(description = "Author name", example = "F. Scott Fitzgerald")
    private String author;
    
    @Schema(description = "ISBN code", example = "978-0-7432-7356-5")
    private String isbn;
    
    @Schema(description = "Series name", example = "Harry Potter")
    private String series;
    
    @Schema(description = "Index in series", example = "1")
    private Integer seriesIndex;
    
    @Schema(description = "Book description or summary")
    private String description;

    @Schema(description = "External cover image URL (http/https)", example = "https://m.media-amazon.com/images/I/81mJ+KxA1PL.jpg")
    private String coverUrl;
    
    // Default constructor
    public BookRequestDto() {}
    
    // Getters and setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getSeries() {
        return series;
    }
    
    public void setSeries(String series) {
        this.series = series;
    }
    
    public Integer getSeriesIndex() {
        return seriesIndex;
    }
    
    public void setSeriesIndex(Integer seriesIndex) {
        this.seriesIndex = seriesIndex;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}