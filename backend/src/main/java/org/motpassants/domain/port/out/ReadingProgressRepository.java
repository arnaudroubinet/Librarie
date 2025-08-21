package org.motpassants.domain.port.out;

import org.motpassants.domain.core.model.ReadingProgress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for reading progress repository operations.
 * Defines the contract for reading progress persistence without coupling to specific technologies.
 * Pure domain interface without infrastructure dependencies.
 */
public interface ReadingProgressRepository {
    
    /**
     * Save a reading progress entry (create or update).
     * 
     * @param readingProgress the reading progress to save
     * @return the saved reading progress
     */
    ReadingProgress save(ReadingProgress readingProgress);
    
    /**
     * Find reading progress by user and book.
     * 
     * @param userId the user ID
     * @param bookId the book ID
     * @return optional containing the reading progress if found
     */
    Optional<ReadingProgress> findByUserIdAndBookId(UUID userId, UUID bookId);
    
    /**
     * Find all reading progress entries for a user.
     * 
     * @param userId the user ID
     * @return list of reading progress entries
     */
    List<ReadingProgress> findByUserId(UUID userId);
    
    /**
     * Delete reading progress by user and book.
     * 
     * @param userId the user ID
     * @param bookId the book ID
     */
    void deleteByUserIdAndBookId(UUID userId, UUID bookId);
    
    /**
     * Check if reading progress exists for user and book.
     * 
     * @param userId the user ID
     * @param bookId the book ID
     * @return true if reading progress exists
     */
    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);
}