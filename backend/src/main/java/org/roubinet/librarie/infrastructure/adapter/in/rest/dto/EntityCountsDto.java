package org.roubinet.librarie.infrastructure.adapter.in.rest.dto;

/**
 * DTO for entity counts in the library system.
 */
public class EntityCountsDto {
    
    private long books;
    private long series;
    private long authors;
    private long publishers;
    private long languages;
    private long formats;
    
    public EntityCountsDto() {}
    
    public EntityCountsDto(long books, long series, long authors, long publishers, long languages, long formats) {
        this.books = books;
        this.series = series;
        this.authors = authors;
        this.publishers = publishers;
        this.languages = languages;
        this.formats = formats;
    }
    
    // Getters and Setters
    public long getBooks() {
        return books;
    }
    
    public void setBooks(long books) {
        this.books = books;
    }
    
    public long getSeries() {
        return series;
    }
    
    public void setSeries(long series) {
        this.series = series;
    }
    
    public long getAuthors() {
        return authors;
    }
    
    public void setAuthors(long authors) {
        this.authors = authors;
    }
    
    public long getPublishers() {
        return publishers;
    }
    
    public void setPublishers(long publishers) {
        this.publishers = publishers;
    }
    
    public long getLanguages() {
        return languages;
    }
    
    public void setLanguages(long languages) {
        this.languages = languages;
    }
    
    public long getFormats() {
        return formats;
    }
    
    public void setFormats(long formats) {
        this.formats = formats;
    }
}