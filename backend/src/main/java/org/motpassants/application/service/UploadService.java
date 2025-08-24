package org.motpassants.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.port.in.UploadUseCase;
import org.motpassants.domain.port.in.IngestUseCase;
import org.motpassants.domain.port.out.FileStorageService;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.SecureFileProcessingPort;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.LoggingPort;
import org.motpassants.domain.core.model.UploadModels;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of upload use case handling file uploads and processing.
 * Implements DATA-001: Upload & Automated Ingest Pipeline.
 */
@ApplicationScoped
public class UploadService implements UploadUseCase {
    
    private final IngestUseCase ingestUseCase;
    private final FileStorageService fileStorageService;
    private final ConfigurationPort configurationPort;
    private final SecureFileProcessingPort secureFileProcessingPort;
    private final BookRepository bookRepository;
    private final LoggingPort loggingPort;
    
    @Inject
    public UploadService(IngestUseCase ingestUseCase,
                        FileStorageService fileStorageService,
                        ConfigurationPort configurationPort,
                        SecureFileProcessingPort secureFileProcessingPort,
                        BookRepository bookRepository,
                        LoggingPort loggingPort) {
        this.ingestUseCase = ingestUseCase;
        this.fileStorageService = fileStorageService;
        this.configurationPort = configurationPort;
        this.secureFileProcessingPort = secureFileProcessingPort;
        this.bookRepository = bookRepository;
        this.loggingPort = loggingPort;
    }
    
    @Override
    public UploadModels.UploadResult processUploadedFile(InputStream inputStream, String filename, String contentType) {
        loggingPort.info("Processing uploaded file: " + filename);
        
        try {
            // Read the incoming stream once into a temp file, then use fresh streams from it
            Path uploadedTemp = createTempFile(inputStream, filename);

            UploadModels.ValidationResult validation;
            String fileHash;

            try {
                // 1) Validate using a new stream from the temp file
                try (InputStream isForValidation = Files.newInputStream(uploadedTemp)) {
                    validation = validateUploadedFile(isForValidation, filename, contentType);
                }

                if (!validation.valid()) {
                    return new UploadModels.UploadResult(null, filename, "VALIDATION_FAILED",
                        validation.errorMessage(), false, validation.fileSize(), null);
                }

                // 2) Calculate hash using a new stream from the temp file
                try (InputStream isForHash = Files.newInputStream(uploadedTemp)) {
                    fileHash = calculateFileHash(isForHash);
                }

                // 3) Check for duplicates
                if (isDuplicateFile(fileHash)) {
                    loggingPort.info("Duplicate file detected: " + filename + " (hash: " + fileHash + ")");
                    return new UploadModels.UploadResult(null, filename, "DUPLICATE",
                        "File already exists in library", false, validation.fileSize(), fileHash);
                }

                // 4) Process through ingestion using the same temp file
                String bookId = ingestUseCase.ingestSingleBook(uploadedTemp);

                if (bookId != null) {
                    return new UploadModels.UploadResult(bookId, filename, "SUCCESS",
                        "File uploaded and processed successfully", true, validation.fileSize(), fileHash);
                } else {
                    return new UploadModels.UploadResult(null, filename, "PROCESSING_FAILED",
                        "Failed to process file through ingestion pipeline", false, validation.fileSize(), fileHash);
                }

            } finally {
                // Always clean up the temp file
                try {
                    Files.deleteIfExists(uploadedTemp);
                } catch (IOException e) {
                    loggingPort.warn("Failed to clean up temporary file: " + uploadedTemp);
                }
            }
            
        } catch (Exception e) {
            loggingPort.error("Error processing uploaded file: " + filename, e);
            return new UploadModels.UploadResult(null, filename, "ERROR", 
                "Internal error: " + e.getMessage(), false, 0, null);
        }
    }
    
    @Override
    public UploadModels.ValidationResult validateUploadedFile(InputStream inputStream, String filename, String contentType) {
        try {
            // 1. Check filename and extension
            if (filename == null || filename.trim().isEmpty()) {
                return new UploadModels.ValidationResult(false, "Filename is required", 0, null);
            }
            
            String extension = getFileExtension(filename);
            if (extension == null || !getAllowedExtensions().contains(extension.toLowerCase())) {
                return new UploadModels.ValidationResult(false, 
                    "File type not allowed. Supported formats: " + String.join(", ", getAllowedExtensions()), 
                    0, extension);
            }
            
            // 2. Check file size (read into temp file first to get accurate size)
            Path tempFile = Files.createTempFile("upload-validation-", "." + extension);
            try {
                long fileSize = Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                
                if (fileSize > getMaxUploadSize()) {
                    return new UploadModels.ValidationResult(false, 
                        "File size exceeds maximum allowed size of " + (getMaxUploadSize() / 1024 / 1024) + "MB", 
                        fileSize, extension);
                }
                
                if (fileSize == 0) {
                    return new UploadModels.ValidationResult(false, "File is empty", fileSize, extension);
                }
                
                // 3. Validate file structure (basic check)
                if (!secureFileProcessingPort.isValidBookFile(tempFile)) {
                    return new UploadModels.ValidationResult(false, "File appears to be corrupted or invalid", fileSize, extension);
                }
                
                return new UploadModels.ValidationResult(true, null, fileSize, extension);
                
            } finally {
                Files.deleteIfExists(tempFile);
            }
            
        } catch (IOException e) {
            loggingPort.error("Error validating uploaded file: " + filename, e);
            return new UploadModels.ValidationResult(false, "Error reading file: " + e.getMessage(), 0, null);
        }
    }
    
    @Override
    public boolean isDuplicateFile(String fileHash) {
        try {
            return bookRepository.existsByFileHash(fileHash);
        } catch (Exception e) {
            loggingPort.error("Error checking for duplicate file with hash: " + fileHash, e);
            return false;
        }
    }
    
    @Override
    public long getMaxUploadSize() {
        return configurationPort.getStorageConfig().getMaxFileSize();
    }
    
    @Override
    public List<String> getAllowedExtensions() {
        String allowedExtensions = configurationPort.getStorageConfig().getAllowedBookExtensions();
        return Arrays.asList(allowedExtensions.split(","));
    }
    
    @Override
    public String calculateFileHash(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
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
            
        } catch (NoSuchAlgorithmException | IOException e) {
            loggingPort.error("Error calculating file hash", e);
            throw new RuntimeException("Failed to calculate file hash", e);
        }
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return null;
    }
    
    private Path createTempFile(InputStream inputStream, String filename) throws IOException {
        String extension = getFileExtension(filename);
        String prefix = "upload-" + UUID.randomUUID().toString().substring(0, 8) + "-";
        String suffix = extension != null ? "." + extension : "";
        
        Path tempFile = Files.createTempFile(prefix, suffix);
        Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        
        loggingPort.debug("Created temporary file: " + tempFile);
        return tempFile;
    }
}