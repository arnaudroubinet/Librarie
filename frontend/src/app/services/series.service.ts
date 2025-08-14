import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Series, SeriesPageResponse } from '../models/series.model';
import { BookService } from './book.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SeriesService {
  private readonly baseUrl = `${environment.apiUrl}/v1/books/series`;
  private bookService = inject(BookService);

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

  getSeriesByAuthor(authorName: string): Observable<Series[]> {
    // Get all books by the author, then extract unique series
    return this.bookService.getBooksByAuthor(authorName, undefined, 100).pipe(
      map(response => {
        const seriesMap = new Map<string, Series>();
        
        response.content.forEach(book => {
          if (book.series) {
            if (!seriesMap.has(book.series)) {
              // Create a basic series object from book data
              seriesMap.set(book.series, {
                id: '', // We don't have series ID from book data
                name: book.series,
                bookCount: 0,
                fallbackImagePath: book.hasCover ? `${book.path}/cover` : undefined
              });
            }
            
            // Increment book count for this series
            const series = seriesMap.get(book.series)!;
            series.bookCount++;
          }
        });
        
        return Array.from(seriesMap.values()).sort((a, b) => a.name.localeCompare(b.name));
      })
    );
  }
}