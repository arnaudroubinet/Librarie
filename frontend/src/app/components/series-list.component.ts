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
import { SeriesService } from '../services/series.service';
import { Series, SeriesPageResponse } from '../models/series.model';

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
    MatBadgeModule
  ],
  template: `
    <div class="plex-library">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <mat-icon class="title-icon">library_books</mat-icon>
            Series Library
            @if (series().length > 0) {
              <span class="series-count">{{ series().length }} series</span>
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
      
      @if (loading()) {
        <div class="loading-section">
          <div class="loading-content">
            <mat-spinner diameter="60" color="accent"></mat-spinner>
            <h3>Loading your series...</h3>
            <p>Gathering your series from the digital shelves</p>
          </div>
        </div>
      } @else {
        @if (series().length === 0) {
          <div class="empty-library">
            <div class="empty-content">
              <mat-icon class="empty-icon">library_books</mat-icon>
              <h2>No series found</h2>
              <p>No series found in your collection. Series are automatically created when books with series information are imported.</p>
              <button mat-raised-button color="accent" routerLink="/library" class="cta-button">
                <mat-icon>add</mat-icon>
                Manage Library
              </button>
            </div>
          </div>
        } @else {
          <div class="series-grid">
            @for (seriesItem of series(); track seriesItem.id) {
              <div class="series-poster" matRipple [routerLink]="['/series', seriesItem.id]">
                <div class="poster-container">
                  <div class="series-cover">
                    @if (getEffectiveImagePath(seriesItem)) {
                      <img [src]="getEffectiveImagePath(seriesItem)" 
                           [alt]="seriesItem.name + ' series'"
                           class="cover-image"
                           (error)="onImageError($event)">
                    } @else {
                      <div class="cover-placeholder">
                        <mat-icon>library_books</mat-icon>
                        <span class="title-text">{{ getShortTitle(seriesItem.name) }}</span>
                      </div>
                    }
                    <div class="cover-overlay">
                      <mat-icon class="play-icon">visibility</mat-icon>
                    </div>
                    
                    <!-- Book count badge -->
                    <div class="book-count-badge">
                      {{ seriesItem.bookCount }}
                    </div>
                  </div>
                  
                  <div class="series-info">
                    <h3 class="series-title" [title]="seriesItem.name">{{ seriesItem.name }}</h3>
                    @if (seriesItem.description) {
                      <p class="series-description">{{ getShortDescription(seriesItem.description) }}</p>
                    }
                    <p class="series-book-count">{{ seriesItem.bookCount }} {{ seriesItem.bookCount === 1 ? 'book' : 'books' }}</p>
                  </div>
                </div>
                
                <div class="series-actions">
                  <button mat-icon-button class="action-btn" (click)="toggleFavorite(seriesItem, $event)">
                    <mat-icon>favorite_border</mat-icon>
                  </button>
                  <button mat-icon-button class="action-btn" (click)="viewDetails(seriesItem, $event)">
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
      background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="series" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse"><rect width="20" height="20" fill="none"/><rect x="2" y="2" width="16" height="4" fill="rgba(229,160,13,0.1)"/><rect x="2" y="7" width="16" height="4" fill="rgba(229,160,13,0.08)"/><rect x="2" y="12" width="16" height="4" fill="rgba(229,160,13,0.06)"/></pattern></defs><rect width="100" height="100" fill="url(%23series)"/></svg>') repeat;
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

    .series-count {
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

    .series-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 24px;
      padding: 32px;
    }

    .series-poster {
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
      position: relative;
      border-radius: 8px;
      overflow: hidden;
    }

    .series-poster:hover {
      transform: scale(1.05) translateY(-8px);
      z-index: 10;
    }

    .poster-container {
      position: relative;
    }

    .series-cover {
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

    .series-poster:hover .cover-overlay {
      opacity: 1;
    }

    .play-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #e5a00d;
    }

    .book-count-badge {
      position: absolute;
      bottom: 8px;
      right: 8px;
      background: rgba(229, 160, 13, 0.9);
      color: #000;
      border-radius: 12px;
      padding: 4px 8px;
      font-size: 12px;
      font-weight: 600;
      min-width: 20px;
      text-align: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    }

    .series-info {
      padding: 12px 0;
    }

    .series-title {
      font-size: 14px;
      font-weight: 600;
      margin: 0 0 4px 0;
      color: #fff;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .series-description {
      font-size: 12px;
      color: #ccc;
      margin: 0 0 4px 0;
      line-height: 1.3;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .series-book-count {
      font-size: 11px;
      color: #888;
      margin: 0;
    }

    .series-actions {
      position: absolute;
      top: 8px;
      right: 8px;
      display: flex;
      gap: 4px;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    .series-poster:hover .series-actions,
    .series-poster:focus-within .series-actions {
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

      .series-grid {
        grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
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
      .series-grid {
        grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
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
  series = signal<Series[]>([]);
  loading = signal(true);
  nextCursor = signal<string | undefined>(undefined);
  previousCursor = signal<string | undefined>(undefined);
  limit = signal(20);
  currentPage = 1;

  constructor(
    private seriesService: SeriesService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadSeries();
  }

  loadSeries(cursor?: string) {
    this.loading.set(true);
    this.seriesService.getAllSeries(cursor, this.limit()).subscribe({
      next: (response: SeriesPageResponse) => {
        this.series.set(response.content);
        this.nextCursor.set(response.nextCursor);
        this.previousCursor.set(response.previousCursor);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading series:', error);
        this.snackBar.open('Failed to load series. Please try again.', 'Close', {
          duration: 3000
        });
        this.loading.set(false);
      }
    });
  }

  loadNext() {
    if (this.nextCursor()) {
      this.currentPage++;
      this.loadSeries(this.nextCursor());
    }
  }

  loadPrevious() {
    if (this.previousCursor()) {
      this.currentPage--;
      this.loadSeries(this.previousCursor());
    }
  }

  getShortTitle(title: string): string {
    return title.length > 25 ? title.substring(0, 25) + '...' : title;
  }

  getShortDescription(description: string): string {
    return description.length > 100 ? description.substring(0, 100) + '...' : description;
  }

  getEffectiveImagePath(series: Series): string | null {
    return series.imagePath || series.fallbackImagePath || null;
  }

  onImageError(event: any) {
    // Hide the broken image and show placeholder
    event.target.style.display = 'none';
  }

  toggleFavorite(series: Series, event: Event) {
    event.stopPropagation();
    event.preventDefault();
    // TODO: Implement favorite functionality
    this.snackBar.open('Favorite functionality coming soon!', 'Close', {
      duration: 2000
    });
  }

  viewDetails(series: Series, event: Event) {
    event.stopPropagation();
    event.preventDefault();
    // Navigate to series details
    window.location.href = `/series/${series.id}`;
  }
}