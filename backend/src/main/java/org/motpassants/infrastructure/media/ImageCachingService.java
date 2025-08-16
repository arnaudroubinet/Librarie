package org.motpassants.infrastructure.media;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.SecureFileProcessingPort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for caching and serving images with security validation.
 */
@ApplicationScoped
public class ImageCachingService {
    
    private static final Logger LOG = Logger.getLogger(ImageCachingService.class);
    
    private final LibrarieConfigProperties config;
    private final SecureFileProcessingService secureFileProcessingService;
    private final ConcurrentMap<String, byte[]> imageCache = new ConcurrentHashMap<>();
    
    @Inject
    public ImageCachingService(LibrarieConfigProperties config, 
                              SecureFileProcessingService secureFileProcessingService) {
        this.config = config;
        this.secureFileProcessingService = secureFileProcessingService;
    }
    
    /**
     * Gets image bytes from cache or loads from file system.
     */
    public byte[] getImage(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }
        
        // Check cache first
        byte[] cachedImage = imageCache.get(relativePath);
        if (cachedImage != null) {
            LOG.debug("Serving image from cache: " + relativePath);
            return cachedImage;
        }
        
        // Load from file system
        try {
            Path imagePath = secureFileProcessingService.sanitizePath(
                config.storage().baseDir(), relativePath);
            
            if (!Files.exists(imagePath)) {
                LOG.warn("Image file not found: " + imagePath);
                return null;
            }
            
            if (!secureFileProcessingPort.isValidImageFile(imagePath)) {
                LOG.warn("Invalid image file: " + imagePath);
                return null;
            }
            
            byte[] imageBytes = Files.readAllBytes(imagePath);
            
            // Cache the image if it's not too large (max 1MB)
            if (imageBytes.length <= 1024 * 1024) {
                imageCache.put(relativePath, imageBytes);
                LOG.debug("Cached image: " + relativePath);
            }
            
            return imageBytes;
            
        } catch (IOException e) {
            LOG.error("Error reading image file: " + relativePath, e);
            return null;
        } catch (SecurityException e) {
            LOG.warn("Security violation accessing image: " + relativePath, e);
            return null;
        }
    }
    
    /**
     * Gets the MIME type for an image file.
     */
    public String getImageMimeType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFilename.endsWith(".bmp")) {
            return "image/bmp";
        }
        
        return "application/octet-stream";
    }
    
    /**
     * Clears the image cache.
     */
    public void clearCache() {
        imageCache.clear();
        LOG.info("Image cache cleared");
    }
    
    /**
     * Gets cache statistics.
     */
    public CacheStats getCacheStats() {
        return new CacheStats(imageCache.size());
    }
    
    public static class CacheStats {
        private final int size;
        
        public CacheStats(int size) {
            this.size = size;
        }
        
        public int getSize() {
            return size;
        }
    }
}