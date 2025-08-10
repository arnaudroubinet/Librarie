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
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages?: number;
  first?: boolean;
  last?: boolean;
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