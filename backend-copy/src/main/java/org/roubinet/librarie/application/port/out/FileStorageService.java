package org.roubinet.librarie.application.port.out;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Secondary port for file storage operations.
 * Handles physical file management for the library.
 */
public interface FileStorageService {
    
    /**
     * Store a file in the library structure.
     * 
     * @param sourceFile the source file to store
     * @param targetPath the target path within the library
     * @return the final stored file path
     */
    Path storeFile(Path sourceFile, Path targetPath);
    
    /**
     * Move a file from ingest directory to permanent storage.
     * 
     * @param ingestFile the file in the ingest directory
     * @param libraryPath the target path in the library
     * @return the final library path
     */
    Path moveToLibrary(Path ingestFile, Path libraryPath);
    
    /**
     * Delete a file from storage.
     * 
     * @param filePath the file to delete
     * @return true if deletion was successful
     */
    boolean deleteFile(Path filePath);
    
    /**
     * Check if a file exists.
     * 
     * @param filePath the file path to check
     * @return true if the file exists
     */
    boolean fileExists(Path filePath);
    
    /**
     * Get the file size in bytes.
     * 
     * @param filePath the file path
     * @return file size in bytes, or empty if file doesn't exist
     */
    Optional<Long> getFileSize(Path filePath);
    
    /**
     * Calculate MD5 hash of a file.
     * 
     * @param filePath the file path
     * @return the MD5 hash, or empty if file doesn't exist
     */
    Optional<String> calculateFileHash(Path filePath);
    
    /**
     * Create a backup copy of a file before processing.
     * Based on CWA's backup functionality.
     * 
     * @param originalFile the file to backup
     * @return the backup file path
     */
    Path createBackup(Path originalFile);
    
    /**
     * Determine the correct library path structure for a book.
     * Follows Calibre's author/title directory structure.
     * 
     * @param authorName the book's author
     * @param title the book's title
     * @param fileName the original file name
     * @return the suggested library path
     */
    Path generateLibraryPath(String authorName, String title, String fileName);
}