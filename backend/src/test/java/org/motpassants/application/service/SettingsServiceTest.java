package org.motpassants.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.motpassants.domain.core.model.*;
import org.motpassants.domain.port.out.AuthorRepositoryPort;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.SeriesRepositoryPort;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SettingsService.
 * Tests business logic for system settings assembly.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SettingsService Unit Tests")
class SettingsServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepositoryPort authorRepository;

    @Mock
    private SeriesRepositoryPort seriesRepository;

    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        settingsService = new SettingsService(bookRepository, authorRepository, seriesRepository);
    }

    @Test
    @DisplayName("Should create system settings with entity counts")
    void shouldCreateSystemSettingsWithEntityCounts() {
        // Mock repository counts
        when(bookRepository.count()).thenReturn(100L);
        when(seriesRepository.count()).thenReturn(20L);
        when(authorRepository.count()).thenReturn(50L);

        Settings settings = settingsService.getSystemSettings();

        assertNotNull(settings);
        assertNotNull(settings.getEntityCounts());
        
        EntityCounts entityCounts = settings.getEntityCounts();
        assertEquals(100L, entityCounts.books());
        assertEquals(20L, entityCounts.series());
        assertEquals(50L, entityCounts.authors());
        assertEquals(0L, entityCounts.publishers()); // Not implemented yet
        assertEquals(0L, entityCounts.languages());  // Not implemented yet
        assertEquals(0L, entityCounts.formats());    // Not implemented yet
        assertEquals(0L, entityCounts.tags());       // Not implemented yet

        // Verify repository calls
        verify(bookRepository).count();
        verify(seriesRepository).count();
        verify(authorRepository).count();
    }

    @Test
    @DisplayName("Should create settings with proper application metadata")
    void shouldCreateSettingsWithProperApplicationMetadata() {
        // Mock repository counts
        when(bookRepository.count()).thenReturn(0L);
        when(seriesRepository.count()).thenReturn(0L);
        when(authorRepository.count()).thenReturn(0L);

        Settings settings = settingsService.getSystemSettings();

        assertNotNull(settings);
        assertEquals("1.0.0-SNAPSHOT", settings.getVersion());
        assertEquals("Librarie", settings.getApplicationName());
        assertEquals(20, settings.getDefaultPageSize());
        assertEquals(100, settings.getMaxPageSize());
    }

    @Test
    @DisplayName("Should create settings with feature flags")
    void shouldCreateSettingsWithFeatureFlags() {
        // Mock repository counts
        when(bookRepository.count()).thenReturn(0L);
        when(seriesRepository.count()).thenReturn(0L);
        when(authorRepository.count()).thenReturn(0L);

        Settings settings = settingsService.getSystemSettings();

        assertNotNull(settings);
        assertNotNull(settings.getFeatureFlags());
        
        FeatureFlags featureFlags = settings.getFeatureFlags();
        assertTrue(featureFlags.enableIngest());
        assertTrue(featureFlags.enableExport());
        assertFalse(featureFlags.enableSync());
    }

    @Test
    @DisplayName("Should create settings with storage configuration")
    void shouldCreateSettingsWithStorageConfiguration() {
        // Mock repository counts
        when(bookRepository.count()).thenReturn(0L);
        when(seriesRepository.count()).thenReturn(0L);
        when(authorRepository.count()).thenReturn(0L);

        Settings settings = settingsService.getSystemSettings();

        assertNotNull(settings);
        assertNotNull(settings.getStorageConfiguration());
        
        StorageConfiguration storageConfig = settings.getStorageConfiguration();
        assertEquals("/data/library", storageConfig.baseDirectory());
        assertNotNull(storageConfig.allowedFileTypes());
        assertFalse(storageConfig.allowedFileTypes().isEmpty());
        
        // Check that common formats are included
        assertTrue(storageConfig.allowedFileTypes().contains("PDF"));
        assertTrue(storageConfig.allowedFileTypes().contains("EPUB"));
        assertTrue(storageConfig.allowedFileTypes().contains("MOBI"));
    }

    @Test
    @DisplayName("Should create settings with supported formats")
    void shouldCreateSettingsWithSupportedFormats() {
        // Mock repository counts
        when(bookRepository.count()).thenReturn(0L);
        when(seriesRepository.count()).thenReturn(0L);
        when(authorRepository.count()).thenReturn(0L);

        Settings settings = settingsService.getSystemSettings();

        assertNotNull(settings);
        assertNotNull(settings.getSupportedFormats());
        assertFalse(settings.getSupportedFormats().isEmpty());
        
        // Check that common formats are supported
        assertTrue(settings.getSupportedFormats().contains("PDF"));
        assertTrue(settings.getSupportedFormats().contains("EPUB"));
        assertTrue(settings.getSupportedFormats().contains("MOBI"));
        assertTrue(settings.getSupportedFormats().contains("AZW3"));
        assertTrue(settings.getSupportedFormats().contains("TXT"));
        assertTrue(settings.getSupportedFormats().contains("HTML"));
        assertTrue(settings.getSupportedFormats().contains("RTF"));
        assertTrue(settings.getSupportedFormats().contains("ODT"));
        assertTrue(settings.getSupportedFormats().contains("DOCX"));
    }

    @Test
    @DisplayName("Should handle repository errors gracefully")
    void shouldHandleRepositoryErrorsGracefully() {
        // Mock repository to throw exception
        when(bookRepository.count()).thenThrow(new RuntimeException("Database error"));

        // Should throw the exception (not handle it silently)
        assertThrows(RuntimeException.class, () -> {
            settingsService.getSystemSettings();
        });
        
        verify(bookRepository).count();
        // Other repositories should not be called after the first failure
    }

    @Test
    @DisplayName("Should create consistent settings across multiple calls")
    void shouldCreateConsistentSettingsAcrossMultipleCalls() {
        // Mock repository counts
        when(bookRepository.count()).thenReturn(42L);
        when(seriesRepository.count()).thenReturn(10L);
        when(authorRepository.count()).thenReturn(25L);

        Settings settings1 = settingsService.getSystemSettings();
        Settings settings2 = settingsService.getSystemSettings();

        // Structure should be consistent (though counts may differ if data changes)
        assertEquals(settings1.getVersion(), settings2.getVersion());
        assertEquals(settings1.getApplicationName(), settings2.getApplicationName());
        assertEquals(settings1.getDefaultPageSize(), settings2.getDefaultPageSize());
        assertEquals(settings1.getMaxPageSize(), settings2.getMaxPageSize());
        assertEquals(settings1.getSupportedFormats(), settings2.getSupportedFormats());
        assertEquals(settings1.getFeatureFlags(), settings2.getFeatureFlags());
        assertEquals(settings1.getStorageConfiguration(), settings2.getStorageConfiguration());

        // Repository methods should be called for each invocation
        verify(bookRepository, times(2)).count();
        verify(seriesRepository, times(2)).count();
        verify(authorRepository, times(2)).count();
    }
}