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
    
    public void populateDemoData() {
        if (!configurationPort.isDemoEnabled()) { log.debug("Demo disabled; skipping demo data population"); return; }
        if (bookRepository.count() > 0) { log.debug("Books already present; skipping demo data population"); return; }
        try {
            log.info("Starting demo data population (async)");
            demoDataPort.seed();
            log.info("Demo data population finished");
        } catch (Exception e) {
            log.warn("Demo data population failed; continuing without demo seed");
        }
    }
    
}