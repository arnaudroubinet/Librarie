package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.UploadModels;

import java.io.InputStream;
import java.util.List;

/**
 * Use case for handling file uploads and processing them through the ingestion pipeline.
 * Implements the DATA-001 requirements for Upload & Automated Ingest Pipeline.
 */
public interface UploadUseCase {
    
    /**
     * Processes an uploaded file through the complete ingestion pipeline.
     * 
     * @param inputStream The uploaded file stream
     * @param filename Original filename
     * @param contentType MIME content type
     * @return Upload result with processing details
     */
    UploadModels.UploadResult processUploadedFile(InputStream inputStream, String filename, String contentType);
    
    /**
     * Validates an uploaded file before processing.
     * 
     * @param inputStream The file stream to validate
     * @param filename Original filename
     * @param contentType MIME content type
     * @return Validation result
     */
    UploadModels.ValidationResult validateUploadedFile(InputStream inputStream, String filename, String contentType);
    
    /**
     * Checks if a file with the given hash already exists in the library.
     * 
     * @param fileHash SHA256 hash of the file
     * @return true if file already exists
     */
    boolean isDuplicateFile(String fileHash);
    
    /**
     * Gets the maximum allowed file size for uploads.
     * 
     * @return Maximum file size in bytes
     */
    long getMaxUploadSize();
    
    /**
     * Gets the list of allowed file extensions for upload.
     * 
     * @return List of allowed extensions (without dots)
     */
    List<String> getAllowedExtensions();
    
    /**
     * Calculates SHA256 hash of a file.
     * 
     * @param inputStream The file stream
     * @return SHA256 hash as hexadecimal string
     */
    String calculateFileHash(InputStream inputStream);
}