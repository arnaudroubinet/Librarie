package org.motpassants.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;
import org.motpassants.domain.port.out.LoggingPort;
import org.motpassants.domain.port.out.metadata.MetadataAggregatorPort;
import org.motpassants.infrastructure.adapter.out.metadata.GoogleBooksProvider;
import org.motpassants.infrastructure.adapter.out.metadata.OpenLibraryProvider;

/**
 * Configuration for metadata providers.
 * Automatically registers all available metadata providers at startup.
 */
@ApplicationScoped
public class MetadataProviderConfiguration {
    
    private final MetadataAggregatorPort metadataAggregatorPort;
    private final GoogleBooksProvider googleBooksProvider;
    private final OpenLibraryProvider openLibraryProvider;
    private final LoggingPort loggingPort;
    
    @Inject
    public MetadataProviderConfiguration(MetadataAggregatorPort metadataAggregatorPort,
                                       GoogleBooksProvider googleBooksProvider,
                                       OpenLibraryProvider openLibraryProvider,
                                       LoggingPort loggingPort) {
        this.metadataAggregatorPort = metadataAggregatorPort;
        this.googleBooksProvider = googleBooksProvider;
        this.openLibraryProvider = openLibraryProvider;
        this.loggingPort = loggingPort;
    }
    
    void onStart(@Observes StartupEvent ev) {
        loggingPort.info("Registering metadata providers...");
        
        // Register Google Books provider (priority 1)
        metadataAggregatorPort.registerProvider(googleBooksProvider);
        
        // Register Open Library provider (priority 2)
        metadataAggregatorPort.registerProvider(openLibraryProvider);
        
        loggingPort.info("Metadata providers registered successfully");
    }
}