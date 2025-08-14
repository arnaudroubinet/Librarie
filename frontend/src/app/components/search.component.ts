import { Component, OnDestroy, signal } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
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
import { SearchService } from '../services/search.service';
import { Book, CursorPageResponse, BookSearchCriteria } from '../models/book.model';
import { Series } from '../models/series.model';
import { Author } from '../models/author.model';
import { UnifiedSearchResult } from '../models/search.model';

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
                     placeholder="Search books, authors, series, or ISBN... (Press / to focus)"
                     (keyup.enter)="performQuickSearch()"
                     (keyup)="onSearchInput($event)"
                     autocomplete="off"
                     #searchInput>
              <mat-icon matSuffix>search</mat-icon>
              <mat-hint>Use keywords like "author:name", "series:title", "year:2023"</mat-hint>
            </mat-form-field>
            <button mat-raised-button 
                    color="primary" 
                    type="button"
                    (click)="performQuickSearch()"
                    [disabled]="!searchForm.get('quickSearch')?.value?.trim()">
              <mat-icon>search</mat-icon>
              Search
            </button>
          </div>

          <!-- Search Suggestions -->
          @if (searchSuggestions().length > 0 && showSuggestions()) {
            <div class="search-suggestions">
              <div class="suggestions-header">
                <mat-icon>auto_awesome</mat-icon>
                Suggestions
              </div>
              <div class="suggestions-list">
                @for (suggestion of searchSuggestions(); track suggestion) {
                  <button class="suggestion-item" (click)="applySuggestion(suggestion)">
                    <mat-icon>history</mat-icon>
                    {{ suggestion }}
                  </button>
                }
              </div>
            </div>
          }

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
            @if (isUnifiedSearch()) {
              @if (books().length > 0 || series().length > 0 || authors().length > 0) {
                <h2>Found {{ books().length + series().length + authors().length }} result(s)</h2>
                @if (lastSearchQuery()) {
                  <p>for "{{ lastSearchQuery() }}"</p>
                }
              } @else {
                <h2>No results found</h2>
                @if (lastSearchQuery()) {
                  <p>for "{{ lastSearchQuery() }}"</p>
                }
              }
            } @else {
              @if (books().length > 0) {
                <h2>Found {{ books().length }} book(s)</h2>
                @if (lastSearchQuery()) {
                  <p>for "{{ lastSearchQuery() }}"</p>
                }
              } @else {
                <h2>No books found</h2>
                @if (lastSearchQuery()) {
                  <p>for "{{ lastSearchQuery() }}"</p>
                }
              }
            }
          </div>
        }
        
        @if (loading()) {
          <div class="loading-section">
            <div class="loading-content">
              <mat-spinner diameter="60" color="accent"></mat-spinner>
              <h3>Searching your library...</h3>
              <p>Finding content that match your criteria</p>
            </div>
          </div>
        } @else if (isUnifiedSearch()) {
          <!-- Unified Search Results -->
          @if (books().length > 0) {
            <div class="results-section">
              <h3 class="section-title">
                <mat-icon>book</mat-icon>
                Books ({{ books().length }})
              </h3>
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
                    </div>
                  </div>
                }
              </div>
            </div>
          }
          
          @if (series().length > 0) {
            <div class="results-section">
              <h3 class="section-title">
                <mat-icon>collections_bookmark</mat-icon>
                Series ({{ series().length }})
              </h3>
              <div class="series-grid">
                @for (s of series(); track s.id) {
                  <div class="series-item" matRipple [routerLink]="['/series', s.id]">
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
                <mat-icon>person</mat-icon>
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
                <p>Try adjusting your search criteria or checking your spelling.</p>
              </div>
            </div>
          }
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
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      color: var(--text-primary);
      font-family: var(--font-family-primary);
    }

    .search-header {
      background: linear-gradient(135deg, var(--primary-color) 0%, #1565c0 100%);
      padding: 48px 32px;
      color: white;
      box-shadow: 0 4px 20px rgba(25, 118, 210, 0.15);
    }

    .search-title {
      font-size: 2.75rem;
      font-weight: 400;
      margin: 0 0 12px 0;
      display: flex;
      align-items: center;
      gap: 20px;
      font-family: var(--font-family-display);
      letter-spacing: -0.02em;
    }

    .title-icon {
      font-size: 3rem;
      width: 3rem;
      height: 3rem;
      color: #ffffff;
      opacity: 0.95;
    }

    .search-subtitle {
      font-size: 1.2rem;
      color: rgba(255, 255, 255, 0.9);
      margin: 0;
      font-weight: 300;
    }

    .search-content {
      padding: 40px 32px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .search-form {
      margin-bottom: 40px;
    }

    .quick-search-section {
      display: flex;
      gap: 16px;
      align-items: flex-end;
      margin-bottom: 32px;
      background: white;
      padding: 24px;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      border: 1px solid #e0e0e0;
    }

    .search-field {
      flex: 1;
    }

    /* Search Suggestions */
    .search-suggestions {
      background: white;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      margin-bottom: 24px;
      overflow: hidden;
      z-index: 100;
    }

    .suggestions-header {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 16px;
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      border-bottom: 1px solid #e0e0e0;
      font-size: 12px;
      font-weight: 500;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .suggestions-header mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      color: var(--primary-color);
    }

    .suggestions-list {
      max-height: 200px;
      overflow-y: auto;
    }

    .suggestion-item {
      display: flex;
      align-items: center;
      gap: 12px;
      width: 100%;
      padding: 12px 16px;
      border: none;
      background: transparent;
      text-align: left;
      cursor: pointer;
      transition: background-color 0.2s ease;
      font-size: 14px;
      color: var(--text-primary);
    }

    .suggestion-item:hover {
      background: rgba(25, 118, 210, 0.04);
    }

    .suggestion-item mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
      color: #9e9e9e;
    }

    .advanced-panel {
      background: white;
      border: 1px solid #e0e0e0;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
      overflow: hidden;
    }

    .advanced-form {
      padding: 24px;
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
      margin-bottom: 20px;
    }

    .advanced-actions {
      display: flex;
      gap: 16px;
      margin-top: 32px;
      padding-top: 20px;
      border-top: 1px solid #f0f0f0;
    }

    .results-header {
      margin-bottom: 32px;
      padding: 24px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      border: 1px solid #e0e0e0;
    }

    .results-header h2 {
      margin: 0 0 8px 0;
      color: var(--text-primary);
      font-weight: 500;
      font-family: var(--font-family-display);
    }

    .results-header p {
      margin: 0;
      color: var(--text-secondary);
      font-style: normal;
    }

    .loading-section {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 40vh;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    }

    .loading-content {
      text-align: center;
      padding: 40px;
    }

    .loading-content h3 {
      margin: 24px 0 8px 0;
      color: var(--text-primary);
      font-weight: 500;
    }

    .loading-content p {
      color: var(--text-secondary);
      margin: 0;
    }

    .empty-results {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 40vh;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    }

    .empty-content {
      text-align: center;
      padding: 40px;
    }

    .empty-icon {
      font-size: 80px;
      width: 80px;
      height: 80px;
      color: #9e9e9e;
      margin-bottom: 24px;
    }

    .empty-content h2 {
      color: var(--text-primary);
      margin: 0 0 16px 0;
      font-weight: 500;
    }

    .empty-content p {
      color: var(--text-secondary);
      margin: 0;
      line-height: 1.6;
    }

    .books-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 24px;
      margin-bottom: 32px;
      padding: 24px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      border: 1px solid #e0e0e0;
    }

    .book-poster {
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
      position: relative;
      border-radius: 12px;
      overflow: hidden;
      background: white;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .book-poster:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
      z-index: 10;
    }

    .book-cover {
      position: relative;
      width: 100%;
      aspect-ratio: 2/3;
      border-radius: 8px;
      overflow: hidden;
      background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      color: #6c757d;
    }

    .cover-placeholder mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 12px;
      color: #adb5bd;
    }

    .title-text {
      font-size: 12px;
      text-align: center;
      padding: 0 8px;
      line-height: 1.2;
      font-weight: 500;
      color: var(--text-secondary);
    }

    .cover-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(25, 118, 210, 0.9);
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
      color: white;
    }

    .book-info {
      padding: 16px;
    }

    .book-title {
      font-size: 14px;
      font-weight: 600;
      margin: 0 0 6px 0;
      color: var(--text-primary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      line-height: 1.4;
    }

    .book-author {
      font-size: 12px;
      color: var(--text-secondary);
      margin: 0 0 6px 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .book-year {
      font-size: 11px;
      color: #9e9e9e;
      margin: 0 0 12px 0;
    }

    .book-metadata {
      display: flex;
      gap: 6px;
      flex-wrap: wrap;
    }

    .metadata-chip {
      font-size: 10px;
      height: 20px;
      line-height: 20px;
      padding: 0 8px;
      border-radius: 10px;
      font-weight: 500;
    }

    .language-chip {
      background: rgba(25, 118, 210, 0.1);
      color: var(--primary-color);
      border: 1px solid rgba(25, 118, 210, 0.2);
    }

    .format-chip {
      background: rgba(158, 158, 158, 0.1);
      color: #616161;
      border: 1px solid rgba(158, 158, 158, 0.2);
    }

    .pagination-section {
      padding: 32px 24px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      border: 1px solid #e0e0e0;
    }

    .pagination-controls {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16px;
    }

    .nav-button {
      background: white;
      color: var(--primary-color);
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      min-width: 120px;
    }

    .nav-button:hover {
      background: rgba(25, 118, 210, 0.04);
      border-color: var(--primary-color);
    }

    .nav-button:disabled {
      background: #f5f5f5;
      color: #bdbdbd;
      border-color: #e0e0e0;
    }

    /* Unified Search Styles */
    .results-section {
      margin-bottom: 32px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      border: 1px solid #e0e0e0;
      overflow: hidden;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 1.5rem;
      font-weight: 500;
      margin: 0;
      color: var(--text-primary);
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      padding: 20px 24px;
      border-bottom: 1px solid #e0e0e0;
      font-family: var(--font-family-display);
    }

    .section-title mat-icon {
      color: var(--primary-color);
      font-size: 1.5rem;
      width: 1.5rem;
      height: 1.5rem;
    }

    .series-grid, .authors-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;
      padding: 24px;
    }

    .series-item, .author-item {
      background: white;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      padding: 20px;
      transition: all 0.2s ease;
    }

    .series-item:hover, .author-item:hover {
      background: rgba(25, 118, 210, 0.02);
      border-color: rgba(25, 118, 210, 0.3);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .series-name, .author-name {
      font-size: 1.1rem;
      font-weight: 600;
      margin: 0 0 8px 0;
      color: var(--text-primary);
      font-family: var(--font-family-display);
    }

    .series-description, .author-bio {
      font-size: 14px;
      color: var(--text-secondary);
      margin: 0 0 12px 0;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .series-count, .author-dates {
      font-size: 12px;
      color: #9e9e9e;
      margin: 0;
      font-weight: 500;
    }
    .back-button {
      background: rgba(25, 118, 210, 0.1);
      border-color: var(--primary-color);
      color: var(--primary-color);
      border-radius: 8px;
    }

    .back-button:hover {
      background: rgba(25, 118, 210, 0.15);
    }

    /* Mobile Responsive Styles */
    @media (max-width: 768px) {
      .search-header {
        padding: 32px 16px;
      }

      .search-content {
        padding: 20px 16px;
      }

      .search-title {
        font-size: 2.2rem;
        gap: 16px;
      }

      .title-icon {
        font-size: 2.5rem;
        width: 2.5rem;
        height: 2.5rem;
      }

      .quick-search-section {
        flex-direction: column;
        align-items: stretch;
        gap: 16px;
        padding: 20px;
      }

      .form-row {
        grid-template-columns: 1fr;
        gap: 16px;
      }

      .books-grid {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
        gap: 16px;
        padding: 20px;
      }

      .series-grid, .authors-grid {
        grid-template-columns: 1fr;
        gap: 16px;
        padding: 20px;
      }

      .pagination-controls {
        flex-direction: column;
        gap: 12px;
      }

      .advanced-actions {
        flex-direction: column;
        gap: 12px;
      }

      .nav-button {
        min-width: 100%;
      }

      .results-section {
        margin-bottom: 20px;
      }

      .section-title {
        font-size: 1.3rem;
        padding: 16px 20px;
      }
    }

    @media (max-width: 480px) {
      .search-header {
        padding: 24px 12px;
      }

      .search-content {
        padding: 16px 12px;
      }

      .search-title {
        font-size: 1.8rem;
        flex-direction: column;
        text-align: center;
        gap: 12px;
      }

      .quick-search-section {
        padding: 16px;
      }

      .books-grid {
        grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
        gap: 12px;
        padding: 16px;
      }

      .series-grid, .authors-grid {
        padding: 16px;
      }

      .section-title {
        font-size: 1.2rem;
        padding: 12px 16px;
      }
    }
  `]
})
export class SearchComponent implements OnDestroy {
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