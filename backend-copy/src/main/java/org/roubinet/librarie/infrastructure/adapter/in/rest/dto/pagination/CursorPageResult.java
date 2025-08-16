package org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination;

import java.util.List;

/**
 * Generic cursor-based pagination result.
 * Contains data and navigation information for cursor pagination.
 *
 * @param <T> the type of items in the result
 */
public class CursorPageResult<T> {
    
    private final List<T> items;
    private final String nextCursor;
    private final String previousCursor;
    private final boolean hasNext;
    private final boolean hasPrevious;
    private final int limit;
    private final Long totalCount; // Optional, can be null for performance
    
    public CursorPageResult(List<T> items, String nextCursor, String previousCursor, 
                           boolean hasNext, boolean hasPrevious, int limit, Long totalCount) {
        this.items = items;
        this.nextCursor = nextCursor;
        this.previousCursor = previousCursor;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
        this.limit = limit;
        this.totalCount = totalCount;
    }
    
    public List<T> getItems() {
        return items;
    }
    
    public String getNextCursor() {
        return nextCursor;
    }
    
    public String getPreviousCursor() {
        return previousCursor;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public Long getTotalCount() {
        return totalCount;
    }
    
    /**
     * Builder for creating CursorPageResult instances.
     */
    public static class Builder<T> {
        private List<T> items;
        private String nextCursor;
        private String previousCursor;
        private boolean hasNext;
        private boolean hasPrevious;
        private int limit;
        private Long totalCount;
        
        public Builder<T> items(List<T> items) {
            this.items = items;
            return this;
        }
        
        public Builder<T> nextCursor(String nextCursor) {
            this.nextCursor = nextCursor;
            return this;
        }
        
        public Builder<T> previousCursor(String previousCursor) {
            this.previousCursor = previousCursor;
            return this;
        }
        
        public Builder<T> hasNext(boolean hasNext) {
            this.hasNext = hasNext;
            return this;
        }
        
        public Builder<T> hasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
            return this;
        }
        
        public Builder<T> limit(int limit) {
            this.limit = limit;
            return this;
        }
        
        public Builder<T> totalCount(Long totalCount) {
            this.totalCount = totalCount;
            return this;
        }
        
        public CursorPageResult<T> build() {
            return new CursorPageResult<>(items, nextCursor, previousCursor, 
                                        hasNext, hasPrevious, limit, totalCount);
        }
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
}