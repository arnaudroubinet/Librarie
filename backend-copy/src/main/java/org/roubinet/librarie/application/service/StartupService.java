package org.roubinet.librarie.application.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Service that handles application startup events and initialization.
 */
@ApplicationScoped
public class StartupService {
    
    private static final Logger LOG = Logger.getLogger(StartupService.class);
    
    private final DemoDataService demoDataService;
    
    @Inject
    public StartupService(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }
    
    /**
     * Handles application startup events.
     * Populates demo data if demo mode is enabled.
     */
    void onStart(@Observes StartupEvent event) {
        LOG.info("Librarie application starting up...");
        
        try {
            demoDataService.populateDemoData();
        } catch (Exception e) {
            LOG.error("Failed to populate demo data", e);
            // Don't fail application startup if demo data population fails
        }
        
        LOG.info("Librarie application startup completed");
    }
}