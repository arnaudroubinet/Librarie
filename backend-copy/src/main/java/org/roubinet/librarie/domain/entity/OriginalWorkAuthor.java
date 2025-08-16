package org.roubinet.librarie.domain.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * OriginalWorkAuthor entity representing the many-to-many relationship between original works and authors.
 * Includes role information (e.g., "author", "editor", "translator", "illustrator", "photographer", etc.) and order for display.
 */
@Entity
@Table(name = "original_work_authors")
@IdClass(OriginalWorkAuthor.OriginalWorkAuthorId.class)
public class OriginalWorkAuthor {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_work_id")
    private OriginalWork originalWork;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @Id
    @Column(name = "role", nullable = false)
    private String role;

    // Default constructor
    public OriginalWorkAuthor() {}

    public OriginalWorkAuthor(OriginalWork originalWork, Author author, String role) {
        this.originalWork = originalWork;
        this.author = author;
        this.role = role;
    }

    // Getters and setters
    public OriginalWork getOriginalWork() {
        return originalWork;
    }

    public void setOriginalWork(OriginalWork originalWork) {
        this.originalWork = originalWork;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OriginalWorkAuthor)) return false;
        OriginalWorkAuthor that = (OriginalWorkAuthor) o;
        return Objects.equals(originalWork, that.originalWork) &&
                Objects.equals(author, that.author) &&
                Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalWork, author, role);
    }

    // Composite key class
    public static class OriginalWorkAuthorId implements Serializable {
        private OriginalWork originalWork;
        private Author author;
        private String role;

        public OriginalWorkAuthorId() {}

        public OriginalWorkAuthorId(OriginalWork originalWork, Author author, String role) {
            this.originalWork = originalWork;
            this.author = author;
            this.role = role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OriginalWorkAuthorId)) return false;
            OriginalWorkAuthorId that = (OriginalWorkAuthorId) o;
            return Objects.equals(originalWork, that.originalWork) &&
                    Objects.equals(author, that.author) &&
                    Objects.equals(role, that.role);
        }

        @Override
        public int hashCode() {
            return Objects.hash(originalWork, author, role);
        }
    }
}