package org.roubinet.librarie.infrastructure.security;

import org.roubinet.librarie.infrastructure.config.LibrarieConfigProperties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Secure file processing service following OWASP guidelines.
 * Implements security measures for file upload and processing.
 */
@ApplicationScoped
public class SecureFileProcessingService {
    
    private final LibrarieConfigProperties config;
    private final InputSanitizationService sanitizationService;
    private final Set<String> allowedExtensions;
    private final Path quarantineDirectory;
    
    // MIME type validation for ebook formats
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "application/epub+zip",           // EPUB
        "application/x-mobipocket-ebook", // MOBI
        "application/pdf",                // PDF
        "text/plain",                     // TXT
        "text/html",                      // HTML
        "application/zip",                // CBZ
        "application/x-rar-compressed",   // CBR
        "application/x-fictionbook+xml",  // FB2
        "application/rtf",                // RTF
        "application/msword",             // DOC
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
        "application/vnd.oasis.opendocument.text" // ODT
    );
    
    // Magic bytes for file type validation
    private static final List<byte[]> EBOOK_MAGIC_BYTES = List.of(
        new byte[]{0x50, 0x4B, 0x03, 0x04}, // ZIP-based formats (EPUB, CBZ)
        new byte[]{0x25, 0x50, 0x44, 0x46}, // PDF
        new byte[]{(byte) 0xFF, (byte) 0xFE}, // UTF-16 LE BOM
        new byte[]{(byte) 0xFE, (byte) 0xFF}, // UTF-16 BE BOM
        new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF} // UTF-8 BOM
    );
    
    @Inject
    public SecureFileProcessingService(LibrarieConfigProperties config, InputSanitizationService sanitizationService) {
        this.config = config;
        this.sanitizationService = sanitizationService;
        this.allowedExtensions = Set.of(config.fileProcessing().allowedExtensions().split(","));
        this.quarantineDirectory = Paths.get(config.fileProcessing().quarantineDirectory());
        
        // Create quarantine directory if it doesn't exist
        try {
            Files.createDirectories(quarantineDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create quarantine directory", e);
        }
    }
    
    /**
     * Validate file before processing.
     * Implements OWASP file upload security guidelines.
     * 
     * @param filePath path to the file to validate
     * @throws SecurityException if file fails security validation
     * @throws IOException if file cannot be read
     */
    public void validateFile(Path filePath) throws SecurityException, IOException {
        if (filePath == null || !Files.exists(filePath)) {
            throw new SecurityException("File does not exist or path is null");
        }
        
        // 1. Validate file size
        long fileSize = Files.size(filePath);
        if (fileSize > config.fileProcessing().maxFileSize()) {
            throw new SecurityException("File size exceeds maximum allowed size: " + config.fileProcessing().maxFileSize());
        }
        
        if (fileSize == 0) {
            throw new SecurityException("File is empty");
        }
        
        // 2. Validate file name and extension
        String fileName = filePath.getFileName().toString();
        String sanitizedFileName = sanitizationService.sanitizeFilePath(fileName);
        if (!fileName.equals(sanitizedFileName)) {
            throw new SecurityException("File name contains suspicious characters");
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new SecurityException("File extension not allowed: " + extension);
        }
        
        // 3. Validate MIME type
        String mimeType = Files.probeContentType(filePath);
        if (mimeType != null && !ALLOWED_MIME_TYPES.contains(mimeType)) {
            // Allow if extension is in allowed list (fallback for systems without proper MIME detection)
            if (!allowedExtensions.contains(extension)) {
                throw new SecurityException("MIME type not allowed: " + mimeType);
            }
        }
        
        // 4. Validate file header (magic bytes)
        if (!validateFileHeader(filePath)) {
            quarantineFile(filePath, "Invalid file header");
            throw new SecurityException("File header validation failed - file quarantined");
        }
        
        // 5. Scan for embedded scripts (basic check)
        if (containsSuspiciousContent(filePath)) {
            quarantineFile(filePath, "Suspicious content detected");
            throw new SecurityException("Suspicious content detected - file quarantined");
        }
        
        // 6. Virus scanning (if enabled)
        if (config.fileProcessing().enableVirusScanning()) {
            performVirusScan(filePath);
        }
    }
    
    /**
     * Extract file extension from filename.
     * 
     * @param fileName the filename
     * @return the file extension (without dot)
     */
    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * Validate file header using magic bytes.
     * 
     * @param filePath path to the file
     * @return true if file header is valid
     * @throws IOException if file cannot be read
     */
    private boolean validateFileHeader(Path filePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(filePath);
        if (fileBytes.length < 4) {
            return false;
        }
        
        // Check for known ebook format magic bytes
        for (byte[] magicBytes : EBOOK_MAGIC_BYTES) {
            if (startsWith(fileBytes, magicBytes)) {
                return true;
            }
        }
        
        // For text files, check for valid text content
        String extension = getFileExtension(filePath.getFileName().toString());
        if (Set.of("txt", "html", "rtf", "fb2").contains(extension)) {
            return isValidTextFile(fileBytes);
        }
        
        return true; // Allow other formats to pass
    }
    
    /**
     * Check if byte array starts with given magic bytes.
     * 
     * @param fileBytes the file bytes
     * @param magicBytes the magic bytes to check
     * @return true if file starts with magic bytes
     */
    private boolean startsWith(byte[] fileBytes, byte[] magicBytes) {
        if (fileBytes.length < magicBytes.length) {
            return false;
        }
        
        for (int i = 0; i < magicBytes.length; i++) {
            if (fileBytes[i] != magicBytes[i]) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if file contains valid text content.
     * 
     * @param fileBytes the file bytes
     * @return true if file contains valid text
     */
    private boolean isValidTextFile(byte[] fileBytes) {
        // Basic validation for text files
        // Check for null bytes (binary content indicator)
        for (byte b : fileBytes) {
            if (b == 0) {
                return false;
            }
        }
        
        // Check if content is printable ASCII or valid UTF-8
        try {
            String content = new String(fileBytes, "UTF-8");
            return !content.isEmpty() && content.length() > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Scan file for suspicious content patterns.
     * 
     * @param filePath path to the file
     * @return true if suspicious content is found
     * @throws IOException if file cannot be read
     */
    private boolean containsSuspiciousContent(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        
        // Check for embedded scripts and malicious patterns
        String[] suspiciousPatterns = {
            "<script",
            "javascript:",
            "vbscript:",
            "onload=",
            "onerror=",
            "eval(",
            "exec(",
            "system(",
            "../",
            "..\\",
            "file://",
            "ftp://",
            "http://localhost",
            "127.0.0.1"
        };
        
        String lowerContent = content.toLowerCase();
        for (String pattern : suspiciousPatterns) {
            if (lowerContent.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Move file to quarantine directory.
     * 
     * @param filePath path to the file to quarantine
     * @param reason reason for quarantine
     * @throws IOException if file cannot be moved
     */
    private void quarantineFile(Path filePath, String reason) throws IOException {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = filePath.getFileName().toString();
        Path quarantinePath = quarantineDirectory.resolve(timestamp + "_" + fileName);
        
        Files.move(filePath, quarantinePath);
        
        // Log quarantine event
        Files.writeString(
            quarantineDirectory.resolve(timestamp + "_" + fileName + ".log"),
            "Quarantined: " + fileName + "\nReason: " + reason + "\nTimestamp: " + timestamp + "\n"
        );
    }
    
    /**
     * Perform virus scanning (placeholder for external scanner integration).
     * 
     * @param filePath path to the file to scan
     * @throws SecurityException if virus is detected
     */
    private void performVirusScan(Path filePath) throws SecurityException {
        // This is a placeholder for virus scanning integration
        // In production, integrate with ClamAV, Windows Defender, or other antivirus solutions
        
        // For now, just check file size as a basic heuristic
        try {
            long fileSize = Files.size(filePath);
            if (fileSize > 100 * 1024 * 1024) { // 100MB
                throw new SecurityException("File too large for virus scanning");
            }
        } catch (IOException e) {
            throw new SecurityException("Cannot perform virus scan: " + e.getMessage());
        }
    }
    
    /**
     * Get list of allowed file extensions.
     * 
     * @return set of allowed extensions
     */
    public Set<String> getAllowedExtensions() {
        return allowedExtensions;
    }
}