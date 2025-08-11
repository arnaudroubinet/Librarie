import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BookService } from '../services/book.service';
import { Book, CursorPageResponse } from '../models/book.model';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSnackBarModule
  ],
  template: `
    <div class="search-container">
      <h1>🔍 Search Books</h1>
      
      <mat-card class="search-card">
        <mat-card-content>
          <div class="search-form">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Search books, authors, series, or ISBN</mat-label>
              <input 
                matInput 
                [(ngModel)]="searchQuery" 
                (keyup.enter)="search()"
                placeholder="Enter your search terms...">
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>
            <button 
              mat-raised-button 
              color="primary" 
              (click)="search()"
              [disabled]="!searchQuery.trim()">
              Search
            </button>
          </div>
        </mat-card-content>
      </mat-card>

      @if (hasSearched() && !loading()) {
        <div class="results-summary">
          @if (books().length > 0) {
            <p>Found {{ books().length }} book(s) for "{{ lastSearchQuery() }}"</p>
          } @else {
            <p>No books found for "{{ lastSearchQuery() }}"</p>
          }
        </div>
      }
      
      @if (loading()) {
        <div class="loading-container">
          <mat-spinner diameter="50"></mat-spinner>
          <p>Searching books...</p>
        </div>
      } @else if (books().length > 0) {
        <div class="book-grid">
          @for (book of books(); track book.id) {
            <mat-card class="book-card">
              <mat-card-header>
                <div mat-card-avatar class="book-avatar">
                  <mat-icon>book</mat-icon>
                </div>
                <mat-card-title>{{ book.title }}</mat-card-title>
                <mat-card-subtitle>
                  @if (book.author) {
                    <span>by {{ book.author }}</span>
                  }
                  @if (book.language) {
                    <mat-chip class="language-chip">{{ book.language }}</mat-chip>
                  }
                </mat-card-subtitle>
              </mat-card-header>
              
              <mat-card-content>
                @if (book.description) {
                  <p class="book-description">{{ book.description }}</p>
                }
                @if (book.series) {
                  <p><strong>Series:</strong> {{ book.series }}
                    @if (book.seriesIndex) {
                      (#{{ book.seriesIndex }})
                    }
                  </p>
                }
                @if (book.isbn) {
                  <p><strong>ISBN:</strong> {{ book.isbn }}</p>
                }
                @if (book.publisher) {
                  <p><strong>Publisher:</strong> {{ book.publisher }}</p>
                }
                @if (book.publicationDate) {
                  <p><strong>Published:</strong> {{ formatDate(book.publicationDate) }}</p>
                }
                @if (book.formats && book.formats.length > 0) {
                  <div class="formats">
                    <strong>Formats:</strong>
                    @for (format of book.formats; track format) {
                      <mat-chip class="format-chip">{{ format }}</mat-chip>
                    }
                  </div>
                }
                @if (book.fileSize) {
                  <p><strong>File Size:</strong> {{ formatFileSize(book.fileSize) }}</p>
                }
              </mat-card-content>
              
              <mat-card-actions>
                <button mat-button [routerLink]="['/books', book.id]">
                  <mat-icon>visibility</mat-icon>
                  View Details
                </button>
                @if (book.hasCover) {
                  <button mat-button>
                    <mat-icon>image</mat-icon>
                    Cover
                  </button>
                }
              </mat-card-actions>
            </mat-card>
          }
        </div>
        
        <div class="pagination-controls">
          @if (previousCursor()) {
            <button mat-raised-button (click)="loadPrevious()">
              <mat-icon>chevron_left</mat-icon>
              Previous
            </button>
          }
          @if (nextCursor()) {
            <button mat-raised-button (click)="loadNext()">
              Next
              <mat-icon>chevron_right</mat-icon>
            </button>
          }
        </div>
      } @else if (hasSearched() && !loading()) {
        <div class="empty-state">
          <mat-icon style="font-size: 64px; height: 64px; width: 64px;">search_off</mat-icon>
          <h2>No books found</h2>
          <p>Try different search terms or check your spelling.</p>
        </div>
      }
    </div>
  `,
  styles: [`
    .search-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 16px;
    }

    .search-card {
      margin-bottom: 24px;
    }

    .search-form {
      display: flex;
      gap: 16px;
      align-items: flex-end;
    }

    .search-field {
      flex: 1;
    }

    .results-summary {
      margin-bottom: 16px;
      color: var(--text-secondary);
      font-style: italic;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 48px;
    }

    .loading-container p {
      margin-top: 16px;
      color: var(--text-secondary);
    }

    .empty-state {
      text-align: center;
      padding: 48px;
      color: var(--text-secondary);
    }

    .empty-state h2 {
      margin: 16px 0;
      color: var(--text-primary);
    }

    .book-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 16px;
      margin-bottom: 32px;
    }

    .book-card {
      transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
    }

    .book-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    }

    .book-avatar {
      background-color: var(--primary-color);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .language-chip,
    .format-chip {
      font-size: 12px;
      height: 20px;
      line-height: 20px;
      margin: 2px;
    }

    .format-chip {
      background-color: #e0e0e0;
    }

    .formats {
      margin: 8px 0;
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 4px;
    }

    .book-description {
      font-style: italic;
      color: var(--text-secondary);
      margin-bottom: 8px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
    }

    mat-card-content p {
      margin: 8px 0;
      font-size: 14px;
    }

    .pagination-controls {
      display: flex;
      justify-content: center;
      gap: 16px;
      margin-top: 24px;
    }

    .pagination-controls button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    @media (max-width: 768px) {
      .search-form {
        flex-direction: column;
        align-items: stretch;
        gap: 12px;
      }
      
      .book-grid {
        grid-template-columns: 1fr;
        gap: 12px;
      }
      
      .search-container {
        padding: 8px;
      }

      .pagination-controls {
        flex-direction: column;
        align-items: center;
      }
    }
  `]
})
export class SearchComponent {
  searchQuery = '';
  books = signal<Book[]>([]);
  loading = signal(false);
  hasSearched = signal(false);
  lastSearchQuery = signal('');
  nextCursor = signal<string | undefined>(undefined);
  previousCursor = signal<string | undefined>(undefined);
  limit = signal(20);

  constructor(
    private bookService: BookService,
    private snackBar: MatSnackBar
  ) {}

  search() {
    if (!this.searchQuery.trim()) {
      return;
    }

    this.loading.set(true);
    this.hasSearched.set(true);
    this.lastSearchQuery.set(this.searchQuery);

    this.performSearch();
  }

  performSearch(cursor?: string) {
    this.bookService.searchBooks(this.lastSearchQuery(), cursor, this.limit()).subscribe({
      next: (response: CursorPageResponse<Book>) => {
        this.books.set(response.content);
        this.nextCursor.set(response.nextCursor);
        this.previousCursor.set(response.previousCursor);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error searching books:', error);
        this.snackBar.open('Failed to search books. Please try again.', 'Close', {
          duration: 3000
        });
        this.loading.set(false);
      }
    });
  }

  loadNext() {
    if (this.nextCursor()) {
      this.loading.set(true);
      this.performSearch(this.nextCursor());
    }
  }

  loadPrevious() {
    if (this.previousCursor()) {
      this.loading.set(true);
      this.performSearch(this.previousCursor());
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}