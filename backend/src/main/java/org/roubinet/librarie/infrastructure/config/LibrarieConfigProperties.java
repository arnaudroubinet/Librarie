package org.roubinet.librarie.infrastructure.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for Librarie application.
 * Provides type-safe configuration with default values.
 */
@ConfigMapping(prefix = "librarie")
public interface LibrarieConfigProperties {
    
    /**
     * Pagination configuration properties.
     */
    PaginationConfig pagination();
    
    /**
     * File processing configuration properties.
     */
    FileProcessingConfig fileProcessing();
    
    /**
     * Security configuration properties.
     */
    SecurityConfig security();
    
    /**
     * Demo configuration properties.
     */
    DemoConfig demo();
    
    interface PaginationConfig {
        /**
         * Default page size for pagination.
         */
        @WithDefault("20")
        int defaultPageSize();
        
        /**
         * Maximum page size allowed.
         */
        @WithDefault("100")
        int maxPageSize();
        
        /**
         * Default page number.
         */
        @WithDefault("0")
        int defaultPageNumber();
    }
    
    interface FileProcessingConfig {
        /**
         * Maximum file size in bytes (default: 500MB).
         */
        @WithDefault("524288000")
        long maxFileSize();
        
        /**
         * Allowed file extensions for upload.
         */
        @WithDefault("epub,mobi,azw,azw3,pdf,cbz,cbr,fb2,txt,rtf,doc,docx,odt,html,lit,lrf,pdb,pml,rb,snb,tcr,txtz")
        String allowedExtensions();
        
        /**
         * Quarantine directory for suspicious files.
         */
        @WithDefault("./quarantine")
        String quarantineDirectory();
        
        /**
         * Enable virus scanning.
         */
        @WithDefault("false")
        boolean enableVirusScanning();
    }
    
    interface SecurityConfig {
        /**
         * Maximum query length to prevent DoS attacks.
         */
        @WithDefault("1000")
        int maxQueryLength();
        
        /**
         * Enable input sanitization.
         */
        @WithDefault("true")
        boolean enableInputSanitization();
        
        /**
         * Allowed characters pattern for search queries.
         */
        @WithDefault("[a-zA-Z0-9\\s\\-_.,!?@]")
        String allowedCharactersPattern();
        
        /**
         * Rate limiting: max requests per minute per IP.
         */
        @WithDefault("100")
        int maxRequestsPerMinute();
    }
    
    interface DemoConfig {
        /**
         * Enable demo mode with sample data population.
         */
        @WithDefault("false")
        boolean enabled();
    }
}