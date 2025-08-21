package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.motpassants.domain.port.out.ConfigurationPort;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for configuration adapters.
 * Tests the complete flow from domain to configuration infrastructure.
 */
@QuarkusTest
@DisplayName("Configuration Adapter Integration Tests")
public class ConfigurationIntegrationTest {

    @Inject
    ConfigurationPort configurationPort;

    @Test
    @DisplayName("Should access demo configuration")
    void shouldAccessDemoConfiguration() {
        // Test that configuration can be accessed
        // The actual value depends on application.properties but method should not throw
        assertDoesNotThrow(() -> {
            boolean demoEnabled = configurationPort.isDemoEnabled();
            // Demo should be enabled in test environment
            assertTrue(demoEnabled, "Demo should be enabled in test environment");
        });
    }

    @Test
    @DisplayName("Should access storage configuration")
    void shouldAccessStorageConfiguration() {
        assertDoesNotThrow(() -> {
            var storageConfig = configurationPort.getStorageConfig();
            assertNotNull(storageConfig, "Storage configuration should not be null");
            assertNotNull(storageConfig.getBaseDir(), "Base directory should not be null");
            assertFalse(storageConfig.getBaseDir().isBlank(), "Base directory should not be blank");
        });
    }

    @Test
    @DisplayName("Should provide valid storage configuration values")
    void shouldProvideValidStorageConfigurationValues() {
        var storageConfig = configurationPort.getStorageConfig();
        
        String baseDirectory = storageConfig.getBaseDir();
        assertNotNull(baseDirectory);
        assertFalse(baseDirectory.isBlank());
        
        // Base directory should be a valid path (not contain invalid characters)
        assertFalse(baseDirectory.contains("\\0"), "Base directory should not contain null characters");
        
        long maxFileSize = storageConfig.getMaxFileSize();
        assertTrue(maxFileSize > 0, "Max file size should be positive");
        
        String allowedBookExtensions = storageConfig.getAllowedBookExtensions();
        assertNotNull(allowedBookExtensions);
        assertFalse(allowedBookExtensions.isBlank(), "Should have some allowed book extensions");
        
        // Common book formats should be allowed
        assertTrue(allowedBookExtensions.toLowerCase().contains("pdf"), 
                  "PDF should be an allowed extension");
        assertTrue(allowedBookExtensions.toLowerCase().contains("epub"), 
                  "EPUB should be an allowed extension");
        
        String allowedImageExtensions = storageConfig.getAllowedImageExtensions();
        assertNotNull(allowedImageExtensions);
        assertFalse(allowedImageExtensions.isBlank(), "Should have some allowed image extensions");
    }
}