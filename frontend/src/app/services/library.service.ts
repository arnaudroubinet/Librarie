import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LibraryStats, ScanResult, SupportedFormatsResponse } from '../models/book.model';

@Injectable({
  providedIn: 'root'
})
export class LibraryService {
  private readonly baseUrl = 'http://localhost:8080/api/library';

  constructor(private http: HttpClient) {}

  getLibraryStats(): Observable<LibraryStats> {
    return this.http.get<LibraryStats>(`${this.baseUrl}/stats`);
  }

  getSupportedFormats(): Observable<SupportedFormatsResponse> {
    return this.http.get<SupportedFormatsResponse>(`${this.baseUrl}/supported-formats`);
  }

  refreshLibrary(): Observable<ScanResult> {
    return this.http.post<ScanResult>(`${this.baseUrl}/refresh`, {});
  }

  scanIngestDirectory(): Observable<ScanResult> {
    return this.http.post<ScanResult>(`${this.baseUrl}/scan`, {});
  }
}