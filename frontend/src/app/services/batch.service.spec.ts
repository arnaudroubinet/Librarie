import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BatchService } from './batch.service';
import { 
  BatchOperationRequest,
  BatchPreviewRequest,
  BatchPreviewResult,
  BatchOperationResult,
  BatchOperation,
  BatchOperationStatus,
  BatchOperationType,
  BatchEditRequest,
  BookPreview,
  FieldChange
} from '../models/batch.model';
import { environment } from '../../environments/environment';

describe('BatchService', () => {
  let service: BatchService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/api/batch`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BatchService]
    });
    service = TestBed.inject(BatchService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('previewBatchEdit', () => {
    it('should preview batch edit changes', () => {
      const request: BatchPreviewRequest = {
        bookIds: ['book-1', 'book-2'],
        editRequest: {
          title: 'New Title',
          overwriteExisting: true
        }
      };
      const mockResult: BatchPreviewResult = {
        previewId: 'preview-123',
        bookPreviews: [
          {
            bookId: 'book-1',
            title: 'Book 1',
            changes: [
              {
                fieldName: 'title',
                currentValue: 'Old Title 1',
                proposedValue: 'New Title',
                changeType: 'UPDATE'
              }
            ]
          }
        ],
        totalBooks: 2,
        affectedFields: ['title']
      };

      service.previewBatchEdit(request).subscribe(result => {
        expect(result).toEqual(mockResult);
      });

      const req = httpMock.expectOne(`${baseUrl}/preview`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResult);
    });
  });

  describe('executeBatchEdit', () => {
    it('should execute batch edit operation', () => {
      const request: BatchOperationRequest = {
        bookIds: ['book-1', 'book-2'],
        editRequest: {
          title: 'New Title',
          overwriteExisting: true
        }
      };
      const mockResult: BatchOperationResult = {
        operationId: 'op-123',
        status: BatchOperationStatus.PENDING,
        totalBooks: 2,
        processedBooks: 0,
        successfulOperations: 0,
        failedOperations: 0,
        startTime: '2025-08-24T12:00:00Z'
      };

      service.executeBatchEdit(request).subscribe(result => {
        expect(result).toEqual(mockResult);
      });

      const req = httpMock.expectOne(`${baseUrl}/edit`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResult);
    });
  });

  describe('executeBatchDelete', () => {
    it('should execute batch delete operation', () => {
      const bookIds = ['book-1', 'book-2', 'book-3'];
      const mockResult: BatchOperationResult = {
        operationId: 'op-456',
        status: BatchOperationStatus.PENDING,
        totalBooks: 3,
        processedBooks: 0,
        successfulOperations: 0,
        failedOperations: 0,
        startTime: '2025-08-24T12:00:00Z'
      };

      service.executeBatchDelete(bookIds).subscribe(result => {
        expect(result).toEqual(mockResult);
      });

      const req = httpMock.expectOne(`${baseUrl}/delete`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ bookIds });
      req.flush(mockResult);
    });
  });

  describe('getBatchOperation', () => {
    it('should get batch operation details', () => {
      const operationId = 'op-123';
      const mockOperation: BatchOperation = {
        id: operationId,
        type: BatchOperationType.EDIT,
        status: BatchOperationStatus.COMPLETED,
        totalBooks: 5,
        processedBooks: 5,
        successfulOperations: 4,
        failedOperations: 1,
        startTime: '2025-08-24T12:00:00Z',
        endTime: '2025-08-24T12:01:30Z',
        errors: ['Failed to update book-3: Validation error']
      };

      service.getBatchOperation(operationId).subscribe(operation => {
        expect(operation).toEqual(mockOperation);
      });

      const req = httpMock.expectOne(`${baseUrl}/operations/${operationId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockOperation);
    });
  });

  describe('cancelBatchOperation', () => {
    it('should cancel batch operation', () => {
      const operationId = 'op-123';

      service.cancelBatchOperation(operationId).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/operations/${operationId}/cancel`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});
      req.flush(null);
    });
  });

  describe('getRecentBatchOperations', () => {
    it('should get recent operations without limit', () => {
      const mockOperations: BatchOperation[] = [
        {
          id: 'op-1',
          type: BatchOperationType.EDIT,
          status: BatchOperationStatus.COMPLETED,
          totalBooks: 3,
          processedBooks: 3,
          successfulOperations: 3,
          failedOperations: 0,
          startTime: '2025-08-24T12:00:00Z',
          endTime: '2025-08-24T12:00:30Z'
        }
      ];

      service.getRecentBatchOperations().subscribe(operations => {
        expect(operations).toEqual(mockOperations);
      });

      const req = httpMock.expectOne(`${baseUrl}/operations`);
      expect(req.request.method).toBe('GET');
      req.flush(mockOperations);
    });

    it('should get recent operations with limit', () => {
      const limit = 10;
      const mockOperations: BatchOperation[] = [];

      service.getRecentBatchOperations(limit).subscribe(operations => {
        expect(operations).toEqual(mockOperations);
      });

      const req = httpMock.expectOne(`${baseUrl}/operations?limit=${limit}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockOperations);
    });
  });

  describe('status checking methods', () => {
    it('should correctly identify running operations', () => {
      expect(service.isOperationRunning(BatchOperationStatus.PENDING)).toBe(true);
      expect(service.isOperationRunning(BatchOperationStatus.RUNNING)).toBe(true);
      expect(service.isOperationRunning(BatchOperationStatus.COMPLETED)).toBe(false);
      expect(service.isOperationRunning(BatchOperationStatus.FAILED)).toBe(false);
      expect(service.isOperationRunning(BatchOperationStatus.CANCELLED)).toBe(false);
    });

    it('should correctly identify completed operations', () => {
      expect(service.isOperationCompleted(BatchOperationStatus.COMPLETED)).toBe(true);
      expect(service.isOperationCompleted(BatchOperationStatus.PENDING)).toBe(false);
      expect(service.isOperationCompleted(BatchOperationStatus.RUNNING)).toBe(false);
      expect(service.isOperationCompleted(BatchOperationStatus.FAILED)).toBe(false);
      expect(service.isOperationCompleted(BatchOperationStatus.CANCELLED)).toBe(false);
    });

    it('should correctly identify failed operations', () => {
      expect(service.isOperationFailed(BatchOperationStatus.FAILED)).toBe(true);
      expect(service.isOperationFailed(BatchOperationStatus.PENDING)).toBe(false);
      expect(service.isOperationFailed(BatchOperationStatus.RUNNING)).toBe(false);
      expect(service.isOperationFailed(BatchOperationStatus.COMPLETED)).toBe(false);
      expect(service.isOperationFailed(BatchOperationStatus.CANCELLED)).toBe(false);
    });

    it('should correctly identify cancelled operations', () => {
      expect(service.isOperationCancelled(BatchOperationStatus.CANCELLED)).toBe(true);
      expect(service.isOperationCancelled(BatchOperationStatus.PENDING)).toBe(false);
      expect(service.isOperationCancelled(BatchOperationStatus.RUNNING)).toBe(false);
      expect(service.isOperationCancelled(BatchOperationStatus.COMPLETED)).toBe(false);
      expect(service.isOperationCancelled(BatchOperationStatus.FAILED)).toBe(false);
    });
  });

  describe('progress and metrics calculations', () => {
    it('should calculate operation progress correctly', () => {
      const operation: BatchOperation = {
        id: 'op-1',
        type: BatchOperationType.EDIT,
        status: BatchOperationStatus.RUNNING,
        totalBooks: 10,
        processedBooks: 7,
        successfulOperations: 6,
        failedOperations: 1,
        startTime: '2025-08-24T12:00:00Z'
      };

      expect(service.getOperationProgress(operation)).toBe(70);
    });

    it('should return 0 progress for zero total books', () => {
      const operation: BatchOperation = {
        id: 'op-1',
        type: BatchOperationType.EDIT,
        status: BatchOperationStatus.COMPLETED,
        totalBooks: 0,
        processedBooks: 0,
        successfulOperations: 0,
        failedOperations: 0,
        startTime: '2025-08-24T12:00:00Z'
      };

      expect(service.getOperationProgress(operation)).toBe(0);
    });

    it('should calculate success rate correctly', () => {
      const operation: BatchOperation = {
        id: 'op-1',
        type: BatchOperationType.EDIT,
        status: BatchOperationStatus.COMPLETED,
        totalBooks: 10,
        processedBooks: 10,
        successfulOperations: 8,
        failedOperations: 2,
        startTime: '2025-08-24T12:00:00Z'
      };

      expect(service.getOperationSuccessRate(operation)).toBe(80);
    });

    it('should return 0 success rate for zero processed books', () => {
      const operation: BatchOperation = {
        id: 'op-1',
        type: BatchOperationType.EDIT,
        status: BatchOperationStatus.PENDING,
        totalBooks: 10,
        processedBooks: 0,
        successfulOperations: 0,
        failedOperations: 0,
        startTime: '2025-08-24T12:00:00Z'
      };

      expect(service.getOperationSuccessRate(operation)).toBe(0);
    });
  });

  describe('duration formatting', () => {
    it('should format completed operation duration', () => {
      const operation: BatchOperation = {
        id: 'op-1',
        type: BatchOperationType.EDIT,
        status: BatchOperationStatus.COMPLETED,
        totalBooks: 5,
        processedBooks: 5,
        successfulOperations: 5,
        failedOperations: 0,
        startTime: '2025-08-24T12:00:00Z',
        endTime: '2025-08-24T12:01:30Z'
      };

      expect(service.getOperationDuration(operation)).toBe('1m 30s');
    });

    it('should handle running operation duration', () => {
      const now = new Date();
      const startTime = new Date(now.getTime() - 45000); // 45 seconds ago
      
      const operation: BatchOperation = {
        id: 'op-1',
        type: BatchOperationType.EDIT,
        status: BatchOperationStatus.RUNNING,
        totalBooks: 5,
        processedBooks: 3,
        successfulOperations: 3,
        failedOperations: 0,
        startTime: startTime.toISOString()
      };

      const duration = service.getOperationDuration(operation);
      expect(duration).toMatch(/\d+s/); // Should be some number of seconds
    });

    it('should return N/A for pending operations', () => {
      const operation: BatchOperation = {
        id: 'op-1',
        type: BatchOperationType.EDIT,
        status: BatchOperationStatus.PENDING,
        totalBooks: 5,
        processedBooks: 0,
        successfulOperations: 0,
        failedOperations: 0,
        startTime: '2025-08-24T12:00:00Z'
      };

      expect(service.getOperationDuration(operation)).toBe('N/A');
    });
  });

  describe('status display methods', () => {
    it('should return correct status display text', () => {
      expect(service.getStatusDisplayText(BatchOperationStatus.PENDING)).toBe('Pending');
      expect(service.getStatusDisplayText(BatchOperationStatus.RUNNING)).toBe('Running');
      expect(service.getStatusDisplayText(BatchOperationStatus.COMPLETED)).toBe('Completed');
      expect(service.getStatusDisplayText(BatchOperationStatus.FAILED)).toBe('Failed');
      expect(service.getStatusDisplayText(BatchOperationStatus.CANCELLED)).toBe('Cancelled');
    });

    it('should return correct CSS classes', () => {
      expect(service.getStatusCssClass(BatchOperationStatus.PENDING)).toBe('status-pending');
      expect(service.getStatusCssClass(BatchOperationStatus.RUNNING)).toBe('status-running');
      expect(service.getStatusCssClass(BatchOperationStatus.COMPLETED)).toBe('status-completed');
      expect(service.getStatusCssClass(BatchOperationStatus.FAILED)).toBe('status-failed');
      expect(service.getStatusCssClass(BatchOperationStatus.CANCELLED)).toBe('status-cancelled');
    });
  });
});