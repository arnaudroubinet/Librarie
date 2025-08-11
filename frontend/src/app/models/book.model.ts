export interface Book {
  id: string;
  title: string;
  titleSort?: string;
  isbn?: string;
  path?: string;
  fileSize?: number;
  fileHash?: string;
  hasCover?: boolean;
  createdAt?: string;
  updatedAt?: string;
  publicationDate?: string;
  metadata?: Record<string, any>;
  language?: string;
  publisher?: string;
  // New fields based on updated DTO
  author?: string;
  series?: string;
  seriesIndex?: number;
  description?: string;
  formats?: string[];
}

export interface CursorPageResponse<T> {
  content: T[];
  nextCursor?: string;
  previousCursor?: string;
  limit: number;
  hasNext?: boolean;
  hasPrevious?: boolean;
  totalCount?: number;
}

// Legacy interface for backward compatibility
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages?: number;
  first?: boolean;
  last?: boolean;
}

export interface BookRequest {
  title: string;
  author?: string;
  isbn?: string;
  description?: string;
  series?: string;
  seriesIndex?: number;
}

export interface CompletionRequest {
  progress: number;
}

export interface CompletionResponse {
  bookId: string;
  progress: number;
  status: string;
  message: string;
}

export interface LibraryStats {
  supportedFormats: number;
  version: string;
  features: string[];
}

export interface ScanResult {
  status: string;
  message: string;
  ingestedBooks?: string[];
  count?: number;
  processedFiles?: number;
}

export interface SupportedFormatsResponse {
  supportedFormats: string[];
  count: number;
}