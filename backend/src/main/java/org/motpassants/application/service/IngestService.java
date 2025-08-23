package org.motpassants.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.port.in.IngestUseCase;
import org.motpassants.domain.port.in.BookUseCase;
import org.motpassants.domain.port.out.FileStorageService;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.SecureFileProcessingPort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Application service implementing automated book ingest functionality.
 * Based on Calibre-Web-Automated ingest capabilities with security enhancements.
 */
@ApplicationScoped
public class IngestService implements IngestUseCase {
    
    private final BookUseCase bookUseCase;
    private final FileStorageService fileStorageService;
    private final SecureFileProcessingPort secureFileProcessingPort;
    private final ConfigurationPort configurationPort;
    
    @Inject
    public IngestService(BookUseCase bookUseCase, 
                        FileStorageService fileStorageService,
                        SecureFileProcessingPort secureFileProcessingPort,
                        ConfigurationPort configurationPort) {
        this.bookUseCase = bookUseCase;
        this.fileStorageService = fileStorageService;
        this.secureFileProcessingPort = secureFileProcessingPort;
        this.configurationPort = configurationPort;
    }
    
    @Override
    public List<String> ingestFromDirectory(String directoryPath) {
        List<String> ingestedBooks = new ArrayList<>();
        
        try {
            Path scanPath = Paths.get(directoryPath);
            if (!Files.exists(scanPath) || !Files.isDirectory(scanPath)) {
                return ingestedBooks;
            }
            
            
            try (Stream<Path> files = Files.walk(scanPath)) {
                files.filter(Files::isRegularFile)
                     .filter(this::canIngest)
                     .forEach(filePath -> {
                         try {
                             String bookId = ingestSingleBook(filePath);
                             if (bookId != null) {
                                 ingestedBooks.add(bookId);
                             }
                         } catch (Exception e) {
                         }
                     });
            }
            
            
        } catch (IOException e) {
        }
        
        return ingestedBooks;
    }
    
    @Override
    public String ingestSingleBook(Path bookPath) {
        if (!canIngest(bookPath)) {
            return null;
        }
        
        try {
            // Calculate file hash and size
            String fileHash = calculateFileHash(bookPath);
            long fileSize = Files.size(bookPath);
            
            // Extract metadata from filename and path
            String filename = bookPath.getFileName().toString();
            String titleFromFilename = extractTitleFromFilename(filename);
            
            // Create book entity using constructor
            Book book = new Book(titleFromFilename, bookPath.toString());
            
            // Set file properties
            book.setFileHash(fileHash);
            book.setFileSize(fileSize);
            
            // Try to extract additional metadata
            enhanceBookMetadata(book, bookPath);
            
            // Save the book
            Book savedBook = bookUseCase.createBook(book);
            
            return savedBook.getId().toString();
            
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public List<String> getSupportedFormats() {
        return Arrays.asList(configurationPort.getStorageConfig().getAllowedBookExtensions().split(","));
    }
    
    @Override
    public boolean canIngest(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            return false;
        }
        
        // Check if it's a valid book file
        if (!secureFileProcessingPort.isValidBookFile(filePath)) {
            return false;
        }
        
        // Check if book already exists in library
        String filename = filePath.getFileName().toString();
        String title = extractTitleFromFilename(filename);
        
        // Simple check - in real implementation, would check database
        // For now, assume we can ingest if file is valid
        return true;
    }
    
    private String extractTitleFromFilename(String filename) {
        // Remove file extension
        int lastDot = filename.lastIndexOf('.');
        String nameWithoutExtension = lastDot > 0 ? filename.substring(0, lastDot) : filename;
        
        // Replace underscores and dashes with spaces
        String title = nameWithoutExtension.replaceAll("[_-]", " ");
        
        // Capitalize first letter of each word
        String[] words = title.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
            }
        }
        
        return result.toString();
    }
    
    private String extractFormatFromFilename(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toUpperCase();
        }
        return "UNKNOWN";
    }
    
    private void enhanceBookMetadata(Book book, Path bookPath) {
        // Try to extract metadata from file content
        // For now, just set basic information
        
        try {
            long fileSize = Files.size(bookPath);
            // Could set file size information
            
            // Could try to parse ebook metadata using libraries like:
            // - Apache Tika for general metadata extraction
            // - epub4j for EPUB files
            // - iText for PDF files
            
            // For demo purposes, just set basic info using setters
            book.setDescription("Automatically ingested from: " + bookPath.getFileName());
            
        } catch (IOException e) {
        }
    }
    
    private String calculateFileHash(Path filePath) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            
            try (java.io.InputStream inputStream = Files.newInputStream(filePath)) {
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate file hash", e);
        }
    }
}