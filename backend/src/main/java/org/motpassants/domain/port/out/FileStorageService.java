package org.motpassants.domain.port.out;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Port for file storage operations.
 */
public interface FileStorageService {
    
    /**
     * Stores a file in the configured storage location.
     */
    Path storeFile(byte[] content, String relativePath) throws IOException;
    
    /**
     * Retrieves a file from storage.
     */
    byte[] retrieveFile(String relativePath) throws IOException;
    
    /**
     * Checks if a file exists in storage.
     */
    boolean fileExists(String relativePath);
    
    /**
     * Deletes a file from storage.
     */
    boolean deleteFile(String relativePath) throws IOException;
    
    /**
     * Lists files in a directory.
     */
    List<String> listFiles(String directoryPath) throws IOException;
    
    /**
     * Gets the absolute path for a relative path.
     */
    Path getAbsolutePath(String relativePath);
    
    /**
     * Creates directory structure if it doesn't exist.
     */
    void ensureDirectoryExists(String directoryPath) throws IOException;
}