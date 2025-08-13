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

          <!-- ISBNdb-inspired layout -->
          <div class="isbndb-layout">
            <!-- Book Cover -->
            <div class="book-cover-container">
              @if (book()!.hasCover) {
                <img src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iIzM0NDk1ZSIvPgo8dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjE0IiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9IjAuM2VtIj5Db3ZlciBBdmFpbGFibGU8L3RleHQ+Cjwvc3ZnPg==" 
                     alt="Book Cover" 
                     class="book-cover" />
              } @else {
                <div class="book-cover-placeholder">
                  <div class="placeholder-content">
                    <div class="placeholder-icon">📖</div>
                    <div class="placeholder-text">No Image Available</div>
                  </div>
                </div>
              }
            </div>

            <!-- Book Information -->
            <div class="book-info-container">
              <!-- Title Section -->
              <div class="title-section">
                <h1 class="book-title">{{ book()!.title }}</h1>
                @if (book()!.titleSort && book()!.titleSort !== book()!.title) {
                  <div class="book-subtitle">({{ book()!.titleSort }})</div>
                }
              </div>

              <!-- Details List -->
              <div class="details-list">
                <div class="detail-item">
                  <span class="detail-label">Full Title:</span>
                  <span class="detail-value">{{ book()!.title }}</span>
                </div>

                @if (book()!.isbn) {
                  <div class="detail-item">
                    <span class="detail-label">ISBN:</span>
                    <span class="detail-value">{{ book()!.isbn }}</span>
                  </div>
                }

                @if (book()!.isbn) {
                  <div class="detail-item">
                    <span class="detail-label">ISBN13:</span>
                    <span class="detail-value">{{ formatISBN13(book()!.isbn!) }}</span>
                  </div>
                }

                @if (getAuthors().length > 0) {
                  <div class="detail-item">
                    <span class="detail-label">Authors:</span>
                    <span class="detail-value author-links">
                      @for (author of getAuthors(); track author; let last = $last) {
                        <span class="author-link">{{ author }}</span>@if (!last) {<span>, </span>}
                      }
                    </span>
                  </div>
                }

                @if (book()!.publisher) {
                  <div class="detail-item">
                    <span class="detail-label">Publisher:</span>
                    <span class="detail-value">{{ book()!.publisher }}</span>
                  </div>
                }

                @if (getEdition()) {
                  <div class="detail-item">
                    <span class="detail-label">Edition:</span>
                    <span class="detail-value">{{ getEdition() }}</span>
                  </div>
                }

                @if (book()!.publicationDate) {
                  <div class="detail-item">
                    <span class="detail-label">Publish Date:</span>
                    <span class="detail-value">{{ formatDate(book()!.publicationDate!) }}</span>
                  </div>
                }

                @if (getBinding()) {
                  <div class="detail-item">
                    <span class="detail-label">Binding:</span>
                    <span class="detail-value">{{ getBinding() }}</span>
                  </div>
                }

                @if (getPages()) {
                  <div class="detail-item">
                    <span class="detail-label">Pages:</span>
                    <span class="detail-value">{{ getPages() }}</span>
                  </div>
                }

                @if (book()!.description) {
                  <div class="detail-item synopsis">
                    <span class="detail-label">Synopsis:</span>
                    <span class="detail-value">{{ book()!.description }}</span>
                  </div>
                }

                @if (book()!.language) {
                  <div class="detail-item">
                    <span class="detail-label">Language:</span>
                    <span class="detail-value">{{ book()!.language }}</span>
                  </div>
                }

                @if (getDimensions()) {
                  <div class="detail-item">
                    <span class="detail-label">Dimensions:</span>
                    <span class="detail-value">{{ getDimensions() }}</span>
                  </div>
                }

                @if (getWeight()) {
                  <div class="detail-item">
                    <span class="detail-label">Weight:</span>
                    <span class="detail-value">{{ getWeight() }}</span>
                  </div>
                }

                @if (getSubjects().length > 0) {
                  <div class="detail-item">
                    <span class="detail-label">Subjects:</span>
                    <span class="detail-value">{{ getSubjects().join(', ') }}</span>
                  </div>
                }
              </div>
            </div>
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
      max-width: 1000px;
      margin: 0 auto;
      padding: 20px;
      font-family: Arial, sans-serif;
      background-color: #ffffff;
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
      color: #666;
    }

    .error-state h2 {
      margin: 16px 0;
      color: #333;
    }

    .back-button {
      margin-bottom: 20px;
    }

    /* ISBNdb-inspired layout */
    .isbndb-layout {
      display: flex;
      gap: 30px;
      align-items: flex-start;
    }

    /* Book Cover */
    .book-cover-container {
      flex: 0 0 200px;
    }

    .book-cover {
      width: 200px;
      height: 300px;
      border: 1px solid #e0e0e0;
      border-radius: 3px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .book-cover-placeholder {
      width: 200px;
      height: 300px;
      border: 1px solid #e0e0e0;
      border-radius: 3px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f8f9fa;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .placeholder-content {
      text-align: center;
      color: #6c757d;
    }

    .placeholder-icon {
      font-size: 48px;
      margin-bottom: 8px;
    }

    .placeholder-text {
      font-size: 12px;
      font-weight: 500;
    }

    /* Book Information */
    .book-info-container {
      flex: 1;
      min-width: 0;
    }

    .title-section {
      margin-bottom: 24px;
    }

    .book-title {
      font-size: 26px;
      font-weight: 600;
      color: #333;
      margin: 0 0 8px 0;
      line-height: 1.3;
    }

    .book-subtitle {
      font-size: 18px;
      color: #666;
      font-weight: 400;
      margin: 0;
    }

    /* Details List - ISBNdb style */
    .details-list {
      background-color: #ffffff;
    }

    .detail-item {
      display: flex;
      padding: 10px 0;
      border-bottom: 1px solid #f0f0f0;
      align-items: flex-start;
    }

    .detail-item:first-child {
      padding-top: 0;
    }

    .detail-item:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }

    .detail-item.synopsis {
      align-items: flex-start;
    }

    .detail-item.synopsis .detail-value {
      margin-top: 0;
    }

    .detail-label {
      flex: 0 0 120px;
      font-weight: 600;
      color: #555;
      font-size: 14px;
      padding-right: 16px;
    }

    .detail-value {
      flex: 1;
      color: #333;
      font-size: 14px;
      line-height: 1.5;
      word-wrap: break-word;
    }

    /* Author links styling */
    .author-links .author-link {
      color: #0066cc;
      text-decoration: none;
      cursor: pointer;
    }

    .author-links .author-link:hover {
      text-decoration: underline;
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .book-detail-container {
        padding: 16px;
      }
      
      .isbndb-layout {
        flex-direction: column;
        gap: 20px;
        align-items: center;
      }
      
      .book-cover-container {
        flex: none;
      }
      
      .book-cover, .book-cover-placeholder {
        width: 160px;
        height: 240px;
      }
      
      .book-info-container {
        width: 100%;
      }
      
      .book-title {
        font-size: 22px;
        text-align: center;
      }
      
      .book-subtitle {
        font-size: 16px;
        text-align: center;
      }
      
      .detail-item {
        flex-direction: column;
        gap: 4px;
        padding: 12px 0;
      }
      
      .detail-label {
        flex: none;
        font-weight: 600;
        margin-bottom: 4px;
      }
      
      .detail-value {
        padding-left: 0;
      }
    }

    /* Tablet breakpoint */
    @media (max-width: 1024px) and (min-width: 769px) {
      .isbndb-layout {
        gap: 24px;
      }
      
      .book-cover-container {
        flex: 0 0 160px;
      }
      
      .book-cover, .book-cover-placeholder {
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

  getSubjects(): string[] {
    const subjects = this.book()?.metadata?.['subjects'];
    if (Array.isArray(subjects)) {
      return subjects;
    }
    if (typeof subjects === 'string') {
      return [subjects];
    }
    return [];
  }
}