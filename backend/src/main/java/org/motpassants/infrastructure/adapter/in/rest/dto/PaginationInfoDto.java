package org.motpassants.infrastructure.adapter.in.rest.dto;

/**
 * Data Transfer Object for pagination information in search results.
 */
public class PaginationInfoDto {
    
    private int booksCount;
    private int authorsCount;
    private int seriesCount;
    private int offset;
    private int limit;
    
    // Default constructor
    public PaginationInfoDto() {}
    
    // Constructor
    public PaginationInfoDto(int booksCount, int authorsCount, int seriesCount) {
        this.booksCount = booksCount;
        this.authorsCount = authorsCount;
        this.seriesCount = seriesCount;
        this.offset = 0; // Always 0 for unified search
        this.limit = Math.max(booksCount, Math.max(authorsCount, seriesCount));
    }
    
    // Constructor with offset and limit
    public PaginationInfoDto(int booksCount, int authorsCount, int seriesCount, int offset, int limit) {
        this.booksCount = booksCount;
        this.authorsCount = authorsCount;
        this.seriesCount = seriesCount;
        this.offset = offset;
        this.limit = limit;
    }
    
    // Getters and setters
    public int getBooksCount() {
        return booksCount;
    }
    
    public void setBooksCount(int booksCount) {
        this.booksCount = booksCount;
    }
    
    public int getAuthorsCount() {
        return authorsCount;
    }
    
    public void setAuthorsCount(int authorsCount) {
        this.authorsCount = authorsCount;
    }
    
    public int getSeriesCount() {
        return seriesCount;
    }
    
    public void setSeriesCount(int seriesCount) {
        this.seriesCount = seriesCount;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    @Override
    public String toString() {
        return "PaginationInfoDto{" +
                "booksCount=" + booksCount +
                ", authorsCount=" + authorsCount +
                ", seriesCount=" + seriesCount +
                ", offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}