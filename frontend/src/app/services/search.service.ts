import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UnifiedSearchResult } from '../models/search.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SearchService {
  private apiUrl = `${environment.apiUrl}/v1/search`;

  constructor(private http: HttpClient) {}

  unifiedSearch(query: string, limit: number = 10): Observable<UnifiedSearchResult> {
    const params = new HttpParams()
      .set('q', query)
      .set('limit', limit.toString());
    
    return this.http.get<UnifiedSearchResult>(this.apiUrl, { params });
  }
}