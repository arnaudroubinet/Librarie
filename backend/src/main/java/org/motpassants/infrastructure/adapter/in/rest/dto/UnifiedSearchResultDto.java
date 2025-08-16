package org.motpassants.infrastructure.adapter.in.rest.dto;

import java.util.List;

/**
 * Data Transfer Object for unified search results.
 */
public class UnifiedSearchResultDto {
    
    private List<BookResponseDto> books;
    private List<AuthorResponseDto> authors;
    private List<SeriesResponseDto> series;
    private int totalResults;
    private String query;
    private Long searchTime; // Search time in milliseconds
    private PaginationInfoDto pagination;
    
    // Default constructor
    public UnifiedSearchResultDto() {}
    
    // Constructor
    public UnifiedSearchResultDto(List<BookResponseDto> books, 
                                 List<AuthorResponseDto> authors,
                                 List<SeriesResponseDto> series,
                                 String query,
                                 Long searchTime,
                                 int limit) {
        this.books = books;
        this.authors = authors;
        this.series = series;
        this.totalResults = books.size() + authors.size() + series.size();
        this.query = query;
        this.searchTime = searchTime;
        this.pagination = new PaginationInfoDto(books.size(), authors.size(), series.size(), 0, limit);
    }
    
    // Getters and setters
    public List<BookResponseDto> getBooks() {
        return books;
    }
    
    public void setBooks(List<BookResponseDto> books) {
        this.books = books;
    }
    
    public List<AuthorResponseDto> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<AuthorResponseDto> authors) {
        this.authors = authors;
    }
    
    public List<SeriesResponseDto> getSeries() {
        return series;
    }
    
    public void setSeries(List<SeriesResponseDto> series) {
        this.series = series;
    }
    
    public int getTotalResults() {
        return totalResults;
    }
    
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public Long getSearchTime() {
        return searchTime;
    }
    
    public void setSearchTime(Long searchTime) {
        this.searchTime = searchTime;
    }
    
    public PaginationInfoDto getPagination() {
        return pagination;
    }
    
    public void setPagination(PaginationInfoDto pagination) {
        this.pagination = pagination;
    }
    
    @Override
    public String toString() {
        return "UnifiedSearchResultDto{" +
                "totalResults=" + totalResults +
                ", books=" + (books != null ? books.size() : 0) +
                ", authors=" + (authors != null ? authors.size() : 0) +
                ", series=" + (series != null ? series.size() : 0) +
                ", query='" + query + '\'' +
                ", searchTime=" + searchTime +
                '}';
    }
}