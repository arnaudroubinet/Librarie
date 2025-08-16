package org.motpassants.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.motpassants.domain.port.in.IngestUseCase;
import org.motpassants.domain.port.in.BookUseCase;
import org.motpassants.domain.port.out.FileStorageService;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.SecureFileProcessingPort;
import org.motpassants.infrastructure.config.LibrarieConfigProperties;

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
    
    private static final Logger LOG = Logger.getLogger(IngestService.class);
    
    private final BookUseCase bookUseCase;
    private final FileStorageService fileStorageService;
    private final SecureFileProcessingPort secureFileProcessingPort;
    private final LibrarieConfigProperties config;
    
    @Inject
    public IngestService(BookUseCase bookUseCase, 
                        FileStorageService fileStorageService,
                        SecureFileProcessingPort secureFileProcessingPort,
                        LibrarieConfigProperties config) {
        this.bookUseCase = bookUseCase;
        this.fileStorageService = fileStorageService;
        this.secureFileProcessingPort = secureFileProcessingPort;
        this.config = config;
    }
    
    @Override
    public List<String> ingestFromDirectory(String directoryPath) {
        List<String> ingestedBooks = new ArrayList<>();
        
        try {
            Path scanPath = Paths.get(directoryPath);
            if (!Files.exists(scanPath) || !Files.isDirectory(scanPath)) {
                LOG.warn("Invalid directory path for ingestion: " + directoryPath);
                return ingestedBooks;
            }
            
            LOG.info("Starting directory ingestion from: " + directoryPath);
            
            try (Stream<Path> files = Files.walk(scanPath)) {
                files.filter(Files::isRegularFile)
                     .filter(this::canIngest)
                     .forEach(filePath -> {
                         try {
                             String bookId = ingestSingleBook(filePath);
                             if (bookId != null) {
                                 ingestedBooks.add(bookId);
                                 LOG.info("Successfully ingested: " + filePath.getFileName());
                             }
                         } catch (Exception e) {
                             LOG.error("Failed to ingest file: " + filePath, e);
                         }
                     });
            }
            
            LOG.info("Directory ingestion completed. Ingested " + ingestedBooks.size() + " books");
            
        } catch (IOException e) {
            LOG.error("Error during directory ingestion: " + directoryPath, e);
        }
        
        return ingestedBooks;
    }
    
    @Override
    public String ingestSingleBook(Path bookPath) {
        if (!canIngest(bookPath)) {
            LOG.warn("Cannot ingest file: " + bookPath);
            return null;
        }
        
        try {
            // Extract metadata from filename and path
            String filename = bookPath.getFileName().toString();
            String titleFromFilename = extractTitleFromFilename(filename);
            
            // Create book entity using constructor
            Book book = new Book(titleFromFilename, bookPath.toString());
            // Note: Book model doesn't have the update methods used here
            // Would need to use setters or add the methods to Book model if needed
            
            // Try to extract additional metadata
            enhanceBookMetadata(book, bookPath);
            
            // Save the book
            Book savedBook = bookUseCase.createBook(book);
            
            LOG.info("Successfully ingested book: " + titleFromFilename + " (ID: " + savedBook.getId() + ")");
            return savedBook.getId().toString();
            
        } catch (Exception e) {
            LOG.error("Failed to ingest book: " + bookPath, e);
            return null;
        }
    }
    
    @Override
    public List<String> getSupportedFormats() {
        return Arrays.asList(config.storage().allowedBookExtensions().split(","));
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
            LOG.warn("Could not enhance metadata for: " + bookPath, e);
        }
    }
}