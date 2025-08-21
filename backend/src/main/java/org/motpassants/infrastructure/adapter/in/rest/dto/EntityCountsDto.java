package org.motpassants.infrastructure.adapter.in.rest.dto;

/**
 * Data Transfer Object for entity counts.
 * Converted to record for immutability and reduced boilerplate.
 */
public record EntityCountsDto(
    long books,
    long series, 
    long authors,
    long publishers,
    long languages,
    long formats,
    long tags
) {}