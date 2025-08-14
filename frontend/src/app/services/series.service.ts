import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, switchMap } from 'rxjs';
import { Series, SeriesPageResponse } from '../models/series.model';
import { environment } from '../../environments/environment';
import { DemoImageService } from './demo-image.service';

@Injectable({
  providedIn: 'root'
})
export class SeriesService {
  private readonly baseUrl = `${environment.apiUrl}/v1/books/series`;

  constructor(private http: HttpClient, private demoImages: DemoImageService) {}

  getAllSeries(cursor?: string, limit: number = 20): Observable<SeriesPageResponse> {
    let params = new HttpParams().set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    // Fetch series and decorate with demo fallback images if missing
  return this.http.get<SeriesPageResponse>(this.baseUrl, { params }).pipe(
      switchMap((page) =>
        this.demoImages.getMapping().pipe(
          map((mapping) => ({
            ...page,
            content: (page.content || []).map((s) => ({
              ...s,
              fallbackImagePath:
                s.imagePath && s.imagePath.trim() !== ''
          ? s.fallbackImagePath
          : this.resolveFromMapping(mapping, s) || s.fallbackImagePath,
            })),
          }))
        )
      )
    );
  }

  getSeriesById(id: string): Observable<Series> {
    return this.http.get<Series>(`${this.baseUrl}/${id}`).pipe(
      switchMap((series) =>
        this.demoImages.getSeriesImage(series.id, series.name).pipe(
          map((img) => ({
            ...series,
            fallbackImagePath: series.imagePath ? series.fallbackImagePath : img || series.fallbackImagePath,
          }))
        )
      )
    );
  }

  getSeriesBooks(seriesName: string, cursor?: string, limit: number = 20): Observable<any> {
    // Since the criteria search isn't working, let's get all books and filter client-side
    // This is a temporary workaround - in production you'd want to fix the backend search
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
        
        return {
          content: filteredBooks,
          nextCursor: null,
          previousCursor: null,
          limit: limit,
          hasNext: false,
          hasPrevious: false,
          totalCount: filteredBooks.length
        };
      })
    );
  }

  private resolveFromMapping(mapping: Record<string, string>, s: Series): string | undefined {
    const byId = mapping[s.id];
    if (byId) return byId;
    if (s.name && mapping[s.name]) return mapping[s.name];
    if (s.name) {
      const normalized = s.name.trim().toLowerCase();
      for (const key of Object.keys(mapping)) {
        if (key.trim().toLowerCase() === normalized) {
          return mapping[key];
        }
      }
    }
    return undefined;
  }
}