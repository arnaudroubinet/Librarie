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
 * BookOriginalWork record representing the immutable many-to-many relationship between books and original works.
 * Supports different relationship types like primary, collection, anthology, adaptation.
 */
@Entity
@Table(name = "book_original_works")
@IdClass(BookOriginalWork.BookOriginalWorkId.class)
public record BookOriginalWork(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    Book book,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_work_id")
    OriginalWork originalWork,

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, columnDefinition = "text default 'PRIMARY'")
    BookOriginalWorkRelationType relationshipType,

    @Column(name = "order_index", nullable = false, columnDefinition = "integer default 0")
    Integer orderIndex
) {
    
    public BookOriginalWork {
        if (relationshipType == null) {
            relationshipType = BookOriginalWorkRelationType.PRIMARY;
        }
        if (orderIndex == null) {
            orderIndex = 0;
        }
    }

    // Composite key class
    public static record BookOriginalWorkId(Book book, OriginalWork originalWork, BookOriginalWorkRelationType relationshipType) implements Serializable {}
}