package org.motpassants.domain.core.model;

/**
 * Value objects for upload operations.
 */
public class UploadModels {
    
    /**
     * Upload result containing processing information.
     */
    public record UploadResult(
        String bookId,
        String filename,
        String status,
        String message,
        boolean success,
        long fileSize,
        String fileHash
    ) {}
    
    /**
     * Validation result for uploaded files.
     */
    public record ValidationResult(
        boolean valid,
        String errorMessage,
        long fileSize,
        String detectedFormat
    ) {}
}