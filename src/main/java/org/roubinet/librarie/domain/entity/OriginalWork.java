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
 * OriginalWork entity representing the intellectual content of a creative work.
 * This separates the abstract work from its physical manifestations (books).
 * For example, "Pride and Prejudice" is an original work that can have many book manifestations.
 */
@Entity
@Table(name = "original_works")
public class OriginalWork extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "title_sort", nullable = false)
    private String titleSort;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "first_publication_date")
    private LocalDate firstPublicationDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_modified", nullable = false)
    private OffsetDateTime lastModified;

    // Relationships
    @OneToMany(mappedBy = "originalWork", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OriginalWorkAuthor> authors = new HashSet<>();

    @OneToMany(mappedBy = "originalWork", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OriginalWorkExternalId> externalIds = new HashSet<>();

    @OneToMany(mappedBy = "originalWork", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookOriginalWork> books = new HashSet<>();

    // Default constructor
    public OriginalWork() {}

    public OriginalWork(String title, String titleSort) {
        this.title = title;
        this.titleSort = titleSort;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getFirstPublicationDate() {
        return firstPublicationDate;
    }

    public void setFirstPublicationDate(LocalDate firstPublicationDate) {
        this.firstPublicationDate = firstPublicationDate;
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

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public Set<OriginalWorkAuthor> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<OriginalWorkAuthor> authors) {
        this.authors = authors;
    }

    public Set<OriginalWorkExternalId> getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(Set<OriginalWorkExternalId> externalIds) {
        this.externalIds = externalIds;
    }

    public Set<BookOriginalWork> getBooks() {
        return books;
    }

    public void setBooks(Set<BookOriginalWork> books) {
        this.books = books;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OriginalWork)) return false;
        OriginalWork that = (OriginalWork) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "OriginalWork{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", titleSort='" + titleSort + '\'' +
                '}';
    }
}