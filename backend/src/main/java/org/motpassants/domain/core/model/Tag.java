package org.motpassants.domain.core.model;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tag domain model representing categorization labels for books.
 * Pure domain object without any infrastructure dependencies.
 */
public class Tag {

    private UUID id;
    private String name;
    private String color;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Relationships
    private Set<Book> books = new HashSet<>();

    // Default constructor
    public Tag() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    // Constructor for creating new tags
    public Tag(String name) {
        this();
        this.name = name;
    }

    public Tag(String name, String color) {
        this(name);
        this.color = color;
    }

    // Business methods
    public void updateName(String newName) {
        this.name = newName;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateColor(String newColor) {
        this.color = newColor;
        this.updatedAt = OffsetDateTime.now();
    }

    public void addBook(Book book) {
        this.books.add(book);
        this.updatedAt = OffsetDateTime.now();
    }

    public void removeBook(Book book) {
        this.books.remove(book);
        this.updatedAt = OffsetDateTime.now();
    }

    public int getBookCount() {
        return books.size();
    }

    public boolean hasValidColor() {
        return color != null && color.matches("^#[0-9A-Fa-f]{6}$");
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return id != null && id.equals(tag.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}