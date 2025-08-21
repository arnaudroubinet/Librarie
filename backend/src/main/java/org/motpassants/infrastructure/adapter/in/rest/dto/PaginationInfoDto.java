package org.motpassants.infrastructure.adapter.in.rest.dto;

/**
 * Data Transfer Object for pagination information in search results.
 * Converted to record for immutability and reduced boilerplate.
 */
public record PaginationInfoDto(
    int booksCount,
    int authorsCount, 
    int seriesCount,
    int offset,
    int limit
) {
    // Constructor for simple case with calculated limit
    public PaginationInfoDto(int booksCount, int authorsCount, int seriesCount) {
        this(booksCount, authorsCount, seriesCount, 0, Math.max(booksCount, Math.max(authorsCount, seriesCount)));
    }
}