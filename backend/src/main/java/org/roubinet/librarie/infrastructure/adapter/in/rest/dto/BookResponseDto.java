package org.roubinet.librarie.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.List;

/**
 * DTO for book API responses.
 * Represents the book data structure exposed via REST API.
 */
@Schema(description = "Book information")
public class BookResponseDto {
    
    @Schema(description = "Unique identifier of the book", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID id;
    
    @Schema(description = "Title of the book", example = "The Great Gatsby")
    private String title;
    
    @Schema(description = "Sortable title (articles moved to end)", example = "Great Gatsby, The")
    private String titleSort;
    
    @Schema(description = "ISBN of the book", example = "978-0-7432-7356-5")
    private String isbn;
    
    @Schema(description = "File path in the library", example = "/library/F. Scott Fitzgerald/The Great Gatsby/The Great Gatsby.epub")
    private String path;
    
    @Schema(description = "File size in bytes", example = "2048576")
    private Long fileSize;
    
    @Schema(description = "MD5 hash of the file", example = "d41d8cd98f00b204e9800998ecf8427e")
    private String fileHash;
    
    @Schema(description = "Whether the book has a cover image", example = "true")
    private Boolean hasCover;
    
    @Schema(description = "When the book was added to the library")
    private OffsetDateTime createdAt;
    
    @Schema(description = "When the book was last updated")
    private OffsetDateTime updatedAt;
    
    @Schema(description = "Publication date of the book", example = "1925-04-10")
    private LocalDate publicationDate;
    
    @Schema(description = "Language of the book", example = "English")
    private String language;
    
    @Schema(description = "Publisher of the book", example = "Charles Scribner's Sons")
    private String publisher;
    
    @Schema(description = "Additional metadata as key-value pairs")
    private Map<String, Object> metadata;
    
    // Simple fields for easier API usage
    @Schema(description = "Primary author name", example = "F. Scott Fitzgerald")
    private String author;
    
    @Schema(description = "Series name", example = "Harry Potter")
    private String series;
    
    @Schema(description = "Index in series", example = "1")
    private Integer seriesIndex;
    
    @Schema(description = "Book description or summary")
    private String description;
    
    @Schema(description = "Available formats for this book", example = "[\"EPUB\", \"PDF\", \"MOBI\"]")
    private List<String> formats;
    
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
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileHash() {
        return fileHash;
    }
    
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
    
    public Boolean getHasCover() {
        return hasCover;
    }
    
    public void setHasCover(Boolean hasCover) {
        this.hasCover = hasCover;
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
    
    public LocalDate getPublicationDate() {
        return publicationDate;
    }
    
    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
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
    
    public List<String> getFormats() {
        return formats;
    }
    
    public void setFormats(List<String> formats) {
        this.formats = formats;
    }
}