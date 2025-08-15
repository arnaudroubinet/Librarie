import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Series, SeriesPageResponse } from '../models/series.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SeriesService {
  private readonly baseUrl = `${environment.apiUrl}/v1/books/series`;
  private readonly ttlMs = 5 * 60 * 1000; // 5 minutes
  private cache = new Map<string, { timestamp: number; data: any }>();

  constructor(private http: HttpClient) {}

  getAllSeries(cursor?: string, limit: number = 20): Observable<SeriesPageResponse> {
    let params = new HttpParams().set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    const key = `getAllSeries|cursor=${cursor || ''}|limit=${limit}`;
    const cached = this.cache.get(key);
    if (cached && Date.now() - cached.timestamp < this.ttlMs) {
      return of(cached.data as SeriesPageResponse);
    }
    // Return API response as-is (no demo image mapping)
    return this.http.get<SeriesPageResponse>(this.baseUrl, { params }).pipe(
      tap(res => this.cache.set(key, { timestamp: Date.now(), data: res }))
    );
  }

  getSeriesById(id: string): Observable<Series> {
    return this.http.get<Series>(`${this.baseUrl}/${id}`);
  }

  getSeriesBooks(seriesName: string, cursor?: string, limit: number = 20): Observable<any> {
    // Since the criteria search isn't working, let's get all books and filter client-side
    // This is a temporary workaround - in production you'd want to fix the backend search
    const key = `getSeriesBooks|name=${seriesName}|cursor=${cursor || ''}|limit=${limit}`;
    const cached = this.cache.get(key);
    if (cached && Date.now() - cached.timestamp < this.ttlMs) {
      return of(cached.data);
    }

    return this.http.get<any>(`${environment.apiUrl}/v1/books?limit=100`).pipe(
      map((response: any) => {
        const filteredBooks = response.content.filter((book: any) => 
          book.series && book.series === seriesName
        );
        
        // Sort by series index
        filteredBooks.sort((a: any, b: any) => {
          const aIndex = a.seriesIndex || 0;
          const bIndex = b.seriesIndex || 0;
          return aIndex - bIndex;
        });
        
  const data = {
          content: filteredBooks,
          nextCursor: null,
          previousCursor: null,
          limit: limit,
          hasNext: false,
          hasPrevious: false,
          totalCount: filteredBooks.length
  };

  // cache the computed response
  this.cache.set(key, { timestamp: Date.now(), data });
  return data;
      })
    );
  }

  // Note: demo image mapping removed; relies on backend-provided image fields only
  clearCache() { this.cache.clear(); }
}