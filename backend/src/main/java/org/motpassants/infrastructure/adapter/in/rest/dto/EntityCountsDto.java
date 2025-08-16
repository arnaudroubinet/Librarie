package org.motpassants.infrastructure.adapter.in.rest.dto;

/**
 * Data Transfer Object for entity counts.
 */
public class EntityCountsDto {
    
    private long books;
    private long series;
    private long authors;
    private long publishers;
    private long languages;
    private long formats;
    private long tags;
    
    // Default constructor
    public EntityCountsDto() {}
    
    // Constructor
    public EntityCountsDto(long books, long series, long authors, long publishers,
                          long languages, long formats, long tags) {
        this.books = books;
        this.series = series;
        this.authors = authors;
        this.publishers = publishers;
        this.languages = languages;
        this.formats = formats;
        this.tags = tags;
    }
    
    // Getters and setters
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
    
    public long getTags() {
        return tags;
    }
    
    public void setTags(long tags) {
        this.tags = tags;
    }
    
    @Override
    public String toString() {
        return "EntityCountsDto{" +
                "books=" + books +
                ", series=" + series +
                ", authors=" + authors +
                ", publishers=" + publishers +
                ", languages=" + languages +
                ", formats=" + formats +
                ", tags=" + tags +
                '}';
    }
}