package org.motpassants.domain.core.model;

import java.util.List;

/**
 * Domain model for cursor-based pagination results.
 * Pure domain object without infrastructure dependencies.
 */
public class PageResult<T> {
    
    private final List<T> items;
    private final String nextCursor;
    private final String previousCursor;
    private final boolean hasNext;
    private final boolean hasPrevious;
    private final int totalCount;

    public PageResult(List<T> items, String nextCursor, String previousCursor, 
                     boolean hasNext, boolean hasPrevious, int totalCount) {
        this.items = items;
        this.nextCursor = nextCursor;
        this.previousCursor = previousCursor;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
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

    public boolean hasNext() {
        return hasNext;
    }

    public boolean hasPrevious() {
        return hasPrevious;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getSize() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}