import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book, PageResponse } from '../models/book.model';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private readonly baseUrl = 'http://localhost:8080/api/books';

  constructor(private http: HttpClient) {}

  getAllBooks(page: number = 0, size: number = 20): Observable<PageResponse<Book>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<Book>>(this.baseUrl, { params });
  }

  getBookById(id: string): Observable<Book> {
    return this.http.get<Book>(`${this.baseUrl}/${id}`);
  }

  searchBooks(query: string, page: number = 0, size: number = 20): Observable<PageResponse<Book>> {
    const params = new HttpParams()
      .set('q', query)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<Book>>(`${this.baseUrl}/search`, { params });
  }

  getBooksByAuthor(author: string, page: number = 0, size: number = 20): Observable<PageResponse<Book>> {
    const params = new HttpParams()
      .set('author', author)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<Book>>(`${this.baseUrl}/by-author`, { params });
  }

  getBooksBySeries(series: string, page: number = 0, size: number = 20): Observable<PageResponse<Book>> {
    const params = new HttpParams()
      .set('series', series)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PageResponse<Book>>(`${this.baseUrl}/by-series`, { params });
  }
}