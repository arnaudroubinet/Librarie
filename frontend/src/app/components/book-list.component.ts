import { Component, OnInit } from '@angular/core';
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
import { Book } from '../models/book.model';
import { InfiniteScrollService } from '../services/infinite-scroll.service';
import { InfiniteScrollDirective } from '../directives/infinite-scroll.directive';

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
    MatBadgeModule,
    InfiniteScrollDirective
  ],
  template: `
    <div class="plex-library" appInfiniteScroll (scrolled)="onScroll()" [disabled]="scrollState.loading()">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <mat-icon class="title-icon">collections</mat-icon>
            Books Library
            @if (scrollState.items().length > 0) {
              <span class="book-count">{{ scrollState.items().length }} books</span>
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
      
      @if (scrollState.loading() && scrollState.items().length === 0) {
        <div class="loading-section">
          <div class="loading-content">
            <mat-spinner diameter="60" color="accent"></mat-spinner>
            <h3>Loading books...</h3>
            <p>Gathering your books from the digital shelves</p>
          </div>
        </div>
      } @else {
        @if (scrollState.isEmpty()) {
          <div class="empty-library">
            <div class="empty-content">
              <mat-icon class="empty-icon">collections_bookmark</mat-icon>
              <h2>No books found</h2>
              <p>Your book library is empty. Start building your digital library by scanning your book directory.</p>
              <button mat-raised-button color="accent" routerLink="/library" class="cta-button">
                <mat-icon>add</mat-icon>
                Scan Library
              </button>
            </div>
          </div>
        } @else {
          <div class="library-content">
            <div class="books-grid">
              @for (book of scrollState.items(); track asBook(book).id) {
                <div class="book-card" 
                     matRipple 
                     [routerLink]="['/books', asBook(book).id]">
                  <div class="book-cover">
                    @if (getEffectiveImagePath(asBook(book))) {
                      <img [src]="getEffectiveImagePath(asBook(book))!" 
                           [alt]="asBook(book).title + ' cover'"
                           class="cover-image"
                           (error)="onImageError($event)">
                    } @else {
                      <div class="cover-placeholder">
                        <mat-icon>menu_book</mat-icon>
                        <span class="book-title-text">{{ getShortTitle(asBook(book).title) }}</span>
                      </div>
                    }
                    <div class="book-overlay">
                      <div class="book-actions">
                        <button mat-icon-button class="action-btn" (click)="viewBookDetails($event, asBook(book).id)">
                          <mat-icon>visibility</mat-icon>
                        </button>
                        <button mat-icon-button class="action-btn" (click)="toggleFavorite($event, asBook(book))">
                          <mat-icon>favorite_border</mat-icon>
                        </button>
                        <button mat-icon-button class="action-btn" (click)="shareBook($event, asBook(book))">
                          <mat-icon>share</mat-icon>
                        </button>
                      </div>
                    </div>
                  </div>
                  
                  <div class="book-info">
                    <h3 class="book-title" [title]="asBook(book).title">{{ getShortTitle(asBook(book).title) }}</h3>
                    @if (asBook(book).contributors) {
                      <p class="book-author">{{ getShortContributors(asBook(book).contributors) }}</p>
                    }
                    @if (asBook(book).series) {
                      <p class="book-series">
                        {{ asBook(book).series }}
                        @if (asBook(book).seriesIndex) {
                          #{{ asBook(book).seriesIndex }}
                        }
                      </p>
                    }
                    @if (asBook(book).publicationDate) {
                      <p class="book-date">{{ getPublicationYear(asBook(book).publicationDate!) }}</p>
                    }
                  </div>
                </div>
              }
            </div>
            
            <!-- Loading indicator for more items -->
            @if (scrollState.loading() && scrollState.items().length > 0) {
              <div class="load-more-container">
                <mat-spinner diameter="30"></mat-spinner>
                <p>Loading more books...</p>
              </div>
            }
            
            <!-- Error indicator for loading more -->
            @if (scrollState.error() && scrollState.items().length > 0) {
              <div class="load-more-error">
                <p>{{ scrollState.error() }}</p>
                <button mat-button color="primary" (click)="scrollState.loadMore()">
                  Try Again
                </button>
              </div>
            }
            
            <!-- End of list indicator -->
            @if (!scrollState.hasMore() && scrollState.items().length > 0) {
              <div class="end-of-list">
                <p>You've reached the end of your book collection</p>
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
    }

    .header-content {
      flex: 1;
    }

    .library-title {
      font-size: 3rem;
      font-weight: 300;
      margin: 0 0 8px 0;
      color: #ffffff;
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .title-icon {
      font-size: 3rem;
      color: #4fc3f7;
    }

    .book-count {
      font-size: 1.1rem;
      opacity: 0.7;
      font-weight: 400;
      margin-left: 16px;
    }

    .library-subtitle {
      font-size: 1.2rem;
      margin: 0;
      opacity: 0.8;
      color: #e1f5fe;
    }

    .header-actions {
      display: flex;
      gap: 16px;
      align-items: center;
    }

    .fab-search {
      background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%) !important;
      color: white !important;
      box-shadow: 0 8px 32px rgba(79, 195, 247, 0.3) !important;
    }

    .fab-search:hover {
      transform: translateY(-2px) !important;
    }

    .loading-section {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 60vh;
      text-align: center;
    }

    .loading-content h3 {
      margin: 24px 0 8px 0;
      font-size: 1.5rem;
      font-weight: 400;
    }

    .loading-content p {
      margin: 0;
      opacity: 0.7;
      font-size: 1rem;
    }

    .empty-library {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 60vh;
      text-align: center;
    }

    .empty-content {
      max-width: 500px;
      padding: 48px 24px;
    }

    .empty-icon {
      font-size: 6rem;
      color: #555;
      margin-bottom: 24px;
    }

    .empty-content h2 {
      font-size: 2rem;
      font-weight: 300;
      margin: 0 0 16px 0;
      color: #ffffff;
    }

    .empty-content p {
      font-size: 1.1rem;
      line-height: 1.6;
      opacity: 0.8;
      margin: 0 0 32px 0;
    }

    .cta-button {
      background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%) !important;
      color: white !important;
      padding: 12px 32px !important;
      font-size: 1.1rem !important;
    }

    .library-content {
      padding: 0;
    }

    .books-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 20px;
      padding: 32px;
      max-width: 1600px;
      margin: 0 auto;
    }

    .book-card {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      overflow: hidden;
      transition: all 0.3s ease;
      cursor: pointer;
      position: relative;
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

    .book-card:hover {
      transform: translateY(-8px);
      box-shadow: 0 25px 50px rgba(0, 0, 0, 0.4);
      border-color: rgba(79, 195, 247, 0.5);
    }

    .book-cover {
      position: relative;
      width: 100%;
      height: 240px;
      overflow: hidden;
    }

    .cover-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .book-card:hover .cover-image {
      transform: scale(1.05);
    }

    .cover-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #2a2a2a 0%, #1a1a1a 100%);
      color: #666;
      text-align: center;
      padding: 16px;
    }

    .cover-placeholder mat-icon {
      font-size: 3rem;
      margin-bottom: 8px;
    }

    .book-title-text {
      font-size: 0.8rem;
      line-height: 1.2;
      word-break: break-word;
    }

    .book-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    .book-card:hover .book-overlay {
      opacity: 1;
    }

    .book-actions {
      display: flex;
      gap: 8px;
    }

    .action-btn {
      background: rgba(255, 255, 255, 0.2) !important;
      color: white !important;
      width: 40px !important;
      height: 40px !important;
      transition: all 0.3s ease !important;
    }

    .action-btn:hover {
      background: rgba(79, 195, 247, 0.8) !important;
      transform: scale(1.1) !important;
    }

    .book-info {
      padding: 16px;
      text-align: center;
    }

    .book-title {
      font-size: 1rem;
      font-weight: 600;
      margin: 0 0 8px 0;
      color: #ffffff;
      line-height: 1.3;
      height: 2.6em;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .book-author {
      font-size: 0.85rem;
      margin: 0 0 4px 0;
      opacity: 0.8;
      color: #b3e5fc;
    }

    .book-series {
      font-size: 0.8rem;
      margin: 0 0 4px 0;
      opacity: 0.7;
      color: #81d4fa;
      font-style: italic;
    }

    .book-date {
      font-size: 0.75rem;
      margin: 0;
      opacity: 0.6;
      color: #4fc3f7;
    }

    .load-more-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 40px 20px;
      gap: 16px;
    }

    .load-more-container p {
      margin: 0;
      opacity: 0.7;
    }

    .load-more-error {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 40px 20px;
      gap: 16px;
    }

    .load-more-error p {
      margin: 0;
      color: #f44336;
    }

    .end-of-list {
      display: flex;
      justify-content: center;
      padding: 40px 20px;
      opacity: 0.6;
    }

    .end-of-list p {
      margin: 0;
      font-style: italic;
    }

    /* Responsive design */
    @media (max-width: 768px) {
      .library-header {
        padding: 24px 16px;
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }

      .library-title {
        font-size: 2rem !important;
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
  scrollState;

  constructor(
    private bookService: BookService,
    private snackBar: MatSnackBar,
    private infiniteScrollService: InfiniteScrollService
  ) {
    // Initialize infinite scroll state
    this.scrollState = this.infiniteScrollService.createInfiniteScrollState(
      (cursor, limit) => this.bookService.getAllBooks(cursor, limit),
      {
        limit: 20,
        enableAlphabeticalSeparators: false // Books don't need alphabetical separators
      }
    );
  }

  ngOnInit() {
    // Initialization is handled by the infinite scroll service
  }

  onScroll() {
    this.scrollState.loadMore();
  }

  trackByFn(index: number, book: Book): string {
    return book.id;
  }

  asBook(item: any): Book {
    return item as Book;
  }

  viewBookDetails(event: Event, bookId: string) {
    event.stopPropagation();
    // Router navigation will be handled by template
  }

  toggleFavorite(event: Event, book: Book) {
    event.stopPropagation();
    // TODO: Implement favorite functionality
    this.snackBar.open('Favorite functionality not implemented yet', 'Close', { duration: 3000 });
  }

  shareBook(event: Event, book: Book) {
    event.stopPropagation();
    // TODO: Implement share functionality
    this.snackBar.open('Share functionality not implemented yet', 'Close', { duration: 3000 });
  }

  getEffectiveImagePath(book: Book): string | null {
    if (book.hasCover && book.id) {
      // Use book cover endpoint if cover exists
      return `/api/v1/books/${book.id}/cover`;
    }
    return null;
  }

  onImageError(event: any) {
    event.target.style.display = 'none';
  }

  getShortTitle(title: string): string {
    return title.length > 30 ? title.substring(0, 30) + '...' : title;
  }

  getShortContributors(contributors?: Record<string, string[]>): string {
    if (!contributors) return '';
    const allContributors = Object.values(contributors).flat();
    if (allContributors.length === 0) return '';
    if (allContributors.length === 1) return allContributors[0];
    return `${allContributors[0]} and ${allContributors.length - 1} more`;
  }

  getPublicationYear(publicationDate: string): number {
    return new Date(publicationDate).getFullYear();
  }
}