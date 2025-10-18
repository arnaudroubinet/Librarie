import { Component, OnDestroy, signal, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MATERIAL_MODULES } from '../shared/materials';
import { getShortTitle as utilGetShortTitle, formatDate as utilFormatDate } from '../utils/author-utils';
import { BookService } from '../services/book.service';
import { SearchService } from '../services/search.service';
import { Book, CursorPageResponse, BookSearchCriteria } from '../models/book.model';
import { Series } from '../models/series.model';
import { Author } from '../models/author.model';
import { UnifiedSearchResult } from '../models/search.model';
import { environment } from '../../environments/environment';
@Component({
  selector: 'app-search',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
  CommonModule,
  FormsModule,
  ReactiveFormsModule,
  RouterModule,
  ...MATERIAL_MODULES
  ],
  template: `
  <div class="motspassants-library motspassants-search">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <iconify-icon class="title-icon" icon="material-symbols:search-rounded"></iconify-icon>
            Search Library
          </h1>
          <p class="library-subtitle">Find exactly what you're looking for in your collection</p>
        </div>
      </div>

      <div class="search-content">
        <form class="search-form" [formGroup]="searchForm" (ngSubmit)="performQuickSearch()">
          <div class="quick-search-section">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Quick search</mat-label>
              <input matInput placeholder="Title, author, series..." formControlName="quickSearch" (input)="onSearchInput($event)" />
            </mat-form-field>
            <button mat-raised-button color="primary" type="submit">Search</button>
          </div>

        @if (showSuggestions()) {
          <div class="search-suggestions">
            <div class="suggestions-header">
              <mat-icon>tips_and_updates</mat-icon>
              Suggestions
            </div>
            <div class="suggestions-list">
              @for (s of searchSuggestions(); track s) {
                <button class="suggestion-item" (click)="applySuggestion(s)">
                  <mat-icon>north_east</mat-icon>
                  <span>{{ s }}</span>
                </button>
              }
            </div>
          </div>
        }

        <mat-accordion class="advanced-panel">
          <mat-expansion-panel>
            <mat-expansion-panel-header>
              <mat-panel-title>Advanced search</mat-panel-title>
              <mat-panel-description>Filter by fields</mat-panel-description>
            </mat-expansion-panel-header>

            <div class="advanced-form" [formGroup]="searchForm">
              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Title contains</mat-label>
                  <input matInput formControlName="title" />
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Authors (comma-separated)</mat-label>
                  <input matInput formControlName="authors" />
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Series contains</mat-label>
                  <input matInput formControlName="series" />
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Publisher contains</mat-label>
                  <input matInput formControlName="publisher" />
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Language</mat-label>
                  <input matInput formControlName="language" />
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Formats</mat-label>
                  <mat-select formControlName="formats" multiple>
                    <mat-option value="EPUB">EPUB</mat-option>
                    <mat-option value="PDF">PDF</mat-option>
                    <mat-option value="MOBI">MOBI</mat-option>
                    <mat-option value="AZW3">AZW3</mat-option>
                  </mat-select>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Published after</mat-label>
                  <input matInput [matDatepicker]="afterPicker" formControlName="publishedAfter">
                  <mat-datepicker-toggle matSuffix [for]="afterPicker"></mat-datepicker-toggle>
                  <mat-datepicker #afterPicker></mat-datepicker>
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Published before</mat-label>
                  <input matInput [matDatepicker]="beforePicker" formControlName="publishedBefore">
                  <mat-datepicker-toggle matSuffix [for]="beforePicker"></mat-datepicker-toggle>
                  <mat-datepicker #beforePicker></mat-datepicker>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Sort by</mat-label>
                  <mat-select formControlName="sortBy">
                    <mat-option value="title">Title</mat-option>
                    <mat-option value="publicationDate">Publication date</mat-option>
                  </mat-select>
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Sort direction</mat-label>
                  <mat-select formControlName="sortDirection">
                    <mat-option value="asc">Ascending</mat-option>
                    <mat-option value="desc">Descending</mat-option>
                  </mat-select>
                </mat-form-field>
              </div>

              <div class="advanced-actions">
                <button mat-stroked-button type="button" (click)="clearForm()">Clear</button>
                <button mat-raised-button color="primary" type="button" (click)="performAdvancedSearch()">Search</button>
              </div>
            </div>
          </mat-expansion-panel>
        </mat-accordion>
        </form>

        @if (loading()) {
          <div class="loading-section">
            <div class="loading-content">
              <mat-spinner diameter="60"></mat-spinner>
              <h3>Searching...</h3>
              <p>Looking through your library</p>
            </div>
          </div>
        } @else {
          @if (!hasSearched()) {
            <div class="empty-results">
              <div class="empty-content">
                <mat-icon class="empty-icon">search</mat-icon>
                <h2>Start typing to search</h2>
                <p>Use quick search or expand advanced options</p>
              </div>
            </div>
          } @else if (isUnifiedSearch()) {
            @if (books().length > 0) {
              <div class="results-section">
                <h3 class="section-title">
                  <iconify-icon icon="material-symbols:book-2-rounded"></iconify-icon>
                  Books ({{ books().length }})
                </h3>
                <div class="books-grid">
                  @for (book of books(); track book.id) {
                    <div class="book-poster" matRipple [routerLink]="['/books', book.id]">
                      <div class="book-cover">
                        @if (book.hasCover) {
                          <img [src]="apiUrl + '/v1/books/' + book.id + '/cover'" [alt]="book.title + ' cover'" class="cover-image" (error)="onImageError($event)">
                        } @else {
                          <div class="cover-placeholder">
                            <mat-icon>book</mat-icon>
                            <span class="title-text">{{ getShortTitle(book.title) }}</span>
                          </div>
                        }
                        <div class="cover-overlay">
                          <mat-icon class="play-icon">visibility</mat-icon>
                        </div>
                      </div>
                      <div class="book-info">
                        <h3 class="book-title" [title]="book.title">{{ book.title }}</h3>
                        @if (book.contributorsDetailed?.['author']?.length) {
                          <p class="book-author">{{ getBookAuthorsLine(book) }}</p>
                        }
                        @if (book.publicationDate) {
                          <p class="book-year">{{ getYear(book.publicationDate) }}</p>
                        }
                      </div>
                    </div>
                  }
                </div>
              </div>
            }

            @if (series().length > 0) {
              <div class="results-section">
                <h3 class="section-title">
                  <iconify-icon icon="material-symbols:books-movies-and-music"></iconify-icon>
                  Series ({{ series().length }})
                </h3>
                <div class="series-grid">
                  @for (s of series(); track s.id) {
                    <div class="series-item" matRipple>
                      <div class="series-info">
                        <h4 class="series-name">{{ s.name }}</h4>
                        @if (s.description) {
                          <p class="series-description">{{ s.description }}</p>
                        }
                        <p class="series-count">{{ s.bookCount }} book(s)</p>
                      </div>
                    </div>
                  }
                </div>
              </div>
            }

            @if (authors().length > 0) {
              <div class="results-section">
                <h3 class="section-title">
                  <iconify-icon icon="material-symbols:supervised-user-circle"></iconify-icon>
                  Authors ({{ authors().length }})
                </h3>
                <div class="authors-grid">
                  @for (author of authors(); track author.id) {
                    <div class="author-item" matRipple>
                      <div class="author-info">
                        <h4 class="author-name">{{ author.name }}</h4>
                        @if (author.birthDate || author.deathDate) {
                          <p class="author-dates">
                            {{ author.birthDate ? getYear(author.birthDate) : '?' }} -
                            {{ author.deathDate ? getYear(author.deathDate) : 'Present' }}
                          </p>
                        }
                        @if (author.bio && author.bio['en']) {
                          <p class="author-bio">{{ author.bio['en'] }}</p>
                        }
                      </div>
                    </div>
                  }
                </div>
              </div>
            }

            @if (books().length === 0 && series().length === 0 && authors().length === 0) {
              <div class="empty-results">
                <div class="empty-content">
                  <mat-icon class="empty-icon">search_off</mat-icon>
                  <h2>No results found</h2>
                  <p>Try adjusting your search.</p>
                </div>
              </div>
            }
          } @else if (books().length > 0) {
            <div class="books-grid">
              @for (book of books(); track book.id) {
                <div class="book-poster" matRipple [routerLink]="['/books', book.id]">
                  <div class="book-cover">
                    @if (book.hasCover) {
                      <img [src]="apiUrl + '/v1/books/' + book.id + '/cover'" [alt]="book.title + ' cover'" class="cover-image" (error)="onImageError($event)">
                    } @else {
                      <div class="cover-placeholder">
                        <mat-icon>book</mat-icon>
                        <span class="title-text">{{ getShortTitle(book.title) }}</span>
                      </div>
                    }
                    <div class="cover-overlay">
                      <mat-icon class="play-icon">visibility</mat-icon>
                    </div>
                  </div>

                  <div class="book-info">
                    <h3 class="book-title" [title]="book.title">{{ book.title }}</h3>
                    @if (book.contributorsDetailed?.['author']?.length) {
                      <p class="book-author">{{ getBookAuthorsLine(book) }}</p>
                    }
                    @if (book.publicationDate) {
                      <p class="book-year">{{ getYear(book.publicationDate) }}</p>
                    }
                    <div class="book-metadata">
                      @if (book.language) {
                        <mat-chip class="metadata-chip language-chip">{{ book.language }}</mat-chip>
                      }
                      @if (book.formats && book.formats.length > 0) {
                        @for (format of book.formats; track format) {
                          <mat-chip class="metadata-chip format-chip">{{ format }}</mat-chip>
                        }
                      }
                    </div>
                  </div>
                </div>
              }
            </div>

            <div class="pagination-section">
              @if (previousCursor() || nextCursor() || hasSearched()) {
                <div class="pagination-controls">
                  @if (previousCursor()) {
                    <button mat-raised-button class="nav-button" (click)="loadPrevious()">
                      <mat-icon>chevron_left</mat-icon>
                      Previous
                    </button>
                  }
                  @if (nextCursor()) {
                    <button mat-raised-button class="nav-button" (click)="loadNext()">
                      Next
                      <mat-icon>chevron_right</mat-icon>
                    </button>
                  } @else if (previousCursor() || hasSearched()) {
                    <button mat-raised-button class="nav-button back-button" (click)="goBack()">
                      <mat-icon>arrow_back</mat-icon>
                      Back
                    </button>
                  }
                </div>
              }
            </div>
          } @else if (hasSearched() && !loading()) {
            <div class="empty-results">
              <div class="empty-content">
                <mat-icon class="empty-icon">search_off</mat-icon>
                <h2>No books found</h2>
                <p>Try adjusting your search.</p>
              </div>
            </div>
          }
        }
      </div>
    </div>
  `,
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnDestroy {
  readonly apiUrl = environment.apiUrl;
  searchForm: FormGroup;
  books = signal<Book[]>([]);
  series = signal<Series[]>([]);
  authors = signal<Author[]>([]);
  loading = signal(false);
  hasSearched = signal(false);
  lastSearchQuery = signal('');
  nextCursor = signal<string | undefined>(undefined);
  previousCursor = signal<string | undefined>(undefined);
  limit = signal(20);
  lastCriteria: BookSearchCriteria | null = null;
  isUnifiedSearch = signal(false);
  private pageHistory: string[] = [];
  private popstateListener?: () => void;
  
  // Modern search features
  searchSuggestions = signal<string[]>([]);
  showSuggestions = signal(false);
  private searchTimeout?: any;
  private searchHistory: string[] = [];

  constructor(
    private bookService: BookService,
    private searchService: SearchService,
    private snackBar: MatSnackBar,
    private fb: FormBuilder,
    private location: Location
  ) {
    // Initialize searchForm immediately to avoid NG01050 error
    this.searchForm = this.fb.group({
      quickSearch: [''],
      title: [''],
      authors: [''],
      series: [''],
      publisher: [''],
      language: [''],
      formats: [[]],
      publishedAfter: [''],
      publishedBefore: [''],
      sortBy: ['title'],
      sortDirection: ['asc']
    });
    
    this.setupBrowserBackSupport();
    this.setupKeyboardShortcuts();
    this.loadSearchHistory();
  }

  ngOnDestroy() {
    if (this.popstateListener) {
      window.removeEventListener('popstate', this.popstateListener);
    }
  }

  private setupBrowserBackSupport() {
    this.popstateListener = () => {
      // Handle browser back/forward navigation
      const state = window.history.state;
      if (state && state.searchPage && state.cursor !== undefined) {
        this.loadSearchPage(state.cursor, state.query, state.criteria);
      }
    };
    window.addEventListener('popstate', this.popstateListener);
  }

  private loadSearchPage(cursor: string, query?: string, criteria?: BookSearchCriteria) {
    this.loading.set(true);
    
    if (criteria) {
      this.bookService.searchBooksByCriteria(criteria, cursor, this.limit()).subscribe({
        next: (response: CursorPageResponse<Book>) => {
          this.books.set(response.content);
          this.nextCursor.set(response.nextCursor);
          this.previousCursor.set(response.previousCursor);
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Error loading search page:', error);
          this.loading.set(false);
        }
      });
    } else if (query) {
      this.bookService.searchBooks(query, cursor, this.limit()).subscribe({
        next: (response: CursorPageResponse<Book>) => {
          this.books.set(response.content);
          this.nextCursor.set(response.nextCursor);
          this.previousCursor.set(response.previousCursor);
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Error loading search page:', error);
          this.loading.set(false);
        }
      });
    }
  }

  private updateBrowserHistory(cursor?: string) {
    const state = {
      searchPage: true,
      cursor: cursor,
      query: this.lastSearchQuery(),
      criteria: this.lastCriteria
    };
    const url = '/search' + (cursor ? '?cursor=' + encodeURIComponent(cursor) : '');
    window.history.pushState(state, '', url);
  }

  performQuickSearch() {
    const query = this.searchForm.get('quickSearch')?.value?.trim();
    if (!query) return;

    this.loading.set(true);
    this.hasSearched.set(true);
    this.lastSearchQuery.set(query);
    this.lastCriteria = null;
    this.isUnifiedSearch.set(true);
    this.showSuggestions.set(false);
    
    // Save to search history
    this.saveSearchHistory(query);

    this.searchService.unifiedSearch(query, 10).subscribe({
      next: (response: UnifiedSearchResult) => {
        this.books.set(response.books);
        this.series.set(response.series);
        this.authors.set(response.authors);
        // Reset pagination cursors for unified search
        this.nextCursor.set(undefined);
        this.previousCursor.set(undefined);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error performing unified search:', error);
        this.snackBar.open('Failed to search. Please try again.', 'Close', {
          duration: 3000
        });
        this.loading.set(false);
      }
    });
  }

  performAdvancedSearch() {
    const formValues = this.searchForm.value;
    
    // Build search criteria
    const criteria: BookSearchCriteria = {
      titleContains: formValues.title || undefined,
      contributorsContain: formValues.authors ? formValues.authors.split(',').map((a: string) => a.trim()) : undefined,
      seriesContains: formValues.series || undefined,
      publisherContains: formValues.publisher || undefined,
      languageEquals: formValues.language || undefined,
      formatsIn: formValues.formats && formValues.formats.length > 0 ? formValues.formats : undefined,
      publishedAfter: formValues.publishedAfter ? formValues.publishedAfter.toISOString().split('T')[0] : undefined,
      publishedBefore: formValues.publishedBefore ? formValues.publishedBefore.toISOString().split('T')[0] : undefined,
      sortBy: formValues.sortBy || 'title',
      sortDirection: formValues.sortDirection || 'asc'
    };

    // Check if any criteria is set
    const hasCriteria = Object.values(criteria).some(value => 
      value !== undefined && value !== null && 
      (Array.isArray(value) ? value.length > 0 : true)
    );

    if (!hasCriteria) {
      this.snackBar.open('Please specify at least one search criteria.', 'Close', {
        duration: 3000
      });
      return;
    }

    this.loading.set(true);
    this.hasSearched.set(true);
    this.lastSearchQuery.set('Advanced Search');
    this.lastCriteria = criteria;
    this.isUnifiedSearch.set(false);
    // Clear non-book results for advanced search
    this.series.set([]);
    this.authors.set([]);

    this.bookService.searchBooksByCriteria(criteria, undefined, this.limit()).subscribe({
      next: (response: CursorPageResponse<Book>) => {
        this.books.set(response.content);
        this.nextCursor.set(response.nextCursor);
        this.previousCursor.set(response.previousCursor);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error searching books:', error);
        this.snackBar.open('Failed to search books. Please try again.', 'Close', {
          duration: 3000
        });
        this.loading.set(false);
      }
    });
  }

  loadNext() {
    if (!this.nextCursor()) return;

    this.loading.set(true);
    this.pageHistory.push(this.nextCursor()!);
    this.updateBrowserHistory(this.nextCursor());

    if (this.lastCriteria) {
      // Continue advanced search with cursor
      this.bookService.searchBooksByCriteria(this.lastCriteria, this.nextCursor(), this.limit()).subscribe({
        next: (response: CursorPageResponse<Book>) => {
          this.books.set(response.content);
          this.nextCursor.set(response.nextCursor);
          this.previousCursor.set(response.previousCursor);
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Error loading next page:', error);
          this.loading.set(false);
        }
      });
    } else {
      // Continue quick search with cursor
      const query = this.searchForm.get('quickSearch')?.value?.trim();
      this.bookService.searchBooks(query, this.nextCursor(), this.limit()).subscribe({
        next: (response: CursorPageResponse<Book>) => {
          this.books.set(response.content);
          this.nextCursor.set(response.nextCursor);
          this.previousCursor.set(response.previousCursor);
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Error loading next page:', error);
          this.loading.set(false);
        }
      });
    }
  }

  loadPrevious() {
    if (!this.previousCursor()) return;

    this.loading.set(true);
    this.pageHistory.pop(); // Remove current page from history
    this.updateBrowserHistory(this.previousCursor());

    if (this.lastCriteria) {
      // Continue advanced search with cursor
      this.bookService.searchBooksByCriteria(this.lastCriteria, this.previousCursor(), this.limit()).subscribe({
        next: (response: CursorPageResponse<Book>) => {
          this.books.set(response.content);
          this.nextCursor.set(response.nextCursor);
          this.previousCursor.set(response.previousCursor);
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Error loading previous page:', error);
          this.loading.set(false);
        }
      });
    } else {
      // Continue quick search with cursor
      const query = this.searchForm.get('quickSearch')?.value?.trim();
      this.bookService.searchBooks(query, this.previousCursor(), this.limit()).subscribe({
        next: (response: CursorPageResponse<Book>) => {
          this.books.set(response.content);
          this.nextCursor.set(response.nextCursor);
          this.previousCursor.set(response.previousCursor);
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Error loading previous page:', error);
          this.loading.set(false);
        }
      });
    }
  }

  goBack() {
    this.location.back();
  }

  clearForm() {
    this.searchForm.reset({
      quickSearch: '',
      title: '',
      authors: '',
      series: '',
      publisher: '',
      language: '',
      formats: [],
      publishedAfter: '',
      publishedBefore: '',
      sortBy: 'title',
      sortDirection: 'asc'
    });
    this.books.set([]);
    this.series.set([]);
    this.authors.set([]);
    this.hasSearched.set(false);
    this.lastSearchQuery.set('');
    this.lastCriteria = null;
    this.isUnifiedSearch.set(false);
    this.showSuggestions.set(false);
  }

  // Modern search features
  private setupKeyboardShortcuts() {
    document.addEventListener('keydown', (event) => {
      // Focus search input when '/' is pressed
      if (event.key === '/' && !event.ctrlKey && !event.metaKey) {
        const activeElement = document.activeElement;
        if (activeElement?.tagName !== 'INPUT' && activeElement?.tagName !== 'TEXTAREA') {
          event.preventDefault();
          const searchInput = document.querySelector('input[formControlName="quickSearch"]') as HTMLInputElement;
          if (searchInput) {
            searchInput.focus();
            searchInput.select();
          }
        }
      }
      // Clear search with Escape
      if (event.key === 'Escape') {
        this.showSuggestions.set(false);
      }
    });
  }

  private loadSearchHistory() {
    const history = localStorage.getItem('searchHistory');
    if (history) {
      this.searchHistory = JSON.parse(history);
    }
  }

  private saveSearchHistory(query: string) {
    if (!query.trim()) return;
    
    // Remove existing entry if present
    this.searchHistory = this.searchHistory.filter(item => item !== query);
    // Add to beginning
    this.searchHistory.unshift(query);
    // Keep only last 10 searches
    this.searchHistory = this.searchHistory.slice(0, 10);
    
    localStorage.setItem('searchHistory', JSON.stringify(this.searchHistory));
  }

  onSearchInput(event: any) {
    const query = event.target.value?.trim();
    
    // Clear previous timeout
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    // Show suggestions after short delay
    this.searchTimeout = setTimeout(() => {
      if (query && query.length > 0) {
        this.updateSearchSuggestions(query);
      } else {
        this.showSuggestions.set(false);
      }
    }, 300);
  }

  private updateSearchSuggestions(query: string) {
    // Filter search history based on current query
    const suggestions = this.searchHistory
      .filter(item => item.toLowerCase().includes(query.toLowerCase()) && item !== query)
      .slice(0, 5);
    
    // Add smart suggestions based on query patterns
    const smartSuggestions = this.generateSmartSuggestions(query);
    
    const allSuggestions = [...suggestions, ...smartSuggestions]
      .filter((item, index, arr) => arr.indexOf(item) === index) // Remove duplicates
      .slice(0, 5);
    
    this.searchSuggestions.set(allSuggestions);
    this.showSuggestions.set(allSuggestions.length > 0);
  }

  private generateSmartSuggestions(query: string): string[] {
    const suggestions: string[] = [];
    
    // If query looks like a year, suggest year searches
    if (/^\d{4}$/.test(query)) {
      suggestions.push('year:' + query);
    }
    
    // If query has multiple words, suggest author and series searches
    if (query.includes(' ') && !query.includes(':')) {
      suggestions.push('author:"' + query + '"');
      suggestions.push('series:"' + query + '"');
    }
    
    return suggestions;
  }

  applySuggestion(suggestion: string) {
    this.searchForm.patchValue({ quickSearch: suggestion });
    this.showSuggestions.set(false);
    this.performQuickSearch();
  }

  getYear(dateString: string): string { return new Date(dateString).getFullYear().toString(); }
  getShortTitle = utilGetShortTitle;

  onImageError(event: any) {
    event.target.style.display = 'none';
  }

  // Helper to render authors without arrow functions in template
  getBookAuthorsLine(book: Book): string {
    const authors = book.contributorsDetailed?.['author'];
    if (Array.isArray(authors) && authors.length > 0) {
      return authors.map(a => a.name).join(', ');
    }
    return '';
  }
}