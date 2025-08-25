package org.motpassants.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.port.in.IngestUseCase;
import org.motpassants.domain.port.in.BookUseCase;
import org.motpassants.domain.port.out.FileStorageService;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.SecureFileProcessingPort;
import org.motpassants.domain.port.out.LoggingPort;
import org.motpassants.domain.port.out.readium.EpubPublicationPort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    private final SecureFileProcessingPort secureFileProcessingPort;
    private final ConfigurationPort configurationPort;
    private final EpubPublicationPort epubService;
    private final LoggingPort loggingPort;

    // Allowed language codes seeded by migration (V1.0.0__Initial_schema.sql)
    private static final Set<String> ALLOWED_LANGUAGE_CODES = Set.of(
        "en-US","en-GB","pl-PL","fr-FR","fr-CA","es-ES","es-MX","de-DE","it-IT",
        "pt-PT","pt-BR","ru-RU","zh-CN","zh-TW","ja-JP","ko-KR","ar-SA","he-IL"
    );

    // Default region mapping for base languages
    private static final Map<String, String> DEFAULT_REGION_FOR_BASE = Map.ofEntries(
        Map.entry("en", "en-US"),
        Map.entry("fr", "fr-FR"),
        Map.entry("es", "es-ES"),
        Map.entry("de", "de-DE"),
        Map.entry("it", "it-IT"),
        Map.entry("pt", "pt-PT"),
        Map.entry("ru", "ru-RU"),
        Map.entry("zh", "zh-CN"),
        Map.entry("ja", "ja-JP"),
        Map.entry("ko", "ko-KR"),
        Map.entry("ar", "ar-SA"),
        Map.entry("he", "he-IL"),
        Map.entry("pl", "pl-PL")
    );
    
    @Inject
    public IngestService(BookUseCase bookUseCase, 
                        FileStorageService fileStorageService,
                        SecureFileProcessingPort secureFileProcessingPort,
                        ConfigurationPort configurationPort,
                        EpubPublicationPort epubService,
                        LoggingPort loggingPort) {
        this.bookUseCase = bookUseCase;
        this.fileStorageService = fileStorageService;
        this.secureFileProcessingPort = secureFileProcessingPort;
        this.configurationPort = configurationPort;
        this.epubService = epubService;
        this.loggingPort = loggingPort;
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
            loggingPort.warn("Ingest rejected by canIngest for: " + bookPath);
            return null;
        }
        
        try {
            // Calculate file hash and size
            String fileHash = calculateFileHash(bookPath);
            long fileSize = Files.size(bookPath);
            
            // Extract metadata from filename and path
        String titleFromFilename = extractTitleFromFilename(bookPath.getFileName().toString());
            
            // Create book entity using constructor
            Book book = new Book(titleFromFilename, bookPath.toString());
            
            // Set file properties
            book.setFileHash(fileHash);
            book.setFileSize(fileSize);
            
            // Try to extract additional metadata from EPUB OPF
            try {
                var pubOpt = epubService.openPublication(bookPath);
                if (pubOpt.isPresent()) {
                    var pub = pubOpt.get();
                    var core = epubService.extractCoreMetadata(pub).orElse(null);
                    if (core != null) {
                        if (core.title() != null && (book.getTitle() == null || book.getTitle().isBlank())) book.setTitle(core.title());
                        if (core.language() != null) {
                            String normalized = normalizeLanguageCode(core.language());
                            if (normalized != null) {
                                if (!normalized.equals(core.language())) {
                                    loggingPort.debug("Normalized language '" + core.language() + "' -> '" + normalized + "'");
                                }
                                book.setLanguage(normalized);
                            } else {
                                loggingPort.warn("Unknown or unsupported language code '" + core.language() + "' - skipping to avoid FK violation");
                            }
                        }
                        if (core.description() != null) book.setDescription(core.description());
                        if (core.isbn() != null && (book.getIsbn() == null || book.getIsbn().isBlank())) book.setIsbn(core.isbn());
                    }
                    // Cover: try OPF-declared cover image first; if missing, fallback to first spine resource snapshot is out-of-scope here
                    var coverZipPathOpt = epubService.findCoverImageZipPath(pub);
                    if (coverZipPathOpt.isPresent()) {
                        var bytesOpt = epubService.readEntryBytes(pub, coverZipPathOpt.get());
                        if (bytesOpt.isPresent()) {
                            // Store cover at books/covers/<bookId> after we know the ID; temporarily stash bytes
                            // We'll set hasCover=true after saving the Book to get its id
                            // To avoid re-opening EPUB later, keep in memory for this ingestion
                            byte[] coverBytes = bytesOpt.get();
                            // Mark a flag via metadata map until saved
                            java.util.Map<String,Object> meta = book.getMetadata() != null ? new java.util.LinkedHashMap<>(book.getMetadata()) : new java.util.LinkedHashMap<>();
                            meta.put("__epubCoverBytes", coverBytes);
                            book.setMetadata(meta);
                        }
                    } else {
                        // Fallback to first-page image extraction
                        var firstImgZipPath = epubService.findFirstPageImageZipPath(pub);
                        if (firstImgZipPath.isPresent()) {
                            var bytesOpt = epubService.readEntryBytes(pub, firstImgZipPath.get());
                            if (bytesOpt.isPresent()) {
                                byte[] coverBytes = bytesOpt.get();
                                java.util.Map<String,Object> meta = book.getMetadata() != null ? new java.util.LinkedHashMap<>(book.getMetadata()) : new java.util.LinkedHashMap<>();
                                meta.put("__epubCoverBytes", coverBytes);
                                book.setMetadata(meta);
                            }
                        }
                    }
                } // else non-EPUB: we keep filename-derived basics only
            } catch (Exception metaEx) {
                loggingPort.warn("EPUB metadata extraction failed for: " + bookPath + " - " + metaEx.getMessage());
            }
            
            // Save the book
            Book savedBook = bookUseCase.createBook(book);

            // Persist cover to filesystem if we captured it
            try {
                var md = savedBook.getMetadata();
                if (md != null && md.containsKey("__epubCoverBytes")) {
                    byte[] bytes = (byte[]) md.remove("__epubCoverBytes");
                    // Save to storage at books/covers/<bookId>
                    String relPath = "books/covers/" + savedBook.getId();
                    fileStorageService.storeFile(bytes, relPath);
                    savedBook.setHasCover(true);
                    // Persist updated hasCover flag
                    bookUseCase.updateBook(savedBook);
                }
            } catch (Exception coverEx) {
                loggingPort.warn("Cover persistence failed for: " + bookPath + " - " + coverEx.getMessage());
            }
            
            return savedBook.getId().toString();
            
        } catch (Exception e) {
            loggingPort.error("Ingestion failed for: " + bookPath, e);
            return null;
        }
    }

    /**
     * Normalize EPUB language string to an allowed language code present in the reference table.
     * - Converts underscores to hyphens (en_US -> en-US)
     * - Lowercases language and uppercases region (en-us -> en-US)
     * - Maps base codes (en, fr, ...) to default regions (en-US, fr-FR, ...)
     * - Returns null if resulting code is not in ALLOWED_LANGUAGE_CODES
     */
    private String normalizeLanguageCode(String input) {
        if (input == null || input.isBlank()) return null;
        String v = input.trim().replace('_', '-');
        String lower = v.toLowerCase();

        String candidate;
        if (lower.contains("-")) {
            String[] parts = lower.split("-", 2);
            String lang = parts[0];
            String region = parts[1];
            if (lang.length() == 2 && region.length() >= 2) {
                candidate = lang + "-" + region.toUpperCase();
            } else {
                // Not a simple ll-CC form; try base mapping
                candidate = DEFAULT_REGION_FOR_BASE.getOrDefault(lang, null);
            }
        } else if (lower.length() == 2) {
            candidate = DEFAULT_REGION_FOR_BASE.getOrDefault(lower, null);
        } else {
            candidate = null;
        }

        if (candidate != null && ALLOWED_LANGUAGE_CODES.contains(candidate)) {
            return candidate;
        }

        // As a last attempt, if candidate is null but base maps to default, check that
        if (candidate == null && lower.length() >= 2) {
            String base = lower.substring(0, 2);
            String def = DEFAULT_REGION_FOR_BASE.get(base);
            if (def != null && ALLOWED_LANGUAGE_CODES.contains(def)) return def;
        }

        return null;
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
    // Title from filename already applied at book creation stage
        
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
    
    // extractFormatFromFilename removed as unused
    
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