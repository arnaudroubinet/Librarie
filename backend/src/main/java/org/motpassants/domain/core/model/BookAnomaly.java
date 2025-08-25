package org.motpassants.domain.core.model;

import java.util.UUID;

/**
 * Represents a detected data anomaly for a book.
 */
public record BookAnomaly(
    UUID bookId,
    String type,
    String message,
    String detail
) {}
