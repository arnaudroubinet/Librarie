package org.motpassants.domain.core.model;

import java.util.List;

/**
 * Unified search result domain model containing results from all entity types.
 */
public class UnifiedSearchResult {
    
    private List<Book> books;
    private List<Author> authors;
    private List<Series> series;
    private int totalResults;
    
    public UnifiedSearchResult() {}
    
    public UnifiedSearchResult(List<Book> books, List<Author> authors, List<Series> series) {
        this.books = books;
        this.authors = authors;
        this.series = series;
        this.totalResults = books.size() + authors.size() + series.size();
    }
    
    // Getters and setters
    public List<Book> getBooks() {
        return books;
    }
    
    public void setBooks(List<Book> books) {
        this.books = books;
    }
    
    public List<Author> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }
    
    public List<Series> getSeries() {
        return series;
    }
    
    public void setSeries(List<Series> series) {
        this.series = series;
    }
    
    public int getTotalResults() {
        return totalResults;
    }
    
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
    
    @Override
    public String toString() {
        return "UnifiedSearchResult{" +
                "totalResults=" + totalResults +
                ", books=" + (books != null ? books.size() : 0) +
                ", authors=" + (authors != null ? authors.size() : 0) +
                ", series=" + (series != null ? series.size() : 0) +
                '}';
    }
}