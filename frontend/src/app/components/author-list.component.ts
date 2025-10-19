import { Component, OnInit, OnDestroy, CUSTOM_ELEMENTS_SCHEMA, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MATERIAL_MODULES } from '../shared/materials';
import { getInitials, getShortBio, formatDates } from '../utils/author-utils';
import { AuthorService } from '../services/author.service';
import { Author } from '../models/author.model';
import { SortField, SortDirection, SortOption } from '../models/book.model';
import { InfiniteScrollService, AlphabeticalSeparator } from '../services/infinite-scroll.service';
import { InfiniteScrollDirective } from '../directives/infinite-scroll.directive';
import { environment } from '../../environments/environment';
import { HeaderActionsService } from '../services/header-actions.service';

@Component({
  selector: 'app-author-list',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    RouterModule,
    ...MATERIAL_MODULES,
    InfiniteScrollDirective
  ],
  template: `
  <div class="motspassants-library" appInfiniteScroll (scrolled)="onScroll()" [disabled]="scrollState.loading()">
      <div class="library-header">
        <div class="header-content">
          <p class="library-subtitle">Discover and explore your favorite authors</p>
          <div class="sort-controls">
            <div class="sort-field">
              <mat-select [value]="selectedSortOption()" (selectionChange)="onSortChange($event.value)" [displayWith]="displaySort" disableRipple>
                @for (opt of sortOptions; track opt.label) {
                  <mat-option [value]="opt">{{ opt.label }}</mat-option>
                }
              </mat-select>
            </div>
          </div>
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
                <iconify-icon icon="material-symbols:search-rounded"></iconify-icon>
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
                      @if (item.id) {
                        <img [src]="apiUrl + '/v1/authors/' + item.id + '/picture'" 
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
export class AuthorListComponent implements OnInit, OnDestroy {
  scrollState;
  readonly apiUrl = environment.apiUrl;
  currentSort: { field: string; direction: string } = { field: 'SORT_NAME', direction: 'ASC' };

  // Sort options for the author list (align with backend AuthorSortCriteria)
  sortOptions: SortOption[] = [
    { label: 'Name A-Z', field: SortField.SORT_NAME, direction: SortDirection.ASC },
    { label: 'Name Z-A', field: SortField.SORT_NAME, direction: SortDirection.DESC }
  ];

  // Signals to mirror book list behavior
  selectedSortOption = signal<SortOption>(this.sortOptions[0]);

  constructor(
    private authorService: AuthorService,
    private snackBar: MatSnackBar,
    protected infiniteScrollService: InfiniteScrollService,
    private headerActions: HeaderActionsService
  ) {
    // Initialize infinite scroll state with alphabetical separators and sort params
    this.scrollState = this.infiniteScrollService.createInfiniteScrollState(
      (cursor, limit) => this.authorService.getAllAuthors(cursor, limit, this.currentSort.field, this.currentSort.direction),
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
  this.headerActions.setRefresh(() => this.refresh());
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.onResize as any);
    this.headerActions.setRefresh(null);
  }

  onScroll() {
    this.scrollState.loadMore();
  }


  trackByFn(index: number, item: Author | AlphabeticalSeparator): string {
    // Return a stable key for separators and authors.
    if (this.infiniteScrollService.isSeparator(item)) {
      // AlphabeticalSeparator likely has a `letter` property; prefix to avoid collisions.
      return `separator-${(item as AlphabeticalSeparator).letter}`;
    }
    // For authors, prefer id but fall back to an index-based key if missing.
    return (item as Author).id ?? `author-${index}`;
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

  getInitials = getInitials;
  getShortBio = getShortBio;
  formatDates = formatDates;

  openAuthorDetails(event: Event, authorId: string) {
    event.stopPropagation();
    // Router navigation will be handled by the template
  }
  
  openWebsite(event: Event, url: string) {
    event.stopPropagation();
    window.open(url, '_blank');
  }

  refresh() {
    this.authorService.clearCache();
    this.scrollState.reset();
    this.snackBar.open('Authors refreshed', 'Close', { duration: 1500 });
  }

  

  // New overloaded handler used by mat-select when passing SortOption
  onSortChange(sortOption: SortOption) {
    if (!sortOption) return;
    this.currentSort = { field: sortOption.field as unknown as string, direction: sortOption.direction as unknown as string };
    this.selectedSortOption.set(sortOption);
    this.authorService.clearCache();
    this.scrollState.reset();
    this.snackBar.open(`Sorted by ${sortOption.label}`, 'Close', { duration: 2000 });
  }

  displaySort(option?: SortOption): string {
    return option ? option.label : '';
  }
}