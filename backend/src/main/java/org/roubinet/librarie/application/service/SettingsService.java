package org.roubinet.librarie.application.service;

import org.roubinet.librarie.application.port.in.SettingsUseCase;
import org.roubinet.librarie.application.port.out.BookRepository;
import org.roubinet.librarie.application.port.out.SeriesRepository;
import org.roubinet.librarie.domain.entity.Author;
import org.roubinet.librarie.domain.entity.Publisher;
import org.roubinet.librarie.domain.entity.Language;
import org.roubinet.librarie.domain.entity.Format;
import org.roubinet.librarie.domain.model.SettingsData;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Application service implementing settings use cases.
 */
@ApplicationScoped
public class SettingsService implements SettingsUseCase {
    
    private final BookRepository bookRepository;
    private final SeriesRepository seriesRepository;
    private final EntityManager entityManager;
    
    @Inject
    public SettingsService(BookRepository bookRepository, SeriesRepository seriesRepository, EntityManager entityManager) {
        this.bookRepository = bookRepository;
        this.seriesRepository = seriesRepository;
        this.entityManager = entityManager;
    }
    
    @Override
    public SettingsData getSystemSettings() {
        String version = getApplicationVersion();
        List<String> supportedFormats = getSupportedFormats();
        Map<String, Long> entityCounts = getEntityCounts();
        
        return new SettingsData(version, supportedFormats, entityCounts);
    }
    
    private String getApplicationVersion() {
        // Try to get version from system property or manifest
        String version = System.getProperty("application.version");
        if (version == null) {
            // Fallback to package implementation version
            Package pkg = getClass().getPackage();
            version = pkg != null ? pkg.getImplementationVersion() : null;
        }
        return version != null ? version : "1.0.0-SNAPSHOT";
    }
    
    private List<String> getSupportedFormats() {
        // Get distinct format types from the Format entity
        List<Format> formats = Format.listAll();
        if (formats.isEmpty()) {
            // Fallback to hardcoded list if no formats in database
            return List.of("epub", "mobi", "azw", "azw3", "pdf", "cbz", "cbr", "fb2", 
                          "txt", "rtf", "doc", "docx", "odt", "html", "lit", "lrf", 
                          "pdb", "pml", "rb", "snb", "tcr", "txtz");
        }
        return formats.stream()
                .map(format -> format.getType().toLowerCase())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    private Map<String, Long> getEntityCounts() {
        Map<String, Long> counts = new HashMap<>();
        
        counts.put("books", bookRepository.count());
        counts.put("series", seriesRepository.getTotalCount());
        counts.put("authors", Author.count());
        counts.put("publishers", Publisher.count());
        
        // For entities that don't extend PanacheEntityBase, use EntityManager
        counts.put("languages", entityManager.createQuery("SELECT COUNT(l) FROM Language l", Long.class).getSingleResult());
        counts.put("formats", Format.count());
        
        return counts;
    }
}