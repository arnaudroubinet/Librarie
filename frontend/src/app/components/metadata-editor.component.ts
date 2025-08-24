import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MetadataService } from '../services/metadata.service';
import { BookService } from '../services/book.service';
import { BookMetadata, MetadataPreview, ProviderStatus, MetadataApplyRequest, AuthorMetadata } from '../models/metadata.model';
import { Book } from '../models/book.model';

@Component({
  selector: 'app-metadata-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatSnackBarModule,
    MatDividerModule,
    MatChipsModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatExpansionModule,
    MatCheckboxModule
  ],
  template: `
    <div class="metadata-editor">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <mat-icon>edit</mat-icon>
            Metadata Editor
          </mat-card-title>
          <mat-card-subtitle>
            @if (book()) {
              Editing metadata for: {{ book()!.title }}
            }
          </mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <mat-tab-group>
            
            <!-- External Search Tab -->
            <mat-tab label="External Search">
              <div class="tab-content">
                <div class="search-section">
                  <h3>Search External Providers</h3>
                  
                  <!-- ISBN Search -->
                  <mat-expansion-panel>
                    <mat-expansion-panel-header>
                      <mat-panel-title>Search by ISBN</mat-panel-title>
                    </mat-expansion-panel-header>
                    
                    <div class="search-form">
                      <mat-form-field appearance="outline" class="full-width">
                        <mat-label>ISBN (10 or 13 digits)</mat-label>
                        <input matInput 
                               [(ngModel)]="searchIsbn" 
                               placeholder="9780545010221"
                               (keyup.enter)="searchByIsbn()">
                        <mat-icon matSuffix>search</mat-icon>
                      </mat-form-field>
                      
                      <button 
                        mat-raised-button
                        color="primary" 
                        [disabled]="isSearching() || !searchIsbn.trim()"
                        (click)="searchByIsbn()">
                        @if (isSearching()) {
                          <mat-spinner diameter="20"></mat-spinner>
                        } @else {
                          <mat-icon>search</mat-icon>
                        }
                        Search by ISBN
                      </button>
                    </div>
                  </mat-expansion-panel>

                  <!-- Title/Author Search -->
                  <mat-expansion-panel>
                    <mat-expansion-panel-header>
                      <mat-panel-title>Search by Title & Author</mat-panel-title>
                    </mat-expansion-panel-header>
                    
                    <div class="search-form">
                      <mat-form-field appearance="outline" class="full-width">
                        <mat-label>Title</mat-label>
                        <input matInput 
                               [(ngModel)]="searchTitle" 
                               placeholder="Book title">
                      </mat-form-field>
                      
                      <mat-form-field appearance="outline" class="full-width">
                        <mat-label>Author (optional)</mat-label>
                        <input matInput 
                               [(ngModel)]="searchAuthor" 
                               placeholder="Author name"
                               (keyup.enter)="searchByTitle()">
                      </mat-form-field>
                      
                      <button 
                        mat-raised-button
                        color="primary" 
                        [disabled]="isSearching() || !searchTitle.trim()"
                        (click)="searchByTitle()">
                        @if (isSearching()) {
                          <mat-spinner diameter="20"></mat-spinner>
                        } @else {
                          <mat-icon>search</mat-icon>
                        }
                        Search by Title
                      </button>
                    </div>
                  </mat-expansion-panel>

                  <!-- Provider Status -->
                  <mat-expansion-panel>
                    <mat-expansion-panel-header>
                      <mat-panel-title>Provider Status</mat-panel-title>
                    </mat-expansion-panel-header>
                    
                    <div class="provider-status">
                      @if (providerStatus().length === 0) {
                        <button mat-raised-button color="primary" (click)="loadProviderStatus()">
                          Check Provider Status
                        </button>
                      } @else {
                        @for (provider of providerStatus(); track provider.providerId) {
                          <div class="provider-item" [class]="provider.isAvailable ? 'available' : 'unavailable'">
                            <mat-icon>{{ provider.isAvailable ? 'check_circle' : 'error' }}</mat-icon>
                            <span class="provider-name">{{ provider.name }}</span>
                            <span class="provider-details">
                              @if (provider.responseTime) {
                                {{ provider.responseTime }}ms
                              }
                              @if (provider.errorMessage) {
                                - {{ provider.errorMessage }}
                              }
                            </span>
                          </div>
                        }
                        <button mat-button (click)="loadProviderStatus()">Refresh</button>
                      }
                    </div>
                  </mat-expansion-panel>
                </div>

                <!-- Search Results -->
                @if (searchResults().length > 0) {
                  <mat-divider></mat-divider>
                  <div class="search-results">
                    <h3>Search Results ({{ searchResults().length }})</h3>
                    
                    @for (result of searchResults(); track $index) {
                      <mat-card class="result-card">
                        <mat-card-content>
                          <div class="result-header">
                            <div class="result-info">
                              @if (result.imageUrl || result.thumbnailUrl) {
                                <img 
                                  [src]="result.imageUrl || result.thumbnailUrl" 
                                  [alt]="result.title"
                                  class="result-thumbnail"
                                  (error)="$event.target.style.display='none'">
                              }
                              
                              <div class="result-details">
                                <h4>{{ result.title }}</h4>
                                @if (result.subtitle) {
                                  <h5>{{ result.subtitle }}</h5>
                                }
                                @if (result.authors && result.authors.length > 0) {
                                  <p class="authors">
                                    {{ getAuthorsText(result.authors) }}
                                  </p>
                                }
                                @if (result.publisher) {
                                  <p class="publisher">{{ result.publisher }}</p>
                                }
                                @if (result.publishedDate) {
                                  <p class="published">{{ result.publishedDate }}</p>
                                }
                                @if (result.provider) {
                                  <div class="provider-badge">{{ result.provider }}</div>
                                }
                                @if (result.confidence) {
                                  <div class="confidence-badge">
                                    Confidence: {{ (result.confidence * 100).toFixed(0) }}%
                                  </div>
                                }
                              </div>
                            </div>
                            
                            <div class="result-actions">
                              <button 
                                mat-button
                                color="primary" 
                                (click)="previewMetadata(result)">
                                Preview
                              </button>
                              <button 
                                mat-raised-button
                                color="accent" 
                                (click)="applyMetadata(result)">
                                Apply
                              </button>
                            </div>
                          </div>
                          
                          @if (result.description) {
                            <mat-expansion-panel class="description-panel">
                              <mat-expansion-panel-header>
                                <mat-panel-title>Description</mat-panel-title>
                              </mat-expansion-panel-header>
                              <p>{{ result.description }}</p>
                            </mat-expansion-panel>
                          }
                        </mat-card-content>
                      </mat-card>
                    }
                  </div>
                }
              </div>
            </mat-tab>

            <!-- Manual Edit Tab -->
            <mat-tab label="Manual Edit">
              <div class="tab-content">
                @if (book()) {
                  <div class="manual-edit-form">
                    <h3>Edit Metadata Manually</h3>
                    
                    <div class="form-row">
                      <mat-form-field appearance="outline" class="full-width">
                        <mat-label>Title</mat-label>
                        <input matInput [(ngModel)]="manualMetadata.title" placeholder="Book title">
                      </mat-form-field>
                    </div>
                    
                    <div class="form-row">
                      <mat-form-field appearance="outline" class="full-width">
                        <mat-label>Subtitle</mat-label>
                        <input matInput [(ngModel)]="manualMetadata.subtitle" placeholder="Book subtitle">
                      </mat-form-field>
                    </div>
                    
                    <div class="form-row">
                      <mat-form-field appearance="outline" class="full-width">
                        <mat-label>Description</mat-label>
                        <textarea 
                          matInput 
                          [(ngModel)]="manualMetadata.description" 
                          rows="4"
                          placeholder="Book description">
                        </textarea>
                      </mat-form-field>
                    </div>
                    
                    <div class="form-row">
                      <mat-form-field appearance="outline" class="half-width">
                        <mat-label>Publisher</mat-label>
                        <input matInput [(ngModel)]="manualMetadata.publisher" placeholder="Publisher name">
                      </mat-form-field>
                      
                      <mat-form-field appearance="outline" class="half-width">
                        <mat-label>Published Date</mat-label>
                        <input matInput [(ngModel)]="manualMetadata.publishedDate" placeholder="YYYY-MM-DD">
                      </mat-form-field>
                    </div>
                    
                    <div class="form-row">
                      <mat-form-field appearance="outline" class="half-width">
                        <mat-label>Language</mat-label>
                        <input matInput [(ngModel)]="manualMetadata.language" placeholder="en-US">
                      </mat-form-field>
                      
                      <mat-form-field appearance="outline" class="half-width">
                        <mat-label>Pages</mat-label>
                        <input matInput [(ngModel)]="manualMetadata.pages" type="number" placeholder="Number of pages">
                      </mat-form-field>
                    </div>
                    
                    <div class="form-row">
                      <mat-form-field appearance="outline" class="half-width">
                        <mat-label>ISBN-10</mat-label>
                        <input matInput [(ngModel)]="manualMetadata.isbn10" placeholder="10-digit ISBN">
                      </mat-form-field>
                      
                      <mat-form-field appearance="outline" class="half-width">
                        <mat-label>ISBN-13</mat-label>
                        <input matInput [(ngModel)]="manualMetadata.isbn13" placeholder="13-digit ISBN">
                      </mat-form-field>
                    </div>
                    
                    <div class="form-options">
                      <mat-checkbox [(ngModel)]="overwriteExisting">
                        Overwrite existing metadata
                      </mat-checkbox>
                    </div>
                    
                    <div class="form-actions">
                      <button 
                        mat-button
                        color="primary" 
                        (click)="previewManualMetadata()">
                        Preview Changes
                      </button>
                      <button 
                        mat-raised-button
                        color="accent" 
                        (click)="applyManualMetadata()">
                        Apply Changes
                      </button>
                      <button mat-button (click)="resetManualMetadata()">
                        Reset
                      </button>
                    </div>
                  </div>
                }
              </div>
            </mat-tab>

            <!-- Preview Tab -->
            @if (preview()) {
              <mat-tab label="Preview Changes">
                <div class="tab-content">
                  <div class="preview-section">
                    <h3>Preview Metadata Changes</h3>
                    
                    <div class="preview-summary">
                      <p>{{ preview()!.changes.length }} field(s) will be changed</p>
                      <mat-checkbox [(ngModel)]="overwriteExisting" (change)="refreshPreview()">
                        Overwrite existing metadata
                      </mat-checkbox>
                    </div>
                    
                    @for (change of preview()!.changes; track change.fieldName) {
                      <mat-card class="change-card">
                        <mat-card-content>
                          <div class="change-header">
                            <h4>{{ change.fieldName }}</h4>
                            <span class="change-type" [class]="'change-' + change.changeType.toLowerCase()">
                              {{ change.changeType }}
                            </span>
                          </div>
                          
                          <div class="change-content">
                            @if (change.currentValue) {
                              <div class="current-value">
                                <strong>Current:</strong> {{ change.currentValue }}
                              </div>
                            }
                            <div class="proposed-value">
                              <strong>Proposed:</strong> {{ change.proposedValue }}
                            </div>
                          </div>
                        </mat-card-content>
                      </mat-card>
                    }
                    
                    <div class="preview-actions">
                      <button 
                        mat-raised-button
                        color="accent" 
                        (click)="applyPreviewedMetadata()">
                        Apply These Changes
                      </button>
                      <button mat-button (click)="clearPreview()">
                        Cancel
                      </button>
                    </div>
                  </div>
                </div>
              </mat-tab>
            }
          </mat-tab-group>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styleUrls: ['./metadata-editor.component.css']
})
export class MetadataEditorComponent implements OnInit {
  bookId = signal<string>('');
  
  book = signal<Book | null>(null);
  searchResults = signal<BookMetadata[]>([]);
  providerStatus = signal<ProviderStatus[]>([]);
  preview = signal<MetadataPreview | null>(null);
  isSearching = signal(false);
  
  searchIsbn = '';
  searchTitle = '';
  searchAuthor = '';
  overwriteExisting = false;
  
  manualMetadata: BookMetadata = {};

  constructor(
    private route: ActivatedRoute,
    private metadataService: MetadataService,
    private bookService: BookService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.bookId.set(params['id']);
      this.loadBook();
    });
  }

  private loadBook() {
    const bookId = this.bookId();
    if (!bookId) return;
    
    this.bookService.getBookDetails(bookId).subscribe({
      next: (book) => {
        this.book.set(book);
        this.initializeManualMetadata(book);
      },
      error: (error) => {
        console.error('Failed to load book:', error);
        this.snackBar.open('Failed to load book details', 'Close', { duration: 3000 });
      }
    });
  }

  private initializeManualMetadata(book: Book) {
    this.manualMetadata = {
      title: book.title,
      subtitle: book.subtitle || '',
      description: book.description,
      publisher: book.publisher,
      publishedDate: book.publicationDate,
      language: book.language,
      pages: book.pageCount || book.pages,
      isbn10: book.isbn10 || book.isbn,
      isbn13: book.isbn13 || book.isbn
    };
  }

  searchByIsbn() {
    if (!this.searchIsbn.trim()) return;
    
    this.isSearching.set(true);
    this.metadataService.searchByIsbn(this.searchIsbn.trim()).subscribe({
      next: (results) => {
        this.searchResults.set(results);
        this.isSearching.set(false);
        if (results.length === 0) {
          this.snackBar.open('No results found for this ISBN', 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        console.error('Search failed:', error);
        this.isSearching.set(false);
        this.snackBar.open('Search failed: ' + (error.error?.message || error.message), 'Close', { duration: 3000 });
      }
    });
  }

  searchByTitle() {
    if (!this.searchTitle.trim()) return;
    
    this.isSearching.set(true);
    this.metadataService.searchByTitle(this.searchTitle.trim(), this.searchAuthor.trim() || undefined).subscribe({
      next: (results) => {
        this.searchResults.set(results);
        this.isSearching.set(false);
        if (results.length === 0) {
          this.snackBar.open('No results found for this title', 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        console.error('Search failed:', error);
        this.isSearching.set(false);
        this.snackBar.open('Search failed: ' + (error.error?.message || error.message), 'Close', { duration: 3000 });
      }
    });
  }

  loadProviderStatus() {
    this.metadataService.getProviderStatus().subscribe({
      next: (status) => {
        this.providerStatus.set(status);
      },
      error: (error) => {
        console.error('Failed to load provider status:', error);
        this.snackBar.open('Failed to load provider status', 'Close', { duration: 3000 });
      }
    });
  }

  previewMetadata(metadata: BookMetadata) {
    const request: MetadataApplyRequest = {
      metadata,
      overwriteExisting: this.overwriteExisting
    };
    
    this.metadataService.previewMetadataChanges(this.bookId(), request).subscribe({
      next: (preview) => {
        this.preview.set(preview);
      },
      error: (error) => {
        console.error('Preview failed:', error);
        this.snackBar.open('Preview failed: ' + (error.error?.message || error.message), 'Close', { duration: 3000 });
      }
    });
  }

  previewManualMetadata() {
    const request: MetadataApplyRequest = {
      metadata: this.manualMetadata,
      overwriteExisting: this.overwriteExisting
    };
    
    this.metadataService.previewMetadataChanges(this.bookId(), request).subscribe({
      next: (preview) => {
        this.preview.set(preview);
      },
      error: (error) => {
        console.error('Preview failed:', error);
        this.snackBar.open('Preview failed: ' + (error.error?.message || error.message), 'Close', { duration: 3000 });
      }
    });
  }

  applyMetadata(metadata: BookMetadata) {
    const request: MetadataApplyRequest = {
      metadata,
      overwriteExisting: this.overwriteExisting
    };
    
    this.metadataService.applyMetadata(this.bookId(), request).subscribe({
      next: () => {
        this.snackBar.open('Metadata applied successfully', 'Close', { duration: 3000 });
        this.loadBook(); // Refresh book data
        this.clearPreview();
      },
      error: (error) => {
        console.error('Apply failed:', error);
        this.snackBar.open('Apply failed: ' + (error.error?.message || error.message), 'Close', { duration: 3000 });
      }
    });
  }

  applyManualMetadata() {
    const request: MetadataApplyRequest = {
      metadata: this.manualMetadata,
      overwriteExisting: this.overwriteExisting
    };
    
    this.metadataService.applyMetadata(this.bookId(), request).subscribe({
      next: () => {
        this.snackBar.open('Metadata applied successfully', 'Close', { duration: 3000 });
        this.loadBook(); // Refresh book data
        this.clearPreview();
      },
      error: (error) => {
        console.error('Apply failed:', error);
        this.snackBar.open('Apply failed: ' + (error.error?.message || error.message), 'Close', { duration: 3000 });
      }
    });
  }

  applyPreviewedMetadata() {
    const previewData = this.preview();
    if (!previewData) return;
    
    const request: MetadataApplyRequest = {
      metadata: previewData.proposedMetadata,
      overwriteExisting: this.overwriteExisting
    };
    
    this.metadataService.applyMetadata(this.bookId(), request).subscribe({
      next: () => {
        this.snackBar.open('Metadata applied successfully', 'Close', { duration: 3000 });
        this.loadBook(); // Refresh book data
        this.clearPreview();
      },
      error: (error) => {
        console.error('Apply failed:', error);
        this.snackBar.open('Apply failed: ' + (error.error?.message || error.message), 'Close', { duration: 3000 });
      }
    });
  }

  refreshPreview() {
    const previewData = this.preview();
    if (previewData) {
      // Re-generate preview with new overwrite setting
      this.previewMetadata(previewData.proposedMetadata);
    }
  }

  clearPreview() {
    this.preview.set(null);
  }

  resetManualMetadata() {
    const book = this.book();
    if (book) {
      this.initializeManualMetadata(book);
    }
  }

  getAuthorsText(authors: AuthorMetadata[]): string {
    return authors.map(a => a.name).join(', ');
  }
}