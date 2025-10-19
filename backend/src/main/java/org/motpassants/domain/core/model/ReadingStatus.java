package org.motpassants.domain.core.model;

/**
 * Enumeration representing the reading status of a book.
 * Part of the domain core model in hexagonal architecture.
 */
public enum ReadingStatus {
    /**
     * Book has not been started yet.
     */
    UNREAD,
    
    /**
     * Book is currently being read.
     */
    READING,
    
    /**
     * Book has been finished.
     */
    FINISHED,
    
    /**
     * Book was started but not finished (Did Not Finish).
     */
    DNF;
    
    /**
     * Parse a string to ReadingStatus, with case-insensitive matching.
     * 
     * @param value the string value to parse
     * @return the corresponding ReadingStatus
     * @throws IllegalArgumentException if the value is not a valid status
     */
    public static ReadingStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return READING; // Default status
        }
        
        try {
            return ReadingStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid reading status: " + value + ". Valid values are: UNREAD, READING, FINISHED, DNF"
            );
        }
    }
}
