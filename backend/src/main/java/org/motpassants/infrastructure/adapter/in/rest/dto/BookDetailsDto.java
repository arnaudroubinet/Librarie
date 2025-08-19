package org.motpassants.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Rich book details for the detail page")
public class BookDetailsDto {
    private UUID id;
    private String title;
    private String titleSort;
    private String isbn;
    private String description;
    private Integer pageCount;
    private Integer publicationYear;
    private String language;
    private String path;
    private Long fileSize;
    private String fileHash;
    private LocalDate publicationDate;
    private Map<String, Object> metadata;
    private String publisher;
    private String series;
    private UUID seriesId;
    private Double seriesIndex;
    private List<String> formats;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Boolean hasCover;
    // role -> list of {id,name}
    private Map<String, List<Map<String, String>>> contributorsDetailed;

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final BookDetailsDto dto = new BookDetailsDto();
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
    public Builder contributorsDetailed(Map<String, List<Map<String, String>>> contributorsDetailed) { dto.contributorsDetailed = contributorsDetailed; return this; }
        public BookDetailsDto build() { return dto; }
    }

    // Getters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getTitleSort() { return titleSort; }
    public String getIsbn() { return isbn; }
    public String getDescription() { return description; }
    public Integer getPageCount() { return pageCount; }
    public Integer getPublicationYear() { return publicationYear; }
    public String getLanguage() { return language; }
    public String getPath() { return path; }
    public Long getFileSize() { return fileSize; }
    public String getFileHash() { return fileHash; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getPublisher() { return publisher; }
    public String getSeries() { return series; }
    public UUID getSeriesId() { return seriesId; }
    public Double getSeriesIndex() { return seriesIndex; }
    public List<String> getFormats() { return formats; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public Boolean getHasCover() { return hasCover; }
    public Map<String, List<Map<String, String>>> getContributorsDetailed() { return contributorsDetailed; }
}
