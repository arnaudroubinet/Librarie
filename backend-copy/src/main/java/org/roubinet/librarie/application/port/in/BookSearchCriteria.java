package org.roubinet.librarie.application.port.in;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Simple query language structure for book search criteria.
 * Designed to be easily serialized/deserialized for frontend-backend communication.
 * Frontend can build these queries as JSON and send them to the backend for validation and execution.
 */
public class BookSearchCriteria {
    
    @JsonProperty("titleContains")
    private String titleContains;
    
    @JsonProperty("contributorsContain")
    private List<String> contributorsContain;
    
    @JsonProperty("seriesContains")
    private String seriesContains;
    
    @JsonProperty("languageEquals")
    private String languageEquals;
    
    @JsonProperty("publisherContains")
    private String publisherContains;
    
    @JsonProperty("publishedAfter")
    private LocalDate publishedAfter;
    
    @JsonProperty("publishedBefore")
    private LocalDate publishedBefore;
    
    @JsonProperty("formatsIn")
    private List<String> formatsIn;
    
    @JsonProperty("descriptionContains")
    private String descriptionContains;
    
    @JsonProperty("isbnEquals")
    private String isbnEquals;
    
    @JsonProperty("metadataEquals")
    private Map<String, Object> metadataEquals;
    
    @JsonProperty("sortBy")
    private String sortBy = "createdAt";
    
    @JsonProperty("sortDirection")
    private String sortDirection = "desc";
    
    // Default constructor for JSON deserialization
    public BookSearchCriteria() {}
    
    // Getters and setters for JSON serialization/deserialization
    public String getTitleContains() { return titleContains; }
    public void setTitleContains(String titleContains) { this.titleContains = titleContains; }
    
    public List<String> getContributorsContain() { return contributorsContain; }
    public void setContributorsContain(List<String> contributorsContain) { this.contributorsContain = contributorsContain; }
    
    public String getSeriesContains() { return seriesContains; }
    public void setSeriesContains(String seriesContains) { this.seriesContains = seriesContains; }
    
    public String getLanguageEquals() { return languageEquals; }
    public void setLanguageEquals(String languageEquals) { this.languageEquals = languageEquals; }
    
    public String getPublisherContains() { return publisherContains; }
    public void setPublisherContains(String publisherContains) { this.publisherContains = publisherContains; }
    
    public LocalDate getPublishedAfter() { return publishedAfter; }
    public void setPublishedAfter(LocalDate publishedAfter) { this.publishedAfter = publishedAfter; }
    
    public LocalDate getPublishedBefore() { return publishedBefore; }
    public void setPublishedBefore(LocalDate publishedBefore) { this.publishedBefore = publishedBefore; }
    
    public List<String> getFormatsIn() { return formatsIn; }
    public void setFormatsIn(List<String> formatsIn) { this.formatsIn = formatsIn; }
    
    public String getDescriptionContains() { return descriptionContains; }
    public void setDescriptionContains(String descriptionContains) { this.descriptionContains = descriptionContains; }
    
    public String getIsbnEquals() { return isbnEquals; }
    public void setIsbnEquals(String isbnEquals) { this.isbnEquals = isbnEquals; }
    
    public Map<String, Object> getMetadataEquals() { return metadataEquals; }
    public void setMetadataEquals(Map<String, Object> metadataEquals) { this.metadataEquals = metadataEquals; }
    
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    
    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
}