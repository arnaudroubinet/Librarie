package org.roubinet.librarie.domain.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * BookPublisher entity representing the many-to-many relationship between books and publishers.
 */
@Entity
@Table(name = "book_publishers")
@IdClass(BookPublisher.BookPublisherId.class)
public class BookPublisher {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @Id
    @Column(name = "role", nullable = false, columnDefinition = "text default 'publisher'")
    private String role = "publisher";

    // Default constructor
    public BookPublisher() {}

    public BookPublisher(Book book, Publisher publisher, String role) {
        this.book = book;
        this.publisher = publisher;
        this.role = role;
    }

    // Getters and setters
    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
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
        if (!(o instanceof BookPublisher)) return false;
        BookPublisher that = (BookPublisher) o;
        return Objects.equals(book, that.book) &&
                Objects.equals(publisher, that.publisher) &&
                Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, publisher, role);
    }

    // Composite key class
    public static class BookPublisherId implements Serializable {
        private Book book;
        private Publisher publisher;
        private String role;

        public BookPublisherId() {}

        public BookPublisherId(Book book, Publisher publisher, String role) {
            this.book = book;
            this.publisher = publisher;
            this.role = role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BookPublisherId)) return false;
            BookPublisherId that = (BookPublisherId) o;
            return Objects.equals(book, that.book) &&
                    Objects.equals(publisher, that.publisher) &&
                    Objects.equals(role, that.role);
        }

        @Override
        public int hashCode() {
            return Objects.hash(book, publisher, role);
        }
    }
}