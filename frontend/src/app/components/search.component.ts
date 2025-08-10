import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BookService } from '../services/book.service';
import { Book, PageResponse } from '../models/book.model';

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
    MatPaginatorModule,
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
                  @if (book.language) {
                    <mat-chip class="language-chip">{{ book.language }}</mat-chip>
                  }
                </mat-card-subtitle>
              </mat-card-header>
              
              <mat-card-content>
                @if (book.isbn) {
                  <p><strong>ISBN:</strong> {{ book.isbn }}</p>
                }
                @if (book.publisher) {
                  <p><strong>Publisher:</strong> {{ book.publisher }}</p>
                }
                @if (book.publicationDate) {
                  <p><strong>Published:</strong> {{ formatDate(book.publicationDate) }}</p>
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
        
        @if (totalElements() > pageSize()) {
          <mat-paginator 
            [length]="totalElements()"
            [pageSize]="pageSize()"
            [pageSizeOptions]="[10, 20, 50]"
            [pageIndex]="currentPage()"
            (page)="onPageChange($event)"
            showFirstLastButtons>
          </mat-paginator>
        }
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

    .language-chip {
      font-size: 12px;
      height: 20px;
      line-height: 20px;
    }

    mat-card-content p {
      margin: 8px 0;
      font-size: 14px;
    }

    mat-paginator {
      margin-top: 24px;
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
    }
  `]
})
export class SearchComponent {
  searchQuery = '';
  books = signal<Book[]>([]);
  loading = signal(false);
  hasSearched = signal(false);
  lastSearchQuery = signal('');
  currentPage = signal(0);
  pageSize = signal(20);
  totalElements = signal(0);

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
    this.currentPage.set(0);

    this.bookService.searchBooks(this.searchQuery, this.currentPage(), this.pageSize()).subscribe({
      next: (response: PageResponse<Book>) => {
        this.books.set(response.content);
        this.totalElements.set(response.totalElements);
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

  onPageChange(event: PageEvent) {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.search();
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