export interface UploadConfig {
  maxFileSize: number;
  allowedExtensions: string[];
}

export interface UploadResult {
  status: 'SUCCESS' | 'DUPLICATE' | 'ERROR';
  message: string;
  bookId?: string;
  fileName?: string;
  fileSize?: number;
  fileHash?: string;
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