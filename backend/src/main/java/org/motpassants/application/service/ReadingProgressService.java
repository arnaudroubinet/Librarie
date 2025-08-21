package org.motpassants.application.service;

import org.motpassants.domain.core.model.ReadingProgress;
import org.motpassants.domain.port.in.ReadingProgressUseCase;
import org.motpassants.domain.port.out.ReadingProgressRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service implementing reading progress management use cases.
 * This is the core business logic layer in the hexagonal architecture.
 * Orchestrates domain objects and outbound ports.
 */
@ApplicationScoped
public class ReadingProgressService implements ReadingProgressUseCase {
    
    private final ReadingProgressRepository readingProgressRepository;
    
    @Inject
    public ReadingProgressService(ReadingProgressRepository readingProgressRepository) {
        this.readingProgressRepository = readingProgressRepository;
    }
    
    @Override
    @Transactional
    public ReadingProgress updateReadingProgress(UUID userId, UUID bookId, Double progress, Integer currentPage, Integer totalPages) {
        // Validate inputs
        if (userId == null || bookId == null) {
            throw new IllegalArgumentException("User ID and Book ID cannot be null");
        }
        
        if (progress != null && (progress < 0.0 || progress > 1.0)) {
            throw new IllegalArgumentException("Progress must be between 0.0 and 1.0");
        }
        
        // Get existing reading progress or create new one
        Optional<ReadingProgress> existingProgress = readingProgressRepository.findByUserIdAndBookId(userId, bookId);
        
        ReadingProgress readingProgress;
        if (existingProgress.isPresent()) {
            readingProgress = existingProgress.get();
            readingProgress.updateProgress(progress, currentPage, totalPages);
        } else {
            readingProgress = ReadingProgress.create(userId, bookId);
            readingProgress.updateProgress(progress, currentPage, totalPages);
        }
        
        return readingProgressRepository.save(readingProgress);
    }
    
    @Override
    public Optional<ReadingProgress> getReadingProgress(UUID userId, UUID bookId) {
        if (userId == null || bookId == null) {
            throw new IllegalArgumentException("User ID and Book ID cannot be null");
        }
        
        return readingProgressRepository.findByUserIdAndBookId(userId, bookId);
    }
    
    @Override
    public List<ReadingProgress> getReadingProgressByUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        return readingProgressRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional
    public void deleteReadingProgress(UUID userId, UUID bookId) {
        if (userId == null || bookId == null) {
            throw new IllegalArgumentException("User ID and Book ID cannot be null");
        }
        
        readingProgressRepository.deleteByUserIdAndBookId(userId, bookId);
    }
    
    @Override
    @Transactional
    public ReadingProgress markAsCompleted(UUID userId, UUID bookId) {
        return updateReadingProgress(userId, bookId, 1.0, null, null);
    }
}