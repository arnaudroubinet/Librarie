package org.motpassants.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Lightweight book item for listings/search results")
public class BookListItemDto {
    @Schema(description = "Book ID")
    private UUID id;
    @Schema(description = "Book title")
    private String title;
    @Schema(description = "Sortable title")
    private String titleSort;
    @Schema(description = "Whether a cover is available")
    private Boolean hasCover;
    @Schema(description = "Publication date (YYYY-MM-DD)")
    private LocalDate publicationDate;
    @Schema(description = "Creation timestamp (for paging info only)")
    private OffsetDateTime createdAt;

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final BookListItemDto dto = new BookListItemDto();
        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder title(String title) { dto.title = title; return this; }
        public Builder titleSort(String titleSort) { dto.titleSort = titleSort; return this; }
        public Builder hasCover(Boolean hasCover) { dto.hasCover = hasCover; return this; }
        public Builder publicationDate(LocalDate publicationDate) { dto.publicationDate = publicationDate; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public BookListItemDto build() { return dto; }
    }

    public java.util.UUID getId() { return id; }
    public void setId(java.util.UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTitleSort() { return titleSort; }
    public void setTitleSort(String titleSort) { this.titleSort = titleSort; }
    public Boolean getHasCover() { return hasCover; }
    public void setHasCover(Boolean hasCover) { this.hasCover = hasCover; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
