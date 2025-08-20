package org.motpassants.infrastructure.adapter.in.rest.dto;

import java.util.UUID;

/**
 * Minimal DTO for listing series on /v1/books/series.
 * Only fields required by the frontend series list page.
 */
public class SeriesListItemDto {

    private UUID id;
    private String name;
    private String sortName;
    private int bookCount; // alias for totalBooks
    private Boolean hasPicture; // to decide if an image should be requested

    public SeriesListItemDto() {}

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final SeriesListItemDto dto = new SeriesListItemDto();
        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder name(String name) { dto.name = name; return this; }
        public Builder sortName(String sortName) { dto.sortName = sortName; return this; }
        public Builder bookCount(int bookCount) { dto.bookCount = bookCount; return this; }
        public Builder hasPicture(Boolean hasPicture) { dto.hasPicture = hasPicture; return this; }
        public SeriesListItemDto build() { return dto; }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSortName() { return sortName; }
    public void setSortName(String sortName) { this.sortName = sortName; }

    public int getBookCount() { return bookCount; }
    public void setBookCount(int bookCount) { this.bookCount = bookCount; }

    public Boolean getHasPicture() { return hasPicture; }
    public void setHasPicture(Boolean hasPicture) { this.hasPicture = hasPicture; }
}
