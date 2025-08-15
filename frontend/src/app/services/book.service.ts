import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Book, CursorPageResponse, BookRequest, CompletionRequest, CompletionResponse, BookSearchCriteria } from '../models/book.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private readonly baseUrl = `${environment.apiUrl}/v1/books`;
  private readonly ttlMs = 5 * 60 * 1000; // 5 minutes
  private cache = new Map<string, { timestamp: number; data: any }>();

  constructor(private http: HttpClient) {}

  getAllBooks(cursor?: string, limit: number = 20): Observable<CursorPageResponse<Book>> {
    let params = new HttpParams().set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    const key = `getAllBooks|cursor=${cursor || ''}|limit=${limit}`;
    const cached = this.cache.get(key);
    if (cached && Date.now() - cached.timestamp < this.ttlMs) {
      return of(cached.data as CursorPageResponse<Book>);
    }
    return this.http.get<CursorPageResponse<Book>>(this.baseUrl, { params }).pipe(
      tap(res => this.cache.set(key, { timestamp: Date.now(), data: res }))
    );
  }

  getBookById(id: string): Observable<Book> {
    return this.http.get<Book>(`${this.baseUrl}/${id}`);
  }

  createBook(book: BookRequest): Observable<Book> {
    return this.http.post<Book>(this.baseUrl, book);
  }

  updateBook(id: string, book: BookRequest): Observable<Book> {
    return this.http.put<Book>(`${this.baseUrl}/${id}`, book);
  }

  deleteBook(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  searchBooks(query: string, cursor?: string, limit: number = 20): Observable<CursorPageResponse<Book>> {
    let params = new HttpParams()
      .set('q', query)
      .set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    const key = `searchBooks|q=${query}|cursor=${cursor || ''}|limit=${limit}`;
    const cached = this.cache.get(key);
    if (cached && Date.now() - cached.timestamp < this.ttlMs) {
      return of(cached.data as CursorPageResponse<Book>);
    }
    return this.http.get<CursorPageResponse<Book>>(`${this.baseUrl}/search`, { params }).pipe(
      tap(res => this.cache.set(key, { timestamp: Date.now(), data: res }))
    );
  }

  searchBooksByCriteria(criteria: BookSearchCriteria, cursor?: string, limit: number = 20): Observable<CursorPageResponse<Book>> {
    let params = new HttpParams().set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    const key = `searchBooksByCriteria|${JSON.stringify(criteria)}|cursor=${cursor || ''}|limit=${limit}`;
    const cached = this.cache.get(key);
    if (cached && Date.now() - cached.timestamp < this.ttlMs) {
      return of(cached.data as CursorPageResponse<Book>);
    }
    return this.http.post<CursorPageResponse<Book>>(`${this.baseUrl}/criteria`, criteria, { params }).pipe(
      tap(res => this.cache.set(key, { timestamp: Date.now(), data: res }))
    );
  }

  buildSearchCriteria(filters: any): BookSearchCriteria {
    return {
      titleContains: filters.title,
      contributorsContain: filters.authors,
      seriesContains: filters.series,
      languageEquals: filters.language,
      publisherContains: filters.publisher,
      publishedAfter: filters.publishedAfter,
      publishedBefore: filters.publishedBefore,
      formatsIn: filters.formats,
      descriptionContains: filters.description,
      isbnEquals: filters.isbn,
      sortBy: filters.sortField || 'createdAt',
      sortDirection: filters.sortOrder || 'desc'
    };
  }

  updateReadingCompletion(bookId: string, progress: number): Observable<CompletionResponse> {
    const completionData: CompletionRequest = { progress };
    return this.http.post<CompletionResponse>(`${this.baseUrl}/${bookId}/completion`, completionData);
  }

  clearCache() { this.cache.clear(); }
}