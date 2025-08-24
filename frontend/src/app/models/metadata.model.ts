export interface BookMetadata {
  title?: string;
  subtitle?: string;
  authors?: AuthorMetadata[];
  description?: string;
  language?: string;
  publishedDate?: string;
  publisher?: string;
  pages?: number;
  isbn10?: string;
  isbn13?: string;
  googleBooksId?: string;
  openLibraryId?: string;
  goodreadsId?: string;
  categories?: string[];
  subjects?: string[];
  thumbnailUrl?: string;
  imageUrl?: string;
  previewLink?: string;
  infoLink?: string;
  averageRating?: number;
  ratingsCount?: number;
  maturityRating?: string;
  printType?: string;
  format?: string;
  confidence?: number;
  provider?: string;
}

export interface AuthorMetadata {
  name: string;
  role?: string;
}

export interface MetadataSearchResult {
  results: BookMetadata[];
  providerResults: Map<string, BookMetadata[]>;
}

export interface MetadataPreview {
  bookId: string;
  currentMetadata: BookMetadata;
  proposedMetadata: BookMetadata;
  changes: FieldChange[];
  overwriteExisting: boolean;
}

export interface FieldChange {
  fieldName: string;
  currentValue?: any;
  proposedValue?: any;
  changeType: 'ADD' | 'UPDATE' | 'REMOVE';
}

export interface ProviderStatus {
  providerId: string;
  name: string;
  isAvailable: boolean;
  lastChecked: string;
  responseTime?: number;
  errorMessage?: string;
}

export interface MetadataApplyRequest {
  metadata: BookMetadata;
  overwriteExisting?: boolean;
}