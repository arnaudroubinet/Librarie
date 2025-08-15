import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Author, AuthorPageResponse, AuthorRequest } from '../models/author.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthorService {
  private readonly baseUrl = `${environment.apiUrl}/v1/authors`;
  private readonly ttlMs = 5 * 60 * 1000; // 5 minutes
  private cache = new Map<string, { timestamp: number; data: any }>();

  constructor(private http: HttpClient) {}

  getAllAuthors(cursor?: string, limit: number = 20): Observable<AuthorPageResponse> {
    let params = new HttpParams().set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    const key = `getAllAuthors|cursor=${cursor || ''}|limit=${limit}`;
    const cached = this.cache.get(key);
    if (cached && Date.now() - cached.timestamp < this.ttlMs) {
      return of(cached.data as AuthorPageResponse);
    }
    return this.http.get<AuthorPageResponse>(this.baseUrl, { params }).pipe(
      tap(res => this.cache.set(key, { timestamp: Date.now(), data: res }))
    );
  }

  getAuthorById(id: string): Observable<Author> {
    return this.http.get<Author>(`${this.baseUrl}/${id}`);
  }

  searchAuthors(query: string, cursor?: string, limit: number = 20): Observable<AuthorPageResponse> {
    let params = new HttpParams()
      .set('q', query)
      .set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    const key = `searchAuthors|q=${query}|cursor=${cursor || ''}|limit=${limit}`;
    const cached = this.cache.get(key);
    if (cached && Date.now() - cached.timestamp < this.ttlMs) {
      return of(cached.data as AuthorPageResponse);
    }
    return this.http.get<AuthorPageResponse>(`${this.baseUrl}/search`, { params }).pipe(
      tap(res => this.cache.set(key, { timestamp: Date.now(), data: res }))
    );
  }

  // Simple search for unified search (returns array)
  searchAuthorsSimple(query: string): Observable<Author[]> {
    const params = new HttpParams().set('q', query).set('limit', '10');
    const key = `searchAuthorsSimple|q=${query}`;
    const cached = this.cache.get(key);
    if (cached && Date.now() - cached.timestamp < this.ttlMs) {
      return of(cached.data as Author[]);
    }
    return this.http.get<Author[]>(`${this.baseUrl}/search`, { params }).pipe(
      tap(res => this.cache.set(key, { timestamp: Date.now(), data: res }))
    );
  }

  createAuthor(author: AuthorRequest): Observable<Author> {
    return this.http.post<Author>(this.baseUrl, author);
  }

  updateAuthor(id: string, author: AuthorRequest): Observable<Author> {
    return this.http.put<Author>(`${this.baseUrl}/${id}`, author);
  }

  deleteAuthor(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  clearCache() { this.cache.clear(); }
}