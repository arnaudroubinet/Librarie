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
                    <div class="series-overlay">
                      <div class="series-actions">
                        <button mat-icon-button class="action-btn" aria-label="Bookmark" (click)="toggleFavorite($event, asSeries(series))">
                          <iconify-icon [icon]="getBookmarkIcon(asSeries(series))"></iconify-icon>
                        </button>
                        <button mat-icon-button class="action-btn" aria-label="Share" (click)="shareSeries($event, asSeries(series))">
                          <iconify-icon icon="material-symbols-light:share"></iconify-icon>
                        </button>
                      </div>
                    </div>
                  </div>

                  <div class="series-info">
                    <h3 class="series-title" [title]="asSeries(series).name">{{ getShortTitle(asSeries(series).name) }}</h3>
                    <p class="book-count">{{ asSeries(series).bookCount }} {{ asSeries(series).bookCount === 1 ? 'book' : 'books' }}</p>
                  </div>
                </div>
              }
            </div>

            @if (scrollState.loading() && scrollState.items().length > 0) {
              <div class="load-more-container">
                <mat-spinner diameter="30"></mat-spinner>
                <p>Loading more series...</p>
              </div>
            }

            @if (scrollState.error() && scrollState.items().length > 0) {
              <div class="load-more-error">
                <p>{{ scrollState.error() }}</p>
                <button mat-button color="primary" (click)="scrollState.loadMore()">Try Again</button>
              </div>
            }

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

    .header-content { flex: 1; }

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

    .library-subtitle { font-size: 0.95rem; margin: 4px 0 0 0; opacity: 0.85; color: #cfcfcf; }

    .header-actions { display: flex; gap: 16px; align-items: center; }

    .fab-search { background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%) !important; color: white !important; box-shadow: 0 8px 32px rgba(79, 195, 247, 0.3) !important; }

    .fab-search:hover { transform: translateY(-2px) !important; }

    .loading-section { display: flex; justify-content: center; align-items: center; min-height: 60vh; text-align: center; }

    .loading-content h3 { margin: 24px 0 8px 0; font-size: 1.5rem; font-weight: 400; }

    .loading-content p { margin: 0; opacity: 0.7; font-size: 1rem; }

    .empty-library { display: flex; justify-content: center; align-items: center; min-height: 60vh; text-align: center; }

    .empty-content { max-width: 500px; padding: 48px 24px; }

    .empty-icon { font-size: 6rem; color: #555; margin-bottom: 24px; }

    .empty-content h2 { font-size: 2rem; font-weight: 300; margin: 0 0 16px 0; color: #ffffff; }

    .empty-content p { font-size: 1.1rem; line-height: 1.6; opacity: 0.8; margin: 0 0 32px 0; }

    .cta-button { background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%) !important; color: white !important; padding: 12px 32px !important; font-size: 1.1rem !important; }

    .library-content { padding: 0; }

    .series-grid {
      display: grid;
      /* Fixed tile width to keep same size on mobile and desktop */
      grid-template-columns: repeat(auto-fill, 240px);
      justify-content: center;
      gap: 24px;
      padding: 32px;
      margin: 0 auto;
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
      /* Keep card height equal to the picture height */
      width: 240px;
      height: 360px;
    }

    .series-card:hover { transform: translateY(-8px); box-shadow: 0 25px 50px rgba(0, 0, 0, 0.4); border-color: rgba(79, 195, 247, 0.5); }

    .series-cover { position: relative; width: 100%; height: 100%; overflow: hidden; }

    .cover-image { width: 100%; height: 100%; object-fit: cover; opacity: 0; transition: opacity 0.3s ease, transform 0.3s ease; }

    .cover-image.loaded { opacity: 1; }

    .series-card:hover .cover-image { transform: scale(1.05); }

    .cover-placeholder { width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; background: linear-gradient(135deg, #2a2a2a 0%, #1a1a1a 100%); color: #666; text-align: center; padding: 16px; }

    .cover-placeholder mat-icon { font-size: 3rem; margin-bottom: 8px; }

    .series-title-text { font-size: 1rem; line-height: 1.2; word-break: break-word; }

    .series-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.7); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.3s ease; z-index: 2; }

    .series-card:hover .series-overlay { opacity: 1; }

    .series-actions { display: flex; gap: 16px; }

    .action-btn { background: rgba(255, 255, 255, 0.2) !important; color: white !important; width: 80px !important; height: 80px !important; transition: all 0.3s ease !important; }

    .action-btn:hover { background: rgba(79, 195, 247, 0.8) !important; transform: scale(1.1) !important; }

    .action-btn iconify-icon { font-size: 28px; width: 28px; height: 28px; }

    .series-info { position: absolute; left: 0; right: 0; bottom: 0; padding: 8px 10px; text-align: left; background: linear-gradient(180deg, rgba(0,0,0,0) 0%, rgba(0,0,0,0.65) 40%, rgba(0,0,0,0.85) 100%); z-index: 1; }

    .series-title { font-size: 1.1rem; font-weight: 600; margin: 0 0 4px 0; color: #ffffff; line-height: 1.2; height: 2.4em; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }

    .book-count { font-size: 0.8rem; margin: 0; opacity: 0.7; color: #4fc3f7; font-weight: 500; }

    .load-more-container { display: flex; flex-direction: column; align-items: center; padding: 40px 20px; gap: 16px; }

    .load-more-container p { margin: 0; opacity: 0.7; }

    .load-more-error { display: flex; flex-direction: column; align-items: center; padding: 40px 20px; gap: 16px; }

    .load-more-error p { margin: 0; color: #f44336; }

    .end-of-list { display: flex; justify-content: center; padding: 40px 20px; opacity: 0.6; }

    .end-of-list p { margin: 0; font-style: italic; }

    /* Responsive design (keep tile size identical across breakpoints) */
    @media (max-width: 768px) {
      .library-header { padding: 24px 16px; flex-direction: column; align-items: flex-start; gap: 16px; }
      .library-title { font-size: 2rem !important; }
      /* Keep same card width and cover height on mobile */
      .series-grid { grid-template-columns: repeat(auto-fill, 240px); justify-content: center; gap: 24px; padding: 24px; }
      .series-actions { opacity: 1; position: static; margin-top: 8px; justify-content: center; }
      .action-btn { width: 64px !important; height: 64px !important; min-width: 64px !important; }
    }

    @media (max-width: 480px) {
      /* Keep same card width and cover height on small phones */
      .series-grid { grid-template-columns: repeat(auto-fill, 240px); justify-content: center; gap: 16px; padding: 16px; }
      .library-title { font-size: 1.75rem; }
      .fab-search { width: 64px; height: 64px; min-width: 64px; }
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

  onScroll() { this.scrollState.loadMore(); }

  trackByFn(index: number, series: Series): string { return series.id; }

  asSeries(item: any): Series { return item as Series; }

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

  getShortTitle(title: string): string { return title.length > 25 ? title.substring(0, 25) + '...' : title; }

  getShortDescription(description: string): string { return description.length > 100 ? description.substring(0, 100) + '...' : description; }

  getEffectiveImagePath(series: Series): string | null {
    if (series.imagePath) return series.imagePath;
    if (series.fallbackImagePath) return series.fallbackImagePath;
    return null;
  }

  onImageError(event: any) { event.target.style.display = 'none'; }

  onImageLoad(event: any) { event.target.classList.add('loaded'); }

  private onResize = () => { /* limit recalculated lazily via limitProvider */ };

  private calculatePageSize(): number {
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;

    // Match /books layout metrics
    const CARD_WIDTH = 240; // px
    const CARD_HEIGHT = 360; // px
    const GRID_GAP = 24; // must match CSS gap for desktop
    const PADDING_X = 32; // horizontal padding of .series-grid

    // Effective content width accounting for grid side padding
    const contentWidth = Math.max(0, viewportWidth - PADDING_X * 2);
    const colWidth = CARD_WIDTH + GRID_GAP;
    const rowHeight = CARD_HEIGHT + GRID_GAP;

    const columns = Math.max(1, Math.floor((contentWidth + GRID_GAP) / colWidth));
    const rows = Math.max(1, Math.floor((viewportHeight + GRID_GAP) / rowHeight));

    // Load one extra row as buffer for smoother scrolling
    const pageSize = columns * (rows + 1);
    return Math.max(10, pageSize);
  }

  getBookmarkIcon(series: Series): string { return this.isBookmarked(series) ? 'material-symbols:bookmark' : 'material-symbols:bookmark-outline'; }

  isBookmarked(series: Series): boolean { return !!series?.id && this.favorites.has(series.id); }

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