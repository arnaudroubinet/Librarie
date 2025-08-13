import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { BookService } from '../services/book.service';
import { Book } from '../models/book.model';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSnackBarModule,
    MatDividerModule
  ],
  template: `
    <div class="book-detail-container">
      @if (loading()) {
        <div class="loading-container">
          <mat-spinner diameter="50"></mat-spinner>
          <p>Loading book details...</p>
        </div>
      } @else if (book()) {
        <div class="book-detail">
          <div class="back-button">
            <button mat-button (click)="goBack()">
              <mat-icon>arrow_back</mat-icon>
              Back to Books
            </button>
          </div>

          <mat-card class="book-card">
            <mat-card-header>
              <div mat-card-avatar class="book-avatar">
                <mat-icon>book</mat-icon>
              </div>
              <mat-card-title>{{ book()!.title }}</mat-card-title>
              <mat-card-subtitle>
                @if (book()!.titleSort && book()!.titleSort !== book()!.title) {
                  <span>Sort Title: {{ book()!.titleSort }}</span>
                }
              </mat-card-subtitle>
            </mat-card-header>

            <mat-card-content>
              <!-- Description Section (if available) -->
              @if (book()!.description) {
                <div class="description-section">
                  <h3>
                    <mat-icon>description</mat-icon>
                    Description
                  </h3>
                  <p class="book-description">{{ book()!.description }}</p>
                </div>
                <mat-divider></mat-divider>
              }

              <div class="book-info-grid">
                <!-- Basic Information -->
                <div class="info-section">
                  <h3>
                    <mat-icon>info</mat-icon>
                    Basic Information
                  </h3>
                  <div class="info-item">
                    <strong>Book ID:</strong>
                    <span class="book-id">{{ book()!.id }}</span>
                  </div>
                  @if (book()!.isbn) {
                    <div class="info-item">
                      <strong>ISBN:</strong>
                      <span>{{ book()!.isbn }}</span>
                    </div>
                  }
                  @if (book()!.language) {
                    <div class="info-item">
                      <strong>Language:</strong>
                      <mat-chip class="language-chip">{{ book()!.language }}</mat-chip>
                    </div>
                  }
                  @if (book()!.publisher) {
                    <div class="info-item">
                      <strong>Publisher:</strong>
                      <span>{{ book()!.publisher }}</span>
                    </div>
                  }
                  @if (book()!.publicationDate) {
                    <div class="info-item">
                      <strong>Publication Date:</strong>
                      <span>{{ formatDate(book()!.publicationDate!) }}</span>
                    </div>
                  }
                </div>

                <!-- Contributors -->
                @if (book()!.contributors && hasContributors()) {
                  <div class="info-section">
                    <h3>
                      <mat-icon>people</mat-icon>
                      Contributors
                    </h3>
                    @for (contributor of getContributorEntries(); track contributor.role) {
                      <div class="info-item">
                        <strong>{{ contributor.role }}:</strong>
                        <div class="contributors-list">
                          @for (person of contributor.people; track person) {
                            <mat-chip class="contributor-chip">{{ person }}</mat-chip>
                          }
                        </div>
                      </div>
                    }
                  </div>
                }

                <!-- Series Information -->
                @if (book()!.series) {
                  <div class="info-section">
                    <h3>
                      <mat-icon>library_books</mat-icon>
                      Series
                    </h3>
                    <div class="info-item">
                      <strong>Series Name:</strong>
                      <span>{{ book()!.series }}</span>
                    </div>
                    @if (book()!.seriesIndex) {
                      <div class="info-item">
                        <strong>Series Index:</strong>
                        <mat-chip class="series-index-chip">#{{ book()!.seriesIndex }}</mat-chip>
                      </div>
                    }
                  </div>
                }

                <!-- Formats -->
                @if (book()!.formats?.length) {
                  <div class="info-section">
                    <h3>
                      <mat-icon>file_copy</mat-icon>
                      Available Formats
                    </h3>
                    <div class="formats-container">
                      @for (format of book()!.formats; track format) {
                        <mat-chip class="format-chip">{{ format.toUpperCase() }}</mat-chip>
                      }
                    </div>
                  </div>
                }

                <!-- File Information -->
                <div class="info-section">
                  <h3>
                    <mat-icon>folder</mat-icon>
                    File Information
                  </h3>
                  @if (book()!.path) {
                    <div class="info-item">
                      <strong>File Path:</strong>
                      <span class="file-path">{{ book()!.path }}</span>
                    </div>
                  }
                  @if (book()!.fileSize) {
                    <div class="info-item">
                      <strong>File Size:</strong>
                      <span>{{ formatFileSize(book()!.fileSize!) }}</span>
                    </div>
                  }
                  @if (book()!.fileHash) {
                    <div class="info-item">
                      <strong>File Hash:</strong>
                      <span class="hash">{{ book()!.fileHash }}</span>
                    </div>
                  }
                  <div class="info-item">
                    <strong>Has Cover:</strong>
                    <mat-chip [style.background-color]="book()!.hasCover ? '#4caf50' : '#f44336'" 
                              [style.color]="'white'">
                      {{ book()!.hasCover ? 'Yes' : 'No' }}
                    </mat-chip>
                  </div>
                </div>

                <!-- Timestamps -->
                <div class="info-section">
                  <h3>
                    <mat-icon>schedule</mat-icon>
                    Timestamps
                  </h3>
                  @if (book()!.createdAt) {
                    <div class="info-item">
                      <strong>Added to Library:</strong>
                      <span>{{ formatDateTime(book()!.createdAt!) }}</span>
                    </div>
                  }
                  @if (book()!.updatedAt) {
                    <div class="info-item">
                      <strong>Last Updated:</strong>
                      <span>{{ formatDateTime(book()!.updatedAt!) }}</span>
                    </div>
                  }
                </div>

                <!-- Metadata -->
                @if (book()!.metadata && hasMetadata()) {
                  <div class="info-section full-width">
                    <h3>
                      <mat-icon>data_object</mat-icon>
                      Additional Metadata
                    </h3>
                    <div class="metadata-container">
                      @for (entry of getMetadataEntries(); track entry.key) {
                        <div class="metadata-item">
                          <strong>{{ entry.key }}:</strong>
                          <span>{{ entry.value }}</span>
                        </div>
                      }
                    </div>
                  </div>
                }
              </div>
            </mat-card-content>

            <mat-card-actions>
              <button mat-raised-button color="primary" disabled>
                <mat-icon>download</mat-icon>
                Download
              </button>
              @if (book()!.hasCover) {
                <button mat-button disabled>
                  <mat-icon>image</mat-icon>
                  View Cover
                </button>
              }
              <button mat-button disabled>
                <mat-icon>edit</mat-icon>
                Edit Metadata
              </button>
            </mat-card-actions>
          </mat-card>
        </div>
      } @else {
        <div class="error-state">
          <mat-icon style="font-size: 64px; height: 64px; width: 64px;">error</mat-icon>
          <h2>Book not found</h2>
          <p>The requested book could not be found.</p>
          <button mat-raised-button color="primary" (click)="goBack()">
            <mat-icon>arrow_back</mat-icon>
            Back to Books
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .book-detail-container {
      max-width: 900px;
      margin: 0 auto;
      padding: 16px;
    }

    .loading-container, .error-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 400px;
      text-align: center;
    }

    .loading-container p, .error-state p {
      margin-top: 16px;
      color: var(--text-secondary);
    }

    .error-state h2 {
      margin: 16px 0;
      color: var(--text-primary);
    }

    .description-section {
      margin-bottom: 24px;
    }

    .description-section h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0 0 16px 0;
      color: var(--primary-color);
      font-size: 18px;
    }

    .book-description {
      color: var(--text-primary);
      line-height: 1.6;
      margin: 0;
      padding: 16px;
      background-color: #f9f9f9;
      border-radius: 8px;
      border-left: 4px solid var(--primary-color);
    }

    .back-button {
      margin-bottom: 16px;
    }

    .book-card {
      max-width: 100%;
    }

    .book-avatar {
      background-color: var(--primary-color);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .book-info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 24px;
      margin-top: 16px;
    }

    .info-section {
      padding: 16px;
      background-color: #fafafa;
      border-radius: 8px;
    }

    .info-section.full-width {
      grid-column: 1 / -1;
    }

    .info-section h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0 0 16px 0;
      color: var(--primary-color);
      font-size: 16px;
    }

    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      padding: 8px 0;
      border-bottom: 1px solid #e0e0e0;
    }

    .info-item:last-child {
      border-bottom: none;
      margin-bottom: 0;
    }

    .info-item strong {
      color: var(--text-primary);
      min-width: 120px;
      text-align: left;
    }

    .info-item span {
      color: var(--text-secondary);
      text-align: right;
      word-break: break-all;
      max-width: 60%;
    }

    .book-id, .hash {
      font-family: monospace;
      font-size: 12px;
      background-color: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
    }

    .file-path {
      font-family: monospace;
      font-size: 12px;
    }

    .language-chip {
      font-size: 12px;
      height: 24px;
      line-height: 24px;
    }

    .contributors-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 4px;
    }

    .contributor-chip {
      font-size: 12px;
      height: 28px;
      line-height: 28px;
      background-color: #e3f2fd;
      color: #1976d2;
    }

    .series-index-chip {
      font-size: 12px;
      height: 24px;
      line-height: 24px;
      background-color: #f3e5f5;
      color: #7b1fa2;
      font-weight: bold;
    }

    .formats-container {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 8px;
    }

    .format-chip {
      font-size: 11px;
      height: 26px;
      line-height: 26px;
      background-color: #e8f5e8;
      color: #2e7d32;
      font-weight: bold;
      letter-spacing: 0.5px;
    }

    .metadata-container {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 12px;
    }

    .metadata-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
      padding: 8px;
      background-color: white;
      border-radius: 4px;
      border: 1px solid #e0e0e0;
    }

    .metadata-item strong {
      color: var(--primary-color);
      font-size: 12px;
      text-transform: uppercase;
    }

    .metadata-item span {
      color: var(--text-primary);
      word-break: break-word;
    }

    @media (max-width: 768px) {
      .book-detail-container {
        padding: 8px;
      }

      .description-section {
        margin-bottom: 16px;
      }

      .book-description {
        padding: 12px;
        font-size: 14px;
      }
      
      .book-info-grid {
        grid-template-columns: 1fr;
        gap: 16px;
      }
      
      .info-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
      }
      
      .info-item span {
        max-width: 100%;
        text-align: left;
      }

      .contributors-list {
        margin-top: 8px;
        gap: 6px;
      }

      .contributor-chip {
        font-size: 11px;
        height: 26px;
        line-height: 26px;
      }

      .formats-container {
        gap: 6px;
      }

      .format-chip {
        font-size: 10px;
        height: 24px;
        line-height: 24px;
      }
    }

    /* Tablet-specific styles */
    @media (min-width: 769px) and (max-width: 1024px) {
      .book-detail-container {
        max-width: 800px;
        padding: 12px;
      }

      .book-info-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 20px;
      }
    }
  `]
})
export class BookDetailComponent implements OnInit {
  book = signal<Book | null>(null);
  loading = signal(true);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadBookDetails(id);
    } else {
      this.loading.set(false);
    }
  }

  loadBookDetails(id: string) {
    this.loading.set(true);
    this.bookService.getBookById(id).subscribe({
      next: (book) => {
        this.book.set(book);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading book details:', error);
        this.snackBar.open('Failed to load book details.', 'Close', {
          duration: 3000
        });
        this.loading.set(false);
      }
    });
  }

  goBack() {
    this.router.navigate(['/books']);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  formatDateTime(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  hasMetadata(): boolean {
    return !!this.book()?.metadata && Object.keys(this.book()!.metadata!).length > 0;
  }

  getMetadataEntries(): Array<{key: string, value: any}> {
    if (!this.book()?.metadata) return [];
    return Object.entries(this.book()!.metadata!).map(([key, value]) => ({
      key,
      value: typeof value === 'object' ? JSON.stringify(value) : String(value)
    }));
  }

  hasContributors(): boolean {
    return !!this.book()?.contributors && Object.keys(this.book()!.contributors!).length > 0;
  }

  getContributorEntries(): Array<{role: string, people: string[]}> {
    if (!this.book()?.contributors) return [];
    return Object.entries(this.book()!.contributors!).map(([role, people]) => ({
      role: this.formatRole(role),
      people
    }));
  }

  private formatRole(role: string): string {
    // Format role names to be more readable
    return role.charAt(0).toUpperCase() + role.slice(1).toLowerCase().replace(/_/g, ' ');
  }
}