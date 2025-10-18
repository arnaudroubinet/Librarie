package org.motpassants.domain.port.out;

/**
 * Port for application configuration.
 */
public interface ConfigurationPort {
    
    /**
     * Demo mode configuration.
     */
    boolean isDemoEnabled();
    
    /**
     * Get the current application profile (e.g., dev, test, prod).
     */
    String getActiveProfile();
    
    /**
     * Storage configuration.
     */
    StorageConfig getStorageConfig();
    
    /**
     * Demo mode configuration.
     */
    DemoConfig getDemoConfig();
    
    /**
     * Security configuration.
     */
    SecurityConfig getSecurityConfig();
    
    interface StorageConfig {
        String getBaseDir();
        long getMaxFileSize();
        String getAllowedBookExtensions();
        String getAllowedImageExtensions();
    }
    
    interface DemoConfig {
        boolean isEnabled();
        int getBookCount();
        int getAuthorCount();
        int getSeriesCount();
    }
    
    interface SecurityConfig {
        boolean isSanitizationEnabled();
        boolean isFileValidationEnabled();
        long getMaxRequestSize();
    }
}