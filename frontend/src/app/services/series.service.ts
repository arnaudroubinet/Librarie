import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
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
}