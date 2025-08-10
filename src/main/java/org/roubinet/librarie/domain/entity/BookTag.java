package org.roubinet.librarie.domain.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * BookTag entity representing the many-to-many relationship between books and tags.
 */
@Entity
@Table(name = "book_tags")
@IdClass(BookTag.BookTagId.class)
public class BookTag {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    // Default constructor
    public BookTag() {}

    public BookTag(Book book, Tag tag) {
        this.book = book;
        this.tag = tag;
    }

    // Getters and setters
    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookTag)) return false;
        BookTag bookTag = (BookTag) o;
        return Objects.equals(book, bookTag.book) && Objects.equals(tag, bookTag.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, tag);
    }

    // Composite key class
    public static class BookTagId implements Serializable {
        private Book book;
        private Tag tag;

        public BookTagId() {}

        public BookTagId(Book book, Tag tag) {
            this.book = book;
            this.tag = tag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BookTagId)) return false;
            BookTagId that = (BookTagId) o;
            return Objects.equals(book, that.book) && Objects.equals(tag, that.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(book, tag);
        }
    }
}