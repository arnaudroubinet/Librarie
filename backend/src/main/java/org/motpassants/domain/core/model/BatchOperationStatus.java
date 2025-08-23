package org.motpassants.domain.core.model;

/**
 * Enumeration of batch operation statuses.
 */
public enum BatchOperationStatus {
    /**
     * Operation is pending execution.
     */
    PENDING,
    
    /**
     * Operation is currently running.
     */
    RUNNING,
    
    /**
     * Operation completed successfully.
     */
    COMPLETED,
    
    /**
     * Operation partially completed with some failures.
     */
    PARTIAL,
    
    /**
     * Operation failed completely.
     */
    FAILED,
    
    /**
     * Operation was cancelled by user.
     */
    CANCELLED
}