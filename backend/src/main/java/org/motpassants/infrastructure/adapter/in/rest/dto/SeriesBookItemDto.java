package org.motpassants.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Lightweight DTO for listing books within a series without pagination.
 * Contains only the fields required by the series detail page.
 */
@Schema(description = "Lightweight book item for series page")
public class SeriesBookItemDto {
    @Schema(description = "Book ID")
    private UUID id;

    @Schema(description = "Book title")
    private String title;

    @Schema(description = "Has local cover available")
    private Boolean hasCover;

    @Schema(description = "Index within the series")
    private Double seriesIndex;

    @Schema(description = "Publication date (YYYY-MM-DD)")
    private LocalDate publicationDate;

    public SeriesBookItemDto() {}

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final SeriesBookItemDto dto = new SeriesBookItemDto();
        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder title(String title) { dto.title = title; return this; }
        public Builder hasCover(Boolean hasCover) { dto.hasCover = hasCover; return this; }
        public Builder seriesIndex(Double seriesIndex) { dto.seriesIndex = seriesIndex; return this; }
        public Builder publicationDate(LocalDate publicationDate) { dto.publicationDate = publicationDate; return this; }
        public SeriesBookItemDto build() { return dto; }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Boolean getHasCover() { return hasCover; }
    public void setHasCover(Boolean hasCover) { this.hasCover = hasCover; }
    public Double getSeriesIndex() { return seriesIndex; }
    public void setSeriesIndex(Double seriesIndex) { this.seriesIndex = seriesIndex; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
}
