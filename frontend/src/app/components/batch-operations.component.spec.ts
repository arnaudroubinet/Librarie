import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { BatchOperationsComponent } from './batch-operations.component';
import { BatchService } from '../services/batch.service';
import { BatchOperation, BatchOperationType, BatchOperationStatus } from '../models/batch.model';

describe('BatchOperationsComponent', () => {
  let component: BatchOperationsComponent;
  let fixture: ComponentFixture<BatchOperationsComponent>;
  let batchServiceSpy: jasmine.SpyObj<BatchService>;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;

  const mockOperations: BatchOperation[] = [
    {
      id: 'op-1',
      type: BatchOperationType.EDIT,
      status: BatchOperationStatus.COMPLETED,
      totalBooks: 5,
      processedBooks: 5,
      successfulOperations: 5,
      failedOperations: 0,
      startTime: '2025-08-24T12:00:00Z',
      endTime: '2025-08-24T12:01:30Z'
    },
    {
      id: 'op-2',
      type: BatchOperationType.DELETE,
      status: BatchOperationStatus.RUNNING,
      totalBooks: 3,
      processedBooks: 2,
      successfulOperations: 2,
      failedOperations: 0,
      startTime: '2025-08-24T12:05:00Z'
    },
    {
      id: 'op-3',
      type: BatchOperationType.EDIT,
      status: BatchOperationStatus.FAILED,
      totalBooks: 10,
      processedBooks: 7,
      successfulOperations: 5,
      failedOperations: 2,
      startTime: '2025-08-24T11:50:00Z',
      endTime: '2025-08-24T11:52:15Z',
      errors: ['Failed to update book-3', 'Failed to update book-8']
    }
  ];

  beforeEach(async () => {
    const batchSpy = jasmine.createSpyObj('BatchService', [
      'getRecentBatchOperations',
      'getBatchOperation',
      'cancelBatchOperation',
      'isOperationRunning',
      'isOperationCompleted',
      'isOperationFailed',
      'isOperationCancelled',
      'getOperationProgress',
      'getOperationSuccessRate',
      'getOperationDuration',
      'getStatusDisplayText',
      'getStatusCssClass'
    ]);
    const snackBarSpyObj = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [BatchOperationsComponent, NoopAnimationsModule],
      providers: [
        { provide: BatchService, useValue: batchSpy },
        { provide: MatSnackBar, useValue: snackBarSpyObj }
      ]
    }).compileComponents();

    batchServiceSpy = TestBed.inject(BatchService) as jasmine.SpyObj<BatchService>;
    snackBarSpy = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;

    // Set up default spy behaviors
    batchServiceSpy.getRecentBatchOperations.and.returnValue(of(mockOperations));
    batchServiceSpy.isOperationRunning.and.callFake((status) => status === BatchOperationStatus.RUNNING);
    batchServiceSpy.isOperationCompleted.and.callFake((status) => status === BatchOperationStatus.COMPLETED);
    batchServiceSpy.isOperationFailed.and.callFake((status) => status === BatchOperationStatus.FAILED);
    batchServiceSpy.isOperationCancelled.and.callFake((status) => status === BatchOperationStatus.CANCELLED);
    batchServiceSpy.getOperationProgress.and.callFake((op) => Math.round((op.processedBooks / op.totalBooks) * 100));
    batchServiceSpy.getOperationSuccessRate.and.callFake((op) => 
      op.processedBooks === 0 ? 0 : Math.round((op.successfulOperations / op.processedBooks) * 100)
    );
    batchServiceSpy.getOperationDuration.and.returnValue('1m 30s');
    batchServiceSpy.getStatusDisplayText.and.callFake((status) => status.toString());
    batchServiceSpy.getStatusCssClass.and.callFake((status) => `status-${status.toLowerCase()}`);
    batchServiceSpy.cancelBatchOperation.and.returnValue(of(void 0));
    snackBarSpy.open.and.returnValue({} as any);

    fixture = TestBed.createComponent(BatchOperationsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load recent operations on init', () => {
    fixture.detectChanges();
    
    expect(batchServiceSpy.getRecentBatchOperations).toHaveBeenCalledWith(10);
    expect(component.recentOperations()).toEqual(mockOperations);
  });

  it('should handle loading error', () => {
    batchServiceSpy.getRecentBatchOperations.and.returnValue(throwError(() => new Error('Failed to load')));
    
    fixture.detectChanges();
    
    expect(snackBarSpy.open).toHaveBeenCalledWith('Failed to load batch operations', 'Close', { duration: 3000 });
  });

  it('should reload operations manually', () => {
    component.recentOperations.set([]);
    
    component.loadRecentOperations();
    
    expect(batchServiceSpy.getRecentBatchOperations).toHaveBeenCalledWith(10);
    expect(component.recentOperations()).toEqual(mockOperations);
  });

  describe('service method delegation', () => {
    beforeEach(() => {
      component.recentOperations.set(mockOperations);
    });

    it('should delegate to batch service for status checks', () => {
      // Test that the component properly exposes the batch service
      expect(component.batchService).toBeDefined();
      expect(component.batchService.isOperationRunning).toBeDefined();
      expect(component.batchService.isOperationCompleted).toBeDefined();
      expect(component.batchService.isOperationFailed).toBeDefined();
    });

    it('should delegate to batch service for progress calculations', () => {
      expect(component.batchService.getOperationProgress).toBeDefined();
      expect(component.batchService.getOperationSuccessRate).toBeDefined();
      expect(component.batchService.getOperationDuration).toBeDefined();
    });

    it('should delegate to batch service for status display', () => {
      expect(component.batchService.getStatusDisplayText).toBeDefined();
      expect(component.batchService.getStatusCssClass).toBeDefined();
    });
  });

  describe('template integration', () => {
    it('should display operations when available', () => {
      component.recentOperations.set(mockOperations);
      fixture.detectChanges();
      
      const compiled = fixture.nativeElement;
      expect(compiled.querySelector('.operation-card')).toBeTruthy();
    });

    it('should show loading message when no operations', () => {
      component.recentOperations.set([]);
      fixture.detectChanges();
      
      const compiled = fixture.nativeElement;
      expect(compiled.textContent).toContain('No recent batch operations found');
    });
  });
});