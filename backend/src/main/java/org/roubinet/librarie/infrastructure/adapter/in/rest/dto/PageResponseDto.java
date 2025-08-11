package org.roubinet.librarie.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Generic DTO for cursor-based paginated API responses.
 * Uses keyset pagination for better performance and consistency.
 * 
 * @param <T> the type of content in the page
 */
@Schema(description = "Cursor-based paginated response")
public class PageResponseDto<T> {
    
    @Schema(description = "List of items in this page")
    private List<T> content;
    
    @Schema(description = "Cursor for the next page (null if no more pages)", example = "eyJpZCI6IjEyMyIsInRpbWVzdGFtcCI6IjIwMjQtMDEtMDFUMTA6MDA6MDBaIn0=")
    private String nextCursor;
    
    @Schema(description = "Cursor for the previous page (null if first page)", example = "eyJpZCI6IjEwMCIsInRpbWVzdGFtcCI6IjIwMjQtMDEtMDFUMDk6MDA6MDBaIn0=")
    private String previousCursor;
    
    @Schema(description = "Number of items requested", example = "20")
    private int limit;
    
    @Schema(description = "Number of items returned in this page", example = "20")
    private int size;
    
    @Schema(description = "Whether this is the first page", example = "false")
    private boolean hasNext;
    
    @Schema(description = "Whether this is the last page", example = "true")
    private boolean hasPrevious;
    
    @Schema(description = "Total number of items (estimated or exact if known)", example = "156")
    private Long totalElements;
    
    // Default constructor
    public PageResponseDto() {}
    
    // Constructor for cursor-based pagination
    public PageResponseDto(List<T> content, String nextCursor, String previousCursor, int limit) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.previousCursor = previousCursor;
        this.limit = limit;
        this.size = content != null ? content.size() : 0;
        this.hasNext = nextCursor != null;
        this.hasPrevious = previousCursor != null;
    }
    
    // Constructor with total elements (when available)
    public PageResponseDto(List<T> content, String nextCursor, String previousCursor, int limit, Long totalElements) {
        this(content, nextCursor, previousCursor, limit);
        this.totalElements = totalElements;
    }
    
    // Getters and setters
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
        this.size = content != null ? content.size() : 0;
    }
    
    public String getNextCursor() {
        return nextCursor;
    }
    
    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
        this.hasNext = nextCursor != null;
    }
    
    public String getPreviousCursor() {
        return previousCursor;
    }
    
    public void setPreviousCursor(String previousCursor) {
        this.previousCursor = previousCursor;
        this.hasPrevious = previousCursor != null;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    public Long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }
}