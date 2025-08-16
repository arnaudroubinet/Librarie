package org.motpassants.domain.core.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Book domain model representing a physical or digital book manifestation.
 * This represents a specific edition/manifestation of one or more original works.
 * Pure domain object without any infrastructure dependencies.
 */
public class Book {

    private UUID id;
    private String title;
    private String titleSort;
    private String isbn;
    private String description;
    private Integer pageCount;
    private Integer publicationYear;
    private String language;  // Simple string for now, can be enhanced later
    private String coverUrl;
    private String path;
    private Long fileSize;
    private String fileHash;
    private Boolean hasCover;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private LocalDate publicationDate;
    private Language languageEntity;  // Keep the entity relationship
    private Publisher publisher;
    private Map<String, Object> metadata;
    private String searchVector;

    // Relationships
    private Set<Format> formats = new HashSet<>();
    private Set<BookOriginalWork> originalWorks = new HashSet<>();
    private Set<BookSeries> series = new HashSet<>();
    private Set<Tag> tags = new HashSet<>();
    private Set<Rating> ratings = new HashSet<>();
    private Set<ReadingProgress> readingProgress = new HashSet<>();

    // Default constructor
    public Book() {
        this.hasCover = false;
    }

    // Constructor for creating new books
    public Book(String title, String path) {
        this();
        this.title = title;
        this.path = path;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    // Business methods
    public void updateTitle(String newTitle, String newTitleSort) {
        this.title = newTitle;
        this.titleSort = newTitleSort;
        this.updatedAt = OffsetDateTime.now();
    }

    public void addFormat(Format format) {
        this.formats.add(format);
        this.updatedAt = OffsetDateTime.now();
    }

    public void removeFormat(Format format) {
        this.formats.remove(format);
        this.updatedAt = OffsetDateTime.now();
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
        this.updatedAt = OffsetDateTime.now();
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean hasFormat(String formatType) {
        return formats.stream()
                .anyMatch(format -> format.getFormatType().equalsIgnoreCase(formatType));
    }

    public void markAsUpdated() {
        this.updatedAt = OffsetDateTime.now();
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

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
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

    public Language getLanguageEntity() {
        return languageEntity;
    }

    public void setLanguageEntity(Language languageEntity) {
        this.languageEntity = languageEntity;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getSearchVector() {
        return searchVector;
    }

    public void setSearchVector(String searchVector) {
        this.searchVector = searchVector;
    }

    public Set<Format> getFormats() {
        return formats;
    }

    public void setFormats(Set<Format> formats) {
        this.formats = formats;
    }

    public Set<BookOriginalWork> getOriginalWorks() {
        return originalWorks;
    }

    public void setOriginalWorks(Set<BookOriginalWork> originalWorks) {
        this.originalWorks = originalWorks;
    }

    public Set<BookSeries> getSeries() {
        return series;
    }

    public void setSeries(Set<BookSeries> series) {
        this.series = series;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(Set<Rating> ratings) {
        this.ratings = ratings;
    }

    public Set<ReadingProgress> getReadingProgress() {
        return readingProgress;
    }

    public void setReadingProgress(Set<ReadingProgress> readingProgress) {
        this.readingProgress = readingProgress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return id != null && id.equals(book.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}