package org.motpassants.domain.core.model;

/**
 * Enumeration representing the reading status of a book for a user.
 * Tracks the lifecycle of reading activity.
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
     * Book has been completed.
     */
    FINISHED,
    
    /**
     * Book was started but not finished (Did Not Finish).
     */
    DNF;
    
    /**
     * Parse a string to ReadingStatus enum.
     * 
     * @param status the status string
     * @return the ReadingStatus enum value
     * @throws IllegalArgumentException if status is invalid
     */
    public static ReadingStatus fromString(String status) {
        if (status == null || status.isBlank()) {
            return UNREAD;
        }
        try {
            return ReadingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid reading status: " + status);
        }
    }
}
