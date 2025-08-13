package org.roubinet.librarie.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Series entity representing a collection of related books or works.
 */
@Entity
@Table(name = "series")
public class Series extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "sort_name", nullable = false)
    private String sortName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "book_count", nullable = false)
    private int bookCount = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<BookSeries> books = new HashSet<>();

    // Default constructor
    public Series() {}

    public Series(String name, String sortName) {
        this.name = name;
        this.sortName = sortName;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getBookCount() {
        return bookCount;
    }

    public void setBookCount(int bookCount) {
        this.bookCount = bookCount;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
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

    public Set<BookSeries> getBooks() {
        return books;
    }

    public void setBooks(Set<BookSeries> books) {
        this.books = books;
    }

    /**
     * Increment the book count when a book is added to this series.
     */
    public void incrementBookCount() {
        this.bookCount++;
    }

    /**
     * Decrement the book count when a book is removed from this series.
     */
    public void decrementBookCount() {
        if (this.bookCount > 0) {
            this.bookCount--;
        }
    }

    /**
     * Get the effective image path for this series.
     * If the series has its own image, return it.
     * Otherwise, return a fallback path based on books in the series.
     */
    public String getEffectiveImagePath(String defaultCoverPath) {
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            return imagePath;
        }
        
        // Find first book with a cover, ordered by series index
        return books.stream()
            .filter(bookSeries -> bookSeries.book() != null && bookSeries.book().getHasCover())
            .sorted((bs1, bs2) -> bs1.seriesIndex().compareTo(bs2.seriesIndex()))
            .map(bookSeries -> bookSeries.book().getPath() + "/cover")
            .findFirst()
            .orElse(defaultCoverPath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Series)) return false;
        Series series = (Series) o;
        return id != null && id.equals(series.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Series{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sortName='" + sortName + '\'' +
                '}';
    }
}