import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType, HttpRequest } from '@angular/common/http';
import { Observable, map, filter } from 'rxjs';
import { environment } from '../../environments/environment';
import { UploadConfig, UploadResult, ValidationResult, UploadProgress } from '../models/upload.model';

@Injectable({
  providedIn: 'root'
})
export class UploadService {
  private readonly baseUrl = `${environment.apiUrl}/api/upload`;

  constructor(private http: HttpClient) {}

  /**
   * Get upload configuration (max file size, allowed extensions)
   */
  getUploadConfig(): Observable<UploadConfig> {
    return this.http.get<UploadConfig>(`${this.baseUrl}/config`);
  }

  /**
   * Upload a book file through the complete ingestion pipeline
   */
  uploadBook(file: File): Observable<UploadResult> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<UploadResult>(`${this.baseUrl}/book`, formData);
  }

  /**
   * Upload a book file with progress tracking
   */
  uploadBookWithProgress(file: File): Observable<UploadProgress | UploadResult> {
    const formData = new FormData();
    formData.append('file', file);

    const req = new HttpRequest('POST', `${this.baseUrl}/book`, formData, {
      reportProgress: true
    });

    return this.http.request(req).pipe(
      map((event: HttpEvent<any>) => {
        switch (event.type) {
          case HttpEventType.UploadProgress:
            const progress: UploadProgress = {
              loaded: event.loaded || 0,
              total: event.total || 0,
              percentage: event.total ? Math.round(100 * event.loaded / event.total) : 0
            };
            return progress;
          case HttpEventType.Response:
            return event.body as UploadResult;
          default:
            // Filter out other events we don't need
            return null;
        }
      }),
      filter((event): event is UploadProgress | UploadResult => event !== null)
    );
  }

  /**
   * Validate a file without processing it
   */
  validateFile(file: File): Observable<ValidationResult> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<ValidationResult>(`${this.baseUrl}/validate`, formData);
  }

  /**
   * Check if file extension is allowed
   */
  isFileExtensionAllowed(fileName: string, allowedExtensions: string[]): boolean {
    const extension = fileName.split('.').pop()?.toLowerCase();
    return extension ? allowedExtensions.includes(extension) : false;
  }

  /**
   * Check if file size is within limits
   */
  isFileSizeAllowed(fileSize: number, maxFileSize: number): boolean {
    return fileSize <= maxFileSize;
  }

  /**
   * Format file size for display
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}