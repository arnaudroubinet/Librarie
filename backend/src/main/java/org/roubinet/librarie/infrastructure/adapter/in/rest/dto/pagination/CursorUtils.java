package org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Utility for creating and parsing pagination cursors.
 * Implements Base64-encoded JSON cursors for secure and efficient pagination.
 * Uses injected singleton ObjectMapper for thread safety and performance.
 */
@ApplicationScoped
public class CursorUtils {
    
    private final ObjectMapper objectMapper;
    
    @Inject
    public CursorUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Create a cursor from ID and timestamp.
     * 
     * @param id the record ID
     * @param timestamp the record timestamp
     * @return Base64-encoded cursor
     */
    public String createCursor(UUID id, OffsetDateTime timestamp) {
        try {
            CursorData cursorData = new CursorData(id.toString(), timestamp.toString());
            String json = objectMapper.writeValueAsString(cursorData);
            return Base64.getEncoder().encodeToString(json.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create cursor", e);
        }
    }
    
    /**
     * Create a cursor from string ID and timestamp.
     * 
     * @param id the record ID as string
     * @param timestamp the record timestamp
     * @return Base64-encoded cursor
     */
    public String createCursor(String id, OffsetDateTime timestamp) {
        try {
            CursorData cursorData = new CursorData(id, timestamp.toString());
            String json = objectMapper.writeValueAsString(cursorData);
            return Base64.getEncoder().encodeToString(json.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create cursor", e);
        }
    }
    
    /**
     * Parse a cursor to extract ID and timestamp.
     * 
     * @param cursor Base64-encoded cursor
     * @return parsed cursor data
     */
    public CursorData parseCursor(String cursor) {
        if (cursor == null || cursor.trim().isEmpty()) {
            return null;
        }
        
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            String json = new String(decodedBytes);
            return objectMapper.readValue(json, CursorData.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor format", e);
        }
    }
    
    /**
     * Check if a cursor is valid.
     * 
     * @param cursor the cursor to validate
     * @return true if cursor is valid
     */
    public boolean isValidCursor(String cursor) {
        try {
            CursorData data = parseCursor(cursor);
            return data != null && data.getId() != null && data.getTimestamp() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Data structure for cursor content.
     */
    public static class CursorData {
        private String id;
        private String timestamp;
        
        public CursorData() {}
        
        public CursorData(String id, String timestamp) {
            this.id = id;
            this.timestamp = timestamp;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}