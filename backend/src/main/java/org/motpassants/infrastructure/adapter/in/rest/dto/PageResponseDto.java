package org.motpassants.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Generic DTO for paginated API responses.
 * Provides a consistent structure for all paginated endpoints.
 * 
 * @param <T> the type of content in the page
 */
@Schema(description = "Paginated response wrapper")
public class PageResponseDto<T> {
    
    @Schema(description = "List of items in this page")
    private List<T> data;
    
    @Schema(description = "Metadata about the pagination")
    private PaginationMetadata metadata;
    
    // Default constructor
    public PageResponseDto() {}
    
    // Constructor
    public PageResponseDto(List<T> data, PaginationMetadata metadata) {
        this.data = data;
        this.metadata = metadata;
    }
    
    // Getters and setters
    public List<T> getData() {
        return data;
    }
    
    public void setData(List<T> data) {
        this.data = data;
    }
    
    public PaginationMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(PaginationMetadata metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Metadata for pagination information.
     */
    @Schema(description = "Pagination metadata")
    public static class PaginationMetadata {
        @Schema(description = "Current page number", example = "1")
        private int page;
        
        @Schema(description = "Number of items per page", example = "20")
        private int pageSize;
        
        @Schema(description = "Total number of items", example = "156")
        private long totalElements;
        
        @Schema(description = "Total number of pages", example = "8")
        private int totalPages;
        
        @Schema(description = "Whether this is the first page", example = "false")
        private boolean first;
        
        @Schema(description = "Whether this is the last page", example = "true")
        private boolean last;
        
        @Schema(description = "Cursor for next page if available")
        private String nextCursor;
        
        @Schema(description = "Cursor for previous page if available")
        private String previousCursor;
        
        public PaginationMetadata() {}
        
        public PaginationMetadata(int page, int pageSize, long totalElements) {
            this.page = page;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
            this.first = page == 1;
            this.last = page >= totalPages;
        }
        
        // Getters and setters
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        
        public boolean isFirst() { return first; }
        public void setFirst(boolean first) { this.first = first; }
        
        public boolean isLast() { return last; }
        public void setLast(boolean last) { this.last = last; }
        
        public String getNextCursor() { return nextCursor; }
        public void setNextCursor(String nextCursor) { this.nextCursor = nextCursor; }
        
        public String getPreviousCursor() { return previousCursor; }
        public void setPreviousCursor(String previousCursor) { this.previousCursor = previousCursor; }
    }
}