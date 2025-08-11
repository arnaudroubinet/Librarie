package org.roubinet.librarie.application.service;

import org.roubinet.librarie.application.port.in.IngestUseCase;
import org.roubinet.librarie.application.port.in.BookUseCase;
import org.roubinet.librarie.application.port.out.FileStorageService;
import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.infrastructure.config.LibrarieConfigProperties;
import org.roubinet.librarie.infrastructure.security.SecureFileProcessingService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Application service implementing automated book ingest functionality.
 * Based on Calibre-Web-Automated ingest capabilities with security enhancements.
 */
@ApplicationScoped
public class IngestService implements IngestUseCase {
    
    private final BookUseCase bookUseCase;
    private final FileStorageService fileStorageService;
    private final SecureFileProcessingService secureFileProcessingService;
    private final LibrarieConfigProperties config;
    
    @Inject
    public IngestService(BookUseCase bookUseCase, 
                        FileStorageService fileStorageService,
                        SecureFileProcessingService secureFileProcessingService,
                        LibrarieConfigProperties config) {
        this.bookUseCase = bookUseCase;
        this.fileStorageService = fileStorageService;
        this.secureFileProcessingService = secureFileProcessingService;
        this.config = config;
    }
    
    @Override
    public String ingestFile(Path filePath) {
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }
        
        try {
            // Validate file security before processing
            secureFileProcessingService.validateFile(filePath);
        } catch (SecurityException | IOException e) {
            throw new IllegalArgumentException("File security validation failed: " + e.getMessage(), e);
        }
        
        String fileName = filePath.getFileName().toString();
        String extension = secureFileProcessingService.getFileExtension(fileName);
        
        if (!isSupportedFormat(extension)) {
            throw new IllegalArgumentException("Unsupported format: " + extension);
        }
        
        try {
            // Extract basic metadata from filename
            // In a full implementation, this would use proper metadata extraction
            String title = extractTitleFromFilename(fileName);
            String authorName = "Unknown Author"; // Would be extracted from metadata
            
            // Generate library path following Calibre conventions
            Path libraryPath = fileStorageService.generateLibraryPath(authorName, title, fileName);
            
            // Move file to library
            Path finalPath = fileStorageService.moveToLibrary(filePath, libraryPath);
            
            // Calculate file size and hash
            Optional<Long> fileSize = fileStorageService.getFileSize(finalPath);
            Optional<String> fileHash = fileStorageService.calculateFileHash(finalPath);
            
            // Create book entity
            Book book = new Book();
            book.setTitle(title);
            book.setPath(finalPath.toString());
            book.setFileSize(fileSize.orElse(null));
            book.setFileHash(fileHash.orElse(null));
            book.setHasCover(false); // Would be detected from actual file
            book.setCreatedAt(OffsetDateTime.now());
            book.setUpdatedAt(OffsetDateTime.now());
            
            Book savedBook = bookUseCase.createBook(book);
            return savedBook.getId().toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest file: " + filePath, e);
        }
    }
    
    @Override
    public List<String> ingestDirectory(Path directoryPath) {
        if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
            throw new IllegalArgumentException("Directory does not exist: " + directoryPath);
        }
        
        List<String> ingestedBooks = new ArrayList<>();
        
        try (Stream<Path> files = Files.walk(directoryPath)) {
            files.filter(Files::isRegularFile)
                 .filter(file -> isSupportedFormat(secureFileProcessingService.getFileExtension(file.getFileName().toString())))
                 .forEach(file -> {
                     try {
                         String bookId = ingestFile(file);
                         ingestedBooks.add(bookId);
                     } catch (Exception e) {
                         // Log error but continue with other files
                         System.err.println("Failed to ingest file: " + file + " - " + e.getMessage());
                     }
                 });
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan directory: " + directoryPath, e);
        }
        
        return ingestedBooks;
    }
    
    @Override
    public List<String> scanIngestDirectory() {
        // In a real implementation, this would get the configured ingest directory from settings
        // For now, returning empty list as placeholder
        return new ArrayList<>();
    }
    
    @Override
    public boolean isSupportedFormat(String fileExtension) {
        if (fileExtension == null) {
            return false;
        }
        return secureFileProcessingService.getAllowedExtensions().contains(fileExtension.toLowerCase());
    }
    
    @Override
    public List<String> getSupportedFormats() {
        return new ArrayList<>(secureFileProcessingService.getAllowedExtensions());
    }
    
    @Override
    public int refreshLibrary() {
        // Placeholder for manual library refresh functionality
        // Would scan the configured ingest directory and process any files found
        return 0;
    }
    
    /**
     * Extract title from filename by removing extension and cleaning up.
     */
    private String extractTitleFromFilename(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        String title = lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
        
        // Clean up common filename patterns
        title = title.replaceAll("_", " ");
        title = title.replaceAll("\\s+", " ");
        title = title.trim();
        
        return title.isEmpty() ? "Unknown Title" : title;
    }
}