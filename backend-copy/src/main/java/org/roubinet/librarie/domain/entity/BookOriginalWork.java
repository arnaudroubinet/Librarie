package org.roubinet.librarie.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;

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
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, columnDefinition = "text default 'PRIMARY'")
    private BookOriginalWorkRelationType relationshipType = BookOriginalWorkRelationType.PRIMARY;

    @Column(name = "order_index", nullable = false, columnDefinition = "integer default 0")
    private Integer orderIndex = 0;

    // Default constructor for JPA
    public BookOriginalWork() {}

    // Constructor with all fields
    public BookOriginalWork(Book book, OriginalWork originalWork, BookOriginalWorkRelationType relationshipType, Integer orderIndex) {
        this.book = book;
        this.originalWork = originalWork;
        this.relationshipType = relationshipType != null ? relationshipType : BookOriginalWorkRelationType.PRIMARY;
        this.orderIndex = orderIndex != null ? orderIndex : 0;
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

    public BookOriginalWorkRelationType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(BookOriginalWorkRelationType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    // Composite key class
    public static record BookOriginalWorkId(Book book, OriginalWork originalWork, BookOriginalWorkRelationType relationshipType) implements Serializable {}
}