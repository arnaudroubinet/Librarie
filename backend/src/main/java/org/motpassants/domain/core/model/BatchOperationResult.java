package org.motpassants.domain.core.model;

import java.util.UUID;

/**
 * Domain model representing the result of a batch operation on a single book.
 */
public record BatchOperationResult(
    UUID bookId,
    String bookTitle,
    boolean success,
    String errorMessage,
    String changesSummary  // Brief description of what was changed
) {
    
    /**
     * Creates a successful result.
     */
    public static BatchOperationResult success(UUID bookId, String bookTitle, String changesSummary) {
        return new BatchOperationResult(bookId, bookTitle, true, null, changesSummary);
    }
    
    /**
     * Creates a failed result.
     */
    public static BatchOperationResult failure(UUID bookId, String bookTitle, String errorMessage) {
        return new BatchOperationResult(bookId, bookTitle, false, errorMessage, null);
    }
}