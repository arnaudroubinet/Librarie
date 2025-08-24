export interface BatchEditRequest {
  title?: string;
  subtitle?: string;
  description?: string;
  language?: string;
  publisher?: string;
  publishedDate?: string;
  format?: string;
  subjects?: string[];
  isbn10?: string;
  isbn13?: string;
  overwriteExisting?: boolean;
}

export interface BatchOperationRequest {
  bookIds: string[];
  editRequest?: BatchEditRequest;
}

export interface BatchPreviewRequest {
  bookIds: string[];
  editRequest: BatchEditRequest;
}

export interface BatchPreviewResult {
  previewId: string;
  bookPreviews: BookPreview[];
  totalBooks: number;
  affectedFields: string[];
}

export interface BookPreview {
  bookId: string;
  title: string;
  changes: FieldChange[];
}

export interface FieldChange {
  fieldName: string;
  currentValue?: any;
  proposedValue?: any;
  changeType: 'ADD' | 'UPDATE' | 'REMOVE';
}

export interface BatchOperationResult {
  operationId: string;
  status: BatchOperationStatus;
  totalBooks: number;
  processedBooks: number;
  successfulOperations: number;
  failedOperations: number;
  startTime: string;
  endTime?: string;
  errors?: string[];
  results?: string[];
}

export enum BatchOperationStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

export enum BatchOperationType {
  EDIT = 'EDIT',
  DELETE = 'DELETE'
}

export interface BatchOperation {
  id: string;
  type: BatchOperationType;
  status: BatchOperationStatus;
  totalBooks: number;
  processedBooks: number;
  successfulOperations: number;
  failedOperations: number;
  startTime: string;
  endTime?: string;
  errors?: string[];
}