import { Component, OnInit, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
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
import { environment } from '../../environments/environment';
import { InfiniteScrollService } from '../services/infinite-scroll.service';
import { InfiniteScrollDirective } from '../directives/infinite-scroll.directive';

@Component({
  selector: 'app-book-list',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
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
            <iconify-icon class="title-icon" icon="ph:books-thin"></iconify-icon>
            Books Library
          </h1>
          <p class="library-subtitle">Discover and explore your digital book collection</p>
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
                           loading="lazy"
                           decoding="async"
                           fetchpriority="low"
                           (load)="onImageLoad($event)"
                           (error)="onImageError($event)">
                    } @else {
                      <div class="cover-placeholder">
                        <mat-icon>menu_book</mat-icon>
                        <span class="book-title-text">{{ getShortTitle(asBook(book).title) }}</span>
                      </div>
                    }
                    <div class="book-overlay">
                      <div class="book-actions">
                        <button mat-icon-button class="action-btn" aria-label="Bookmark" (click)="toggleFavorite($event, asBook(book))">
                          <iconify-icon [icon]="getBookmarkIcon(asBook(book))"></iconify-icon>
                        </button>
                        <button mat-icon-button class="action-btn" aria-label="Share" (click)="shareBook($event, asBook(book))">
                          <iconify-icon icon="material-symbols-light:share"></iconify-icon>
                        </button>
                      </div>
                    </div>
                  </div>
                  
                  <div class="book-info">
                    <h3 class="book-title" [title]="asBook(book).title">{{ getShortTitle(asBook(book).title) }}</h3>
                    @if (asBook(book).contributorsDetailed || asBook(book).contributors) {
                      <p class="book-author">{{ getShortContributors(asBook(book).contributors, asBook(book).contributorsDetailed) }}</p>
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
      background: transparent;
      color: #ffffff;
      padding: 0;
    }

    .library-header {
      /* Transparent to keep a single page gradient */
      background: transparent;
      padding: 24px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      /* Remove bottom border and gap to avoid separation */
      /* border-bottom: 1px solid #333; */
      margin-bottom: 0;
    }

    .header-content {
      flex: 1;
    }

    .library-title {
      font-size: 20px;
      font-weight: 600;
      margin: 0;
      color: #ffffff;
      display: flex;
      align-items: center;
      gap: 12px;
      letter-spacing: -0.5px;
    }

  .title-icon { font-size: 32px; width: 32px; height: 32px; color: #e5a00d; margin-right: 12px; }

  /* Removed book-count display */

    .library-subtitle {
      font-size: 0.95rem;
      margin: 4px 0 0 0;
      opacity: 0.85;
      color: #cfcfcf;
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
      /* Fixed tile width to keep same size on mobile and desktop */
      grid-template-columns: repeat(auto-fill, 240px);
      justify-content: center;
      gap: 24px;
      padding: 32px;
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
  /* Keep card height equal to the picture height */
  width: 240px;
  height: 360px;
    }

    .book-card:hover {
      transform: translateY(-8px);
      box-shadow: 0 25px 50px rgba(0, 0, 0, 0.4);
      border-color: rgba(79, 195, 247, 0.5);
    }

    .book-cover {
      position: relative;
      width: 100%;
      /* Match card height so the picture defines the tile size */
      height: 100%;
      overflow: hidden;
    }

    .cover-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      opacity: 0;
      transition: opacity 0.3s ease, transform 0.3s ease;
    }

    .cover-image.loaded {
      opacity: 1;
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

  .cover-placeholder mat-icon { font-size: 3rem; margin-bottom: 8px; }

  .book-title-text { font-size: 1rem; line-height: 1.2; word-break: break-word; }

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
  z-index: 2; /* Above info strip */
    }

    .book-card:hover .book-overlay {
      opacity: 1;
    }

    .book-actions {
      display: flex;
      gap: 16px;
    }

    .action-btn {
      background: rgba(255, 255, 255, 0.2) !important;
      color: white !important;
      width: 80px !important;
      height: 80px !important;
      transition: all 0.3s ease !important;
    }

    .action-btn:hover {
      background: rgba(79, 195, 247, 0.8) !important;
      transform: scale(1.1) !important;
    }

    .action-btn iconify-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
    }

    .book-info {
      /* Overlay compact info at bottom of the cover to keep total height equal to the image */
      position: absolute;
      left: 0;
      right: 0;
      bottom: 0;
      padding: 8px 10px;
      text-align: left;
      background: linear-gradient(180deg, rgba(0,0,0,0) 0%, rgba(0,0,0,0.65) 40%, rgba(0,0,0,0.85) 100%);
      z-index: 1;
    }

    .book-title {
      font-size: 1.1rem;
      font-weight: 600;
      margin: 0 0 4px 0;
      color: #ffffff;
      line-height: 1.2;
      height: 2.4em; /* 2 lines */
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

  .book-author { font-size: 0.9rem; margin: 0 0 2px 0; opacity: 0.85; color: #b3e5fc; }

  .book-series { font-size: 0.85rem; margin: 0 0 2px 0; opacity: 0.75; color: #81d4fa; font-style: italic; }

  .book-date { font-size: 0.8rem; margin: 0; opacity: 0.7; color: #4fc3f7; }

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

    /* Responsive design (keep tile size identical across breakpoints) */
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

      /* Keep same card width and cover height on mobile */
      .books-grid {
        grid-template-columns: repeat(auto-fill, 240px);
        justify-content: center;
        gap: 24px;
        padding: 24px;
      }

      .book-actions {
        opacity: 1; /* Always visible on mobile for touch devices */
        position: static;
        margin-top: 8px;
        justify-content: center;
      }

      .action-btn {
        width: 64px;
        height: 64px;
        min-width: 64px; /* Better touch target for mobile */
      }
    }

    @media (max-width: 480px) {
      /* Keep same card width and cover height on small phones */
      .books-grid {
        grid-template-columns: repeat(auto-fill, 240px);
        justify-content: center;
        gap: 16px;
        padding: 16px;
      }

      .library-title {
        font-size: 1.75rem;
      }

      .fab-search {
        width: 64px;
        height: 64px;
        min-width: 64px;
      }
    }
  `]
})
export class BookListComponent implements OnInit {
  scrollState;
  private readonly CARD_WIDTH = 240; // px
  private readonly CARD_HEIGHT = 360; // px
  private readonly GRID_GAP = 24; // must match CSS gap for desktop
  private readonly PADDING_X = 32; // horizontal padding of .books-grid

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
        enableAlphabeticalSeparators: false,
        limitProvider: () => this.calculatePageSize()
      }
    );
  }

  ngOnInit() {
  // Initialization is handled by the infinite scroll service
  // Recalculate on resize to keep loads efficient
  window.addEventListener('resize', this.onResize, { passive: true });
  }

  onScroll() {
    this.scrollState.loadMore();
  }

  private onResize = () => {
    // No immediate action required; limitProvider is read on each load
  };

  trackByFn(index: number, book: Book): string {
    return book.id;
  }

  asBook(item: any): Book {
    return item as Book;
  }

  // Clicking anywhere on the card (outside action buttons) opens details via [routerLink]

  toggleFavorite(event: Event, book: Book) {
    event.stopPropagation();
    // TODO: Implement bookmark functionality
    this.snackBar.open('Bookmark functionality not implemented yet', 'Close', { duration: 3000 });
  }

  shareBook(event: Event, book: Book) {
    event.stopPropagation();
    const url = `${window.location.origin}/books/${book.id}`;
    const done = () => this.snackBar.open('Book link copied to clipboard', 'Close', { duration: 2000 });
    const fail = () => this.snackBar.open('Failed to copy link', 'Close', { duration: 2500 });

    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(url).then(done).catch(() => {
        // Fallback if writeText fails
        this.legacyCopy(url) ? done() : fail();
      });
    } else {
      this.legacyCopy(url) ? done() : fail();
    }
  }

  private legacyCopy(text: string): boolean {
    try {
      const textarea = document.createElement('textarea');
      textarea.value = text;
      textarea.style.position = 'fixed';
      textarea.style.left = '-9999px';
      document.body.appendChild(textarea);
      textarea.focus();
      textarea.select();
      const success = document.execCommand('copy');
      document.body.removeChild(textarea);
      return success;
    } catch {
      return false;
    }
  }

  getEffectiveImagePath(book: Book): string | null {
    if (book.hasCover && book.id) {
      // Use book cover endpoint if cover exists
  return `${environment.apiUrl}/v1/books/${book.id}/cover`;
    }
    return null;
  }

  onImageError(event: any) {
    event.target.style.display = 'none';
  }

  onImageLoad(event: any) {
    event.target.classList.add('loaded');
  }

  getShortTitle(title: string): string {
    return title.length > 30 ? title.substring(0, 30) + '...' : title;
  }

  getShortContributors(contributors?: Record<string, string[]>, contributorsDetailed?: Record<string, Array<{id: string; name: string}>>): string {
    let allContributors: string[] = [];
    if (contributorsDetailed) {
      allContributors = Object.values(contributorsDetailed).flat().map(c => c.name);
    } else if (contributors) {
      allContributors = Object.values(contributors).flat();
    }
    if (allContributors.length === 0) return '';
    if (allContributors.length === 1) return allContributors[0];
    return `${allContributors[0]} and ${allContributors.length - 1} more`;
  }

  getPublicationYear(publicationDate: string): number {
    return new Date(publicationDate).getFullYear();
  }

  private calculatePageSize(): number {
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;

    // Effective content width accounting for grid side padding
    const contentWidth = Math.max(0, viewportWidth - this.PADDING_X * 2);
    const colWidth = this.CARD_WIDTH + this.GRID_GAP;
    const rowHeight = this.CARD_HEIGHT + this.GRID_GAP;

    const columns = Math.max(1, Math.floor((contentWidth + this.GRID_GAP) / colWidth));
    const rows = Math.max(1, Math.floor((viewportHeight + this.GRID_GAP) / rowHeight));

    // Load one extra row as buffer for smoother scrolling
    const pageSize = columns * (rows + 1);
    return Math.max(10, pageSize);
  }

  getBookmarkIcon(book: Book): string {
    return this.isBookmarked(book) ? 'material-symbols:bookmark' : 'material-symbols:bookmark-outline';
  }

  isBookmarked(book: Book): boolean {
    // TODO: wire to real bookmark state when implemented
    return false;
  }
}