package org.motpassants.infrastructure.adapter.in.rest.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for series detail page needs.
 */
public class SeriesDetailsDto {
    private UUID id;
    private String name;
    private String sortName;
    private String description;
    private String imagePath;
    private int bookCount;
    private Boolean isCompleted;
    private Boolean hasPicture;
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public SeriesDetailsDto() {}

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final SeriesDetailsDto dto = new SeriesDetailsDto();
        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder name(String v) { dto.name = v; return this; }
        public Builder sortName(String v) { dto.sortName = v; return this; }
        public Builder description(String v) { dto.description = v; return this; }
        public Builder imagePath(String v) { dto.imagePath = v; return this; }
        public Builder bookCount(int v) { dto.bookCount = v; return this; }
    // Backward compatibility: allow setting via legacy name
    public Builder totalBooks(int v) { dto.bookCount = v; return this; }
        public Builder isCompleted(Boolean v) { dto.isCompleted = v; return this; }
        public Builder hasPicture(Boolean v) { dto.hasPicture = v; return this; }
        public Builder metadata(Map<String, Object> v) { dto.metadata = v; return this; }
        public Builder createdAt(OffsetDateTime v) { dto.createdAt = v; return this; }
        public Builder updatedAt(OffsetDateTime v) { dto.updatedAt = v; return this; }
        public SeriesDetailsDto build() { return dto; }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSortName() { return sortName; }
    public void setSortName(String sortName) { this.sortName = sortName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getBookCount() { return bookCount; }
    public void setBookCount(int bookCount) { this.bookCount = bookCount; }

    // Backward compatibility: expose legacy property name expected by tests/clients
    @JsonProperty("totalBooks")
    public int getTotalBooks() { return bookCount; }
    public void setTotalBooks(int totalBooks) { this.bookCount = totalBooks; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public Boolean getHasPicture() { return hasPicture; }
    public void setHasPicture(Boolean hasPicture) { this.hasPicture = hasPicture; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
