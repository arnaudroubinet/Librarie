package org.motpassants.domain.port.out;

import java.nio.file.Path;

/**
 * Port for secure file processing operations.
 */
public interface SecureFileProcessingPort {
    
    /**
     * Validates if a file is a valid book file.
     */
    boolean isValidBookFile(Path filePath);
    
    /**
     * Validates if a file is a valid image file.
     */
    boolean isValidImageFile(Path filePath);
    
    /**
     * Sanitizes a file path to prevent directory traversal attacks.
     */
    Path sanitizePath(String basePath, String relativePath);
    
    /**
     * Gets safe filename from potentially unsafe input.
     */
    String getSafeFilename(String filename);
}