package org.motpassants.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
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
    
    @Schema(description = "File path in storage")
    private String path;
    
    @Schema(description = "File size in bytes")
    private Long fileSize;
    
    @Schema(description = "File content hash")
    private String fileHash;
    
    @Schema(description = "Publication date (YYYY-MM-DD)")
    private LocalDate publicationDate;
    
    @Schema(description = "Additional metadata")
    private Map<String, Object> metadata;
    
    @Schema(description = "Publisher name")
    private String publisher;
    
    @Schema(description = "Series name (first series if multiple)")
    private String series;
    
    @Schema(description = "Series UUID (first series if multiple)")
    private UUID seriesId;
    
    @Schema(description = "Series index within the series (first series if multiple)")
    private Double seriesIndex;
    
    @Schema(description = "Available format types")
    private List<String> formats;
    
    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private OffsetDateTime updatedAt;
    
    @Schema(description = "Whether a cover is available for this book")
    private Boolean hasCover;
    
    // Default constructor
    public BookResponseDto() {}

    // Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final BookResponseDto dto = new BookResponseDto();
        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder title(String title) { dto.title = title; return this; }
        public Builder titleSort(String titleSort) { dto.titleSort = titleSort; return this; }
        public Builder isbn(String isbn) { dto.isbn = isbn; return this; }
        public Builder description(String description) { dto.description = description; return this; }
        public Builder pageCount(Integer pageCount) { dto.pageCount = pageCount; return this; }
        public Builder publicationYear(Integer publicationYear) { dto.publicationYear = publicationYear; return this; }
        public Builder language(String language) { dto.language = language; return this; }
        public Builder path(String path) { dto.path = path; return this; }
        public Builder fileSize(Long fileSize) { dto.fileSize = fileSize; return this; }
        public Builder fileHash(String fileHash) { dto.fileHash = fileHash; return this; }
        public Builder publicationDate(LocalDate publicationDate) { dto.publicationDate = publicationDate; return this; }
        public Builder metadata(Map<String, Object> metadata) { dto.metadata = metadata; return this; }
        public Builder publisher(String publisher) { dto.publisher = publisher; return this; }
        public Builder series(String series) { dto.series = series; return this; }
        public Builder seriesId(UUID seriesId) { dto.seriesId = seriesId; return this; }
    public Builder seriesIndex(Double seriesIndex) { dto.seriesIndex = seriesIndex; return this; }
        public Builder formats(List<String> formats) { dto.formats = formats; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { dto.updatedAt = updatedAt; return this; }
        public Builder hasCover(Boolean hasCover) { dto.hasCover = hasCover; return this; }
        public BookResponseDto build() { return dto; }
    }
    
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
    
    // Cover fields removed from API; presence can be inferred by clients via cover endpoint
    
    public LocalDate getPublicationDate() {
        return publicationDate;
    }
    
    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getSeries() {
        return series;
    }
    
    public void setSeries(String series) {
        this.series = series;
    }
    
    public UUID getSeriesId() {
        return seriesId;
    }
    
    public void setSeriesId(UUID seriesId) {
        this.seriesId = seriesId;
    }
    
    public Double getSeriesIndex() {
        return seriesIndex;
    }
    
    public void setSeriesIndex(Double seriesIndex) {
        this.seriesIndex = seriesIndex;
    }
    
    public List<String> getFormats() {
        return formats;
    }
    
    public void setFormats(List<String> formats) {
        this.formats = formats;
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
    
    public Boolean getHasCover() { return hasCover; }
    public void setHasCover(Boolean hasCover) { this.hasCover = hasCover; }
}