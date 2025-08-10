package org.roubinet.librarie.domain.entity;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 * BookTag record representing the immutable many-to-many relationship between books and tags.
 */
@Entity
@Table(name = "book_tags")
@IdClass(BookTag.BookTagId.class)
public record BookTag(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    Book book,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    Tag tag
) {
    // Composite key class
    public static record BookTagId(Book book, Tag tag) implements Serializable {}
}