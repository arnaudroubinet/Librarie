package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.BatchEditRequest;
import org.motpassants.domain.core.model.BatchEditPreview;
import org.motpassants.domain.core.model.BatchOperation;
import org.motpassants.domain.core.model.BatchOperationResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port defining batch operation use cases.
 * Represents the primary ports (driving ports) for batch operations on books.
 * Pure domain interface without infrastructure dependencies.
 */
public interface BatchOperationUseCase {
    
    /**
     * Execute a batch edit operation on multiple books.
     * Changes are applied transactionally with individual item rollback on failure.
     * 
     * @param bookIds the list of book IDs to edit
     * @param editRequest the metadata changes to apply
     * @param userId the user performing the operation
     * @return the batch operation with results
     */
    BatchOperation executeBatchEdit(List<UUID> bookIds, BatchEditRequest editRequest, UUID userId);
    
    /**
     * Execute a batch delete operation on multiple books.
     * Books are deleted individually with cleanup of associated files.
     * 
     * @param bookIds the list of book IDs to delete
     * @param userId the user performing the operation
     * @return the batch operation with results
     */
    BatchOperation executeBatchDelete(List<UUID> bookIds, UUID userId);
    
    /**
     * Preview the changes that would be made by a batch edit operation.
     * Does not actually apply any changes.
     * 
     * @param bookIds the list of book IDs to preview
     * @param editRequest the metadata changes to preview
     * @return list of preview results showing what would change
     */
    List<BatchEditPreview> previewBatchEdit(List<UUID> bookIds, BatchEditRequest editRequest);
    
    /**
     * Get the status and results of a batch operation.
     * 
     * @param operationId the operation ID
     * @return optional containing the operation if found
     */
    Optional<BatchOperation> getBatchOperation(UUID operationId);
    
    /**
     * Cancel a running batch operation.
     * 
     * @param operationId the operation ID
     * @param userId the user requesting cancellation
     * @return true if operation was cancelled, false if not found or not cancellable
     */
    boolean cancelBatchOperation(UUID operationId, UUID userId);
    
    /**
     * Get recent batch operations for a user.
     * 
     * @param userId the user ID
     * @param limit maximum number of operations to return
     * @return list of recent operations
     */
    List<BatchOperation> getRecentBatchOperations(UUID userId, int limit);
}