import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { 
  BatchOperationRequest,
  BatchPreviewRequest,
  BatchPreviewResult,
  BatchOperationResult,
  BatchOperation,
  BatchOperationStatus 
} from '../models/batch.model';

@Injectable({
  providedIn: 'root'
})
export class BatchService {
  private readonly baseUrl = `${environment.apiUrl}/api/batch`;

  constructor(private http: HttpClient) {}

  /**
   * Preview batch edit changes before applying them
   */
  previewBatchEdit(request: BatchPreviewRequest): Observable<BatchPreviewResult> {
    return this.http.post<BatchPreviewResult>(`${this.baseUrl}/preview`, request);
  }

  /**
   * Execute a batch edit operation on multiple books
   */
  executeBatchEdit(request: BatchOperationRequest): Observable<BatchOperationResult> {
    return this.http.post<BatchOperationResult>(`${this.baseUrl}/edit`, request);
  }

  /**
   * Execute a batch delete operation on multiple books
   */
  executeBatchDelete(bookIds: string[]): Observable<BatchOperationResult> {
    const request = { bookIds };
    return this.http.post<BatchOperationResult>(`${this.baseUrl}/delete`, request);
  }

  /**
   * Get status and results of a specific batch operation
   */
  getBatchOperation(operationId: string): Observable<BatchOperation> {
    return this.http.get<BatchOperation>(`${this.baseUrl}/operations/${operationId}`);
  }

  /**
   * Cancel a running batch operation
   */
  cancelBatchOperation(operationId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/operations/${operationId}/cancel`, {});
  }

  /**
   * Get recent batch operations for the current user
   */
  getRecentBatchOperations(limit?: number): Observable<BatchOperation[]> {
    let params = new HttpParams();
    if (limit) {
      params = params.set('limit', limit.toString());
    }
    return this.http.get<BatchOperation[]>(`${this.baseUrl}/operations`, { params });
  }

  /**
   * Check if an operation is still running
   */
  isOperationRunning(status: BatchOperationStatus): boolean {
    return status === BatchOperationStatus.PENDING || status === BatchOperationStatus.RUNNING;
  }

  /**
   * Check if an operation completed successfully
   */
  isOperationCompleted(status: BatchOperationStatus): boolean {
    return status === BatchOperationStatus.COMPLETED;
  }

  /**
   * Check if an operation failed
   */
  isOperationFailed(status: BatchOperationStatus): boolean {
    return status === BatchOperationStatus.FAILED;
  }

  /**
   * Check if an operation was cancelled
   */
  isOperationCancelled(status: BatchOperationStatus): boolean {
    return status === BatchOperationStatus.CANCELLED;
  }

  /**
   * Get operation progress percentage
   */
  getOperationProgress(operation: BatchOperation): number {
    if (operation.totalBooks === 0) return 0;
    return Math.round((operation.processedBooks / operation.totalBooks) * 100);
  }

  /**
   * Get operation success rate percentage
   */
  getOperationSuccessRate(operation: BatchOperation): number {
    if (operation.processedBooks === 0) return 0;
    return Math.round((operation.successfulOperations / operation.processedBooks) * 100);
  }

  /**
   * Format operation duration
   */
  getOperationDuration(operation: BatchOperation): string {
    if (!operation.endTime) {
      if (operation.status === BatchOperationStatus.RUNNING) {
        const now = new Date();
        const start = new Date(operation.startTime);
        return this.formatDuration(now.getTime() - start.getTime());
      }
      return 'N/A';
    }
    
    const start = new Date(operation.startTime);
    const end = new Date(operation.endTime);
    return this.formatDuration(end.getTime() - start.getTime());
  }

  private formatDuration(milliseconds: number): string {
    const seconds = Math.floor(milliseconds / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);

    if (hours > 0) {
      return `${hours}h ${minutes % 60}m ${seconds % 60}s`;
    } else if (minutes > 0) {
      return `${minutes}m ${seconds % 60}s`;
    } else {
      return `${seconds}s`;
    }
  }

  /**
   * Get status display text
   */
  getStatusDisplayText(status: BatchOperationStatus): string {
    switch (status) {
      case BatchOperationStatus.PENDING:
        return 'Pending';
      case BatchOperationStatus.RUNNING:
        return 'Running';
      case BatchOperationStatus.COMPLETED:
        return 'Completed';
      case BatchOperationStatus.FAILED:
        return 'Failed';
      case BatchOperationStatus.CANCELLED:
        return 'Cancelled';
      default:
        return 'Unknown';
    }
  }

  /**
   * Get status CSS class for styling
   */
  getStatusCssClass(status: BatchOperationStatus): string {
    switch (status) {
      case BatchOperationStatus.PENDING:
        return 'status-pending';
      case BatchOperationStatus.RUNNING:
        return 'status-running';
      case BatchOperationStatus.COMPLETED:
        return 'status-completed';
      case BatchOperationStatus.FAILED:
        return 'status-failed';
      case BatchOperationStatus.CANCELLED:
        return 'status-cancelled';
      default:
        return 'status-unknown';
    }
  }
}