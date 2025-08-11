import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatRippleModule } from '@angular/material/core';
import { BookService } from '../services/book.service';
import { Book, CursorPageResponse, BookSearchCriteria } from '../models/book.model';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSnackBarModule,
    MatSelectModule,
    MatExpansionModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatRippleModule
  ],
  template: `
    <div class="plex-search">
      <div class="search-header">
        <div class="header-content">
          <h1 class="search-title">
            <mat-icon class="title-icon">search</mat-icon>
            Search Library
          </h1>
          <p class="search-subtitle">Find exactly what you're looking for in your collection</p>
        </div>
      </div>

      <div class="search-content">
        <form [formGroup]="searchForm" class="search-form">
          <!-- Quick Search -->
          <div class="quick-search-section">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Quick Search</mat-label>
              <input matInput 
                     formControlName="quickSearch"
                     placeholder="Search books, authors, series, or ISBN..."
                     (keyup.enter)="performQuickSearch()">
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>
            <button mat-raised-button 
                    color="accent" 
                    type="button"
                    (click)="performQuickSearch()"
                    [disabled]="!searchForm.get('quickSearch')?.value?.trim()">
              Search
            </button>
          </div>

          <!-- Advanced Search -->
          <mat-expansion-panel class="advanced-panel">
            <mat-expansion-panel-header>
              <mat-panel-title>
                <mat-icon>tune</mat-icon>
                Advanced Search
              </mat-panel-title>
              <mat-panel-description>
                Use detailed criteria to find specific books
              </mat-panel-description>
            </mat-expansion-panel-header>

            <div class="advanced-form">
              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Title</mat-label>
                  <input matInput formControlName="title" placeholder="Book title contains...">
                </mat-form-field>
                
                <mat-form-field appearance="outline">
                  <mat-label>Authors</mat-label>
                  <input matInput formControlName="authors" placeholder="Author names (comma separated)">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Series</mat-label>
                  <input matInput formControlName="series" placeholder="Series name">
                </mat-form-field>
                
                <mat-form-field appearance="outline">
                  <mat-label>Publisher</mat-label>
                  <input matInput formControlName="publisher" placeholder="Publisher name">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Language</mat-label>
                  <mat-select formControlName="language">
                    <mat-option value="">Any Language</mat-option>
                    <mat-option value="English">English</mat-option>
                    <mat-option value="French">French</mat-option>
                    <mat-option value="German">German</mat-option>
                    <mat-option value="Spanish">Spanish</mat-option>
                    <mat-option value="Chinese">Chinese</mat-option>
                    <mat-option value="Japanese">Japanese</mat-option>
                  </mat-select>
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Format</mat-label>
                  <mat-select formControlName="formats" multiple>
                    <mat-option value="EPUB">EPUB</mat-option>
                    <mat-option value="PDF">PDF</mat-option>
                    <mat-option value="MOBI">MOBI</mat-option>
                    <mat-option value="AZW3">AZW3</mat-option>
                    <mat-option value="TXT">TXT</mat-option>
                  </mat-select>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Published After</mat-label>
                  <input matInput 
                         [matDatepicker]="afterPicker" 
                         formControlName="publishedAfter"
                         placeholder="From date">
                  <mat-datepicker-toggle matSuffix [for]="afterPicker"></mat-datepicker-toggle>
                  <mat-datepicker #afterPicker></mat-datepicker>
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Published Before</mat-label>
                  <input matInput 
                         [matDatepicker]="beforePicker" 
                         formControlName="publishedBefore"
                         placeholder="To date">
                  <mat-datepicker-toggle matSuffix [for]="beforePicker"></mat-datepicker-toggle>
                  <mat-datepicker #beforePicker></mat-datepicker>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Sort By</mat-label>
                  <mat-select formControlName="sortBy">
                    <mat-option value="title">Title</mat-option>
                    <mat-option value="createdAt">Date Added</mat-option>
                    <mat-option value="publicationDate">Publication Date</mat-option>
                    <mat-option value="author">Author</mat-option>
                  </mat-select>
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Sort Direction</mat-label>
                  <mat-select formControlName="sortDirection">
                    <mat-option value="asc">Ascending</mat-option>
                    <mat-option value="desc">Descending</mat-option>
                  </mat-select>
                </mat-form-field>
              </div>

              <div class="advanced-actions">
                <button mat-raised-button color="accent" type="button" (click)="performAdvancedSearch()">
                  <mat-icon>search</mat-icon>
                  Advanced Search
                </button>
                <button mat-button type="button" (click)="clearForm()">
                  <mat-icon>clear</mat-icon>
                  Clear
                </button>
              </div>
            </div>
          </mat-expansion-panel>
        </form>

        <!-- Search Results -->
        @if (hasSearched() && !loading()) {
          <div class="results-header">
            @if (books().length > 0) {
              <h2>Found {{ books().length }} result(s)</h2>
              @if (lastSearchQuery()) {
                <p>for "{{ lastSearchQuery() }}"</p>
              }
            } @else {
              <h2>No results found</h2>
              @if (lastSearchQuery()) {
                <p>for "{{ lastSearchQuery() }}"</p>
              }
            }
          </div>
        }
        
        @if (loading()) {
          <div class="loading-section">
            <div class="loading-content">
              <mat-spinner diameter="60" color="accent"></mat-spinner>
              <h3>Searching your library...</h3>
              <p>Finding books that match your criteria</p>
            </div>
          </div>
        } @else if (books().length > 0) {
          <div class="books-grid">
            @for (book of books(); track book.id) {
              <div class="book-poster" matRipple [routerLink]="['/books', book.id]">
                <div class="book-cover">
                  @if (book.hasCover) {
                    <img [src]="'/api/books/' + book.id + '/cover'" 
                         [alt]="book.title + ' cover'"
                         class="cover-image"
                         (error)="onImageError($event)">
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
                  @if (book.contributors?.['author']?.length) {
                    <p class="book-author">{{ book.contributors!['author'].join(', ') }}</p>
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
            @if (previousCursor() || nextCursor()) {
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
                }
              </div>
            }
          </div>
        } @else if (hasSearched() && !loading()) {
          <div class="empty-results">
            <div class="empty-content">
              <mat-icon class="empty-icon">search_off</mat-icon>
              <h2>No books found</h2>
              <p>Try adjusting your search criteria or checking your spelling.</p>
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .plex-search {
      min-height: 100vh;
      background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
      color: #ffffff;
    }

    .search-header {
      background: linear-gradient(135deg, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.4) 100%);
      padding: 40px 32px;
      border-bottom: 1px solid #333;
    }

    .search-title {
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

    .search-subtitle {
      font-size: 1.1rem;
      color: #ccc;
      margin: 0;
      font-weight: 300;
    }

    .search-content {
      padding: 32px;
    }

    .search-form {
      margin-bottom: 32px;
    }

    .quick-search-section {
      display: flex;
      gap: 16px;
      align-items: flex-end;
      margin-bottom: 24px;
    }

    .search-field {
      flex: 1;
    }

    .advanced-panel {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid #333;
    }

    .advanced-form {
      padding: 16px 0;
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
      margin-bottom: 16px;
    }

    .advanced-actions {
      display: flex;
      gap: 16px;
      margin-top: 24px;
    }

    .results-header {
      margin-bottom: 24px;
      padding: 16px 0;
      border-bottom: 1px solid #333;
    }

    .results-header h2 {
      margin: 0 0 8px 0;
      color: #fff;
      font-weight: 400;
    }

    .results-header p {
      margin: 0;
      color: #ccc;
      font-style: italic;
    }

    .loading-section {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 40vh;
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

    .empty-results {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 40vh;
    }

    .empty-content {
      text-align: center;
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
      margin: 0;
      line-height: 1.6;
    }

    .books-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 24px;
      margin-bottom: 32px;
    }

    .book-poster {
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
      position: relative;
      border-radius: 8px;
      overflow: hidden;
    }

    .book-poster:hover {
      transform: scale(1.05) translateY(-8px);
      z-index: 10;
    }

    .book-cover {
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

    .book-poster:hover .cover-overlay {
      opacity: 1;
    }

    .play-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #e5a00d;
    }

    .book-info {
      padding: 12px 0;
    }

    .book-title {
      font-size: 14px;
      font-weight: 600;
      margin: 0 0 4px 0;
      color: #fff;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .book-author {
      font-size: 12px;
      color: #ccc;
      margin: 0 0 4px 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .book-year {
      font-size: 11px;
      color: #888;
      margin: 0 0 8px 0;
    }

    .book-metadata {
      display: flex;
      gap: 4px;
      flex-wrap: wrap;
    }

    .metadata-chip {
      font-size: 10px;
      height: 18px;
      line-height: 18px;
      padding: 0 6px;
      border-radius: 9px;
    }

    .language-chip {
      background: rgba(229, 160, 13, 0.2);
      color: #e5a00d;
    }

    .format-chip {
      background: rgba(255, 255, 255, 0.1);
      color: #ccc;
    }

    .pagination-section {
      padding: 32px 0;
      border-top: 1px solid #333;
    }

    .pagination-controls {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 24px;
    }

    .nav-button {
      background: rgba(255, 255, 255, 0.1);
      color: #fff;
      border: 1px solid #555;
    }

    .nav-button:hover {
      background: rgba(229, 160, 13, 0.2);
      border-color: #e5a00d;
    }

    @media (max-width: 768px) {
      .search-header {
        padding: 24px 16px;
      }

      .search-content {
        padding: 16px;
      }

      .search-title {
        font-size: 2rem;
      }

      .quick-search-section {
        flex-direction: column;
        align-items: stretch;
        gap: 12px;
      }

      .form-row {
        grid-template-columns: 1fr;
        gap: 12px;
      }

      .books-grid {
        grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
        gap: 16px;
      }

      .pagination-controls {
        flex-direction: column;
        gap: 16px;
      }

      .advanced-actions {
        flex-direction: column;
      }
    }
  `]
})
export class SearchComponent {
  searchForm: FormGroup;
  books = signal<Book[]>([]);
  loading = signal(false);
  hasSearched = signal(false);
  lastSearchQuery = signal('');
  nextCursor = signal<string | undefined>(undefined);
  previousCursor = signal<string | undefined>(undefined);
  limit = signal(20);
  lastCriteria: BookSearchCriteria | null = null;

  constructor(
    private bookService: BookService,
    private snackBar: MatSnackBar,
    private fb: FormBuilder
  ) {
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
  }

  performQuickSearch() {
    const query = this.searchForm.get('quickSearch')?.value?.trim();
    if (!query) return;

    this.loading.set(true);
    this.hasSearched.set(true);
    this.lastSearchQuery.set(query);
    this.lastCriteria = null;

    this.bookService.searchBooks(query, undefined, this.limit()).subscribe({
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
    this.hasSearched.set(false);
    this.lastSearchQuery.set('');
    this.lastCriteria = null;
  }

  getYear(dateString: string): string {
    return new Date(dateString).getFullYear().toString();
  }

  getShortTitle(title: string): string {
    return title.length > 30 ? title.substring(0, 30) + '...' : title;
  }

  onImageError(event: any) {
    event.target.style.display = 'none';
  }
}