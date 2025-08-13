import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { Series, SeriesPageResponse } from '../models/series.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SeriesService {
  private readonly baseUrl = `${environment.apiUrl}/v1/books/series`;

  constructor(private http: HttpClient) {}

  getAllSeries(cursor?: string, limit: number = 20): Observable<SeriesPageResponse> {
    let params = new HttpParams().set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    
    return this.http.get<SeriesPageResponse>(this.baseUrl, { params });
  }

  getSeriesById(id: string): Observable<Series> {
    return this.http.get<Series>(`${this.baseUrl}/${id}`);
  }

  getSeriesBooks(seriesName: string, cursor?: string, limit: number = 20): Observable<any> {
    // Use the books search endpoint with series criteria
    let params = new HttpParams().set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    
    const criteria = {
      seriesContains: seriesName,
      sortBy: 'seriesIndex',
      sortDirection: 'asc'
    };
    
    return this.http.post<any>(`${environment.apiUrl}/v1/books/criteria`, criteria, { params });
  }
}