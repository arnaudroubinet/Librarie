import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ReadingProgressData {
  progress: number; // 0-100
  currentPage?: number;
  totalPages?: number;
  isCompleted: boolean;
  lastReadAt?: string;
}

export interface ReadingProgressRequest {
  progress: number;
  currentPage?: number;
  totalPages?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReadingProgressService {
  private http = inject(HttpClient);
  private baseUrl = environment.production ? '' : 'http://localhost:8080';
  
  // Signal for current reading progress
  private _currentProgress = signal<ReadingProgressData | null>(null);
  readonly currentProgress = this._currentProgress.asReadonly();

  // Subject for real-time progress updates
  private progressUpdateSubject = new BehaviorSubject<ReadingProgressData | null>(null);
  public progressUpdate$ = this.progressUpdateSubject.asObservable();

  /**
   * Get reading progress for a book
   */
  getReadingProgress(bookId: string): Observable<ReadingProgressData> {
    return this.http.get<ReadingProgressData>(`${this.baseUrl}/v1/books/${bookId}/progress`);
  }

  /**
   * Update reading progress for a book
   */
  updateReadingProgress(bookId: string, progress: ReadingProgressRequest): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post(`${this.baseUrl}/v1/books/${bookId}/completion`, progress, { headers });
  }

  /**
   * Set current reading progress in component state
   */
  setCurrentProgress(progress: ReadingProgressData | null): void {
    this._currentProgress.set(progress);
    this.progressUpdateSubject.next(progress);
  }

  /**
   * Save reading progress and update local state
   */
  saveProgress(bookId: string, progress: ReadingProgressRequest): Observable<any> {
    const result = this.updateReadingProgress(bookId, progress);
    
    // Update local state
    const progressData: ReadingProgressData = {
      progress: progress.progress,
      currentPage: progress.currentPage,
      totalPages: progress.totalPages,
      isCompleted: progress.progress >= 100,
      lastReadAt: new Date().toISOString()
    };
    
    this.setCurrentProgress(progressData);
    
    return result;
  }

  /**
   * Auto-save progress with debouncing
   */
  autoSaveProgress(bookId: string, progress: ReadingProgressRequest): void {
    // Debounce auto-save to avoid too many requests
    clearTimeout((this as any).autoSaveTimeout);
    (this as any).autoSaveTimeout = setTimeout(() => {
      this.saveProgress(bookId, progress).subscribe({
        next: () => console.log('Reading progress auto-saved'),
        error: (error) => console.error('Failed to auto-save reading progress:', error)
      });
    }, 2000); // Save after 2 seconds of inactivity
  }

  /**
   * Clear current progress
   */
  clearProgress(): void {
    this._currentProgress.set(null);
    this.progressUpdateSubject.next(null);
  }
}