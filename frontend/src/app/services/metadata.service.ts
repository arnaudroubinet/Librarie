import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Metadata search result from the API
 */
export interface MetadataSearchResult {
  title: string;
  subtitle?: string;
  authors?: string[];
  isbn10?: string;
  isbn13?: string;
  description?: string;
  pageCount?: number;
  publisher?: string;
  publishedDate?: string;
  language?: string;
  categories?: string[];
  coverImageUrl?: string;
  averageRating?: number;
  ratingsCount?: number;
  source: string;
  providerBookId: string;
  confidenceScore: number;
}

/**
 * Service for fetching book metadata from external providers
 */
@Injectable({
  providedIn: 'root'
})
export class MetadataService {
  private apiUrl = '/api/metadata';

  constructor(private http: HttpClient) {}

  /**
   * Search for metadata by ISBN
   * @param isbn The ISBN to search for (ISBN-10 or ISBN-13)
   * @returns Observable of metadata search results
   */
  searchByIsbn(isbn: string): Observable<MetadataSearchResult[]> {
    return this.http.get<MetadataSearchResult[]>(`${this.apiUrl}/search/isbn/${isbn}`);
  }

  /**
   * Search for metadata by title and optional author
   * @param title The book title
   * @param author Optional author name
   * @returns Observable of metadata search results
   */
  searchByTitleAndAuthor(title: string, author?: string): Observable<MetadataSearchResult[]> {
    const params: any = { title };
    if (author) {
      params.author = author;
    }
    return this.http.get<MetadataSearchResult[]>(`${this.apiUrl}/search/title`, { params });
  }
}
