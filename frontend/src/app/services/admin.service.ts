import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface BookAnomalyDTO {
  bookId: string;
  type: string;
  message: string;
  detail: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly baseUrl = `${environment.apiUrl}/api/admin`;

  constructor(private http: HttpClient) {}

  getBookAnomalies(): Observable<BookAnomalyDTO[]> {
    return this.http.get<BookAnomalyDTO[]>(`${this.baseUrl}/anomalies`);
  }
}
