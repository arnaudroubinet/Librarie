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
import { SeriesService } from '../services/series.service';
import { Series } from '../models/series.model';
import { InfiniteScrollService } from '../services/infinite-scroll.service';
import { InfiniteScrollDirective } from '../directives/infinite-scroll.directive';

@Component({
  selector: 'app-series-list',
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
            <mat-icon class="title-icon">library_books</mat-icon>
            Series Library
            @if (scrollState.items().length > 0) {
              <span class="series-count">{{ scrollState.items().length }} series</span>
            }
          </h1>
          <p class="library-subtitle">Explore your book series collections</p>
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
                           (error)="onImageError($event)">
                    } @else {
                      <div class="cover-placeholder">
                        <mat-icon>library_books</mat-icon>
                        <span class="series-title-text">{{ getShortTitle(asSeries(series).name) }}</span>
                      </div>
                    }
                    <div class="series-overlay">
                      <div class="series-actions">
                        <button mat-icon-button class="action-btn" (click)="viewDetails($event, asSeries(series))">
                          <mat-icon>info</mat-icon>
                        </button>
                        <button mat-icon-button class="action-btn" (click)="toggleFavorite($event, asSeries(series))">
                          <mat-icon>favorite_border</mat-icon>
                        </button>
                      </div>
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
      color: #ff7043;
    }

    .series-count {
      font-size: 1.1rem;
      opacity: 0.7;
      font-weight: 400;
      margin-left: 16px;
    }

    .library-subtitle {
      font-size: 1.2rem;
      margin: 0;
      opacity: 0.8;
      color: #fbe9e7;
    }

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
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 24px;
      padding: 32px;
      max-width: 1600px;
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
    }

    .series-card:hover .series-overlay {
      opacity: 1;
    }

    .series-actions {
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

      .series-grid {
        grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
        gap: 16px;
        padding: 16px;
      }

      .series-actions {
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
      .series-grid {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
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
        enableAlphabeticalSeparators: false // Series don't need alphabetical separators
      }
    );
  }

  ngOnInit() {
    // Initialization is handled by the infinite scroll service
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

  viewDetails(event: Event, series: Series) {
    event.stopPropagation();
    // Router navigation will be handled by template
  }

  toggleFavorite(event: Event, series: Series) {
    event.stopPropagation();
    // TODO: Implement favorite functionality
    this.snackBar.open('Favorite functionality not implemented yet', 'Close', { duration: 3000 });
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
}