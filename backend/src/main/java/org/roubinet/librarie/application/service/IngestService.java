package org.roubinet.librarie.application.service;

import org.roubinet.librarie.application.port.in.IngestUseCase;
import org.roubinet.librarie.application.port.in.BookUseCase;
import org.roubinet.librarie.application.port.out.FileStorageService;
import org.roubinet.librarie.domain.entity.Book;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Application service implementing automated book ingest functionality.
 * Based on Calibre-Web-Automated ingest capabilities.
 */
@ApplicationScoped
public class IngestService implements IngestUseCase {
    
    // Supported formats based on CWA documentation
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
        "epub", "mobi", "azw", "azw3", "azw4", "cbz", "cbr", "cb7", "cbc", 
        "chm", "djvu", "docx", "fb2", "fbz", "html", "htmlz", "lit", "lrf", 
        "odt", "pdf", "prc", "pdb", "pml", "rb", "rtf", "snb", "tcr", "txtz"
    );
    
    private final BookUseCase bookUseCase;
    private final FileStorageService fileStorageService;
    
    @Inject
    public IngestService(BookUseCase bookUseCase, FileStorageService fileStorageService) {
        this.bookUseCase = bookUseCase;
        this.fileStorageService = fileStorageService;
    }
    
    @Override
    public String ingestFile(Path filePath) {
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }
        
        String fileName = filePath.getFileName().toString();
        String extension = getFileExtension(fileName);
        
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
                 .filter(file -> isSupportedFormat(getFileExtension(file.getFileName().toString())))
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
        return SUPPORTED_FORMATS.contains(fileExtension.toLowerCase());
    }
    
    @Override
    public List<String> getSupportedFormats() {
        return new ArrayList<>(SUPPORTED_FORMATS);
    }
    
    @Override
    public int refreshLibrary() {
        // Placeholder for manual library refresh functionality
        // Would scan the configured ingest directory and process any files found
        return 0;
    }
    
    /**
     * Extract file extension from filename.
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
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