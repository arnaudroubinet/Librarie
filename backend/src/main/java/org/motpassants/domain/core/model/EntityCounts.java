package org.motpassants.domain.core.model;

/**
 * Entity counts domain model representing statistics about library entities.
 * Converted to record for immutability and reduced boilerplate.
 */
public record EntityCounts(
    long books,
    long series,
    long authors,
    long publishers, 
    long languages,
    long formats,
    long tags
) {}