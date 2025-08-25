export interface UploadConfig {
  maxFileSize: number;
  allowedExtensions: string[];
}

export interface UploadResult {
  status: 'SUCCESS' | 'DUPLICATE' | 'ERROR' | 'VALIDATION_FAILED' | 'PROCESSING_FAILED';
  message: string;
  bookId?: string;
  fileName?: string;
  fileSize?: number;
  fileHash?: string;
  // Optional diagnostic fields
  errorCode?: string;
  detail?: string;
  traceId?: string;
}

export interface ValidationResult {
  isValid: boolean;
  errors: string[];
  fileName: string;
  fileSize: number;
  fileExtension: string;
}

export interface UploadProgress {
  loaded: number;
  total: number;
  percentage: number;
}