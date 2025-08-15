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
import { AuthorService } from '../services/author.service';
import { Author } from '../models/author.model';
import { InfiniteScrollService, AlphabeticalSeparator } from '../services/infinite-scroll.service';
import { InfiniteScrollDirective } from '../directives/infinite-scroll.directive';

@Component({
  selector: 'app-author-list',
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
            <iconify-icon class="title-icon" icon="ph:users-three-thin"></iconify-icon>
            Authors Library
          </h1>
          <p class="library-subtitle">Discover and explore your favorite authors</p>
        </div>
        
      </div>
      
      @if (scrollState.loading() && scrollState.items().length === 0) {
        <div class="loading-section">
          <div class="loading-content">
            <mat-spinner diameter="60" color="accent"></mat-spinner>
            <h3>Loading authors...</h3>
            <p>Gathering your authors from the digital shelves</p>
          </div>
        </div>
      } @else {
        @if (scrollState.isEmpty()) {
          <div class="empty-library">
            <div class="empty-content">
              <mat-icon class="empty-icon">people</mat-icon>
              <h2>No authors found</h2>
              <p>No authors found in your collection. Start building your digital library by scanning your book directory.</p>
              <button mat-raised-button color="accent" routerLink="/library" class="cta-button">
                <iconify-icon icon="cil:magnifying-glass"></iconify-icon>
                Scan Library
              </button>
            </div>
          </div>
        } @else {
          <div class="authors-grid">
            @for (item of scrollState.items(); track trackByFn($index, item)) {
              @if (infiniteScrollService.isSeparator(item)) {
                <div class="alphabetical-separator">
                  <div class="separator-line"></div>
                  <span class="separator-letter">{{ item.letter }}</span>
                  <div class="separator-line"></div>
                </div>
              } @else {
                <div class="author-card" matRipple [routerLink]="['/authors', item.id]">
                  <div class="card-container">
                    <div class="author-photo">
                      @if (item.metadata?.['imageUrl']) {
                        <img [src]="item.metadata!['imageUrl']" 
                             [alt]="item.name + ' photo'"
                             class="photo-image"
                             (error)="onImageError($event)">
                      } @else {
                        <div class="photo-placeholder">
                          <mat-icon>person</mat-icon>
                          <span class="name-text">{{ getInitials(item.name) }}</span>
                        </div>
                      }
                      <div class="photo-overlay">
                        <mat-icon class="view-icon">visibility</mat-icon>
                      </div>
                    </div>
                    
                    <div class="author-info">
                      <h3 class="author-name" [title]="item.name">{{ item.name }}</h3>
                      @if (item.bio?.['en']) {
                        <p class="author-bio">{{ getShortBio(item.bio!['en']) }}</p>
                      }
                      @if (item.birthDate || item.deathDate) {
                        <p class="author-dates">
                          {{ formatDates(item.birthDate, item.deathDate) }}
                        </p>
                      }
                    </div>
                  </div>
                  
                  
                </div>
              }
            }
          </div>
          
          <!-- Loading indicator for more items -->
          @if (scrollState.loading() && scrollState.items().length > 0) {
            <div class="load-more-container">
              <mat-spinner diameter="30"></mat-spinner>
              <p>Loading more authors...</p>
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
              <p>You've reached the end of the author list</p>
            </div>
          }
        }
      }
    </div>
  `,
  styleUrls: ['./author-list.component.css']
})
export class AuthorListComponent implements OnInit {
  scrollState;

  constructor(
    private authorService: AuthorService,
    private snackBar: MatSnackBar,
    protected infiniteScrollService: InfiniteScrollService
  ) {
    // Initialize infinite scroll state with alphabetical separators
    this.scrollState = this.infiniteScrollService.createInfiniteScrollState(
      (cursor, limit) => this.authorService.getAllAuthors(cursor, limit),
      {
        limit: 20,
    enableAlphabeticalSeparators: true,
    sortProperty: 'sortName',
    limitProvider: () => this.calculatePageSize()
      }
    );
  }

  ngOnInit() {
  // Initialization is handled by the infinite scroll service
  window.addEventListener('resize', this.onResize, { passive: true });
  }

  onScroll() {
    this.scrollState.loadMore();
  }


  trackByFn(index: number, item: Author | AlphabeticalSeparator): string {
    return this.infiniteScrollService.isSeparator(item) ? item.id : item.id;
  }

  onImageError(event: any) {
    event.target.style.display = 'none';
  }

  private onResize = () => { /* recalculated via limitProvider */ };

  private getLayoutMetrics() {
    const w = window.innerWidth;
    if (w <= 480) return { CARD_WIDTH: 180, GRID_GAP: 16, PADDING_X: 16, CARD_HEIGHT: 320 };
    if (w <= 768) return { CARD_WIDTH: 200, GRID_GAP: 20, PADDING_X: 24, CARD_HEIGHT: 340 };
    if (w <= 1024) return { CARD_WIDTH: 220, GRID_GAP: 24, PADDING_X: 32, CARD_HEIGHT: 360 };
    return { CARD_WIDTH: 240, GRID_GAP: 24, PADDING_X: 32, CARD_HEIGHT: 360 };
  }

  private calculatePageSize(): number {
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;
    const { CARD_WIDTH, GRID_GAP, PADDING_X, CARD_HEIGHT } = this.getLayoutMetrics();
    const contentWidth = Math.max(0, viewportWidth - PADDING_X * 2);
    const colWidth = CARD_WIDTH + GRID_GAP;
    const rowHeight = CARD_HEIGHT + GRID_GAP + 120; // include bio and name area
    const columns = Math.max(1, Math.floor((contentWidth + GRID_GAP) / colWidth));
    const rows = Math.max(1, Math.floor((viewportHeight + GRID_GAP) / rowHeight));
    const pageSize = columns * (rows + 1);
    return Math.max(10, pageSize);
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .map(part => part.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

  getShortBio(bio: string): string {
    if (!bio) return '';
    const maxLength = 120;
    return bio.length > maxLength ? bio.substring(0, maxLength) + '...' : bio;
  }

  formatDates(birthDate?: string, deathDate?: string): string {
    const birth = birthDate ? new Date(birthDate).getFullYear() : '?';
    const death = deathDate ? new Date(deathDate).getFullYear() : '';
    
    if (death) {
      return `${birth} - ${death}`;
    } else {
      return `Born ${birth}`;
    }
  }

  openAuthorDetails(event: Event, authorId: string) {
    event.stopPropagation();
    // Router navigation will be handled by the template
  }
  
  openWebsite(event: Event, url: string) {
    event.stopPropagation();
    window.open(url, '_blank');
  }
}