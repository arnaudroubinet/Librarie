package org.motpassants.domain.core.model;

/**
 * Enum representing different metadata provider sources.
 * Used to track which external API provided metadata for a book.
 */
public enum MetadataSource {
    GOOGLE_BOOKS("Google Books API"),
    OPEN_LIBRARY("Open Library API"),
    ISBNDB("ISBNdb API"),
    MANUAL("Manual Entry"),
    UNKNOWN("Unknown Source");

    private final String displayName;

    MetadataSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
