import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { 
  BookMetadata, 
  MetadataSearchResult, 
  MetadataPreview, 
  ProviderStatus, 
  MetadataApplyRequest 
} from '../models/metadata.model';

@Injectable({
  providedIn: 'root'
})
export class MetadataService {
  private readonly baseUrl = `${environment.apiUrl}/api/metadata`;

  constructor(private http: HttpClient) {}

  /**
   * Search for metadata by ISBN from all external providers
   */
  searchByIsbn(isbn: string): Observable<BookMetadata[]> {
    return this.http.get<BookMetadata[]>(`${this.baseUrl}/search/isbn/${isbn}`);
  }

  /**
   * Search for metadata by title and author from all external providers
   */
  searchByTitle(title: string, author?: string): Observable<BookMetadata[]> {
    let params = new HttpParams().set('title', title);
    if (author) {
      params = params.set('author', author);
    }
    return this.http.get<BookMetadata[]>(`${this.baseUrl}/search/title`, { params });
  }

  /**
   * Get the best merged metadata result from all providers
   */
  getBestMetadata(isbn: string): Observable<BookMetadata> {
    return this.http.get<BookMetadata>(`${this.baseUrl}/best/${isbn}`);
  }

  /**
   * Preview metadata changes before applying them to a book
   */
  previewMetadataChanges(bookId: string, request: MetadataApplyRequest): Observable<MetadataPreview> {
    return this.http.post<MetadataPreview>(`${this.baseUrl}/preview/${bookId}`, request);
  }

  /**
   * Apply metadata to a book
   */
  applyMetadata(bookId: string, request: MetadataApplyRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/apply/${bookId}`, request);
  }

  /**
   * Get status of all metadata providers
   */
  getProviderStatus(): Observable<ProviderStatus[]> {
    return this.http.get<ProviderStatus[]>(`${this.baseUrl}/providers/status`);
  }

  /**
   * Test connection to a specific provider
   */
  testProvider(providerId: string): Observable<ProviderStatus> {
    return this.http.post<ProviderStatus>(`${this.baseUrl}/providers/${providerId}/test`, {});
  }

  /**
   * Get available metadata providers
   */
  getProviders(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/providers`);
  }

  /**
   * Merge multiple metadata results with confidence-based selection
   */
  mergeMetadata(results: BookMetadata[]): BookMetadata {
    if (results.length === 0) {
      return {};
    }
    
    if (results.length === 1) {
      return results[0];
    }

    // Sort by confidence score (highest first)
    const sortedResults = results.sort((a, b) => (b.confidence || 0) - (a.confidence || 0));
    
    const merged: BookMetadata = {};
    
    // Field precedence rules: prefer non-null values, longest descriptions, highest confidence
    this.mergeField(merged, sortedResults, 'title');
    this.mergeField(merged, sortedResults, 'subtitle');
    this.mergeField(merged, sortedResults, 'description', (values) => 
      values.reduce((longest, current) => 
        current && current.length > (longest?.length || 0) ? current : longest, '')
    );
    this.mergeField(merged, sortedResults, 'language');
    this.mergeField(merged, sortedResults, 'publishedDate');
    this.mergeField(merged, sortedResults, 'publisher');
    this.mergeField(merged, sortedResults, 'pages');
    this.mergeField(merged, sortedResults, 'isbn10');
    this.mergeField(merged, sortedResults, 'isbn13');
    this.mergeField(merged, sortedResults, 'googleBooksId');
    this.mergeField(merged, sortedResults, 'openLibraryId');
    this.mergeField(merged, sortedResults, 'goodreadsId');
    this.mergeField(merged, sortedResults, 'previewLink');
    this.mergeField(merged, sortedResults, 'infoLink');
    this.mergeField(merged, sortedResults, 'maturityRating');
    this.mergeField(merged, sortedResults, 'printType');
    this.mergeField(merged, sortedResults, 'format');

    // Aggregate authors
    this.mergeArrayField(merged, sortedResults, 'authors');
    
    // Aggregate categories and subjects
    this.mergeArrayField(merged, sortedResults, 'categories');
    this.mergeArrayField(merged, sortedResults, 'subjects');

    // Image quality preference (larger images prioritized)
    this.mergeImageField(merged, sortedResults, 'imageUrl');
    this.mergeImageField(merged, sortedResults, 'thumbnailUrl');

    // Rating averaging and count summation
    this.mergeRatings(merged, sortedResults);

    return merged;
  }

  private mergeField(
    merged: BookMetadata, 
    results: BookMetadata[], 
    field: keyof BookMetadata,
    customMerge?: (values: any[]) => any
  ): void {
    const values = results.map(r => r[field]).filter(v => v != null && v !== '');
    if (values.length > 0) {
      merged[field] = customMerge ? customMerge(values) : values[0];
    }
  }

  private mergeArrayField(merged: BookMetadata, results: BookMetadata[], field: keyof BookMetadata): void {
    const allValues: any[] = [];
    results.forEach(r => {
      const value = r[field];
      if (Array.isArray(value)) {
        allValues.push(...value);
      }
    });
    const uniqueValues = [...new Set(allValues)];
    if (uniqueValues.length > 0) {
      (merged as any)[field] = uniqueValues;
    }
  }

  private mergeImageField(merged: BookMetadata, results: BookMetadata[], field: 'imageUrl' | 'thumbnailUrl'): void {
    const images = results.map(r => r[field]).filter(url => url) as string[];
    if (images.length > 0) {
      // Prefer images with dimensions in URL or assume larger based on provider priority
      merged[field] = images[0]; // First result has highest confidence
    }
  }

  private mergeRatings(merged: BookMetadata, results: BookMetadata[]): void {
    const ratings = results.filter(r => r.averageRating != null);
    const counts = results.filter(r => r.ratingsCount != null);
    
    if (ratings.length > 0) {
      const totalWeightedRating = ratings.reduce((sum, r) => 
        sum + (r.averageRating! * (r.ratingsCount || 1)), 0);
      const totalCount = ratings.reduce((sum, r) => sum + (r.ratingsCount || 1), 0);
      merged.averageRating = totalWeightedRating / totalCount;
    }
    
    if (counts.length > 0) {
      merged.ratingsCount = counts.reduce((sum, r) => sum + (r.ratingsCount || 0), 0);
    }
  }
}