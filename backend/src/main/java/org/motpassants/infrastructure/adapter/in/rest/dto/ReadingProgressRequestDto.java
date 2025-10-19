package org.motpassants.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for reading progress update requests.
 */
public class ReadingProgressRequestDto {
    
    @JsonProperty("currentPage")
    private Integer currentPage;
    
    @JsonProperty("totalPages")
    private Integer totalPages;
    
    @JsonProperty("progress")
    private Double progress;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("progressLocator")
    private String progressLocator;
    
    @JsonProperty("notes")
    private String notes;

    // Default constructor
    public ReadingProgressRequestDto() {}

    // Getters and setters
    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProgressLocator() {
        return progressLocator;
    }

    public void setProgressLocator(String progressLocator) {
        this.progressLocator = progressLocator;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
