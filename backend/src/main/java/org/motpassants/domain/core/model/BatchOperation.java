package org.motpassants.domain.core.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Domain model representing a batch operation on multiple books.
 * Supports both batch edit and batch delete operations.
 */
public record BatchOperation(
    UUID operationId,
    BatchOperationType type,
    List<UUID> bookIds,
    BatchEditRequest editRequest,
    UUID userId,
    OffsetDateTime createdAt,
    BatchOperationStatus status,
    List<BatchOperationResult> results,
    String errorMessage
) {
    
    /**
     * Creates a new batch edit operation.
     */
    public static BatchOperation createEdit(List<UUID> bookIds, BatchEditRequest editRequest, UUID userId) {
        return new BatchOperation(
            UUID.randomUUID(),
            BatchOperationType.EDIT,
            bookIds,
            editRequest,
            userId,
            OffsetDateTime.now(),
            BatchOperationStatus.PENDING,
            List.of(),
            null
        );
    }
    
    /**
     * Creates a new batch delete operation.
     */
    public static BatchOperation createDelete(List<UUID> bookIds, UUID userId) {
        return new BatchOperation(
            UUID.randomUUID(),
            BatchOperationType.DELETE,
            bookIds,
            null,
            userId,
            OffsetDateTime.now(),
            BatchOperationStatus.PENDING,
            List.of(),
            null
        );
    }
    
    /**
     * Updates the operation with results.
     */
    public BatchOperation withResults(List<BatchOperationResult> results, BatchOperationStatus status) {
        return new BatchOperation(
            operationId,
            type,
            bookIds,
            editRequest,
            userId,
            createdAt,
            status,
            results,
            errorMessage
        );
    }
    
    /**
     * Updates the operation with an error.
     */
    public BatchOperation withError(String errorMessage) {
        return new BatchOperation(
            operationId,
            type,
            bookIds,
            editRequest,
            userId,
            createdAt,
            BatchOperationStatus.FAILED,
            results,
            errorMessage
        );
    }
}