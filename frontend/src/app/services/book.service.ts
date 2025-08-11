import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book, CursorPageResponse, BookRequest, CompletionRequest, CompletionResponse } from '../models/book.model';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private readonly baseUrl = 'http://localhost:8080/v1/books';

  constructor(private http: HttpClient) {}

  getAllBooks(cursor?: string, limit: number = 20): Observable<CursorPageResponse<Book>> {
    let params = new HttpParams().set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    
    return this.http.get<CursorPageResponse<Book>>(this.baseUrl, { params });
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
    
    return this.http.get<CursorPageResponse<Book>>(`${this.baseUrl}/search`, { params });
  }

  updateReadingCompletion(bookId: string, progress: number): Observable<CompletionResponse> {
    const completionData: CompletionRequest = { progress };
    return this.http.post<CompletionResponse>(`${this.baseUrl}/${bookId}/completion`, completionData);
  }
}