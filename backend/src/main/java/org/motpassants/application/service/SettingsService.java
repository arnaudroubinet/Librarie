package org.motpassants.application.service;

import org.motpassants.domain.core.model.Settings;
import org.motpassants.domain.core.model.EntityCounts;
import org.motpassants.domain.core.model.FeatureFlags;
import org.motpassants.domain.core.model.StorageConfiguration;
import org.motpassants.domain.port.in.SettingsUseCase;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.AuthorRepositoryPort;
import org.motpassants.domain.port.out.SeriesRepositoryPort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;

/**
 * Settings service implementing business logic for system settings.
 * Orchestrates between domain models and repository ports.
 */
@ApplicationScoped
public class SettingsService implements SettingsUseCase {
    
    private final BookRepository bookRepository;
    private final AuthorRepositoryPort authorRepository;
    private final SeriesRepositoryPort seriesRepository;
    
    @Inject
    public SettingsService(BookRepository bookRepository, 
                          AuthorRepositoryPort authorRepository,
                          SeriesRepositoryPort seriesRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.seriesRepository = seriesRepository;
    }
    
    @Override
    public Settings getSystemSettings() {
        // Get entity counts from repositories
        EntityCounts entityCounts = new EntityCounts(
            bookRepository.count(),
            seriesRepository.count(), 
            authorRepository.count(),
            0L, // publishers - not implemented yet
            0L, // languages - not implemented yet
            0L, // formats - not implemented yet
            0L  // tags - not implemented yet
        );
        
        // Define supported formats
        List<String> supportedFormats = Arrays.asList(
            "PDF", "EPUB", "MOBI", "AZW3", "TXT", "HTML", "RTF", "ODT", "DOCX"
        );
        
        // Define feature flags
        FeatureFlags featureFlags = new FeatureFlags(
            true,  // enableIngest
            true,  // enableExport
            false  // enableSync
        );
        
        // Define storage configuration
        StorageConfiguration storageConfiguration = new StorageConfiguration(
            "/data/library",
            Arrays.asList("PDF", "EPUB", "MOBI", "AZW3", "TXT", "HTML", "RTF", "ODT", "DOCX")
        );
        
        // Build settings
        Settings settings = new Settings(
            "1.0.0-SNAPSHOT", // version
            "Librarie",       // applicationName
            supportedFormats,
            entityCounts,
            featureFlags,
            20,               // defaultPageSize
            100,              // maxPageSize
            storageConfiguration
        );
        
        return settings;
    }
}