package org.roubinet.librarie.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Book entity representing a physical or digital book manifestation.
 * This represents a specific edition/manifestation of one or more original works.
 */
@Entity
@Table(name = "books")
public class Book extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "title_sort", nullable = false)
    private String titleSort;

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "has_cover", nullable = false, columnDefinition = "boolean default false")
    private Boolean hasCover = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_modified", nullable = false)
    private OffsetDateTime lastModified;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @ManyToOne
    @JoinColumn(name = "language_code", referencedColumnName = "code")
    private Language language;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "search_vector")
    private String searchVector;

    // Relationships
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Format> formats = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookOriginalWork> originalWorks = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookSeries> series = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookTag> tags = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookPublisher> publishers = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Rating> ratings = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReadingProgress> readingProgress = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DownloadHistory> downloadHistory = new HashSet<>();

    // Default constructor
    public Book() {}

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

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
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

    public Set<BookTag> getTags() {
        return tags;
    }

    public void setTags(Set<BookTag> tags) {
        this.tags = tags;
    }

    public Set<BookPublisher> getPublishers() {
        return publishers;
    }

    public void setPublishers(Set<BookPublisher> publishers) {
        this.publishers = publishers;
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

    public Set<DownloadHistory> getDownloadHistory() {
        return downloadHistory;
    }

    public void setDownloadHistory(Set<DownloadHistory> downloadHistory) {
        this.downloadHistory = downloadHistory;
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
                ", isbn='" + isbn + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}