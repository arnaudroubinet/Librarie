import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatRippleModule } from '@angular/material/core';
import { MatBadgeModule } from '@angular/material/badge';
import { BookService } from '../services/book.service';
import { Book, CursorPageResponse } from '../models/book.model';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSnackBarModule,
    MatRippleModule,
    MatBadgeModule
  ],
  template: `
    <div class="plex-library">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <mat-icon class="title-icon">library_books</mat-icon>
            Books Library
            @if (books().length > 0) {
              <span class="book-count">{{ books().length }} books</span>
            }
          </h1>
          <p class="library-subtitle">Discover and explore your digital book collection</p>
        </div>
        <div class="header-actions">
          <button mat-fab color="accent" routerLink="/search" class="fab-search">
            <mat-icon>search</mat-icon>
          </button>
        </div>
      </div>
      
      @if (loading()) {
        <div class="loading-section">
          <div class="loading-content">
            <mat-spinner diameter="60" color="accent"></mat-spinner>
            <h3>Loading your library...</h3>
            <p>Gathering your books from the digital shelves</p>
          </div>
        </div>
      } @else {
        @if (books().length === 0) {
          <div class="empty-library">
            <div class="empty-content">
              <mat-icon class="empty-icon">library_books</mat-icon>
              <h2>Your library awaits</h2>
              <p>No books found in your collection. Start building your digital library by scanning your book directory.</p>
              <button mat-raised-button color="accent" routerLink="/library" class="cta-button">
                <mat-icon>add</mat-icon>
                Manage Library
              </button>
            </div>
          </div>
        } @else {
          <div class="books-grid">
            @for (book of books(); track book.id) {
              <div class="book-poster" matRipple [routerLink]="['/books', book.id]">
                <div class="poster-container">
                  <div class="book-cover">
                    @if (book.hasCover) {
                      <img [src]="'/api/books/' + book.id + '/cover'" 
                           [alt]="book.title + ' cover'"
                           class="cover-image"
                           (error)="onImageError($event)">
                    } @else {
                      <div class="cover-placeholder">
                        <mat-icon>book</mat-icon>
                        <span class="title-text">{{ getShortTitle(book.title) }}</span>
                      </div>
                    }
                    <div class="cover-overlay">
                      <mat-icon class="play-icon">visibility</mat-icon>
                    </div>
                  </div>
                  
                  <div class="book-info">
                    <h3 class="book-title" [title]="book.title">{{ book.title }}</h3>
                    @if (book.contributors?.['author']?.length) {
                      <p class="book-author">{{ book.contributors!['author'][0] }}</p>
                    }
                    @if (book.publicationDate) {
                      <p class="book-year">{{ getYear(book.publicationDate) }}</p>
                    }
                    
                    <div class="book-metadata">
                      @if (book.language) {
                        <mat-chip class="metadata-chip language-chip">{{ book.language }}</mat-chip>
                      }
                      @if (book.formats && book.formats.length > 0) {
                        <mat-chip class="metadata-chip format-chip">{{ book.formats[0] }}</mat-chip>
                      }
                    </div>
                  </div>
                </div>
                
                <div class="book-actions">
                  <button mat-icon-button class="action-btn" (click)="toggleFavorite(book, $event)">
                    <mat-icon>favorite_border</mat-icon>
                  </button>
                  <button mat-icon-button class="action-btn" (click)="viewDetails(book, $event)">
                    <mat-icon>info</mat-icon>
                  </button>
                </div>
              </div>
            }
          </div>
          
          <div class="pagination-section">
            @if (previousCursor() || nextCursor()) {
              <div class="pagination-controls">
                @if (previousCursor()) {
                  <button mat-raised-button class="nav-button" (click)="loadPrevious()">
                    <mat-icon>chevron_left</mat-icon>
                    Previous
                  </button>
                }
                <div class="pagination-info">
                  <span>Page {{ currentPage }}</span>
                </div>
                @if (nextCursor()) {
                  <button mat-raised-button class="nav-button" (click)="loadNext()">
                    Next
                    <mat-icon>chevron_right</mat-icon>
                  </button>
                }
              </div>
            }
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .plex-library {
      min-height: 100vh;
      background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
      color: #ffffff;
      padding: 0;
    }

    .library-header {
      background: linear-gradient(135deg, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.4) 100%);
      padding: 40px 32px;
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      border-bottom: 1px solid #333;
      position: relative;
      overflow: hidden;
    }

    .library-header::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="books" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse"><rect width="20" height="20" fill="none"/><rect x="2" y="5" width="3" height="12" fill="rgba(229,160,13,0.1)"/><rect x="6" y="3" width="3" height="14" fill="rgba(229,160,13,0.08)"/><rect x="10" y="6" width="3" height="11" fill="rgba(229,160,13,0.06)"/><rect x="14" y="4" width="3" height="13" fill="rgba(229,160,13,0.04)"/></pattern></defs><rect width="100" height="100" fill="url(%23books)"/></svg>') repeat;
      opacity: 0.1;
      z-index: 0;
    }

    .header-content {
      position: relative;
      z-index: 1;
    }

    .library-title {
      font-size: 2.5rem;
      font-weight: 300;
      margin: 0 0 8px 0;
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .title-icon {
      font-size: 2.5rem;
      width: 2.5rem;
      height: 2.5rem;
      color: #e5a00d;
    }

    .book-count {
      font-size: 1rem;
      color: #888;
      font-weight: 400;
      margin-left: 16px;
    }

    .library-subtitle {
      font-size: 1.1rem;
      color: #ccc;
      margin: 0;
      font-weight: 300;
    }

    .header-actions {
      position: relative;
      z-index: 1;
    }

    .fab-search {
      background: linear-gradient(135deg, #e5a00d 0%, #cc9000 100%);
      color: #000;
    }

    .loading-section {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 60vh;
    }

    .loading-content {
      text-align: center;
    }

    .loading-content h3 {
      margin: 24px 0 8px 0;
      color: #fff;
      font-weight: 400;
    }

    .loading-content p {
      color: #ccc;
      margin: 0;
    }

    .empty-library {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 60vh;
    }

    .empty-content {
      text-align: center;
      max-width: 400px;
    }

    .empty-icon {
      font-size: 80px;
      width: 80px;
      height: 80px;
      color: #555;
      margin-bottom: 24px;
    }

    .empty-content h2 {
      color: #fff;
      margin: 0 0 16px 0;
      font-weight: 400;
    }

    .empty-content p {
      color: #ccc;
      margin: 0 0 32px 0;
      line-height: 1.6;
    }

    .cta-button {
      background: linear-gradient(135deg, #e5a00d 0%, #cc9000 100%);
      color: #000;
      font-weight: 600;
    }

    .books-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 24px;
      padding: 32px;
    }

    .book-poster {
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
      position: relative;
      border-radius: 8px;
      overflow: hidden;
    }

    .book-poster:hover {
      transform: scale(1.05) translateY(-8px);
      z-index: 10;
    }

    .poster-container {
      position: relative;
    }

    .book-cover {
      position: relative;
      width: 100%;
      aspect-ratio: 2/3;
      border-radius: 8px;
      overflow: hidden;
      background: linear-gradient(135deg, #333 0%, #555 100%);
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    }

    .cover-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .cover-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #2a2a2a 0%, #1a1a1a 100%);
      color: #777;
    }

    .cover-placeholder mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 12px;
      color: #666;
    }

    .title-text {
      font-size: 12px;
      text-align: center;
      padding: 0 8px;
      line-height: 1.2;
      font-weight: 500;
    }

    .cover-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    .book-poster:hover .cover-overlay {
      opacity: 1;
    }

    .play-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #e5a00d;
    }

    .book-info {
      padding: 12px 0;
    }

    .book-title {
      font-size: 14px;
      font-weight: 600;
      margin: 0 0 4px 0;
      color: #fff;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .book-author {
      font-size: 12px;
      color: #ccc;
      margin: 0 0 4px 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .book-year {
      font-size: 11px;
      color: #888;
      margin: 0 0 8px 0;
    }

    .book-metadata {
      display: flex;
      gap: 4px;
      flex-wrap: wrap;
    }

    .metadata-chip {
      font-size: 10px;
      height: 18px;
      line-height: 18px;
      padding: 0 6px;
      border-radius: 9px;
    }

    .language-chip {
      background: rgba(229, 160, 13, 0.2);
      color: #e5a00d;
    }

    .format-chip {
      background: rgba(255, 255, 255, 0.1);
      color: #ccc;
    }

    .book-actions {
      position: absolute;
      top: 8px;
      right: 8px;
      display: flex;
      gap: 4px;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    .book-poster:hover .book-actions,
    .book-poster:focus-within .book-actions {
      opacity: 1;
    }

    .action-btn {
      background: rgba(0, 0, 0, 0.8);
      color: #fff;
      width: 36px;
      height: 36px;
      min-width: 36px;
      border-radius: 50%;
      border: 2px solid rgba(255, 255, 255, 0.2);
      transition: all 0.2s ease;
    }

    .action-btn:hover {
      background: rgba(0, 0, 0, 0.9);
      border-color: #e5a00d;
      color: #e5a00d;
      transform: scale(1.1);
    }

    .action-btn mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .pagination-section {
      padding: 32px;
      border-top: 1px solid #333;
    }

    .pagination-controls {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 24px;
    }

    .nav-button {
      background: linear-gradient(135deg, rgba(229, 160, 13, 0.2) 0%, rgba(204, 144, 0, 0.2) 100%);
      color: #fff;
      border: 1px solid #555;
      padding: 8px 16px;
      min-height: 40px;
      display: flex;
      align-items: center;
      gap: 8px;
      transition: all 0.2s ease;
    }

    .nav-button:hover {
      background: linear-gradient(135deg, rgba(229, 160, 13, 0.4) 0%, rgba(204, 144, 0, 0.4) 100%);
      border-color: #e5a00d;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(229, 160, 13, 0.3);
    }

    .nav-button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .nav-button mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .pagination-info {
      color: #ccc;
      font-size: 14px;
    }

    @media (max-width: 768px) {
      .library-header {
        padding: 24px 16px;
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }

      .library-title {
        font-size: 2rem;
      }

      .books-grid {
        grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
        gap: 16px;
        padding: 16px;
      }

      .book-actions {
        opacity: 1; /* Always visible on mobile for touch devices */
        position: static;
        margin-top: 8px;
        justify-content: center;
      }

      .action-btn {
        width: 44px;
        height: 44px;
        min-width: 44px; /* Better touch target for mobile */
      }

      .pagination-controls {
        flex-direction: column;
        gap: 16px;
      }

      .nav-button {
        padding: 12px 24px;
        font-size: 16px;
        min-height: 48px; /* Better touch target */
      }
    }

    @media (max-width: 480px) {
      .books-grid {
        grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
        gap: 12px;
        padding: 12px;
      }

      .library-title {
        font-size: 1.75rem;
      }

      .fab-search {
        width: 48px;
        height: 48px;
        min-width: 48px;
      }
    }
  `]
})
export class BookListComponent implements OnInit {
  books = signal<Book[]>([]);
  loading = signal(true);
  nextCursor = signal<string | undefined>(undefined);
  previousCursor = signal<string | undefined>(undefined);
  limit = signal(20);
  currentPage = 1;

  constructor(
    private bookService: BookService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadBooks();
  }

  loadBooks(cursor?: string) {
    this.loading.set(true);
    this.bookService.getAllBooks(cursor, this.limit()).subscribe({
      next: (response: CursorPageResponse<Book>) => {
        this.books.set(response.content);
        this.nextCursor.set(response.nextCursor);
        this.previousCursor.set(response.previousCursor);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading books:', error);
        this.snackBar.open('Failed to load books. Please try again.', 'Close', {
          duration: 3000
        });
        this.loading.set(false);
      }
    });
  }

  loadNext() {
    if (this.nextCursor()) {
      this.currentPage++;
      this.loadBooks(this.nextCursor());
    }
  }

  loadPrevious() {
    if (this.previousCursor()) {
      this.currentPage--;
      this.loadBooks(this.previousCursor());
    }
  }

  getYear(dateString: string): string {
    return new Date(dateString).getFullYear().toString();
  }

  getShortTitle(title: string): string {
    return title.length > 30 ? title.substring(0, 30) + '...' : title;
  }

  onImageError(event: any) {
    // Hide the broken image and show placeholder
    event.target.style.display = 'none';
  }

  toggleFavorite(book: Book, event: Event) {
    event.stopPropagation();
    event.preventDefault();
    // TODO: Implement favorite functionality
    this.snackBar.open('Favorite functionality coming soon!', 'Close', {
      duration: 2000
    });
  }

  viewDetails(book: Book, event: Event) {
    event.stopPropagation();
    event.preventDefault();
    // Navigate to book details
    window.location.href = `/books/${book.id}`;
  }
}