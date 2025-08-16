package org.roubinet.librarie.infrastructure.adapter.in.rest.dto;

import java.util.List;

/**
 * DTO for unified search results containing books, series, and authors.
 */
public class UnifiedSearchResultDto {
    
    private List<BookResponseDto> books;
    private List<SeriesResponseDto> series;
    private List<AuthorResponseDto> authors;
    
    public UnifiedSearchResultDto() {}
    
    public UnifiedSearchResultDto(List<BookResponseDto> books, 
                                  List<SeriesResponseDto> series, 
                                  List<AuthorResponseDto> authors) {
        this.books = books;
        this.series = series;
        this.authors = authors;
    }
    
    public List<BookResponseDto> getBooks() {
        return books;
    }
    
    public void setBooks(List<BookResponseDto> books) {
        this.books = books;
    }
    
    public List<SeriesResponseDto> getSeries() {
        return series;
    }
    
    public void setSeries(List<SeriesResponseDto> series) {
        this.series = series;
    }
    
    public List<AuthorResponseDto> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<AuthorResponseDto> authors) {
        this.authors = authors;
    }
}