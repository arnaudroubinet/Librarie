package org.roubinet.librarie.application.service;

import org.roubinet.librarie.application.port.in.SettingsUseCase;
import org.roubinet.librarie.application.port.out.BookRepository;
import org.roubinet.librarie.application.port.out.SeriesRepository;
import org.roubinet.librarie.application.port.out.AuthorRepository;
import org.roubinet.librarie.application.port.out.PublisherRepository;
import org.roubinet.librarie.application.port.out.LanguageRepository;
import org.roubinet.librarie.application.port.out.FormatRepository;
import org.roubinet.librarie.domain.model.SettingsData;
import org.roubinet.librarie.domain.model.EntityCounts;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Arrays;

/**
 * Application service implementing settings use cases.
 */
@ApplicationScoped
public class SettingsService implements SettingsUseCase {
    
    private final BookRepository bookRepository;
    private final SeriesRepository seriesRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final LanguageRepository languageRepository;
    private final FormatRepository formatRepository;
    
    @ConfigProperty(name = "quarkus.application.version")
    String applicationVersion;
    
    @ConfigProperty(name = "librarie.file-processing.allowed-extensions")
    String allowedExtensions;
    
    @Inject
    public SettingsService(BookRepository bookRepository, 
                          SeriesRepository seriesRepository,
                          AuthorRepository authorRepository,
                          PublisherRepository publisherRepository,
                          LanguageRepository languageRepository,
                          FormatRepository formatRepository) {
        this.bookRepository = bookRepository;
        this.seriesRepository = seriesRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.languageRepository = languageRepository;
        this.formatRepository = formatRepository;
    }
    
    @Override
    public SettingsData getSystemSettings() {
        String version = getApplicationVersion();
        List<String> supportedFormats = getSupportedFormats();
        EntityCounts entityCounts = getEntityCounts();
        
        return new SettingsData(version, supportedFormats, entityCounts);
    }
    
    private String getApplicationVersion() {
        return applicationVersion != null ? applicationVersion : "1.0.0-SNAPSHOT";
    }
    
    private List<String> getSupportedFormats() {
        // Get supported formats from application properties
        if (allowedExtensions != null && !allowedExtensions.isEmpty()) {
            return Arrays.asList(allowedExtensions.split(","));
        }
        
        // Fallback to hardcoded list if no configuration
        return List.of("epub", "mobi", "azw", "azw3", "pdf", "cbz", "cbr", "fb2", 
                      "txt", "rtf", "doc", "docx", "odt", "html", "lit", "lrf", 
                      "pdb", "pml", "rb", "snb", "tcr", "txtz");
    }
    
    private EntityCounts getEntityCounts() {
        long books = bookRepository.count();
        long series = seriesRepository.getTotalCount();
        long authors = authorRepository.count();
        long publishers = publisherRepository.count();
        long languages = languageRepository.count();
        long formats = formatRepository.count();
        
        return new EntityCounts(books, series, authors, publishers, languages, formats);
    }
}