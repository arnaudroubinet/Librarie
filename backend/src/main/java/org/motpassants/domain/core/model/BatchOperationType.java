package org.motpassants.domain.core.model;

/**
 * Enumeration of batch operation types.
 */
public enum BatchOperationType {
    /**
     * Batch edit operation - applies changes to multiple books.
     */
    EDIT,
    
    /**
     * Batch delete operation - deletes multiple books.
     */
    DELETE
}