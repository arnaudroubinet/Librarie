package org.motpassants.application.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Service that handles application startup events and initialization.
 */
@ApplicationScoped
public class StartupService {
    
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
        try {
            demoDataService.populateDemoData();
        } catch (Exception e) {
            // Don't fail application startup if demo data population fails
        }
    }
}