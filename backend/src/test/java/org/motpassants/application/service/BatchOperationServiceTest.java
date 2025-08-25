package org.motpassants.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.motpassants.domain.core.model.*;
import org.motpassants.domain.port.out.BatchOperationRepository;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.FileStorageService;
import org.motpassants.domain.port.out.LoggingPort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BatchOperationService.
 */
@ExtendWith(MockitoExtension.class)
class BatchOperationServiceTest {
    
    @Mock
    private BatchOperationRepository batchOperationRepository;
    
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private FileStorageService fileStorageService;
    
    @Mock
    private LoggingPort loggingPort;
    
    @InjectMocks
    private BatchOperationService batchOperationService;
    
    private UUID userId;
    private UUID bookId1;
    private UUID bookId2;
    private Book testBook1;
    private Book testBook2;
    
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bookId1 = UUID.randomUUID();
        bookId2 = UUID.randomUUID();
        
        testBook1 = createTestBook(bookId1, "Test Book 1", "Description 1");
        testBook2 = createTestBook(bookId2, "Test Book 2", "Description 2");
    }
    
    @Test
    void executeBatchEdit_WithValidRequest_ShouldUpdateBooks() {
        // Arrange
        var bookIds = List.of(bookId1, bookId2);
        var editRequest = BatchEditRequest.builder()
            .title("New Title")
            .description("New Description")
            .build();
        
        var operation = BatchOperation.createEdit(bookIds, editRequest, userId);
        
        when(bookRepository.findById(bookId1)).thenReturn(Optional.of(testBook1));
        when(bookRepository.findById(bookId2)).thenReturn(Optional.of(testBook2));
        when(batchOperationRepository.save(any(BatchOperation.class))).thenReturn(operation);
        when(batchOperationRepository.update(any(UUID.class), any(BatchOperation.class)))
            .thenAnswer(invocation -> invocation.getArgument(1));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        var result = batchOperationService.executeBatchEdit(bookIds, editRequest, userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(BatchOperationStatus.COMPLETED, result.status());
        assertEquals(2, result.results().size());
        assertTrue(result.results().stream().allMatch(BatchOperationResult::success));
        
        verify(bookRepository, times(2)).save(any(Book.class));
        verify(batchOperationRepository, times(1)).save(any(BatchOperation.class));
        verify(batchOperationRepository, times(2)).update(any(UUID.class), any(BatchOperation.class));
    }
    
    @Test
    void executeBatchEdit_WithEmptyBookIds_ShouldThrowException() {
        // Arrange
        var editRequest = BatchEditRequest.builder().title("New Title").build();
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> batchOperationService.executeBatchEdit(List.of(), editRequest, userId));
    }
    
    @Test
    void executeBatchEdit_WithNullEditRequest_ShouldThrowException() {
        // Arrange
        var bookIds = List.of(bookId1);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> batchOperationService.executeBatchEdit(bookIds, null, userId));
    }
    
    @Test
    void executeBatchEdit_WithNoChanges_ShouldThrowException() {
        // Arrange
        var bookIds = List.of(bookId1);
        var editRequest = BatchEditRequest.builder().build(); // No changes
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> batchOperationService.executeBatchEdit(bookIds, editRequest, userId));
    }
    
    @Test
    void executeBatchEdit_WithNonExistentBook_ShouldRecordFailure() {
        // Arrange
        var bookIds = List.of(bookId1, bookId2);
        var editRequest = BatchEditRequest.builder().title("New Title").build();
        var operation = BatchOperation.createEdit(bookIds, editRequest, userId);
        
        when(bookRepository.findById(bookId1)).thenReturn(Optional.of(testBook1));
        when(bookRepository.findById(bookId2)).thenReturn(Optional.empty()); // Book not found
        when(batchOperationRepository.save(any(BatchOperation.class))).thenReturn(operation);
        when(batchOperationRepository.update(any(UUID.class), any(BatchOperation.class)))
            .thenAnswer(invocation -> invocation.getArgument(1));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        var result = batchOperationService.executeBatchEdit(bookIds, editRequest, userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(BatchOperationStatus.PARTIAL, result.status());
        assertEquals(2, result.results().size());
        
        var results = result.results();
        assertTrue(results.get(0).success()); // First book should succeed
        assertFalse(results.get(1).success()); // Second book should fail
        assertEquals("Book not found", results.get(1).errorMessage());
    }
    
    @Test
    void executeBatchDelete_WithValidRequest_ShouldDeleteBooks() throws Exception {
        // Arrange
        var bookIds = List.of(bookId1, bookId2);
        var operation = BatchOperation.createDelete(bookIds, userId);
        
        when(bookRepository.findById(bookId1)).thenReturn(Optional.of(testBook1));
        when(bookRepository.findById(bookId2)).thenReturn(Optional.of(testBook2));
        when(batchOperationRepository.save(any(BatchOperation.class))).thenReturn(operation);
        when(batchOperationRepository.update(any(UUID.class), any(BatchOperation.class)))
            .thenAnswer(invocation -> invocation.getArgument(1));
        when(fileStorageService.deleteFile(anyString())).thenReturn(true);
        
        // Act
        var result = batchOperationService.executeBatchDelete(bookIds, userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(BatchOperationStatus.COMPLETED, result.status());
        assertEquals(2, result.results().size());
        assertTrue(result.results().stream().allMatch(BatchOperationResult::success));
        
        verify(bookRepository, times(2)).deleteById(any(UUID.class));
        verify(fileStorageService, times(2)).deleteFile(anyString());
    }
    
    @Test
    void previewBatchEdit_WithValidRequest_ShouldReturnPreviews() {
        // Arrange
        var bookIds = List.of(bookId1, bookId2);
        var editRequest = BatchEditRequest.builder()
            .title("New Title")
            .description("New Description")
            .build();
        
        when(bookRepository.findById(bookId1)).thenReturn(Optional.of(testBook1));
        when(bookRepository.findById(bookId2)).thenReturn(Optional.of(testBook2));
        
        // Act
        var result = batchOperationService.previewBatchEdit(bookIds, editRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        var preview1 = result.get(0);
        assertEquals(bookId1, preview1.bookId());
        assertEquals("Test Book 1", preview1.currentTitle());
        assertEquals("New Title", preview1.newTitle());
        assertTrue(preview1.hasChanges());
        
        var preview2 = result.get(1);
        assertEquals(bookId2, preview2.bookId());
        assertEquals("Test Book 2", preview2.currentTitle());
        assertEquals("New Title", preview2.newTitle());
        assertTrue(preview2.hasChanges());
    }
    
    @Test
    void previewBatchEdit_WithNoChanges_ShouldReturnNoChangePreviews() {
        // Arrange
        var bookIds = List.of(bookId1);
        var editRequest = BatchEditRequest.builder().build(); // No changes
        
        when(bookRepository.findById(bookId1)).thenReturn(Optional.of(testBook1));
        
        // Act
        var result = batchOperationService.previewBatchEdit(bookIds, editRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        var preview = result.get(0);
        assertEquals(bookId1, preview.bookId());
        assertEquals("Test Book 1", preview.currentTitle());
        assertEquals("Test Book 1", preview.newTitle()); // No change
        assertFalse(preview.hasChanges());
    }
    
    @Test
    void getBatchOperation_WithExistingId_ShouldReturnOperation() {
        // Arrange
        var operationId = UUID.randomUUID();
        var operation = BatchOperation.createEdit(List.of(bookId1), 
            BatchEditRequest.builder().title("Test").build(), userId);
        
        when(batchOperationRepository.findById(operationId)).thenReturn(Optional.of(operation));
        
        // Act
        var result = batchOperationService.getBatchOperation(operationId);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(operation, result.get());
    }
    
    @Test
    void getBatchOperation_WithNonExistentId_ShouldReturnEmpty() {
        // Arrange
        var operationId = UUID.randomUUID();
        
        when(batchOperationRepository.findById(operationId)).thenReturn(Optional.empty());
        
        // Act
        var result = batchOperationService.getBatchOperation(operationId);
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    void cancelBatchOperation_WithValidRequest_ShouldCancelOperation() {
        // Arrange
        var operationId = UUID.randomUUID();
        var operation = BatchOperation.createEdit(List.of(bookId1), 
            BatchEditRequest.builder().title("Test").build(), userId)
            .withResults(List.of(), BatchOperationStatus.RUNNING);
        
        when(batchOperationRepository.findById(operationId)).thenReturn(Optional.of(operation));
        when(batchOperationRepository.update(eq(operationId), any(BatchOperation.class)))
            .thenAnswer(invocation -> invocation.getArgument(1));
        
        // Act
        var result = batchOperationService.cancelBatchOperation(operationId, userId);
        
        // Assert
        assertTrue(result);
        verify(batchOperationRepository).update(eq(operationId), any(BatchOperation.class));
    }
    
    @Test
    void getRecentBatchOperations_ShouldReturnOperations() {
        // Arrange
        var operations = List.of(
            BatchOperation.createEdit(List.of(bookId1), 
                BatchEditRequest.builder().title("Test").build(), userId)
        );
        
        when(batchOperationRepository.findRecentByUserId(userId, 10)).thenReturn(operations);
        
        // Act
        var result = batchOperationService.getRecentBatchOperations(userId, 10);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(operations.get(0), result.get(0));
    }
    
    private Book createTestBook(UUID id, String title, String description) {
        var book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setDescription(description);
        book.setPath("/books/" + id + ".epub");
        book.setCreatedAt(OffsetDateTime.now());
        book.setUpdatedAt(OffsetDateTime.now());
        return book;
    }
}