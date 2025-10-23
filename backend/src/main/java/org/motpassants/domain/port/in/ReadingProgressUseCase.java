package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.ReadingProgress;
import org.motpassants.domain.core.model.ReadingStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port defining reading progress use cases.
 * Represents the primary ports (driving ports) for reading progress operations.
 * Pure domain interface without infrastructure dependencies.
 */
public interface ReadingProgressUseCase {
    
    /**
     * Update or create reading progress for a user and book.
     * 
     * @param userId the user ID
     * @param bookId the book ID  
     * @param progress the reading progress (0.0 to 1.0)
     * @param currentPage the current page number
     * @param totalPages the total number of pages
     * @param progressLocator the progress locator (CFI or other format)
     * @return the updated reading progress
     */
    ReadingProgress updateReadingProgress(UUID userId, UUID bookId, Double progress, Integer currentPage, Integer totalPages, String progressLocator);
    
    /**
     * Update reading progress with status.
     * 
     * @param userId the user ID
     * @param bookId the book ID
     * @param progress the reading progress (0.0 to 1.0)
     * @param currentPage the current page number
     * @param totalPages the total number of pages
     * @param progressLocator the progress locator
     * @param status the reading status
     * @return the updated reading progress
     */
    ReadingProgress updateReadingProgressWithStatus(UUID userId, UUID bookId, Double progress, Integer currentPage, 
                                                    Integer totalPages, String progressLocator, ReadingStatus status);
    
    /**
     * Get reading progress for a user and book.
     * 
     * @param userId the user ID
     * @param bookId the book ID
     * @return optional containing the reading progress if found
     */
    Optional<ReadingProgress> getReadingProgress(UUID userId, UUID bookId);
    
    /**
     * Get all reading progress for a user.
     * 
     * @param userId the user ID
     * @return list of reading progress entries for the user
     */
    List<ReadingProgress> getReadingProgressByUser(UUID userId);
    
    /**
     * Delete reading progress for a user and book.
     * 
     * @param userId the user ID
     * @param bookId the book ID
     */
    void deleteReadingProgress(UUID userId, UUID bookId);
    
    /**
     * Mark a book as completed for a user.
     * 
     * @param userId the user ID
     * @param bookId the book ID
     * @return the updated reading progress
     */
    ReadingProgress markAsCompleted(UUID userId, UUID bookId);
    
    /**
     * Mark a book as started for a user.
     * 
     * @param userId the user ID
     * @param bookId the book ID
     * @return the updated reading progress
     */
    ReadingProgress markAsStarted(UUID userId, UUID bookId);
    
    /**
     * Mark a book as DNF (Did Not Finish) for a user.
     * 
     * @param userId the user ID
     * @param bookId the book ID
     * @return the updated reading progress
     */
    ReadingProgress markAsDnf(UUID userId, UUID bookId);
}