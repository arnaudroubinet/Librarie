package org.motpassants.infrastructure.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Service for secure file processing and validation.
 * Provides safety measures for file operations and content validation.
 */
@ApplicationScoped
public class SecureFileProcessingService {
    
    private static final Logger LOG = Logger.getLogger(SecureFileProcessingService.class);
    
    // Allowed file extensions for books
    private static final List<String> ALLOWED_BOOK_EXTENSIONS = Arrays.asList(
        ".pdf", ".epub", ".mobi", ".azw", ".azw3", ".fb2", ".txt", ".rtf", ".doc", ".docx"
    );
    
    // Allowed image extensions for covers
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"
    );
    
    // Maximum file size (100MB)
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;
    
    /**
     * Validates if a file is a valid book file.
     */
    public boolean isValidBookFile(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            return false;
        }
        
        try {
            // Check file size
            long fileSize = Files.size(filePath);
            if (fileSize > MAX_FILE_SIZE || fileSize <= 0) {
                LOG.warn("File size validation failed for: " + filePath);
                return false;
            }
            
            // Check file extension
            String fileName = filePath.getFileName().toString().toLowerCase();
            boolean hasValidExtension = ALLOWED_BOOK_EXTENSIONS.stream()
                .anyMatch(fileName::endsWith);
            
            if (!hasValidExtension) {
                LOG.warn("Invalid book file extension: " + fileName);
                return false;
            }
            
            // Basic content validation
            return isValidFileContent(filePath);
            
        } catch (IOException e) {
            LOG.error("Error validating book file: " + filePath, e);
            return false;
        }
    }
    
    /**
     * Validates if a file is a valid image file.
     */
    public boolean isValidImageFile(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            return false;
        }
        
        try {
            // Check file size (max 10MB for images)
            long fileSize = Files.size(filePath);
            if (fileSize > 10 * 1024 * 1024 || fileSize <= 0) {
                LOG.warn("Image file size validation failed for: " + filePath);
                return false;
            }
            
            // Check file extension
            String fileName = filePath.getFileName().toString().toLowerCase();
            boolean hasValidExtension = ALLOWED_IMAGE_EXTENSIONS.stream()
                .anyMatch(fileName::endsWith);
            
            if (!hasValidExtension) {
                LOG.warn("Invalid image file extension: " + fileName);
                return false;
            }
            
            return true;
            
        } catch (IOException e) {
            LOG.error("Error validating image file: " + filePath, e);
            return false;
        }
    }
    
    /**
     * Sanitizes a file path to prevent directory traversal attacks.
     */
    public Path sanitizePath(String basePath, String relativePath) {
        if (basePath == null || relativePath == null) {
            throw new IllegalArgumentException("Base path and relative path cannot be null");
        }
        
        // Normalize paths and prevent directory traversal
        Path base = Paths.get(basePath).normalize();
        Path relative = Paths.get(relativePath).normalize();
        
        // Ensure relative path doesn't contain ".."
        if (relative.toString().contains("..")) {
            throw new SecurityException("Path traversal attempt detected: " + relativePath);
        }
        
        Path resolved = base.resolve(relative).normalize();
        
        // Ensure resolved path is within base directory
        if (!resolved.startsWith(base)) {
            throw new SecurityException("Path outside base directory: " + resolved);
        }
        
        return resolved;
    }
    
    /**
     * Basic file content validation to detect potentially malicious files.
     */
    private boolean isValidFileContent(Path filePath) {
        try {
            // Read first few bytes to check for malicious content
            byte[] header = Files.readAllBytes(filePath);
            if (header.length > 1024) {
                header = Arrays.copyOf(header, 1024);
            }
            
            // Convert to string for basic checks
            String headerStr = new String(header).toLowerCase();
            
            // Check for script tags or executable content
            if (headerStr.contains("<script") || 
                headerStr.contains("javascript:") ||
                headerStr.contains("vbscript:") ||
                headerStr.contains("data:text/html")) {
                LOG.warn("Potentially malicious content detected in file: " + filePath);
                return false;
            }
            
            return true;
            
        } catch (IOException e) {
            LOG.error("Error reading file content for validation: " + filePath, e);
            return false;
        }
    }
    
    /**
     * Gets safe filename from potentially unsafe input.
     */
    public String getSafeFilename(String filename) {
        if (filename == null) {
            return null;
        }
        
        // Remove path separators and dangerous characters
        return filename.replaceAll("[/\\\\:*?\"<>|]", "_")
                      .replaceAll("\\s+", "_")
                      .trim();
    }
}