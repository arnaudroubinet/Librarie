package org.motpassants.domain.port.out;

import org.motpassants.domain.core.model.BatchOperation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for batch operation persistence.
 * Secondary port (driven port) for storing and retrieving batch operations.
 * Pure domain interface without infrastructure dependencies.
 */
public interface BatchOperationRepository {
    
    /**
     * Save a batch operation.
     * 
     * @param operation the operation to save
     * @return the saved operation
     */
    BatchOperation save(BatchOperation operation);
    
    /**
     * Find a batch operation by ID.
     * 
     * @param operationId the operation ID
     * @return optional containing the operation if found
     */
    Optional<BatchOperation> findById(UUID operationId);
    
    /**
     * Find recent batch operations for a user.
     * 
     * @param userId the user ID
     * @param limit maximum number of operations to return
     * @return list of recent operations ordered by creation date descending
     */
    List<BatchOperation> findRecentByUserId(UUID userId, int limit);
    
    /**
     * Update the status of a batch operation.
     * 
     * @param operationId the operation ID
     * @param operation the updated operation
     * @return the updated operation
     */
    BatchOperation update(UUID operationId, BatchOperation operation);
    
    /**
     * Delete old batch operations for cleanup.
     * 
     * @param olderThanDays operations older than this many days will be deleted
     * @return number of operations deleted
     */
    int deleteOldOperations(int olderThanDays);
}