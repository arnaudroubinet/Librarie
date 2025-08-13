import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Author } from '../models/author.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthorService {
  private apiUrl = `${environment.apiUrl}/v1/authors`;

  constructor(private http: HttpClient) {}

  getAllAuthors(): Observable<Author[]> {
    return this.http.get<Author[]>(this.apiUrl);
  }

  getAuthorById(id: string): Observable<Author> {
    return this.http.get<Author>(`${this.apiUrl}/${id}`);
  }

  searchAuthors(query: string): Observable<Author[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<Author[]>(`${this.apiUrl}/search`, { params });
  }
}