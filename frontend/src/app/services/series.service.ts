import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Series, SeriesPageResponse } from '../models/series.model';
import { BookSortCriteria, SortField, SortDirection } from '../models/book.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SeriesService {
  private readonly baseUrl = `${environment.apiUrl}/v1/books/series`;
  private readonly ttlMs = 5 * 60 * 1000; // 5 minutes
  private cache = new Map<string, { timestamp: number; data: any }>();

  constructor(private http: HttpClient) {}

  getAllSeries(cursor?: string, limit: number = 20, sortCriteria?: BookSortCriteria): Observable<SeriesPageResponse> {
    let params = new HttpParams().set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }

    if (sortCriteria) {
      params = params.set('sortField', sortCriteria.field);
      params = params.set('sortDirection', sortCriteria.direction);
    }
    const key = `getAllSeries|cursor=${cursor || ''}|limit=${limit}|sort=${sortCriteria ? `${sortCriteria.field}_${sortCriteria.direction}` : 'default'}`;
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

  getSeriesBooksById(seriesId: string): Observable<any[]> {
    const key = `getSeriesBooksById|id=${seriesId}`;
    const cached = this.cache.get(key);
    if (cached && Date.now() - cached.timestamp < this.ttlMs) {
      return of(cached.data as any[]);
    }
    return this.http.get<any[]>(`${this.baseUrl}/${seriesId}/books`).pipe(
      tap(res => this.cache.set(key, { timestamp: Date.now(), data: res }))
    );
  }

  // Note: demo image mapping removed; relies on backend-provided image fields only
  clearCache() { this.cache.clear(); }
}