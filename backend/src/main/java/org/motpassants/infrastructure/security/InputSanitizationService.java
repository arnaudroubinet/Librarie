package org.motpassants.infrastructure.security;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.regex.Pattern;

/**
 * Service for input sanitization and validation.
 * Provides security measures against injection attacks and malicious input.
 */
@ApplicationScoped
public class InputSanitizationService {
    
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern SAFE_PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9._/-]+$");
    
    /**
     * Sanitizes user input to prevent injection attacks.
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove null bytes and control characters
        return input.replaceAll("[\u0000-\u001f\u007f-\u009f]", "")
                   .trim();
    }
    
    /**
     * Validates filename for safety.
     */
    public boolean isValidFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        return SAFE_FILENAME_PATTERN.matcher(filename).matches() &&
               !filename.equals(".") &&
               !filename.equals("..");
    }
    
    /**
     * Validates file path for safety.
     */
    public boolean isValidPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        
        return SAFE_PATH_PATTERN.matcher(path).matches() &&
               !path.contains("..") &&
               !path.startsWith("/");
    }
    
    /**
     * Sanitizes HTML content.
     */
    public String sanitizeHtml(String html) {
        if (html == null) {
            return null;
        }
        
        // Basic HTML sanitization - remove script tags and dangerous attributes
        return html.replaceAll("<script[^>]*>.*?</script>", "")
                   .replaceAll("on\\w+\\s*=\\s*[\"'][^\"']*[\"']", "")
                   .replaceAll("javascript:", "");
    }
}