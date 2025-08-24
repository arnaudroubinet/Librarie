import { Component, OnInit, signal, computed, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { UploadService } from '../services/upload.service';
import { BookService } from '../services/book.service';
import { UploadConfig, UploadResult, UploadProgress } from '../models/upload.model';
import { Router } from '@angular/router';

interface UploadItem {
  file: File;
  progress: number;
  status: 'pending' | 'uploading' | 'success' | 'error' | 'duplicate';
  result?: UploadResult;
  error?: string;
}

interface UploadHistoryItem {
  filename: string;
  fileSize: number;
  status: 'success' | 'error' | 'duplicate';
  message?: string;
  bookId?: string;
  fileHash?: string | null;
  timestamp: number; // epoch ms
}

@Component({
  selector: 'app-upload',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressBarModule,
    MatIconModule,
    MatSnackBarModule,
    MatDividerModule,
    MatListModule
  ],
  template: `
    <div class="motspassants-library">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <iconify-icon class="title-icon" icon="material-symbols:cloud-upload"></iconify-icon>
            Upload Books
          </h1>
          <p class="library-subtitle">
            @if (config()) {
              Drag and drop or browse files to add to your library · Max {{ formatFileSize(config()!.maxFileSize) }} · Allowed: {{ config()!.allowedExtensions.join(', ').toUpperCase() }}
            } @else { Upload your books to the library }
          </p>
        </div>
      </div>

      <div class="library-content">
        <div class="upload-container">
          <mat-card class="upload-card dark-card">
            <mat-card-content>
              <!-- Drop Zone -->
              <div 
                class="drop-zone"
                [class.drag-over]="isDragOver()"
                [class.disabled]="isUploading()"
                (dragover)="onDragOver($event)"
                (dragleave)="onDragLeave($event)"
                (drop)="onDrop($event)"
                (click)="fileInput.click()">
                
                <div class="drop-zone-content">
                  @if (isUploading()) {
                    <iconify-icon class="upload-icon uploading" icon="material-symbols:cloud-upload"></iconify-icon>
                    <p>Uploading {{ uploadQueue().length }} file(s)...</p>
                    <mat-progress-bar mode="determinate" [value]="overallProgress()"></mat-progress-bar>
                  } @else {
                    <iconify-icon class="upload-icon" icon="material-symbols:cloud-upload"></iconify-icon>
                    <p class="primary-text">Drag and drop book files here</p>
                    <p class="secondary-text">or click to browse your files</p>
                    <button mat-button class="upload-icon-btn" type="button" aria-label="Choose files" (click)="$event.stopPropagation(); fileInput.click()">
                      <iconify-icon icon="material-symbols:folder-open-rounded"></iconify-icon>
                    </button>
                  }
                </div>
              </div>

              <input 
                #fileInput
                type="file" 
                multiple 
                accept=".epub,.pdf,.mobi,.azw,.azw3,.fb2,.txt,.rtf,.doc,.docx"
                (change)="onFileSelected($event)"
                style="display: none;">

              <!-- Upload Queue -->
              @if (uploadQueue().length > 0) {
                <mat-divider></mat-divider>
                <div class="upload-queue">
                  <h3>Upload Queue</h3>
                  
                  @for (item of uploadQueue(); track item.file.name + '|' + item.file.size) {
        <mat-card class="upload-item dark-card" [class]="'status-' + item.status">
                      <mat-card-content>
                        <div class="upload-item-header">
                          <div class="file-info">
          <iconify-icon [class]="getStatusIconClass(item.status)" [icon]="getStatusIcon(item.status)"></iconify-icon>
                            <div class="file-details">
                              <div class="file-name">{{ item.file.name }}</div>
                              <div class="file-size">{{ formatFileSize(item.file.size) }}</div>
                            </div>
                          </div>
                          
                          <div class="upload-actions">
                            @if (item.status === 'success' && item.result?.bookId) {
                              <button mat-button class="upload-icon-btn" aria-label="View book" (click)="viewBook(item.result!.bookId!)">
                                <iconify-icon icon="material-symbols:visibility-rounded"></iconify-icon>
                              </button>
                              <button mat-button class="upload-icon-btn" aria-label="Edit metadata" (click)="editBook(item.result!.bookId!)">
                                <iconify-icon icon="material-symbols:edit-rounded"></iconify-icon>
                              </button>
                            }
                            @if (item.status === 'pending' || item.status === 'error') {
                              <button mat-button class="upload-icon-btn" aria-label="Remove from queue" (click)="removeFromQueue(item)">
                                <iconify-icon icon="material-symbols:delete-outline-rounded"></iconify-icon>
                              </button>
                            }
                          </div>
                        </div>
                        
                        @if (item.status === 'uploading') {
                          <mat-progress-bar mode="determinate" [value]="item.progress"></mat-progress-bar>
                        }
                        
                        @if (item.error) {
                          <div class="error-message">{{ item.error }}</div>
                        }
                        
                        @if (item.result?.message) {
                          <div class="result-message" [class]="'result-' + item.result!.status.toLowerCase()">
                            {{ item.result!.message }}
                            @if (item.result?.detail) {
                              <div class="result-detail">{{ item.result!.detail }}</div>
                            }
                            @if (item.result?.traceId) {
                              <div class="result-trace">Trace ID: {{ item.result!.traceId }}</div>
                            }
                          </div>
                        }
                      </mat-card-content>
                    </mat-card>
                  }
                  
                  <div class="queue-actions">
                    <button mat-button class="upload-icon-btn" aria-label="Upload all" [disabled]="isUploading() || !hasValidFiles()" (click)="uploadAll()">
                      <iconify-icon icon="material-symbols:cloud-upload"></iconify-icon>
                    </button>
                    <button mat-button class="upload-icon-btn" aria-label="Clear queue" [disabled]="isUploading()" (click)="clearQueue()">
                      <iconify-icon icon="material-symbols:delete-outline-rounded"></iconify-icon>
                    </button>
                  </div>
                </div>
              }

              <!-- Upload History -->
              @if (uploadHistory().length > 0) {
                <mat-divider></mat-divider>
                <div class="upload-history">
                  <div class="history-header">
                    <h3>Recent Uploads</h3>
                    <button mat-button class="upload-icon-btn" aria-label="Clear history" (click)="clearHistory()">
                      <iconify-icon icon="material-symbols:delete-forever-rounded"></iconify-icon>
                    </button>
                  </div>
                  @for (h of uploadHistory(); track h.timestamp) {
                    <mat-card class="upload-item dark-card" [class]="'status-' + h.status">
                      <mat-card-content>
                        <div class="upload-item-header">
                          <div class="file-info">
                            <iconify-icon [class]="getStatusIconClass(h.status)" [icon]="getStatusIcon(h.status)"></iconify-icon>
                            <div class="file-details">
                              <div class="file-name">{{ h.filename }}</div>
                              <div class="file-size">{{ formatFileSize(h.fileSize) }} · {{ h.timestamp | date:'short' }}</div>
                            </div>
                          </div>
                          <div class="upload-actions">
                            @if (h.status === 'success' && h.bookId) {
                              <button mat-button class="upload-icon-btn" aria-label="View book" (click)="viewBook(h.bookId)">
                                <iconify-icon icon="material-symbols:visibility-rounded"></iconify-icon>
                              </button>
                              <button mat-button class="upload-icon-btn" aria-label="Edit metadata" (click)="editBook(h.bookId)">
                                <iconify-icon icon="material-symbols:edit-rounded"></iconify-icon>
                              </button>
                            }
                            <button mat-button class="upload-icon-btn" aria-label="Remove from history" (click)="removeHistoryEntry(h)">
                              <iconify-icon icon="material-symbols:delete-outline-rounded"></iconify-icon>
                            </button>
                          </div>
                        </div>
                        @if (h.message) {
                          <div class="result-message" [class]="'result-' + h.status">{{ h.message }}</div>
                        }
                      </mat-card-content>
                    </mat-card>
                  }
                </div>
              }
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./upload.component.css']
})
export class UploadComponent implements OnInit {
  config = signal<UploadConfig | null>(null);
  uploadQueue = signal<UploadItem[]>([]);
  isDragOver = signal(false);
  isUploading = signal(false);
  uploadHistory = signal<UploadHistoryItem[]>([]);

  overallProgress = computed(() => {
    const queue = this.uploadQueue();
    if (queue.length === 0) return 0;
    
    const totalProgress = queue.reduce((sum, item) => sum + item.progress, 0);
    return Math.round(totalProgress / queue.length);
  });

  constructor(
    private uploadService: UploadService,
    private bookService: BookService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUploadConfig();
  this.loadHistory();
  }

  private loadUploadConfig() {
    this.uploadService.getUploadConfig().subscribe({
      next: (config) => {
        this.config.set(config);
      },
      error: (error) => {
        console.error('Failed to load upload config:', error);
        this.snackBar.open('Failed to load upload configuration', 'Close', { duration: 3000 });
      }
    });
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(true);
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(false);
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(false);

    const files = Array.from(event.dataTransfer?.files || []);
    this.addFilesToQueue(files);
  }

  onFileSelected(event: any) {
    const files = Array.from(event.target.files as FileList || []);
    this.addFilesToQueue(files);
    event.target.value = ''; // Reset input
  }

  private addFilesToQueue(files: File[]) {
    const config = this.config();
    if (!config) return;

    const validFiles: UploadItem[] = [];
    const errors: string[] = [];

    files.forEach(file => {
      // Check file extension
      if (!this.uploadService.isFileExtensionAllowed(file.name, config.allowedExtensions)) {
        errors.push(`${file.name}: Invalid file type. Allowed: ${config.allowedExtensions.join(', ')}`);
        return;
      }

      // Check file size
      if (!this.uploadService.isFileSizeAllowed(file.size, config.maxFileSize)) {
        errors.push(`${file.name}: File too large. Max size: ${this.formatFileSize(config.maxFileSize)}`);
        return;
      }

      // Check for duplicates in queue
      const existing = this.uploadQueue().find(item => 
        item.file.name === file.name && item.file.size === file.size
      );
      if (existing) {
        errors.push(`${file.name}: Already in upload queue`);
        return;
      }

      validFiles.push({
        file,
        progress: 0,
        status: 'pending'
      });
    });

    if (validFiles.length > 0) {
  // Track which items we add so we can auto-upload just those
  const addedNames = new Set(validFiles.map(v => v.file.name + '|' + v.file.size));
  this.uploadQueue.update(queue => [...queue, ...validFiles]);
  // Auto-upload newly added files
  this.maybeStartUploads(addedNames);
    }

    if (errors.length > 0) {
      this.snackBar.open(errors.join('\\n'), 'Close', { duration: 5000 });
    }
  }

  async uploadAll() {
  if (this.isUploading()) return; // prevent re-entrancy
    const pendingItems = this.uploadQueue().filter(item => item.status === 'pending');
    if (pendingItems.length === 0) return;

    this.isUploading.set(true);

    for (const item of pendingItems) {
      await this.uploadSingleFile(item);
    }

    this.isUploading.set(false);
    this.bookService.clearCache(); // Refresh book list
  }

  private uploadSingleFile(item: UploadItem): Promise<void> {
    return new Promise((resolve) => {
      // Update item status
      this.uploadQueue.update(queue => 
        queue.map(q => q === item ? { ...q, status: 'uploading' as const } : q)
      );

      this.uploadService.uploadBookWithProgress(item.file).subscribe({
        next: (event) => {
          if ('percentage' in event) {
            // Progress update
            this.uploadQueue.update(queue => 
              queue.map(q => q === item ? { ...q, progress: event.percentage } : q)
            );
          } else {
            // Upload complete
            const result = event as UploadResult;
            const status = result.status === 'SUCCESS' ? 'success' : 
                          result.status === 'DUPLICATE' ? 'duplicate' : 'error';
            
            this.uploadQueue.update(queue => 
              queue.map(q => q === item ? { 
                ...q, 
                status, 
                progress: 100, 
                result 
              } : q)
            );

            // Persist to history
            this.appendHistory({
              filename: item.file.name,
              fileSize: item.file.size,
              status,
              message: result.message,
              bookId: result.bookId || undefined,
              fileHash: (result as any).fileHash ?? null,
              timestamp: Date.now()
            });

            if (result.status === 'SUCCESS') {
              this.snackBar.open(`Successfully uploaded: ${item.file.name}`, 'Close', { duration: 3000 });
            } else if (result.status === 'DUPLICATE') {
              this.snackBar.open(`Duplicate file detected: ${item.file.name}`, 'Close', { duration: 3000 });
            }

            // Auto-clean queue after completion (any terminal status)
            this.uploadQueue.update(queue => 
              queue.filter(q => !(q.file.name === item.file.name && q.file.size === item.file.size))
            );
            
            // Use setTimeout to ensure signal updates complete before resolving
            setTimeout(() => resolve(), 0);
          }
        },
        error: (error) => {
          const errBody: any = error?.error;
          const isDuplicate = error?.status === 409 
            || (errBody && typeof errBody === 'object' && (errBody.status === 'DUPLICATE' || /duplicate/i.test(errBody.message || '')));

          if (isDuplicate) {
            // Treat as duplicate terminal status
            this.uploadQueue.update(queue => 
              queue.map(q => q === item ? { 
                ...q, 
                status: 'duplicate' as const, 
                result: {
                  status: 'DUPLICATE',
                  message: errBody?.message || 'Duplicate file detected',
                  bookId: errBody?.bookId,
                  fileName: item.file.name,
                  fileSize: item.file.size,
                  fileHash: errBody?.fileHash
                } as any
              } : q)
            );

            this.snackBar.open(`Duplicate file detected: ${item.file.name}`, 'Close', { duration: 3000 });

            // Persist to history
            this.appendHistory({
              filename: item.file.name,
              fileSize: item.file.size,
              status: 'duplicate',
              message: errBody?.message || 'Duplicate file detected',
              bookId: errBody?.bookId,
              fileHash: errBody?.fileHash ?? null,
              timestamp: Date.now()
            });

            // Remove from queue
            this.uploadQueue.update(queue => 
              queue.filter(q => !(q.file.name === item.file.name && q.file.size === item.file.size))
            );

          } else {
            // Generic error handling
            this.uploadQueue.update(queue => 
              queue.map(q => q === item ? { 
                ...q, 
                status: 'error' as const, 
                error: errBody?.message || error.message || 'Upload failed',
                progress: 100
              } : q)
            );
            
            this.snackBar.open(`Upload failed: ${item.file.name}`, 'Close', { duration: 3000 });

            // Persist to history
            this.appendHistory({
              filename: item.file.name,
              fileSize: item.file.size,
              status: 'error',
              message: errBody?.message || error.message || 'Upload failed',
              fileHash: errBody?.fileHash ?? null,
              timestamp: Date.now()
            });

            // Remove from queue on generic errors as well
            this.uploadQueue.update(queue => 
              queue.filter(q => !(q.file.name === item.file.name && q.file.size === item.file.size))
            );
          }

          // Use setTimeout to ensure signal updates complete before resolving
          setTimeout(() => resolve(), 0);
        }
      });
    });
  }

  removeFromQueue(item: UploadItem) {
  // Compare by file reference to remove the correct (possibly updated) item
  this.uploadQueue.update(queue => queue.filter(q => q.file !== item.file));
  }

  clearQueue() {
    this.uploadQueue.set([]);
  }

  hasValidFiles(): boolean {
    return this.uploadQueue().some(item => item.status === 'pending');
  }

  viewBook(bookId: string) {
    this.router.navigate(['/books', bookId]);
  }

  editBook(bookId: string) {
    this.router.navigate(['/books', bookId, 'metadata']);
  }

  formatFileSize(bytes: number): string {
    return this.uploadService.formatFileSize(bytes);
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'pending': return 'material-symbols:schedule';
      case 'uploading': return 'material-symbols:cloud-upload';
      case 'success': return 'material-symbols:check-circle';
      case 'duplicate': return 'material-symbols:content-copy';
      case 'error': return 'material-symbols:error';
      default: return 'material-symbols:help';
    }
  }

  getStatusIconClass(status: string): string {
    switch (status) {
      case 'pending': return 'status-pending';
      case 'uploading': return 'status-uploading';
      case 'success': return 'status-success';
      case 'duplicate': return 'status-duplicate';
      case 'error': return 'status-error';
      default: return '';
    }
  }

  // Auto-upload helper: start uploads for newly added items
  private async maybeStartUploads(addedKeys: Set<string>) {
    // Find the specific newly added pending items
    const pendingNew = this.uploadQueue().filter(i => i.status === 'pending' && addedKeys.has(i.file.name + '|' + i.file.size));
    if (pendingNew.length === 0) return;
    if (this.isUploading()) return; // don't overlap
    this.isUploading.set(true);
    for (const item of pendingNew) {
      await this.uploadSingleFile(item);
    }
    this.isUploading.set(false);
    this.bookService.clearCache();
  }

  // Upload history persistence
  private loadHistory() {
    try {
      const raw = localStorage.getItem('uploadHistory');
      if (raw) {
        const parsed = JSON.parse(raw) as UploadHistoryItem[];
        this.uploadHistory.set(parsed);
      }
    } catch {}
  }

  private saveHistory() {
    try {
      const data = JSON.stringify(this.uploadHistory());
      localStorage.setItem('uploadHistory', data);
    } catch {}
  }

  private appendHistory(entry: UploadHistoryItem) {
    // keep last 50
    const next = [entry, ...this.uploadHistory()].slice(0, 50);
    this.uploadHistory.set(next);
    this.saveHistory();
  }

  clearHistory() {
    this.uploadHistory.set([]);
    try { localStorage.removeItem('uploadHistory'); } catch {}
  }

  removeHistoryEntry(entry: UploadHistoryItem) {
    this.uploadHistory.update(list => list.filter(e => e !== entry));
    this.saveHistory();
  }
}