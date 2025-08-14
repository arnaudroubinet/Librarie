import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Author, AuthorPageResponse, AuthorRequest } from '../models/author.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthorService {
  private readonly baseUrl = `${environment.apiUrl}/v1/authors`;

  constructor(private http: HttpClient) {}

  getAllAuthors(cursor?: string, limit: number = 20): Observable<AuthorPageResponse> {
    let params = new HttpParams().set('limit', limit.toString());
    
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    
    return this.http.get<AuthorPageResponse>(this.baseUrl, { params });
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
    
    return this.http.get<AuthorPageResponse>(`${this.baseUrl}/search`, { params });
  }

  // Simple search for unified search (returns array)
  searchAuthorsSimple(query: string): Observable<Author[]> {
    const params = new HttpParams().set('q', query).set('limit', '10');
    return this.http.get<Author[]>(`${this.baseUrl}/search`, { params });
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
}