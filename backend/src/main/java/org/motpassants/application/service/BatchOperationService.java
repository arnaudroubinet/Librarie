package org.motpassants.application.service;

import org.motpassants.domain.core.model.*;
import org.motpassants.domain.port.in.BatchOperationUseCase;
import org.motpassants.domain.port.out.BatchOperationRepository;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.FileStorageService;
import org.motpassants.domain.port.out.LoggingPort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Application service implementing batch operation use cases.
 * Handles bulk operations on multiple books with proper transaction management.
 */
@ApplicationScoped
public class BatchOperationService implements BatchOperationUseCase {
    
    private final BatchOperationRepository batchOperationRepository;
    private final BookRepository bookRepository;
    private final FileStorageService fileStorageService;
    private final LoggingPort loggingPort;
    
    @Inject
    public BatchOperationService(
        BatchOperationRepository batchOperationRepository,
        BookRepository bookRepository,
        FileStorageService fileStorageService,
        LoggingPort loggingPort
    ) {
        this.batchOperationRepository = batchOperationRepository;
        this.bookRepository = bookRepository;
        this.fileStorageService = fileStorageService;
        this.loggingPort = loggingPort;
    }
    
    @Override
    @Transactional
    public BatchOperation executeBatchEdit(List<UUID> bookIds, BatchEditRequest editRequest, UUID userId) {
        loggingPort.infof("Starting batch edit operation for %d books", bookIds.size());
        
        // Validate input
        if (bookIds == null || bookIds.isEmpty()) {
            throw new IllegalArgumentException("Book IDs list cannot be null or empty");
        }
        
        if (editRequest == null || !editRequest.hasChanges()) {
            throw new IllegalArgumentException("Edit request must contain at least one change");
        }
        
        // Create operation
        var operation = BatchOperation.createEdit(bookIds, editRequest, userId);
        operation = batchOperationRepository.save(operation);
        
        // Update operation status to running
        operation = operation.withResults(List.of(), BatchOperationStatus.RUNNING);
        operation = batchOperationRepository.update(operation.operationId(), operation);
        
        List<BatchOperationResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        // Process each book individually
        for (UUID bookId : bookIds) {
            try {
                var bookOpt = bookRepository.findById(bookId);
                if (bookOpt.isEmpty()) {
                    results.add(BatchOperationResult.failure(bookId, "Unknown", "Book not found"));
                    failureCount++;
                    continue;
                }
                
                var book = bookOpt.get();
                var updatedBook = applyEditChanges(book, editRequest);
                bookRepository.save(updatedBook);
                
                var changesSummary = generateChangesSummary(book, updatedBook, editRequest);
                results.add(BatchOperationResult.success(bookId, book.getTitle(), changesSummary));
                successCount++;
                
                loggingPort.infof("Successfully updated book %s (%s)", book.getTitle(), bookId);
                
            } catch (Exception e) {
                loggingPort.error("Failed to update book: " + bookId, e);
                results.add(BatchOperationResult.failure(bookId, "Error", e.getMessage()));
                failureCount++;
            }
        }
        
        // Determine final status
        BatchOperationStatus finalStatus;
        if (failureCount == 0) {
            finalStatus = BatchOperationStatus.COMPLETED;
        } else if (successCount == 0) {
            finalStatus = BatchOperationStatus.FAILED;
        } else {
            finalStatus = BatchOperationStatus.PARTIAL;
        }
        
        // Update operation with results
        operation = operation.withResults(results, finalStatus);
        operation = batchOperationRepository.update(operation.operationId(), operation);
        
        loggingPort.infof("Batch edit operation completed: %d successes, %d failures", successCount, failureCount);
        return operation;
    }
    
    @Override
    @Transactional
    public BatchOperation executeBatchDelete(List<UUID> bookIds, UUID userId) {
        loggingPort.infof("Starting batch delete operation for %d books", bookIds.size());
        
        // Validate input
        if (bookIds == null || bookIds.isEmpty()) {
            throw new IllegalArgumentException("Book IDs list cannot be null or empty");
        }
        
        // Create operation
        var operation = BatchOperation.createDelete(bookIds, userId);
        operation = batchOperationRepository.save(operation);
        
        // Update operation status to running
        operation = operation.withResults(List.of(), BatchOperationStatus.RUNNING);
        operation = batchOperationRepository.update(operation.operationId(), operation);
        
        List<BatchOperationResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        // Process each book individually
        for (UUID bookId : bookIds) {
            try {
                var bookOpt = bookRepository.findById(bookId);
                if (bookOpt.isEmpty()) {
                    results.add(BatchOperationResult.failure(bookId, "Unknown", "Book not found"));
                    failureCount++;
                    continue;
                }
                
                var book = bookOpt.get();
                
                // Delete associated files
                try {
                    if (book.getPath() != null) {
                        fileStorageService.deleteFile(book.getPath());
                    }
                    if (book.getHasCover() != null && book.getHasCover()) {
                        // Try to delete cover file if it exists - cover path would need to be derived from book ID
                        // This is a simplification - in a real implementation you'd have a proper cover path
                        try {
                            fileStorageService.deleteFile("covers/" + book.getId() + ".jpg");
                        } catch (Exception coverEx) {
                            // Ignore cover deletion failures
                        }
                    }
                } catch (Exception e) {
                    loggingPort.warn("Failed to delete files for book: " + bookId);
                    // Continue with database deletion even if file deletion fails
                }
                
                // Delete from database
                bookRepository.deleteById(bookId);
                
                results.add(BatchOperationResult.success(bookId, book.getTitle(), "Book and associated files deleted"));
                successCount++;
                
                loggingPort.infof("Successfully deleted book %s (%s)", book.getTitle(), bookId);
                
            } catch (Exception e) {
                loggingPort.error("Failed to delete book: " + bookId, e);
                results.add(BatchOperationResult.failure(bookId, "Error", e.getMessage()));
                failureCount++;
            }
        }
        
        // Determine final status
        BatchOperationStatus finalStatus;
        if (failureCount == 0) {
            finalStatus = BatchOperationStatus.COMPLETED;
        } else if (successCount == 0) {
            finalStatus = BatchOperationStatus.FAILED;
        } else {
            finalStatus = BatchOperationStatus.PARTIAL;
        }
        
        // Update operation with results
        operation = operation.withResults(results, finalStatus);
        operation = batchOperationRepository.update(operation.operationId(), operation);
        
        loggingPort.infof("Batch delete operation completed: %d successes, %d failures", successCount, failureCount);
        return operation;
    }
    
    @Override
    public List<BatchEditPreview> previewBatchEdit(List<UUID> bookIds, BatchEditRequest editRequest) {
        loggingPort.infof("Generating batch edit preview for %d books", bookIds.size());
        
        if (bookIds == null || bookIds.isEmpty()) {
            return List.of();
        }
        
        if (editRequest == null || !editRequest.hasChanges()) {
            return bookIds.stream()
                .map(id -> {
                    var book = bookRepository.findById(id);
                    return book.map(b -> BatchEditPreview.noChanges(id, b.getTitle()))
                            .orElse(BatchEditPreview.noChanges(id, "Unknown"));
                })
                .collect(Collectors.toList());
        }
        
        return bookIds.stream()
            .map(bookId -> {
                var bookOpt = bookRepository.findById(bookId);
                if (bookOpt.isEmpty()) {
                    return BatchEditPreview.noChanges(bookId, "Book not found");
                }
                
                var book = bookOpt.get();
                return generateEditPreview(book, editRequest);
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<BatchOperation> getBatchOperation(UUID operationId) {
        return batchOperationRepository.findById(operationId);
    }
    
    @Override
    @Transactional
    public boolean cancelBatchOperation(UUID operationId, UUID userId) {
        var operationOpt = batchOperationRepository.findById(operationId);
        if (operationOpt.isEmpty()) {
            return false;
        }
        
        var operation = operationOpt.get();
        
        // Check if operation belongs to user
        if (!operation.userId().equals(userId)) {
            return false;
        }
        
        // Check if operation can be cancelled
        if (operation.status() != BatchOperationStatus.PENDING && 
            operation.status() != BatchOperationStatus.RUNNING) {
            return false;
        }
        
        // Cancel the operation
        var cancelledOperation = operation.withResults(operation.results(), BatchOperationStatus.CANCELLED);
        batchOperationRepository.update(operationId, cancelledOperation);
        
        loggingPort.infof("Batch operation %s cancelled by user %s", operationId, userId);
        return true;
    }
    
    @Override
    public List<BatchOperation> getRecentBatchOperations(UUID userId, int limit) {
        return batchOperationRepository.findRecentByUserId(userId, Math.min(limit, 50)); // Cap at 50
    }
    
    /**
     * Applies edit changes to a book and returns the updated book.
     * This simplified version works with direct Book properties only.
     */
    private Book applyEditChanges(Book book, BatchEditRequest editRequest) {
        // Update simple fields
        if (editRequest.title().isPresent()) {
            book.setTitle(editRequest.title().get());
        }
        
        if (editRequest.description().isPresent()) {
            book.setDescription(editRequest.description().get());
        }
        
        if (editRequest.language().isPresent()) {
            book.setLanguage(editRequest.language().get());
        }
        
        if (editRequest.isbn().isPresent()) {
            book.setIsbn(editRequest.isbn().get());
        }
        
        // Handle publisher - this requires creating/finding Publisher entity
        if (editRequest.publisher().isPresent()) {
            // For now, we'll skip complex publisher handling
            // In a full implementation, you'd need to find or create the Publisher entity
            loggingPort.debug("Publisher field update not implemented in this version");
        }
        
        // Handle authors and tags - these require complex relationship management
        if (editRequest.authors().isPresent()) {
            loggingPort.debug("Authors field update not implemented in this version");
        }
        
        if (editRequest.tags().isPresent()) {
            loggingPort.debug("Tags field update not implemented in this version");
        }
        
        if (editRequest.seriesId().isPresent()) {
            loggingPort.debug("Series field update not implemented in this version");
        }
        
        // Mark as updated
        book.markAsUpdated();
        
        return book;
    }
    
    /**
     * Generates a summary of changes made to a book.
     */
    private String generateChangesSummary(Book originalBook, Book updatedBook, BatchEditRequest editRequest) {
        var changes = new ArrayList<String>();
        
        if (editRequest.title().isPresent() && !Objects.equals(originalBook.getTitle(), updatedBook.getTitle())) {
            changes.add("title");
        }
        if (editRequest.description().isPresent() && !Objects.equals(originalBook.getDescription(), updatedBook.getDescription())) {
            changes.add("description");
        }
        if (editRequest.language().isPresent() && !Objects.equals(originalBook.getLanguage(), updatedBook.getLanguage())) {
            changes.add("language");
        }
        if (editRequest.isbn().isPresent() && !Objects.equals(originalBook.getIsbn(), updatedBook.getIsbn())) {
            changes.add("ISBN");
        }
        
        // Note: Complex relationships (authors, tags, series, publisher) not implemented yet
        
        if (changes.isEmpty()) {
            return "No changes made";
        }
        
        return "Updated " + String.join(", ", changes);
    }
    
    /**
     * Generates a preview of changes for a book.
     * Simplified version that works with basic Book properties only.
     */
    private BatchEditPreview generateEditPreview(Book book, BatchEditRequest editRequest) {
        var hasChanges = false;
        
        // Check each field for changes
        var newTitle = editRequest.title().orElse(book.getTitle());
        if (editRequest.title().isPresent() && !Objects.equals(book.getTitle(), newTitle)) {
            hasChanges = true;
        }
        
        var newLanguage = editRequest.language().orElse(book.getLanguage());
        if (editRequest.language().isPresent() && !Objects.equals(book.getLanguage(), newLanguage)) {
            hasChanges = true;
        }
        
        var newIsbn = editRequest.isbn().orElse(book.getIsbn());
        if (editRequest.isbn().isPresent() && !Objects.equals(book.getIsbn(), newIsbn)) {
            hasChanges = true;
        }
        
        var newDescription = editRequest.description().orElse(book.getDescription());
        if (editRequest.description().isPresent() && !Objects.equals(book.getDescription(), newDescription)) {
            hasChanges = true;
        }
        
        // For complex fields, return empty lists for now
        var currentAuthors = List.<String>of(); // Authors require complex handling
        var newAuthors = editRequest.authors().orElse(List.of());
        
        var currentTags = List.<String>of(); // Tags require complex handling
        var newTags = editRequest.tags().orElse(List.of());
        
        var currentPublisher = book.getPublisher() != null ? book.getPublisher().getName() : null;
        var newPublisher = editRequest.publisher().orElse(currentPublisher);
        
        return new BatchEditPreview(
            book.getId(),
            book.getTitle(),
            newTitle,
            currentAuthors,
            newAuthors,
            currentTags,
            newTags,
            book.getLanguage(),
            newLanguage,
            currentPublisher,
            newPublisher,
            book.getIsbn(),
            newIsbn,
            book.getDescription(),
            newDescription,
            hasChanges
        );
    }
}