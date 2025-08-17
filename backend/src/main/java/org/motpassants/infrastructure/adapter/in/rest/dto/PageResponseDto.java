package org.motpassants.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Generic DTO for paginated API responses.
 * Provides a consistent structure for all paginated endpoints.
 * Uses the same format as backend-copy for frontend compatibility.
 * 
 * @param <T> the type of content in the page
 */
@Schema(description = "Paginated response wrapper")
public class PageResponseDto<T> {
    
    @Schema(description = "List of items in this page")
    private List<T> content;
    
    @Schema(description = "Cursor for next page if available")
    private String nextCursor;
    
    @Schema(description = "Cursor for previous page if available")  
    private String previousCursor;
    
    @Schema(description = "Limit used in this request", example = "20")
    private int limit;
    
    @Schema(description = "Number of items in current page", example = "20")
    private int size;
    
    @Schema(description = "Whether there is a next page available", example = "true")
    private boolean hasNext;
    
    @Schema(description = "Whether there is a previous page available", example = "false")
    private boolean hasPrevious;
    
    @Schema(description = "Total number of items (null if not calculated)")
    private Long totalElements;
    
    // Default constructor
    public PageResponseDto() {}
    
    // Constructor
    public PageResponseDto(List<T> content, String nextCursor, String previousCursor, int limit, boolean hasNext, boolean hasPrevious, Long totalElements) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.previousCursor = previousCursor;
        this.limit = limit;
        this.size = content != null ? content.size() : 0;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
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
    }
    
    public String getPreviousCursor() { 
        return previousCursor; 
    }
    
    public void setPreviousCursor(String previousCursor) { 
        this.previousCursor = previousCursor; 
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