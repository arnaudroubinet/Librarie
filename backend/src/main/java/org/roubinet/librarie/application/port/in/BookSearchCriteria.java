package org.roubinet.librarie.application.port.in;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DSL for building complex book search criteria.
 * Provides a fluent interface for constructing queries with multiple filters,
 * sorting options, and field specifications.
 */
public class BookSearchCriteria {
    
    private String titleContains;
    private List<String> contributorsContain = new ArrayList<>();
    private String seriesContains;
    private String languageEquals;
    private String publisherContains;
    private LocalDate publishedAfter;
    private LocalDate publishedBefore;
    private List<String> formatsIn = new ArrayList<>();
    private String descriptionContains;
    private String isbnEquals;
    private Map<String, Object> metadataEquals;
    private SortField sortBy = SortField.CREATED_AT;
    private SortDirection sortDirection = SortDirection.DESC;
    
    // Private constructor to enforce builder pattern
    private BookSearchCriteria() {}
    
    // Getters
    public String getTitleContains() { return titleContains; }
    public List<String> getContributorsContain() { return contributorsContain; }
    public String getSeriesContains() { return seriesContains; }
    public String getLanguageEquals() { return languageEquals; }
    public String getPublisherContains() { return publisherContains; }
    public LocalDate getPublishedAfter() { return publishedAfter; }
    public LocalDate getPublishedBefore() { return publishedBefore; }
    public List<String> getFormatsIn() { return formatsIn; }
    public String getDescriptionContains() { return descriptionContains; }
    public String getIsbnEquals() { return isbnEquals; }
    public Map<String, Object> getMetadataEquals() { return metadataEquals; }
    public SortField getSortBy() { return sortBy; }
    public SortDirection getSortDirection() { return sortDirection; }
    
    /**
     * Available sort fields for book queries.
     */
    public enum SortField {
        TITLE,
        TITLE_SORT,
        CREATED_AT,
        UPDATED_AT,
        PUBLICATION_DATE,
        SERIES_INDEX
    }
    
    /**
     * Sort direction options.
     */
    public enum SortDirection {
        ASC,
        DESC
    }
    
    /**
     * Builder for constructing BookSearchCriteria with fluent DSL.
     */
    public static class Builder {
        private final BookSearchCriteria criteria = new BookSearchCriteria();
        
        /**
         * Filter by title containing text (case-insensitive).
         */
        public Builder titleContains(String title) {
            criteria.titleContains = title;
            return this;
        }
        
        /**
         * Filter by contributor (author, illustrator, etc.) containing text.
         */
        public Builder contributorContains(String contributor) {
            criteria.contributorsContain.add(contributor);
            return this;
        }
        
        /**
         * Filter by multiple contributors containing text.
         */
        public Builder contributorsContain(List<String> contributors) {
            criteria.contributorsContain.addAll(contributors);
            return this;
        }
        
        /**
         * Filter by series containing text (case-insensitive).
         */
        public Builder seriesContains(String series) {
            criteria.seriesContains = series;
            return this;
        }
        
        /**
         * Filter by exact language match.
         */
        public Builder languageEquals(String language) {
            criteria.languageEquals = language;
            return this;
        }
        
        /**
         * Filter by publisher containing text (case-insensitive).
         */
        public Builder publisherContains(String publisher) {
            criteria.publisherContains = publisher;
            return this;
        }
        
        /**
         * Filter by books published after a date.
         */
        public Builder publishedAfter(LocalDate date) {
            criteria.publishedAfter = date;
            return this;
        }
        
        /**
         * Filter by books published before a date.
         */
        public Builder publishedBefore(LocalDate date) {
            criteria.publishedBefore = date;
            return this;
        }
        
        /**
         * Filter by available format.
         */
        public Builder hasFormat(String format) {
            criteria.formatsIn.add(format);
            return this;
        }
        
        /**
         * Filter by multiple available formats.
         */
        public Builder hasFormats(List<String> formats) {
            criteria.formatsIn.addAll(formats);
            return this;
        }
        
        /**
         * Filter by description containing text (case-insensitive).
         */
        public Builder descriptionContains(String description) {
            criteria.descriptionContains = description;
            return this;
        }
        
        /**
         * Filter by exact ISBN match.
         */
        public Builder isbnEquals(String isbn) {
            criteria.isbnEquals = isbn;
            return this;
        }
        
        /**
         * Filter by metadata key-value pairs.
         */
        public Builder metadataEquals(Map<String, Object> metadata) {
            criteria.metadataEquals = metadata;
            return this;
        }
        
        /**
         * Sort results by field.
         */
        public Builder sortBy(SortField field) {
            criteria.sortBy = field;
            return this;
        }
        
        /**
         * Set sort direction.
         */
        public Builder sortDirection(SortDirection direction) {
            criteria.sortDirection = direction;
            return this;
        }
        
        /**
         * Sort ascending.
         */
        public Builder ascending() {
            criteria.sortDirection = SortDirection.ASC;
            return this;
        }
        
        /**
         * Sort descending.
         */
        public Builder descending() {
            criteria.sortDirection = SortDirection.DESC;
            return this;
        }
        
        /**
         * Build the criteria object.
         */
        public BookSearchCriteria build() {
            return criteria;
        }
    }
    
    /**
     * Create a new criteria builder.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Create criteria to find books by a specific author.
     */
    public static BookSearchCriteria byAuthor(String authorName) {
        return builder()
            .contributorContains(authorName)
            .sortBy(SortField.TITLE_SORT)
            .ascending()
            .build();
    }
    
    /**
     * Create criteria to find books in a specific series.
     */
    public static BookSearchCriteria bySeries(String seriesName) {
        return builder()
            .seriesContains(seriesName)
            .sortBy(SortField.SERIES_INDEX)
            .ascending()
            .build();
    }
    
    /**
     * Create criteria for full-text search across title, contributors, and description.
     */
    public static BookSearchCriteria fullTextSearch(String query) {
        return builder()
            .titleContains(query)
            .contributorContains(query)
            .descriptionContains(query)
            .sortBy(SortField.CREATED_AT)
            .descending()
            .build();
    }
}