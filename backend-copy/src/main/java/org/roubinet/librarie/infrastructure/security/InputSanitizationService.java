package org.roubinet.librarie.infrastructure.security;

import org.roubinet.librarie.infrastructure.config.LibrarieConfigProperties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.regex.Pattern;

/**
 * Input sanitization service following OWASP guidelines.
 * Prevents XSS, SQL injection, and other injection attacks.
 */
@ApplicationScoped
public class InputSanitizationService {
    
    private final LibrarieConfigProperties config;
    private final Pattern allowedCharactersPattern;
    
    // OWASP recommended patterns for different input types
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "('|(\\-\\-)|(;)|(\\|)|(\\*)|(%)|(union)|(select)|(insert)|(delete)|(drop)|(create)|(alter)|(exec)|(execute)|(script)|(javascript)|(vbscript)|(onload)|(onerror)|(onclick))",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(<script[^>]*>.*?</script>)|(<.*?javascript:.*?>)|(<.*?on\\w+\\s*=.*?>)|(<\\s*script)|(<\\s*object)|(<\\s*embed)|(<\\s*applet)|(<\\s*form)|(<\\s*iframe)|(<\\s*frame)|(<\\s*frameset)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "(\\.\\.[\\/\\\\])|(\\.\\.\\\\)|(\\.\\./)|(\\%2e\\%2e[\\/\\\\])|(\\%2e\\%2e\\%5c)|(\\%2e\\%2e\\%2f)|(\\%252e\\%252e[\\/\\\\])",
        Pattern.CASE_INSENSITIVE
    );
    
    @Inject
    public InputSanitizationService(LibrarieConfigProperties config) {
        this.config = config;
        this.allowedCharactersPattern = Pattern.compile(config.security().allowedCharactersPattern());
    }
    
    /**
     * Sanitize search query input to prevent injection attacks.
     * 
     * @param query the search query to sanitize
     * @return sanitized query
     * @throws SecurityException if input contains malicious content
     */
    public String sanitizeSearchQuery(String query) {
        if (query == null) {
            return "";
        }
        
        if (!config.security().enableInputSanitization()) {
            return query;
        }
        
        // Check length limit
        if (query.length() > config.security().maxQueryLength()) {
            throw new SecurityException("Query too long. Maximum length: " + config.security().maxQueryLength());
        }
        
        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(query).find()) {
            throw new SecurityException("Potentially malicious SQL injection pattern detected");
        }
        
        // Check for XSS patterns
        if (XSS_PATTERN.matcher(query).find()) {
            throw new SecurityException("Potentially malicious XSS pattern detected");
        }
        
        // Check for path traversal patterns
        if (PATH_TRAVERSAL_PATTERN.matcher(query).find()) {
            throw new SecurityException("Path traversal pattern detected");
        }
        
        // Sanitize by allowing only safe characters
        String sanitized = query.replaceAll("[^" + config.security().allowedCharactersPattern().replaceAll("[\\[\\]]", "") + "]", "");
        
        return sanitized.trim();
    }
    
    /**
     * Sanitize file path to prevent path traversal attacks.
     * 
     * @param filePath the file path to sanitize
     * @return sanitized file path
     * @throws SecurityException if path contains traversal patterns
     */
    public String sanitizeFilePath(String filePath) {
        if (filePath == null) {
            return "";
        }
        
        if (!config.security().enableInputSanitization()) {
            return filePath;
        }
        
        // Check for path traversal patterns
        if (PATH_TRAVERSAL_PATTERN.matcher(filePath).find()) {
            throw new SecurityException("Path traversal attack detected in file path");
        }
        
        // Normalize path and remove dangerous sequences
        return filePath
            .replaceAll("\\.\\.", "")  // Remove all .. sequences
            .replaceAll("[\\/\\\\]+", "/")  // Normalize path separators
            .replaceAll("^/+", "")  // Remove leading slashes
            .trim();
    }
    
    /**
     * Sanitize general text input for storage and display.
     * 
     * @param input the text input to sanitize
     * @return sanitized text
     */
    public String sanitizeTextInput(String input) {
        if (input == null) {
            return "";
        }
        
        if (!config.security().enableInputSanitization()) {
            return input;
        }
        
        // Remove XSS patterns
        String sanitized = XSS_PATTERN.matcher(input).replaceAll("");
        
        // HTML encode dangerous characters
        sanitized = sanitized
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
            
        return sanitized.trim();
    }
    
    /**
     * Validate if the input matches allowed patterns.
     * 
     * @param input the input to validate
     * @return true if input is safe
     */
    public boolean isInputSafe(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        
        return allowedCharactersPattern.matcher(input).matches() &&
               !SQL_INJECTION_PATTERN.matcher(input).find() &&
               !XSS_PATTERN.matcher(input).find() &&
               !PATH_TRAVERSAL_PATTERN.matcher(input).find();
    }
}