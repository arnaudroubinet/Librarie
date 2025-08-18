package org.motpassants.application.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.SecureFileProcessingPort;
import org.motpassants.domain.port.out.LoggingPort;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Service that handles application startup events and initialization.
 */
@ApplicationScoped
public class StartupService {

    private final DemoDataService demoDataService;
    private final ConfigurationPort configurationPort;
    private final SecureFileProcessingPort secureFileProcessingPort;
    private final LoggingPort log;
    
    @Inject
    public StartupService(DemoDataService demoDataService,
                          ConfigurationPort configurationPort,
                          SecureFileProcessingPort secureFileProcessingPort,
                          LoggingPort log) {
        this.demoDataService = demoDataService;
        this.configurationPort = configurationPort;
        this.secureFileProcessingPort = secureFileProcessingPort;
        this.log = log;
    }
    
    /**
     * Handles application startup events.
     * Populates demo data if demo mode is enabled.
     */
    void onStart(@Observes StartupEvent event) {
        try {
            ensureStorageBaseDirectory();
            // Run demo seeding asynchronously to avoid blocking startup
            log.info("Scheduling asynchronous demo data population...");
            CompletableFuture.runAsync(() -> {
                try {
                    demoDataService.populateDemoData();
                } catch (Throwable t) {
                    log.warn("Asynchronous demo data population failed; continuing");
                }
            });
        } catch (Exception e) {
            // Don't fail application startup if demo data population fails
            log.warn("Demo data population failed at startup; continuing without demo seed");
        }
    }

    /**
     * Ensure the configured storage base directory exists and create common subfolders.
     * In dev, application-dev.properties sets it to target/assets.
     */
    private void ensureStorageBaseDirectory() {
        try {
            String baseDir = configurationPort.getStorageConfig().getBaseDir();
            Path base = secureFileProcessingPort.sanitizePath(baseDir, ".");
            // Create base directory
            try { Files.createDirectories(base); } catch (Exception ignored) {}
            // Create common subfolders used by controllers/services
            Path[] folders = new Path[] {
                base.resolve("books").resolve("covers"),
                base.resolve("authors").resolve("pictures"),
                base.resolve("series").resolve("covers"),
                base.resolve("demo").resolve("books")
            };
            for (Path p : folders) {
                try { Files.createDirectories(p); } catch (Exception ignored) {}
            }
            log.infof("Storage directory ready at %s", base.toString());
        } catch (Exception e) {
            // Non-fatal; later file operations will attempt to create as needed
            log.warn("Failed to pre-create storage directories; will retry lazily");
        }
    }
}