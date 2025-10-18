package org.motpassants.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.LoggingPort;
import org.motpassants.domain.port.out.DemoDataPort;

/**
 * Service for populating demo data when demo mode is enabled.
 * Creates comprehensive demo data including famous authors, series, and books.
 * 
 * Production Safety: Demo data will never run in production profile.
 * An exception is thrown if demo is enabled in production to prevent accidental activation.
 */
@ApplicationScoped
public class DemoDataService {
    
    private final ConfigurationPort configurationPort;
    private final BookRepository bookRepository;
    private final LoggingPort log;
    private final DemoDataPort demoDataPort;
    
    @Inject
    public DemoDataService(ConfigurationPort configurationPort, 
                          BookRepository bookRepository,
                          LoggingPort log,
                          DemoDataPort demoDataPort) {
        this.configurationPort = configurationPort;
        this.bookRepository = bookRepository;
        this.log = log;
        this.demoDataPort = demoDataPort;
    }
    
    /**
     * Populates demo data if enabled and database is empty.
     * 
     * @throws IllegalStateException if demo is enabled in production profile
     */
    public void populateDemoData() {
        if (!configurationPort.isDemoEnabled()) { 
            log.debug("Demo disabled; skipping demo data population"); 
            return; 
        }
        
        // Production safeguard: prevent demo data in production
        String activeProfile = configurationPort.getActiveProfile();
        if ("prod".equalsIgnoreCase(activeProfile)) {
            String errorMsg = "SECURITY: Demo data is enabled in production profile! " +
                             "This is a critical misconfiguration. " +
                             "Set librarie.demo.enabled=false in production.";
            IllegalStateException exception = new IllegalStateException(errorMsg);
            log.error(errorMsg, exception);
            throw exception;
        }
        
        if (bookRepository.count() > 0) { 
            log.debug("Books already present; skipping demo data population"); 
            return; 
        }
        
        try {
            log.info("Starting demo data population (async) in profile: " + activeProfile);
            demoDataPort.seed();
            log.info("Demo data population finished");
        } catch (Exception e) {
            log.warn("Demo data population failed; continuing without demo seed");
        }
    }
    
}