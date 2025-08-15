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
            <button mat-icon-button class="refresh-btn" aria-label="Refresh series" (click)="refresh()">
              <iconify-icon icon="material-symbols-light:refresh-rounded"></iconify-icon>
            </button>
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
                <iconify-icon icon="cil:magnifying-glass"></iconify-icon>
                Scan Library
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
  styleUrls: ['./series-list.component.css']
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

  refresh() {
    this.seriesService.clearCache();
    this.scrollState.reset();
    this.snackBar.open('Series refreshed', 'Close', { duration: 1500 });
  }
}