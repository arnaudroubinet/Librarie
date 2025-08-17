package org.motpassants.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Request DTO for book creation and updates.
 */
@Schema(description = "Book request data")
public class BookRequestDto {
    
    @Schema(description = "Book title", required = true, example = "The Great Gatsby")
    private String title;
    
    @Schema(description = "Book ISBN", example = "978-0-7432-7356-5")
    private String isbn;
    
    @Schema(description = "Book description or summary")
    private String description;
    
    @Schema(description = "Number of pages", example = "180")
    private Integer pageCount;
    
    @Schema(description = "Publication year", example = "1925")
    private Integer publicationYear;
    
    @Schema(description = "Language code", example = "en")
    private String language;
    
    
    // Default constructor
    public BookRequestDto() {}
    
    // Getters and setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getPageCount() {
        return pageCount;
    }
    
    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }
    
    public Integer getPublicationYear() {
        return publicationYear;
    }
    
    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    // Cover URL removed: images are hydrated to assets; not accepted via API
}