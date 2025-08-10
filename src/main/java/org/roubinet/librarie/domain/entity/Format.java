package org.roubinet.librarie.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Format entity representing different file formats of a book (e.g., EPUB, PDF, MOBI).
 * A single book can have multiple formats.
 */
@Entity
@Table(name = "formats")
public class Format extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "format_type", nullable = false)
    private String formatType;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "quality_score", columnDefinition = "integer default 0")
    private Integer qualityScore = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "format", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReadingProgress> readingProgress = new HashSet<>();

    @OneToMany(mappedBy = "format", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DownloadHistory> downloadHistory = new HashSet<>();

    // Default constructor
    public Format() {}

    public Format(Book book, String formatType, String filePath, Long fileSize) {
        this.book = book;
        this.formatType = formatType;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getFormatType() {
        return formatType;
    }

    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Integer qualityScore) {
        this.qualityScore = qualityScore;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
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
        if (!(o instanceof Format)) return false;
        Format format = (Format) o;
        return id != null && id.equals(format.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Format{" +
                "id=" + id +
                ", formatType='" + formatType + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}