package org.motpassants.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.motpassants.domain.core.model.ReadingProgress;
import org.motpassants.domain.core.model.ReadingStatus;
import org.motpassants.domain.port.out.ReadingProgressRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReadingProgressService.
 * Tests enhanced reading progress tracking with status management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReadingProgressService Unit Tests")
class ReadingProgressServiceTest {

    @Mock
    private ReadingProgressRepository readingProgressRepository;

    private ReadingProgressService readingProgressService;

    private UUID userId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        readingProgressService = new ReadingProgressService(readingProgressRepository);
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should calculate progress percentage correctly")
    void shouldCalculateProgressPercentageCorrectly() {
        // Given
        ReadingProgress progress = new ReadingProgress();
        progress.setCurrentPage(150);
        progress.setTotalPages(300);
        
        // When
        double percentage = progress.getProgressPercentage();
        
        // Then
        assertEquals(50.0, percentage, 0.01);
    }

    @Test
    @DisplayName("Should mark book as finished correctly")
    void shouldMarkBookAsFinishedCorrectly() {
        // Given
        ReadingProgress progress = ReadingProgress.create(userId, bookId);
        progress.setTotalPages(300);
        
        // When
        progress.markAsFinished();
        
        // Then
        assertNotNull(progress.getFinishedAt());
        assertEquals(300, progress.getCurrentPage());
        assertEquals(ReadingStatus.FINISHED, progress.getStatus());
        assertTrue(progress.getIsCompleted());
        assertEquals(1.0, progress.getProgress());
    }

    @Test
    @DisplayName("Should mark book as started correctly")
    void shouldMarkBookAsStartedCorrectly() {
        // Given
        ReadingProgress progress = ReadingProgress.create(userId, bookId);
        
        // When
        progress.markAsStarted();
        
        // Then
        assertNotNull(progress.getStartedAt());
        assertEquals(ReadingStatus.READING, progress.getStatus());
    }

    @Test
    @DisplayName("Should mark book as DNF correctly")
    void shouldMarkBookAsDnfCorrectly() {
        // Given
        ReadingProgress progress = ReadingProgress.create(userId, bookId);
        progress.markAsStarted();
        
        // When
        progress.markAsDnf();
        
        // Then
        assertEquals(ReadingStatus.DNF, progress.getStatus());
        assertFalse(progress.getIsCompleted());
    }

    @Test
    @DisplayName("Should update reading progress with status")
    void shouldUpdateReadingProgressWithStatus() {
        // Given
        ReadingProgress existingProgress = ReadingProgress.create(userId, bookId);
        when(readingProgressRepository.findByUserIdAndBookId(userId, bookId))
            .thenReturn(Optional.of(existingProgress));
        when(readingProgressRepository.save(any(ReadingProgress.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        ReadingProgress result = readingProgressService.updateReadingProgressWithStatus(
            userId, bookId, 0.5, 150, 300, null, ReadingStatus.READING);
        
        // Then
        assertNotNull(result);
        assertEquals(0.5, result.getProgress());
        assertEquals(150, result.getCurrentPage());
        assertEquals(300, result.getTotalPages());
        assertEquals(ReadingStatus.READING, result.getStatus());
        verify(readingProgressRepository).save(any(ReadingProgress.class));
    }

    @Test
    @DisplayName("Should create new progress when marking as started")
    void shouldCreateNewProgressWhenMarkingAsStarted() {
        // Given
        when(readingProgressRepository.findByUserIdAndBookId(userId, bookId))
            .thenReturn(Optional.empty());
        when(readingProgressRepository.save(any(ReadingProgress.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        ReadingProgress result = readingProgressService.markAsStarted(userId, bookId);
        
        // Then
        assertNotNull(result);
        assertEquals(ReadingStatus.READING, result.getStatus());
        assertNotNull(result.getStartedAt());
        verify(readingProgressRepository).save(any(ReadingProgress.class));
    }

    @Test
    @DisplayName("Should mark existing progress as completed")
    void shouldMarkExistingProgressAsCompleted() {
        // Given
        ReadingProgress existingProgress = ReadingProgress.create(userId, bookId);
        existingProgress.setTotalPages(300);
        when(readingProgressRepository.findByUserIdAndBookId(userId, bookId))
            .thenReturn(Optional.of(existingProgress));
        when(readingProgressRepository.save(any(ReadingProgress.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        ReadingProgress result = readingProgressService.markAsCompleted(userId, bookId);
        
        // Then
        assertNotNull(result);
        assertEquals(ReadingStatus.FINISHED, result.getStatus());
        assertEquals(1.0, result.getProgress());
        assertTrue(result.getIsCompleted());
        assertNotNull(result.getFinishedAt());
        assertEquals(300, result.getCurrentPage());
        verify(readingProgressRepository).save(any(ReadingProgress.class));
    }

    @Test
    @DisplayName("Should throw exception when marking DNF without existing progress")
    void shouldThrowExceptionWhenMarkingDnfWithoutExistingProgress() {
        // Given
        when(readingProgressRepository.findByUserIdAndBookId(userId, bookId))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            readingProgressService.markAsDnf(userId, bookId);
        });
    }

    @Test
    @DisplayName("Should validate null user ID")
    void shouldValidateNullUserId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            readingProgressService.updateReadingProgress(null, bookId, 0.5, 100, 200, null);
        });
    }

    @Test
    @DisplayName("Should validate null book ID")
    void shouldValidateNullBookId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            readingProgressService.updateReadingProgress(userId, null, 0.5, 100, 200, null);
        });
    }

    @Test
    @DisplayName("Should validate progress range")
    void shouldValidateProgressRange() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            readingProgressService.updateReadingProgress(userId, bookId, 1.5, 100, 200, null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            readingProgressService.updateReadingProgress(userId, bookId, -0.1, 100, 200, null);
        });
    }

    @Test
    @DisplayName("Should auto-update status to READING when progress is updated from UNREAD")
    void shouldAutoUpdateStatusToReadingWhenProgressUpdated() {
        // Given
        ReadingProgress progress = ReadingProgress.create(userId, bookId);
        progress.setStatus(ReadingStatus.UNREAD);
        
        // When
        progress.updateProgress(0.25, 50, 200);
        
        // Then
        assertEquals(ReadingStatus.READING, progress.getStatus());
        assertNotNull(progress.getStartedAt());
    }

    @Test
    @DisplayName("Should auto-update status to FINISHED when progress reaches 100%")
    void shouldAutoUpdateStatusToFinishedWhenProgressReaches100() {
        // Given
        ReadingProgress progress = ReadingProgress.create(userId, bookId);
        progress.setStatus(ReadingStatus.READING);
        progress.setTotalPages(200);
        
        // When
        progress.updateProgress(1.0, 200, 200);
        
        // Then
        assertEquals(ReadingStatus.FINISHED, progress.getStatus());
        assertNotNull(progress.getFinishedAt());
    }

    @Test
    @DisplayName("Should increment sync version on update")
    void shouldIncrementSyncVersionOnUpdate() {
        // Given
        ReadingProgress progress = ReadingProgress.create(userId, bookId);
        long initialVersion = progress.getSyncVersion();
        
        // When
        progress.updateProgress(0.5, 100, 200);
        
        // Then
        assertEquals(initialVersion + 1, progress.getSyncVersion());
    }
}
