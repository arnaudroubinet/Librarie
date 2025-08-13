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

          <!-- ISBNdb-style layout: two columns with cover on left, details on right -->
          <div class="book-layout">
            <!-- Book Cover Column -->
            <div class="book-cover-section">
              <div class="book-cover">
                @if (book()!.hasCover) {
                  <div class="cover-placeholder">
                    <mat-icon>image</mat-icon>
                    <span>Cover Available</span>
                  </div>
                } @else {
                  <div class="cover-placeholder no-cover">
                    <mat-icon>book</mat-icon>
                    <span>No Cover</span>
                  </div>
                }
              </div>
            </div>

            <!-- Book Details Column -->
            <div class="book-details-section">
              <h1 class="book-title">{{ book()!.title }}</h1>
              @if (book()!.titleSort && book()!.titleSort !== book()!.title) {
                <h2 class="book-subtitle">({{ book()!.titleSort }})</h2>
              }

              <div class="book-metadata">
                <!-- Full Title -->
                <div class="metadata-row">
                  <span class="metadata-label">Full Title:</span>
                  <span class="metadata-value">{{ book()!.title }}</span>
                </div>

                <!-- ISBN -->
                @if (book()!.isbn) {
                  <div class="metadata-row">
                    <span class="metadata-label">ISBN:</span>
                    <span class="metadata-value">{{ book()!.isbn }}</span>
                  </div>
                }

                <!-- ISBN13 (derived from ISBN if available) -->
                @if (book()!.isbn) {
                  <div class="metadata-row">
                    <span class="metadata-label">ISBN13:</span>
                    <span class="metadata-value">{{ formatISBN13(book()!.isbn!) }}</span>
                  </div>
                }

                <!-- Authors -->
                @if (getAuthors().length > 0) {
                  <div class="metadata-row">
                    <span class="metadata-label">{{ getAuthors().length > 1 ? 'Authors:' : 'Author:' }}</span>
                    <span class="metadata-value">{{ getAuthors().join(', ') }}</span>
                  </div>
                }

                <!-- Publisher -->
                @if (book()!.publisher) {
                  <div class="metadata-row">
                    <span class="metadata-label">Publisher:</span>
                    <span class="metadata-value">{{ book()!.publisher }}</span>
                  </div>
                }

                <!-- Edition -->
                @if (getEdition()) {
                  <div class="metadata-row">
                    <span class="metadata-label">Edition:</span>
                    <span class="metadata-value">{{ getEdition() }}</span>
                  </div>
                }

                <!-- Publish Date -->
                @if (book()!.publicationDate) {
                  <div class="metadata-row">
                    <span class="metadata-label">Publish Date:</span>
                    <span class="metadata-value">{{ formatDate(book()!.publicationDate!) }}</span>
                  </div>
                }

                <!-- Binding -->
                @if (getBinding()) {
                  <div class="metadata-row">
                    <span class="metadata-label">Binding:</span>
                    <span class="metadata-value">{{ getBinding() }}</span>
                  </div>
                }

                <!-- Pages -->
                @if (getPages()) {
                  <div class="metadata-row">
                    <span class="metadata-label">Pages:</span>
                    <span class="metadata-value">{{ getPages() }}</span>
                  </div>
                }

                <!-- Description/Synopsis -->
                @if (book()!.description) {
                  <div class="metadata-row">
                    <span class="metadata-label">Synopsis:</span>
                    <span class="metadata-value">{{ book()!.description }}</span>
                  </div>
                }

                <!-- Language -->
                @if (book()!.language) {
                  <div class="metadata-row">
                    <span class="metadata-label">Language:</span>
                    <span class="metadata-value">{{ book()!.language }}</span>
                  </div>
                }

                <!-- Series -->
                @if (book()!.series) {
                  <div class="metadata-row">
                    <span class="metadata-label">Series:</span>
                    <span class="metadata-value">{{ book()!.series }}{{ book()!.seriesIndex ? ' #' + book()!.seriesIndex : '' }}</span>
                  </div>
                }

                <!-- Formats -->
                @if (book()!.formats && book()!.formats!.length > 0) {
                  <div class="metadata-row">
                    <span class="metadata-label">Formats:</span>
                    <span class="metadata-value">{{ book()!.formats!.join(', ') }}</span>
                  </div>
                }

                <!-- Contributors (other than authors) -->
                @if (getOtherContributors().length > 0) {
                  @for (contributor of getOtherContributors(); track contributor.role) {
                    <div class="metadata-row">
                      <span class="metadata-label">{{ contributor.role }}:</span>
                      <span class="metadata-value">{{ contributor.names.join(', ') }}</span>
                    </div>
                  }
                }

                <!-- Dimensions -->
                @if (getDimensions()) {
                  <div class="metadata-row">
                    <span class="metadata-label">Dimensions:</span>
                    <span class="metadata-value">{{ getDimensions() }}</span>
                  </div>
                }

                <!-- Weight -->
                @if (getWeight()) {
                  <div class="metadata-row">
                    <span class="metadata-label">Weight:</span>
                    <span class="metadata-value">{{ getWeight() }}</span>
                  </div>
                }

                <!-- File Information -->
                @if (book()!.path) {
                  <div class="metadata-row">
                    <span class="metadata-label">File Path:</span>
                    <span class="metadata-value file-path">{{ book()!.path }}</span>
                  </div>
                }

                @if (book()!.fileSize) {
                  <div class="metadata-row">
                    <span class="metadata-label">File Size:</span>
                    <span class="metadata-value">{{ formatFileSize(book()!.fileSize!) }}</span>
                  </div>
                }
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="book-actions">
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
          </div>
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
      max-width: 1200px;
      margin: 0 auto;
      padding: 20px;
      font-family: Arial, sans-serif;
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

    .back-button {
      margin-bottom: 20px;
    }

    /* ISBNdb-style layout */
    .book-layout {
      display: flex;
      gap: 40px;
      margin-bottom: 30px;
    }

    /* Book Cover Section */
    .book-cover-section {
      flex: 0 0 200px;
    }

    .book-cover {
      width: 200px;
      height: 300px;
      border: 1px solid #ddd;
      border-radius: 4px;
      overflow: hidden;
    }

    .cover-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      color: #666;
      text-align: center;
    }

    .cover-placeholder.no-cover {
      background-color: #e0e0e0;
    }

    .cover-placeholder mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 8px;
    }

    .cover-placeholder span {
      font-size: 12px;
      font-weight: 500;
    }

    /* Book Details Section */
    .book-details-section {
      flex: 1;
    }

    .book-title {
      font-size: 28px;
      font-weight: 600;
      color: #333;
      margin: 0 0 8px 0;
      line-height: 1.2;
    }

    .book-subtitle {
      font-size: 20px;
      font-weight: 400;
      color: #666;
      margin: 0 0 24px 0;
      line-height: 1.2;
    }

    /* Metadata Rows */
    .book-metadata {
      background-color: #fff;
    }

    .metadata-row {
      display: flex;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
      align-items: flex-start;
    }

    .metadata-row:last-child {
      border-bottom: none;
    }

    .metadata-label {
      flex: 0 0 140px;
      font-weight: 600;
      color: #555;
      font-size: 14px;
      padding-right: 16px;
    }

    .metadata-value {
      flex: 1;
      color: #333;
      font-size: 14px;
      line-height: 1.4;
      word-wrap: break-word;
    }

    .metadata-value.file-path {
      font-family: monospace;
      font-size: 12px;
      background-color: #f8f8f8;
      padding: 2px 4px;
      border-radius: 3px;
    }

    /* Actions */
    .book-actions {
      display: flex;
      gap: 12px;
      padding: 20px 0;
      border-top: 1px solid #e0e0e0;
      margin-top: 20px;
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .book-detail-container {
        padding: 16px;
      }
      
      .book-layout {
        flex-direction: column;
        gap: 20px;
      }
      
      .book-cover-section {
        flex: none;
        align-self: center;
      }
      
      .book-cover {
        width: 160px;
        height: 240px;
      }
      
      .book-title {
        font-size: 24px;
        text-align: center;
      }
      
      .book-subtitle {
        font-size: 18px;
        text-align: center;
      }
      
      .metadata-row {
        flex-direction: column;
        gap: 4px;
        padding: 12px 0;
      }
      
      .metadata-label {
        flex: none;
        font-weight: 600;
        margin-bottom: 4px;
      }
      
      .metadata-value {
        padding-left: 0;
      }
    }

    /* Tablet breakpoint */
    @media (max-width: 1024px) and (min-width: 769px) {
      .book-layout {
        gap: 30px;
      }
      
      .book-cover-section {
        flex: 0 0 160px;
      }
      
      .book-cover {
        width: 160px;
        height: 240px;
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

  formatISBN13(isbn: string): string {
    // Simple ISBN-13 formatter - in real app this would be more sophisticated
    if (isbn.length === 13) return isbn;
    if (isbn.length === 10) {
      // Simple conversion for display purposes
      return '978' + isbn.substring(0, 9);
    }
    return isbn;
  }

  getAuthors(): string[] {
    if (!this.book()?.contributors) return [];
    return this.book()!.contributors!['author'] || [];
  }

  getOtherContributors(): Array<{role: string, names: string[]}> {
    if (!this.book()?.contributors) return [];
    
    const result: Array<{role: string, names: string[]}> = [];
    const contributors = this.book()!.contributors!;
    
    for (const [role, names] of Object.entries(contributors)) {
      if (role !== 'author' && names.length > 0) {
        // Capitalize first letter of role
        const displayRole = role.charAt(0).toUpperCase() + role.slice(1);
        result.push({ role: displayRole, names });
      }
    }
    
    return result;
  }

  getEdition(): string | null {
    return this.book()?.metadata?.['edition'] || null;
  }

  getBinding(): string | null {
    return this.book()?.metadata?.['binding'] || null;
  }

  getPages(): string | null {
    const pages = this.book()?.metadata?.['pages'];
    return pages ? pages.toString() : null;
  }

  getDimensions(): string | null {
    return this.book()?.metadata?.['dimensions'] || null;
  }

  getWeight(): string | null {
    return this.book()?.metadata?.['weight'] || null;
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
}