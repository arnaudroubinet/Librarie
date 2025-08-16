package org.motpassants.infrastructure.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for the Librarie application.
 */
@ConfigMapping(prefix = "librarie")
public interface LibrarieConfigProperties {
    
    /**
     * Storage configuration.
     */
    Storage storage();
    
    /**
     * Demo mode configuration.
     */
    Demo demo();
    
    /**
     * Security configuration.
     */
    Security security();
    
    interface Storage {
        /**
         * Base directory for file storage.
         */
        @WithDefault("./storage")
        String baseDir();
        
        /**
         * Maximum file size for uploads (in bytes).
         */
        @WithDefault("104857600") // 100MB
        long maxFileSize();
        
        /**
         * Allowed file extensions for books.
         */
        @WithDefault("pdf,epub,mobi,azw,azw3,fb2,txt,rtf,doc,docx")
        String allowedBookExtensions();
        
        /**
         * Allowed file extensions for images.
         */
        @WithDefault("jpg,jpeg,png,gif,webp,bmp")
        String allowedImageExtensions();
    }
    
    interface Demo {
        /**
         * Whether demo mode is enabled.
         */
        @WithDefault("false")
        boolean enabled();
        
        /**
         * Number of demo books to create.
         */
        @WithDefault("100")
        int bookCount();
        
        /**
         * Number of demo authors to create.
         */
        @WithDefault("50")
        int authorCount();
        
        /**
         * Number of demo series to create.
         */
        @WithDefault("20")
        int seriesCount();
    }
    
    interface Security {
        /**
         * Whether input sanitization is enabled.
         */
        @WithDefault("true")
        boolean sanitizationEnabled();
        
        /**
         * Whether file content validation is enabled.
         */
        @WithDefault("true")
        boolean fileValidationEnabled();
        
        /**
         * Maximum request size (in bytes).
         */
        @WithDefault("10485760") // 10MB
        long maxRequestSize();
    }
}