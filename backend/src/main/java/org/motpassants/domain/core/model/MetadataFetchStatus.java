package org.motpassants.domain.core.model;

/**
 * Status of a metadata fetch operation.
 * Tracks the lifecycle of metadata retrieval for a book.
 */
public enum MetadataFetchStatus {
    PENDING("Pending fetch"),
    IN_PROGRESS("Fetch in progress"),
    COMPLETED("Fetch completed successfully"),
    FAILED("Fetch failed"),
    PARTIAL("Partial results obtained"),
    APPROVED("Metadata approved and applied"),
    REJECTED("Metadata rejected by user");

    private final String description;

    MetadataFetchStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == APPROVED || this == REJECTED;
    }

    public boolean isSuccessful() {
        return this == COMPLETED || this == PARTIAL || this == APPROVED;
    }

    @Override
    public String toString() {
        return description;
    }
}
