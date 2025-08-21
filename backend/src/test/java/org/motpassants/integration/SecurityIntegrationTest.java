package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.motpassants.domain.port.out.SecureFileProcessingPort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for security adapters.
 * Tests the complete flow including file validation and security concerns.
 */
@QuarkusTest
@DisplayName("Security Adapter Integration Tests")
public class SecurityIntegrationTest {

    @Inject
    SecureFileProcessingPort secureFileProcessingPort;

    @Test
    @DisplayName("Should validate valid book files")
    void shouldValidateValidBookFiles() throws IOException {
        // Create a test PDF file
        Path tempDir = Files.createTempDirectory("test-");
        Path pdfFile = tempDir.resolve("test.pdf");
        Files.write(pdfFile, "%PDF-1.4".getBytes());

        // Create a test EPUB file  
        Path epubFile = tempDir.resolve("test.epub");
        Files.write(epubFile, "PK".getBytes());

        assertTrue(secureFileProcessingPort.isValidBookFile(pdfFile));
        assertTrue(secureFileProcessingPort.isValidBookFile(epubFile));
        
        // Cleanup
        Files.deleteIfExists(pdfFile);
        Files.deleteIfExists(epubFile);
        Files.deleteIfExists(tempDir);
    }

    @Test
    @DisplayName("Should reject invalid book files")
    void shouldRejectInvalidBookFiles() throws IOException {
        Path tempDir = Files.createTempDirectory("test-");
        
        // Create a malicious file
        Path maliciousFile = tempDir.resolve("malicious.exe");
        Files.write(maliciousFile, "MZ".getBytes());

        // Create a text file with wrong extension
        Path textFile = tempDir.resolve("text.pdf");
        Files.write(textFile, "This is not a PDF".getBytes());

        assertFalse(secureFileProcessingPort.isValidBookFile(maliciousFile));
        assertFalse(secureFileProcessingPort.isValidBookFile(textFile));
        
        // Cleanup
        Files.deleteIfExists(maliciousFile);
        Files.deleteIfExists(textFile);
        Files.deleteIfExists(tempDir);
    }

    @Test
    @DisplayName("Should validate valid image files")
    void shouldValidateValidImageFiles() throws IOException {
        Path tempDir = Files.createTempDirectory("test-");
        
        // Create a test JPEG file
        Path jpegFile = tempDir.resolve("test.jpg");
        Files.write(jpegFile, new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF});

        // Create a test PNG file
        Path pngFile = tempDir.resolve("test.png");
        Files.write(pngFile, new byte[]{(byte)0x89, 0x50, 0x4E, 0x47});

        assertTrue(secureFileProcessingPort.isValidImageFile(jpegFile));
        assertTrue(secureFileProcessingPort.isValidImageFile(pngFile));
        
        // Cleanup
        Files.deleteIfExists(jpegFile);
        Files.deleteIfExists(pngFile);
        Files.deleteIfExists(tempDir);
    }

    @Test
    @DisplayName("Should reject invalid image files") 
    void shouldRejectInvalidImageFiles() throws IOException {
        Path tempDir = Files.createTempDirectory("test-");
        
        // Create a malicious file with image extension
        Path maliciousFile = tempDir.resolve("malicious.jpg");
        Files.write(maliciousFile, "#!/bin/bash\necho 'malicious'".getBytes());

        assertFalse(secureFileProcessingPort.isValidImageFile(maliciousFile));
        
        // Cleanup
        Files.deleteIfExists(maliciousFile);
        Files.deleteIfExists(tempDir);
    }

    @Test
    @DisplayName("Should handle non-existent files gracefully")
    void shouldHandleNonExistentFilesGracefully() throws IOException {
        Path tempDir = Files.createTempDirectory("test-");
        Path nonExistentFile = tempDir.resolve("nonexistent.pdf");

        assertFalse(secureFileProcessingPort.isValidBookFile(nonExistentFile));
        assertFalse(secureFileProcessingPort.isValidImageFile(nonExistentFile));
        
        // Cleanup
        Files.deleteIfExists(tempDir);
    }
}