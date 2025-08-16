package org.roubinet.librarie.infrastructure.adapter.out.storage;

import org.roubinet.librarie.application.port.out.FileStorageService;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * File system implementation of FileStorageService.
 * Handles physical file operations for the library.
 */
@ApplicationScoped
public class FileSystemStorageAdapter implements FileStorageService {
    
    @Override
    public Path storeFile(Path sourceFile, Path targetPath) {
        try {
            // Ensure target directory exists
            Files.createDirectories(targetPath.getParent());
            
            // Copy file to target location
            return Files.copy(sourceFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + sourceFile + " to " + targetPath, e);
        }
    }
    
    @Override
    public Path moveToLibrary(Path ingestFile, Path libraryPath) {
        try {
            // Ensure target directory exists
            Files.createDirectories(libraryPath.getParent());
            
            // Move file to library location
            return Files.move(ingestFile, libraryPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move file: " + ingestFile + " to " + libraryPath, e);
        }
    }
    
    @Override
    public boolean deleteFile(Path filePath) {
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + filePath + " - " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean fileExists(Path filePath) {
        return Files.exists(filePath);
    }
    
    @Override
    public Optional<Long> getFileSize(Path filePath) {
        try {
            if (Files.exists(filePath)) {
                return Optional.of(Files.size(filePath));
            }
            return Optional.empty();
        } catch (IOException e) {
            System.err.println("Failed to get file size for: " + filePath + " - " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<String> calculateFileHash(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                return Optional.empty();
            }
            
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = md5.digest(fileBytes);
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return Optional.of(hexString.toString());
        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("Failed to calculate hash for: " + filePath + " - " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public Path createBackup(Path originalFile) {
        try {
            // Create backup directory structure
            Path backupDir = Paths.get("backups")
                .resolve(java.time.LocalDate.now().toString());
            Files.createDirectories(backupDir);
            
            String fileName = originalFile.getFileName().toString();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String backupFileName = timestamp + "_" + fileName;
            
            Path backupPath = backupDir.resolve(backupFileName);
            
            return Files.copy(originalFile, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create backup for: " + originalFile, e);
        }
    }
    
    @Override
    public Path generateLibraryPath(String authorName, String title, String fileName) {
        // Sanitize names for file system
        String sanitizedAuthor = sanitizeFileName(authorName);
        String sanitizedTitle = sanitizeFileName(title);
        
        // Follow Calibre's directory structure: Author/Title/filename
        return Paths.get("library")
            .resolve(sanitizedAuthor)
            .resolve(sanitizedTitle)
            .resolve(fileName);
    }
    
    /**
     * Sanitize a string for use as a file/directory name.
     */
    private String sanitizeFileName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Unknown";
        }
        
        // Replace problematic characters with underscores
        String sanitized = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        
        // Trim and limit length
        sanitized = sanitized.trim();
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }
        
        return sanitized.isEmpty() ? "Unknown" : sanitized;
    }
}