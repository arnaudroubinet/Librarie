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
import { SeriesService } from '../services/series.service';
import { Series } from '../models/series.model';
import { InfiniteScrollService } from '../services/infinite-scroll.service';
import { InfiniteScrollDirective } from '../directives/infinite-scroll.directive';

@Component({
  selector: 'app-series-list',
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
            <iconify-icon class="title-icon" icon="icon-park-outline:bookshelf"></iconify-icon>
            Series Library
            @if (scrollState.items().length > 0) {
              <span class="series-count">{{ scrollState.items().length }} series</span>
            }
          </h1>
          <p class="library-subtitle">Explore your book series collections</p>
        </div>
        
      </div>
      
      @if (scrollState.loading() && scrollState.items().length === 0) {
        <div class="loading-section">
          <div class="loading-content">
            <mat-spinner diameter="60" color="accent"></mat-spinner>
            <h3>Loading series...</h3>
            <p>Gathering your series from the digital shelves</p>
          </div>
        </div>
      } @else {
        @if (scrollState.isEmpty()) {
          <div class="empty-library">
            <div class="empty-content">
              <mat-icon class="empty-icon">library_books</mat-icon>
              <h2>No series found</h2>
              <p>No series found in your collection. Series will appear here when you add books that are part of a series.</p>
              <button mat-raised-button color="accent" routerLink="/library" class="cta-button">
                <mat-icon>add</mat-icon>
                Manage Library
              </button>
            </div>
          </div>
        } @else {
          <div class="library-content">
            <div class="series-grid">
              @for (series of scrollState.items(); track asSeries(series).id) {
                <div class="series-card" 
                     matRipple 
                     [routerLink]="['/series', asSeries(series).id]">
                  <div class="series-cover">
                    @if (getEffectiveImagePath(asSeries(series))) {
          <img [src]="getEffectiveImagePath(asSeries(series))!" 
                           [alt]="asSeries(series).name + ' cover'"
                           class="cover-image"
            loading="lazy"
            decoding="async"
            fetchpriority="low"
            (load)="onImageLoad($event)"
                           (error)="onImageError($event)">
                    } @else {
                      <div class="cover-placeholder">
                        <mat-icon>library_books</mat-icon>
                        <span class="series-title-text">{{ getShortTitle(asSeries(series).name) }}</span>
                      </div>
                    }
                    <div class="series-overlay"></div>
                    <div class="series-actions">
                      <button mat-icon-button class="action-btn" aria-label="Bookmark" (click)="toggleFavorite($event, asSeries(series))">
                        <iconify-icon [icon]="getBookmarkIcon(asSeries(series))"></iconify-icon>
                      </button>
                      <button mat-icon-button class="action-btn" aria-label="Share" (click)="shareSeries($event, asSeries(series))">
                        <iconify-icon icon="material-symbols-light:share"></iconify-icon>
                      </button>
                    </div>
                  </div>
                  
                  <div class="series-info">
                    <h3 class="series-title" [title]="asSeries(series).name">{{ getShortTitle(asSeries(series).name) }}</h3>
                    @if (asSeries(series).description) {
                      <p class="series-description">{{ getShortDescription(asSeries(series).description!) }}</p>
                    }
                    <p class="book-count">
                      {{ asSeries(series).bookCount }} {{ asSeries(series).bookCount === 1 ? 'book' : 'books' }}
                    </p>
                  </div>
                </div>
              }
            </div>
            
            <!-- Loading indicator for more items -->
            @if (scrollState.loading() && scrollState.items().length > 0) {
              <div class="load-more-container">
                <mat-spinner diameter="30"></mat-spinner>
                <p>Loading more series...</p>
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
                <p>You've reached the end of your series collection</p>
              </div>
            }
          </div>
        }
      }
    </div>
  `,
  styles: [`
    :host {
      /* Responsive layout variables shared with /books page */
      --card-w: 220px;
      --grid-gap: 24px;
      --grid-pad: 32px;
    }
    .plex-library {
      min-height: 100vh;
      background: transparent;
      color: #ffffff;
      padding: 0;
    }

    .library-header {
      background: transparent;
      padding: 24px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      /* Remove separator line and gap */
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

    .series-count {
      font-size: 1.1rem;
      opacity: 0.7;
      font-weight: 400;
      margin-left: 16px;
    }

  .library-subtitle { font-size: 0.95rem; margin: 4px 0 0 0; opacity: 0.9; color: #888; }

    .header-actions {
      display: flex;
      gap: 16px;
      align-items: center;
    }

    .fab-search {
      background: linear-gradient(135deg, #ff7043 0%, #ff5722 100%) !important;
      color: white !important;
      box-shadow: 0 8px 32px rgba(255, 112, 67, 0.3) !important;
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
      background: linear-gradient(135deg, #ff7043 0%, #ff5722 100%) !important;
      color: white !important;
      padding: 12px 32px !important;
      font-size: 1.1rem !important;
    }

    .library-content {
      padding: 0;
    }

    .series-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, var(--card-w));
      gap: var(--grid-gap);
      padding: var(--grid-pad);
      max-width: 1600px;
      margin: 0 auto;
      justify-content: center;
    }

    .series-card {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      overflow: hidden;
      transition: all 0.3s ease;
      cursor: pointer;
      position: relative;
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      width: var(--card-w);
    }

    .series-card:hover {
      transform: translateY(-8px);
      box-shadow: 0 25px 50px rgba(0, 0, 0, 0.4);
      border-color: rgba(255, 112, 67, 0.5);
    }

    .series-cover {
      position: relative;
      width: 100%;
      height: 180px;
      overflow: hidden;
    }

    .cover-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .series-card:hover .cover-image {
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

    .series-title-text {
      font-size: 0.9rem;
      line-height: 1.2;
      word-break: break-word;
    }

    .series-overlay {
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
      pointer-events: none; /* don't block taps */
    }

    .series-card:hover .series-overlay {
      opacity: 1;
    }

    .series-actions {
      display: flex;
      gap: 8px;
      position: absolute;
      top: 8px;
      right: 8px;
      z-index: 2;
      opacity: 0;
      transition: opacity 0.2s ease;
      pointer-events: auto; /* allow tapping action buttons */
    }

    .series-card:hover .series-actions { opacity: 1; }

    .action-btn {
      background: rgba(255, 255, 255, 0.2) !important;
      color: white !important;
      width: 40px !important;
      height: 40px !important;
      transition: all 0.3s ease !important;
    }

    .action-btn:hover {
      background: rgba(255, 112, 67, 0.8) !important;
      transform: scale(1.1) !important;
    }

    .series-info {
      padding: 16px;
      text-align: center;
    }

    .series-title {
      font-size: 1.1rem;
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

    .series-description {
      font-size: 0.85rem;
      margin: 0 0 8px 0;
      opacity: 0.8;
      color: #ffccbc;
      line-height: 1.4;
      height: 2.8em;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .book-count {
      font-size: 0.8rem;
      margin: 0;
      opacity: 0.7;
      color: #ff8a65;
      font-weight: 500;
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
    @media (max-width: 1024px) {
      :host { --card-w: 210px; }
    }

    @media (max-width: 768px) {
      :host { --card-w: 200px; --grid-gap: 20px; --grid-pad: 24px; }
      .library-header {
        padding: 24px 16px;
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }

      .library-title {
        font-size: 1.5rem;
      }

      .series-actions { opacity: 1; }
      .action-btn { width: 44px !important; height: 44px !important; min-width: 44px !important; }
    }

    @media (max-width: 480px) {
      :host { --card-w: 180px; --grid-gap: 16px; --grid-pad: 16px; }

      .library-title {
        font-size: 1.35rem;
      }

      .fab-search {
        width: 48px;
        height: 48px;
        min-width: 48px;
      }
    }
  `]
})
export class SeriesListComponent implements OnInit {
  scrollState;

  constructor(
    private seriesService: SeriesService,
    private snackBar: MatSnackBar,
    private infiniteScrollService: InfiniteScrollService
  ) {
    // Initialize infinite scroll state
    this.scrollState = this.infiniteScrollService.createInfiniteScrollState(
      (cursor, limit) => this.seriesService.getAllSeries(cursor, limit),
      {
        limit: 20,
        enableAlphabeticalSeparators: false,
        limitProvider: () => this.calculatePageSize()
      }
    );
  }

  ngOnInit() {
    // Recalculate on resize
    window.addEventListener('resize', this.onResize, { passive: true });
  }

  onScroll() {
    this.scrollState.loadMore();
  }

  trackByFn(index: number, series: Series): string {
    return series.id;
  }

  asSeries(item: any): Series {
    return item as Series;
  }

  toggleFavorite(event: Event, series: Series) {
    event.stopPropagation();
    if (!series?.id) return;
    if (this.favorites.has(series.id)) {
      this.favorites.delete(series.id);
      this.snackBar.open('Removed bookmark', 'Close', { duration: 1500 });
    } else {
      this.favorites.add(series.id);
      this.snackBar.open('Bookmarked', 'Close', { duration: 1500 });
    }
  }

  shareSeries(event: Event, series: Series) {
    event.stopPropagation();
    const url = `${window.location.origin}/series/${series.id}`;
    const done = () => this.snackBar.open('Link ready to share', 'Close', { duration: 2000 });
    const fail = () => this.snackBar.open('Failed to copy link', 'Close', { duration: 2500 });

    const title = series.name || 'Series';
    if ((navigator as any).share) {
      (navigator as any)
        .share({ title, url })
        .then(() => this.snackBar.open('Shared', 'Close', { duration: 1500 }))
        .catch(() => {
          if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(url).then(done).catch(() => {
              this.legacyCopy(url) ? done() : fail();
            });
          } else {
            this.legacyCopy(url) ? done() : fail();
          }
        });
      return;
    }

    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(url).then(done).catch(() => {
        this.legacyCopy(url) ? done() : fail();
      });
    } else {
      this.legacyCopy(url) ? done() : fail();
    }
  }

  getShortTitle(title: string): string {
    return title.length > 25 ? title.substring(0, 25) + '...' : title;
  }

  getShortDescription(description: string): string {
    return description.length > 100 ? description.substring(0, 100) + '...' : description;
  }

  getEffectiveImagePath(series: Series): string | null {
    if (series.imagePath) {
      return series.imagePath;
    }
    if (series.fallbackImagePath) {
      return series.fallbackImagePath;
    }
    return null;
  }

  onImageError(event: any) {
    event.target.style.display = 'none';
  }

  onImageLoad(event: any) {
    event.target.classList.add('loaded');
  }

  private onResize = () => {
    // limit recalculated lazily via limitProvider
  };

  private getLayoutMetrics() {
    const w = window.innerWidth;
    if (w <= 480) return { CARD_WIDTH: 180, GRID_GAP: 16, PADDING_X: 16, CARD_HEIGHT: 260 };
    if (w <= 768) return { CARD_WIDTH: 200, GRID_GAP: 20, PADDING_X: 24, CARD_HEIGHT: 280 };
    if (w <= 1024) return { CARD_WIDTH: 210, GRID_GAP: 24, PADDING_X: 32, CARD_HEIGHT: 300 };
    return { CARD_WIDTH: 220, GRID_GAP: 24, PADDING_X: 32, CARD_HEIGHT: 300 };
  }

  private calculatePageSize(): number {
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;
    const { CARD_WIDTH, GRID_GAP, PADDING_X, CARD_HEIGHT } = this.getLayoutMetrics();
    const contentWidth = Math.max(0, viewportWidth - PADDING_X * 2);
    const colWidth = CARD_WIDTH + GRID_GAP;
    const rowHeight = CARD_HEIGHT + GRID_GAP + 80; // include info section approx
    const columns = Math.max(1, Math.floor((contentWidth + GRID_GAP) / colWidth));
    const rows = Math.max(1, Math.floor((viewportHeight + GRID_GAP) / rowHeight));
    const pageSize = columns * (rows + 1);
    return Math.max(10, pageSize);
  }

  getBookmarkIcon(series: Series): string {
    return this.isBookmarked(series) ? 'material-symbols:bookmark' : 'material-symbols:bookmark-outline';
  }

  isBookmarked(series: Series): boolean {
    return !!series?.id && this.favorites.has(series.id);
  }

  private favorites = new Set<string>();

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
}