package org.roubinet.librarie.domain.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * BookOriginalWork entity representing the many-to-many relationship between books and original works.
 * Supports different relationship types like primary, collection, anthology, adaptation.
 */
@Entity
@Table(name = "book_original_works")
@IdClass(BookOriginalWork.BookOriginalWorkId.class)
public class BookOriginalWork {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_work_id")
    private OriginalWork originalWork;

    @Id
    @Column(name = "relationship_type", nullable = false, columnDefinition = "text default 'primary'")
    private String relationshipType = "primary";

    @Column(name = "order_index", nullable = false, columnDefinition = "integer default 0")
    private Integer orderIndex = 0;

    // Default constructor
    public BookOriginalWork() {}

    public BookOriginalWork(Book book, OriginalWork originalWork, String relationshipType, Integer orderIndex) {
        this.book = book;
        this.originalWork = originalWork;
        this.relationshipType = relationshipType;
        this.orderIndex = orderIndex;
    }

    // Getters and setters
    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public OriginalWork getOriginalWork() {
        return originalWork;
    }

    public void setOriginalWork(OriginalWork originalWork) {
        this.originalWork = originalWork;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookOriginalWork)) return false;
        BookOriginalWork that = (BookOriginalWork) o;
        return Objects.equals(book, that.book) &&
                Objects.equals(originalWork, that.originalWork) &&
                Objects.equals(relationshipType, that.relationshipType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, originalWork, relationshipType);
    }

    // Composite key class
    public static class BookOriginalWorkId implements Serializable {
        private Book book;
        private OriginalWork originalWork;
        private String relationshipType;

        public BookOriginalWorkId() {}

        public BookOriginalWorkId(Book book, OriginalWork originalWork, String relationshipType) {
            this.book = book;
            this.originalWork = originalWork;
            this.relationshipType = relationshipType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BookOriginalWorkId)) return false;
            BookOriginalWorkId that = (BookOriginalWorkId) o;
            return Objects.equals(book, that.book) &&
                    Objects.equals(originalWork, that.originalWork) &&
                    Objects.equals(relationshipType, that.relationshipType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(book, originalWork, relationshipType);
        }
    }
}