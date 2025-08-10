package org.roubinet.librarie.domain.entity;

import jakarta.persistence.*;

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
    @Column(name = "relationship_type", nullable = false, columnDefinition = "text default 'primary'")
    String relationshipType,

    @Column(name = "order_index", nullable = false, columnDefinition = "integer default 0")
    Integer orderIndex
) {
    
    public BookOriginalWork {
        if (relationshipType == null) {
            relationshipType = "primary";
        }
        if (orderIndex == null) {
            orderIndex = 0;
        }
    }

    // Composite key class
    public static record BookOriginalWorkId(Book book, OriginalWork originalWork, String relationshipType) implements Serializable {}
}