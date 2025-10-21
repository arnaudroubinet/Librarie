import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MetadataService, MetadataSearchResult } from '../services/metadata.service';

/**
 * Component for searching and selecting book metadata
 * Used in book detail view to fetch and apply metadata
 */
@Component({
  selector: 'app-metadata-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="metadata-search-container">
      <div class="search-controls">
        <div class="search-tabs">
          <button 
            class="tab-button" 
            [class.active]="searchMode === 'isbn'"
            (click)="searchMode = 'isbn'">
            Search by ISBN
          </button>
          <button 
            class="tab-button" 
            [class.active]="searchMode === 'title'"
            (click)="searchMode = 'title'">
            Search by Title
          </button>
        </div>

        <div class="search-form" *ngIf="searchMode === 'isbn'">
          <input 
            type="text" 
            [(ngModel)]="isbnQuery" 
            placeholder="Enter ISBN-10 or ISBN-13"
            class="search-input"
            (keyup.enter)="searchMetadata()">
          <button 
            class="search-button" 
            (click)="searchMetadata()"
            [disabled]="loading">
            {{ loading ? 'Searching...' : 'Search' }}
          </button>
        </div>

        <div class="search-form" *ngIf="searchMode === 'title'">
          <input 
            type="text" 
            [(ngModel)]="titleQuery" 
            placeholder="Enter book title"
            class="search-input"
            (keyup.enter)="searchMetadata()">
          <input 
            type="text" 
            [(ngModel)]="authorQuery" 
            placeholder="Enter author (optional)"
            class="search-input"
            (keyup.enter)="searchMetadata()">
          <button 
            class="search-button" 
            (click)="searchMetadata()"
            [disabled]="loading">
            {{ loading ? 'Searching...' : 'Search' }}
          </button>
        </div>
      </div>

      <div class="search-results" *ngIf="results.length > 0">
        <h3>Search Results ({{ results.length }})</h3>
        <div class="result-list">
          <div 
            *ngFor="let result of results" 
            class="result-card"
            [class.selected]="selectedResult === result"
            (click)="selectResult(result)">
            <div class="result-cover">
              <img 
                *ngIf="result.coverImageUrl" 
                [src]="result.coverImageUrl" 
                [alt]="result.title"
                class="cover-image">
              <div *ngIf="!result.coverImageUrl" class="no-cover">No Cover</div>
            </div>
            <div class="result-info">
              <h4 class="result-title">{{ result.title }}</h4>
              <p class="result-subtitle" *ngIf="result.subtitle">{{ result.subtitle }}</p>
              <p class="result-authors" *ngIf="result.authors && result.authors.length > 0">
                by {{ result.authors.join(', ') }}
              </p>
              <div class="result-details">
                <span class="detail-item" *ngIf="result.publisher">{{ result.publisher }}</span>
                <span class="detail-item" *ngIf="result.publishedDate">{{ result.publishedDate | date: 'yyyy' }}</span>
                <span class="detail-item" *ngIf="result.pageCount">{{ result.pageCount }} pages</span>
                <span class="detail-item" *ngIf="result.isbn13">ISBN: {{ result.isbn13 }}</span>
              </div>
              <div class="result-metadata">
                <span class="metadata-source">{{ result.source }}</span>
                <span class="confidence-score">
                  Confidence: {{ (result.confidenceScore * 100).toFixed(0) }}%
                </span>
              </div>
              <p class="result-description" *ngIf="result.description">
                {{ result.description | slice:0:200 }}{{ result.description.length > 200 ? '...' : '' }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div class="no-results" *ngIf="searched && results.length === 0 && !loading">
        <p>No metadata found. Try a different search term.</p>
      </div>

      <div class="error-message" *ngIf="error">
        <p>Error: {{ error }}</p>
      </div>

      <div class="action-buttons" *ngIf="selectedResult">
        <button class="apply-button" (click)="applyMetadata()">
          Apply Selected Metadata
        </button>
        <button class="cancel-button" (click)="cancel()">
          Cancel
        </button>
      </div>
    </div>
  `,
  styles: [`
    .metadata-search-container {
      padding: 20px;
      background: var(--surface-color, #1a1a1a);
      border-radius: 8px;
      margin: 20px 0;
    }

    .search-tabs {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
    }

    .tab-button {
      padding: 10px 20px;
      border: none;
      background: var(--surface-variant, #2a2a2a);
      color: var(--text-color, #ffffff);
      cursor: pointer;
      border-radius: 4px;
      transition: all 0.3s ease;
    }

    .tab-button.active {
      background: var(--primary-color, #4a9eff);
      color: white;
    }

    .search-form {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
    }

    .search-input {
      flex: 1;
      padding: 12px;
      border: 1px solid var(--border-color, #333);
      background: var(--surface-variant, #2a2a2a);
      color: var(--text-color, #ffffff);
      border-radius: 4px;
      font-size: 14px;
    }

    .search-button {
      padding: 12px 24px;
      background: var(--primary-color, #4a9eff);
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.3s ease;
    }

    .search-button:hover:not(:disabled) {
      background: var(--primary-hover, #3a8eef);
    }

    .search-button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .result-list {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }

    .result-card {
      display: flex;
      gap: 20px;
      padding: 20px;
      background: var(--surface-variant, #2a2a2a);
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 2px solid transparent;
    }

    .result-card:hover {
      border-color: var(--primary-color, #4a9eff);
      transform: translateY(-2px);
    }

    .result-card.selected {
      border-color: var(--primary-color, #4a9eff);
      background: var(--surface-selected, #3a3a3a);
    }

    .result-cover {
      flex-shrink: 0;
      width: 120px;
    }

    .cover-image {
      width: 100%;
      height: auto;
      border-radius: 4px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    }

    .no-cover {
      width: 100%;
      height: 160px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--surface-color, #1a1a1a);
      border-radius: 4px;
      color: var(--text-secondary, #888);
    }

    .result-info {
      flex: 1;
    }

    .result-title {
      margin: 0 0 5px 0;
      font-size: 18px;
      font-weight: 600;
      color: var(--text-color, #ffffff);
    }

    .result-subtitle {
      margin: 0 0 10px 0;
      font-size: 14px;
      color: var(--text-secondary, #aaa);
    }

    .result-authors {
      margin: 0 0 10px 0;
      font-size: 14px;
      color: var(--text-secondary, #aaa);
    }

    .result-details {
      display: flex;
      flex-wrap: wrap;
      gap: 15px;
      margin-bottom: 10px;
    }

    .detail-item {
      font-size: 13px;
      color: var(--text-secondary, #999);
    }

    .result-metadata {
      display: flex;
      gap: 15px;
      margin-bottom: 10px;
    }

    .metadata-source {
      padding: 4px 8px;
      background: var(--surface-color, #1a1a1a);
      border-radius: 4px;
      font-size: 12px;
      color: var(--primary-color, #4a9eff);
    }

    .confidence-score {
      padding: 4px 8px;
      background: var(--success-color, #2a7a2a);
      border-radius: 4px;
      font-size: 12px;
      color: white;
    }

    .result-description {
      margin: 10px 0 0 0;
      font-size: 13px;
      color: var(--text-secondary, #aaa);
      line-height: 1.5;
    }

    .no-results, .error-message {
      padding: 20px;
      text-align: center;
      color: var(--text-secondary, #aaa);
    }

    .error-message {
      color: var(--error-color, #ff4444);
    }

    .action-buttons {
      display: flex;
      gap: 10px;
      margin-top: 20px;
      justify-content: flex-end;
    }

    .apply-button {
      padding: 12px 24px;
      background: var(--success-color, #2a7a2a);
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.3s ease;
    }

    .apply-button:hover {
      background: var(--success-hover, #1a6a1a);
    }

    .cancel-button {
      padding: 12px 24px;
      background: var(--surface-variant, #2a2a2a);
      color: var(--text-color, #ffffff);
      border: 1px solid var(--border-color, #333);
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.3s ease;
    }

    .cancel-button:hover {
      background: var(--surface-color, #1a1a1a);
    }
  `]
})
export class MetadataSearchComponent {
  @Input() bookIsbn?: string;
  @Input() bookTitle?: string;
  @Output() metadataSelected = new EventEmitter<MetadataSearchResult>();
  @Output() cancelled = new EventEmitter<void>();

  searchMode: 'isbn' | 'title' = 'isbn';
  isbnQuery = '';
  titleQuery = '';
  authorQuery = '';
  
  results: MetadataSearchResult[] = [];
  selectedResult: MetadataSearchResult | null = null;
  loading = false;
  searched = false;
  error: string | null = null;

  constructor(private metadataService: MetadataService) {}

  ngOnInit() {
    // Pre-fill search fields if book data is provided
    if (this.bookIsbn) {
      this.isbnQuery = this.bookIsbn;
      this.searchMode = 'isbn';
    } else if (this.bookTitle) {
      this.titleQuery = this.bookTitle;
      this.searchMode = 'title';
    }
  }

  searchMetadata() {
    this.loading = true;
    this.searched = false;
    this.error = null;
    this.results = [];
    this.selectedResult = null;

    let searchObservable;

    if (this.searchMode === 'isbn' && this.isbnQuery.trim()) {
      searchObservable = this.metadataService.searchByIsbn(this.isbnQuery.trim());
    } else if (this.searchMode === 'title' && this.titleQuery.trim()) {
      searchObservable = this.metadataService.searchByTitleAndAuthor(
        this.titleQuery.trim(),
        this.authorQuery.trim() || undefined
      );
    } else {
      this.loading = false;
      this.error = 'Please enter a search term';
      return;
    }

    searchObservable.subscribe({
      next: (results) => {
        this.results = results;
        this.searched = true;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.error || 'Failed to search metadata';
        this.loading = false;
        this.searched = true;
      }
    });
  }

  selectResult(result: MetadataSearchResult) {
    this.selectedResult = result;
  }

  applyMetadata() {
    if (this.selectedResult) {
      this.metadataSelected.emit(this.selectedResult);
    }
  }

  cancel() {
    this.cancelled.emit();
  }
}
