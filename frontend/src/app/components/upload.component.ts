import { Component, OnInit, signal, computed } from '@angular/core';
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

@Component({
  selector: 'app-upload',
  standalone: true,
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
    <div class="upload-container">
      <mat-card class="upload-card">
        <mat-card-header>
          <mat-card-title>
            <mat-icon>cloud_upload</mat-icon>
            Upload Books
          </mat-card-title>
          <mat-card-subtitle>
            @if (config()) {
              Drag and drop files here or click to browse. 
              Max size: {{ formatFileSize(config()!.maxFileSize) }}.
              Allowed: {{ config()!.allowedExtensions.join(', ').toUpperCase() }}
            }
          </mat-card-subtitle>
        </mat-card-header>

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
                <mat-icon class="upload-icon uploading">cloud_upload</mat-icon>
                <p>Uploading {{ uploadQueue().length }} file(s)...</p>
                <mat-progress-bar mode="determinate" [value]="overallProgress()"></mat-progress-bar>
              } @else {
                <mat-icon class="upload-icon">cloud_upload</mat-icon>
                <p class="primary-text">Drag and drop book files here</p>
                <p class="secondary-text">or click to browse your files</p>
                <button mat-raised-button color="primary">Choose Files</button>
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
              
              @for (item of uploadQueue(); track item.file.name) {
                <mat-card class="upload-item" [class]="'status-' + item.status">
                  <mat-card-content>
                    <div class="upload-item-header">
                      <div class="file-info">
                        <mat-icon [class]="getStatusIconClass(item.status)">
                          {{ getStatusIcon(item.status) }}
                        </mat-icon>
                        <div class="file-details">
                          <div class="file-name">{{ item.file.name }}</div>
                          <div class="file-size">{{ formatFileSize(item.file.size) }}</div>
                        </div>
                      </div>
                      
                      <div class="upload-actions">
                        @if (item.status === 'success' && item.result?.bookId) {
                          <button 
                            mat-button
                            color="primary" 
                            (click)="viewBook(item.result!.bookId!)">
                            View Book
                          </button>
                        }
                        @if (item.status === 'pending' || item.status === 'error') {
                          <button 
                            mat-button
                            color="warn" 
                            (click)="removeFromQueue(item)">
                            Remove
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
                      </div>
                    }
                  </mat-card-content>
                </mat-card>
              }
              
              <div class="queue-actions">
                <button 
                  mat-raised-button
                  color="primary" 
                  [disabled]="isUploading() || !hasValidFiles()"
                  (click)="uploadAll()">
                  Upload All
                </button>
                <button 
                  mat-button
                  color="warn" 
                  [disabled]="isUploading()"
                  (click)="clearQueue()">
                  Clear Queue
                </button>
              </div>
            </div>
          }
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styleUrls: ['./upload.component.css']
})
export class UploadComponent implements OnInit {
  config = signal<UploadConfig | null>(null);
  uploadQueue = signal<UploadItem[]>([]);
  isDragOver = signal(false);
  isUploading = signal(false);

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
      this.uploadQueue.update(queue => [...queue, ...validFiles]);
    }

    if (errors.length > 0) {
      this.snackBar.open(errors.join('\\n'), 'Close', { duration: 5000 });
    }
  }

  async uploadAll() {
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

            if (result.status === 'SUCCESS') {
              this.snackBar.open(`Successfully uploaded: ${item.file.name}`, 'Close', { duration: 3000 });
            } else if (result.status === 'DUPLICATE') {
              this.snackBar.open(`Duplicate file detected: ${item.file.name}`, 'Close', { duration: 3000 });
            }
            
            // Use setTimeout to ensure signal updates complete before resolving
            setTimeout(() => resolve(), 0);
          }
        },
        error: (error) => {
          this.uploadQueue.update(queue => 
            queue.map(q => q === item ? { 
              ...q, 
              status: 'error' as const, 
              error: error.error?.message || error.message || 'Upload failed' 
            } : q)
          );
          
          this.snackBar.open(`Upload failed: ${item.file.name}`, 'Close', { duration: 3000 });
          
          // Use setTimeout to ensure signal updates complete before resolving
          setTimeout(() => resolve(), 0);
        }
      });
    });
  }

  removeFromQueue(item: UploadItem) {
    this.uploadQueue.update(queue => queue.filter(q => q !== item));
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

  formatFileSize(bytes: number): string {
    return this.uploadService.formatFileSize(bytes);
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'pending': return 'schedule';
      case 'uploading': return 'cloud_upload';
      case 'success': return 'check_circle';
      case 'duplicate': return 'content_copy';
      case 'error': return 'error';
      default: return 'help';
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
}