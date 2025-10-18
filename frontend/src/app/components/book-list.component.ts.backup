import { Component, OnInit, CUSTOM_ELEMENTS_SCHEMA, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatRippleModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
// MatFormFieldModule removed to use plain inline container for the select
import { BookService } from '../services/book.service';
import { Book, SortField, SortDirection, BookSortCriteria, SortOption } from '../models/book.model';
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
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatRippleModule,
  MatSelectModule,
    InfiniteScrollDirective
  ],
  template: `
  <div class="motspassants-library" appInfiniteScroll (scrolled)="onScroll()" [disabled]="scrollState.loading()">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <iconify-icon class="title-icon" icon="material-symbols:book-2-rounded"></iconify-icon>
            Books Library
            <button mat-icon-button class="refresh-btn" aria-label="Refresh books" (click)="refresh()">
              <iconify-icon icon="material-symbols-light:refresh-rounded"></iconify-icon>
            </button>
          </h1>
          <p class="library-subtitle">Discover and explore your digital book collection</p>
        
          <!-- Sort control: matches look-and-feel used across the app. -->
          <div class="header-actions">
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
            <h3>Loading books...</h3>
            <p>Gathering your books from the digital shelves</p>
          </div>
        </div>
      } @else if (scrollState.error() && scrollState.items().length === 0) {
        <div class="empty-library">
          <div class="empty-content">
            <mat-icon class="empty-icon" color="warn">cloud_off</mat-icon>
            <h2>Can’t reach the backend</h2>
            <p>{{ scrollState.error() }}</p>
            <div style="display:flex; gap:12px; justify-content:center;">
              <button mat-raised-button color="primary" (click)="refresh()">
                <iconify-icon icon="material-symbols:refresh-rounded"></iconify-icon>
                Retry
              </button>
              <a mat-button href="http://localhost:8080/q/dev-ui/extensions" target="_blank" rel="noopener">Open Dev UI</a>
            </div>
          </div>
        </div>
      } @else if (scrollState.isEmpty()) {
        <div class="empty-library">
          <div class="empty-content">
            <mat-icon class="empty-icon">collections_bookmark</mat-icon>
            <h2>No books found</h2>
            <p>Your book library is empty. Start building your digital library by scanning your book directory.</p>
            <button mat-raised-button color="accent" routerLink="/library" class="cta-button">
              <iconify-icon icon="material-symbols:search-rounded"></iconify-icon>
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
    </div>
  `,
  styleUrls: ['./book-list.component.css']
})
export class BookListComponent implements OnInit {
  scrollState;
  private readonly CARD_WIDTH = 240; // px
  private readonly CARD_HEIGHT = 360; // px
  private readonly GRID_GAP = 24; // must match CSS gap for desktop
  private readonly PADDING_X = 32; // horizontal padding of .books-grid

  // Available sort options for UI
  sortOptions: SortOption[] = [
    {
      label: 'Recently Updated',
      field: SortField.UPDATED_AT,
      direction: SortDirection.DESC
    },
    {
      label: 'Oldest Updated',
      field: SortField.UPDATED_AT,
      direction: SortDirection.ASC
    },
    {
      label: 'Title A-Z',
      field: SortField.TITLE_SORT,
      direction: SortDirection.ASC
    },
    {
      label: 'Title Z-A',
      field: SortField.TITLE_SORT,
      direction: SortDirection.DESC
    },
    {
      label: 'Publication Date (Newest)',
      field: SortField.PUBLICATION_DATE,
      direction: SortDirection.DESC
    },
    {
      label: 'Publication Date (Oldest)',
      field: SortField.PUBLICATION_DATE,
      direction: SortDirection.ASC
    }
  ];

  // Sort state - initialize after sortOptions
  currentSortCriteria = signal<BookSortCriteria>(BookService.DEFAULT_SORT);
  selectedSortOption = signal<SortOption>(this.getSortOptionFromCriteria(BookService.DEFAULT_SORT));

  constructor(
    private bookService: BookService,
    private snackBar: MatSnackBar,
    private infiniteScrollService: InfiniteScrollService
  ) {
    // Initialize infinite scroll state with sorting support
    this.scrollState = this.infiniteScrollService.createInfiniteScrollState(
      (cursor, limit) => this.bookService.getAllBooks(cursor, limit, this.currentSortCriteria()),
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
  return this.isBookmarked(book) ? 'material-symbols:book' : 'material-symbols:book-outline';
  }

  isBookmarked(book: Book): boolean {
    // TODO: wire to real bookmark state when implemented
    return false;
  }

  refresh() {
    this.bookService.clearCache();
    this.scrollState.reset();
    this.snackBar.open('Books refreshed', 'Close', { duration: 1500 });
  }

  // Sort handling methods
  onSortChange(sortOption: SortOption) {
    const newCriteria: BookSortCriteria = {
      field: sortOption.field,
      direction: sortOption.direction
    };
    
    this.currentSortCriteria.set(newCriteria);
    this.selectedSortOption.set(sortOption);
    
    // Clear cache and reset scroll state to apply new sorting
    this.bookService.clearCache();
    this.scrollState.reset();
    
    this.snackBar.open(`Sorted by ${sortOption.label}`, 'Close', { duration: 2000 });
  }

  onNativeSortChange(event: any) {
    const val = event.target.value as string;
    const [field, dir] = val.split('_');
    const match = this.sortOptions.find(o => o.field === field && String(o.direction) === dir);
    if (match) {
      this.onSortChange(match);
    }
  }

  // Show human readable label when an object is selected in mat-select
  displaySort(option?: SortOption): string {
    return option ? option.label : '';
  }


  private getSortOptionFromCriteria(criteria: BookSortCriteria): SortOption {
    if (!this.sortOptions || this.sortOptions.length === 0) {
      // Return a default option if sortOptions not initialized yet
      return {
        label: 'Recently Updated',
        field: SortField.UPDATED_AT,
        direction: SortDirection.DESC
      };
    }
    return this.sortOptions.find(option => 
      option.field === criteria.field && option.direction === criteria.direction
    ) || this.sortOptions[0];
  }
}