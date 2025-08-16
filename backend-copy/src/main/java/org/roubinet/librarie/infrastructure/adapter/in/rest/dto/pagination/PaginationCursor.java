package org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Cursor for keyset pagination.
 * Provides efficient pagination using seek method instead of offset.
 */
@Schema(description = "Pagination cursor for keyset-based pagination")
public class PaginationCursor {
    
    @Schema(description = "Cursor value for pagination", example = "eyJpZCI6IjEyMyIsInRpbWVzdGFtcCI6IjIwMjQtMDEtMDFUMTA6MDA6MDBaIn0=")
    private String cursor;
    
    @Schema(description = "Direction of pagination", example = "NEXT")
    private Direction direction;
    
    @Schema(description = "Number of items to fetch", example = "20")
    private int limit;
    
    public enum Direction {
        NEXT, PREVIOUS
    }
    
    // Default constructor
    public PaginationCursor() {}
    
    public PaginationCursor(String cursor, Direction direction, int limit) {
        this.cursor = cursor;
        this.direction = direction;
        this.limit = limit;
    }
    
    // Getters and setters
    public String getCursor() {
        return cursor;
    }
    
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    @Override
    public String toString() {
        return "PaginationCursor{" +
                "cursor='" + cursor + '\'' +
                ", direction=" + direction +
                ", limit=" + limit +
                '}';
    }
}