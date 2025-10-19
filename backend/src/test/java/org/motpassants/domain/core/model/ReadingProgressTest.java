package org.motpassants.domain.core.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReadingProgress domain model.
 * Tests business logic and state transitions.
 */
@DisplayName("ReadingProgress Domain Model Tests")
class ReadingProgressTest {

    @Test
    @DisplayName("Should create new reading progress with default values")
    void shouldCreateNewReadingProgressWithDefaultValues() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        
        // When
        ReadingProgress progress = ReadingProgress.create(userId, bookId);
        
        // Then
        assertNotNull(progress.getId());
        assertEquals(userId, progress.getUserId());
        assertEquals(bookId, progress.getBookId());
        assertEquals(0.0, progress.getProgress());
        assertEquals(ReadingStatus.UNREAD, progress.getStatus());
        assertFalse(progress.getIsCompleted());
        assertNull(progress.getStartedAt());
        assertNull(progress.getFinishedAt());
        assertNotNull(progress.getCreatedAt());
        assertNotNull(progress.getUpdatedAt());
        assertEquals(1L, progress.getSyncVersion());
    }

    @Test
    @DisplayName("Should calculate progress percentage correctly")
    void shouldCalculateProgressPercentageCorrectly() {
        // Given
        ReadingProgress progress = new ReadingProgress();
        progress.setProgress(0.5);
        
        // When
        double percentage = progress.getProgressPercentage();
        
        // Then
        assertEquals(50.0, percentage, 0.01);
    }

    @Test
    @DisplayName("Should return zero percentage when progress is null")
    void shouldReturnZeroPercentageWhenProgressIsNull() {
        // Given
        ReadingProgress progress = new ReadingProgress();
        progress.setProgress(null);
        
        // When
        double percentage = progress.getProgressPercentage();
        
        // Then
        assertEquals(0.0, percentage, 0.01);
    }

    @Test
    @DisplayName("Should mark book as finished correctly")
    void shouldMarkBookAsFinished() {
        // Given
        ReadingProgress progress = ReadingProgress.create(UUID.randomUUID(), UUID.randomUUID());
        progress.setTotalPages(300);
        LocalDateTime beforeFinish = LocalDateTime.now();
        
        // When
        progress.markAsFinished();
        
        // Then
        assertEquals(ReadingStatus.FINISHED, progress.getStatus());
        assertEquals(1.0, progress.getProgress());
        assertTrue(progress.getIsCompleted());
        assertNotNull(progress.getFinishedAt());
        assertTrue(progress.getFinishedAt().isAfter(beforeFinish) || progress.getFinishedAt().isEqual(beforeFinish));
        assertEquals(300, progress.getCurrentPage());
        assertEquals(2L, progress.getSyncVersion());
    }

    @Test
    @DisplayName("Should mark book as started correctly")
    void shouldMarkBookAsStarted() {
        // Given
        ReadingProgress progress = ReadingProgress.create(UUID.randomUUID(), UUID.randomUUID());
        LocalDateTime beforeStart = LocalDateTime.now();
        
        // When
        progress.markAsStarted();
        
        // Then
        assertEquals(ReadingStatus.READING, progress.getStatus());
        assertNotNull(progress.getStartedAt());
        assertTrue(progress.getStartedAt().isAfter(beforeStart) || progress.getStartedAt().isEqual(beforeStart));
        assertNotNull(progress.getLastReadAt());
        assertEquals(2L, progress.getSyncVersion());
    }

    @Test
    @DisplayName("Should mark book as DNF correctly")
    void shouldMarkBookAsDNF() {
        // Given
        ReadingProgress progress = ReadingProgress.create(UUID.randomUUID(), UUID.randomUUID());
        progress.setProgress(0.5);
        
        // When
        progress.markAsDNF();
        
        // Then
        assertEquals(ReadingStatus.DNF, progress.getStatus());
        assertFalse(progress.getIsCompleted());
        assertNotNull(progress.getLastReadAt());
        assertEquals(2L, progress.getSyncVersion());
    }

    @Test
    @DisplayName("Should automatically set started_at when first progress update occurs")
    void shouldAutoSetStartedAtOnFirstProgressUpdate() {
        // Given
        ReadingProgress progress = ReadingProgress.create(UUID.randomUUID(), UUID.randomUUID());
        assertNull(progress.getStartedAt());
        
        // When
        progress.updateProgress(0.1, 10, 100);
        
        // Then
        assertNotNull(progress.getStartedAt());
        assertEquals(ReadingStatus.READING, progress.getStatus());
    }

    @Test
    @DisplayName("Should not update started_at on subsequent progress updates")
    void shouldNotUpdateStartedAtOnSubsequentUpdates() {
        // Given
        ReadingProgress progress = ReadingProgress.create(UUID.randomUUID(), UUID.randomUUID());
        progress.updateProgress(0.1, 10, 100);
        LocalDateTime initialStartedAt = progress.getStartedAt();
        
        // When
        try {
            Thread.sleep(10); // Small delay to ensure time difference
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        progress.updateProgress(0.2, 20, 100);
        
        // Then
        assertEquals(initialStartedAt, progress.getStartedAt());
    }

    @Test
    @DisplayName("Should automatically mark as finished when progress reaches 1.0")
    void shouldAutoMarkAsFinishedWhenProgressReachesOne() {
        // Given
        ReadingProgress progress = ReadingProgress.create(UUID.randomUUID(), UUID.randomUUID());
        progress.setTotalPages(100);
        
        // When
        progress.updateProgress(1.0, 100, 100);
        
        // Then
        assertEquals(ReadingStatus.FINISHED, progress.getStatus());
        assertTrue(progress.getIsCompleted());
        assertNotNull(progress.getFinishedAt());
    }

    @Test
    @DisplayName("Should increment sync version on each update")
    void shouldIncrementSyncVersionOnEachUpdate() {
        // Given
        ReadingProgress progress = ReadingProgress.create(UUID.randomUUID(), UUID.randomUUID());
        long initialVersion = progress.getSyncVersion();
        
        // When
        progress.updateProgress(0.5, 50, 100);
        
        // Then
        assertEquals(initialVersion + 1, progress.getSyncVersion());
    }

    @Test
    @DisplayName("Should update last_read_at on progress update")
    void shouldUpdateLastReadAtOnProgressUpdate() {
        // Given
        ReadingProgress progress = ReadingProgress.create(UUID.randomUUID(), UUID.randomUUID());
        LocalDateTime beforeUpdate = LocalDateTime.now();
        
        // When
        progress.updateProgress(0.3, 30, 100);
        
        // Then
        assertNotNull(progress.getLastReadAt());
        assertTrue(progress.getLastReadAt().isAfter(beforeUpdate) || progress.getLastReadAt().isEqual(beforeUpdate));
    }

    @Test
    @DisplayName("Should parse reading status from string correctly")
    void shouldParseReadingStatusFromString() {
        // Test all valid statuses
        assertEquals(ReadingStatus.UNREAD, ReadingStatus.fromString("UNREAD"));
        assertEquals(ReadingStatus.READING, ReadingStatus.fromString("READING"));
        assertEquals(ReadingStatus.FINISHED, ReadingStatus.fromString("FINISHED"));
        assertEquals(ReadingStatus.DNF, ReadingStatus.fromString("DNF"));
        
        // Test case insensitivity
        assertEquals(ReadingStatus.READING, ReadingStatus.fromString("reading"));
        assertEquals(ReadingStatus.FINISHED, ReadingStatus.fromString("finished"));
    }

    @Test
    @DisplayName("Should return default status for null or blank string")
    void shouldReturnDefaultStatusForNullOrBlank() {
        assertEquals(ReadingStatus.READING, ReadingStatus.fromString(null));
        assertEquals(ReadingStatus.READING, ReadingStatus.fromString(""));
        assertEquals(ReadingStatus.READING, ReadingStatus.fromString("   "));
    }

    @Test
    @DisplayName("Should throw exception for invalid status string")
    void shouldThrowExceptionForInvalidStatus() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReadingStatus.fromString("INVALID_STATUS");
        });
    }
}
