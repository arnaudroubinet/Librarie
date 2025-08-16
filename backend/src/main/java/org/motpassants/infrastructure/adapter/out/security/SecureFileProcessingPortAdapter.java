package org.motpassants.infrastructure.adapter.out.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.port.out.SecureFileProcessingPort;
import org.motpassants.infrastructure.security.SecureFileProcessingService;

import java.nio.file.Path;

/**
 * Infrastructure adapter for secure file processing.
 */
@ApplicationScoped
public class SecureFileProcessingPortAdapter implements SecureFileProcessingPort {
    
    private final SecureFileProcessingService secureFileProcessingService;
    
    @Inject
    public SecureFileProcessingPortAdapter(SecureFileProcessingService secureFileProcessingService) {
        this.secureFileProcessingService = secureFileProcessingService;
    }
    
    @Override
    public boolean isValidBookFile(Path filePath) {
        return secureFileProcessingService.isValidBookFile(filePath);
    }
    
    @Override
    public boolean isValidImageFile(Path filePath) {
        return secureFileProcessingService.isValidImageFile(filePath);
    }
    
    @Override
    public Path sanitizePath(String basePath, String relativePath) {
        return secureFileProcessingService.sanitizePath(basePath, relativePath);
    }
    
    @Override
    public String getSafeFilename(String filename) {
        return secureFileProcessingService.getSafeFilename(filename);
    }
}