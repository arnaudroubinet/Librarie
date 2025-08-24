import { Component, OnInit, signal, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MATERIAL_MODULES } from '../shared/materials';
import { getShortTitle as utilGetShortTitle, formatDate as utilFormatDate } from '../utils/author-utils';
import { BookService } from '../services/book.service';
import { Book } from '../models/book.model';
// SeriesService and AuthorService no longer needed; series/author IDs come from backend
import { environment } from '../../environments/environment';
// Removed unused rxjs helpers

@Component({
  selector: 'app-book-detail',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
  CommonModule,
  RouterModule,
  ...MATERIAL_MODULES
  ],
  template: `
  <div class="book-detail-container motspassants-library">
      @if (loading()) {
        <div class="loading-container">
          <mat-spinner diameter="50"></mat-spinner>
          <p>Loading book details...</p>
        </div>
      } @else if (book()) {
        <div class="book-detail">
          <div class="back-button">
            <button mat-button (click)="goBack()">
              <iconify-icon icon="material-symbols:arrow-back-rounded"></iconify-icon>
              Back
            </button>
          </div>

          <!-- Unified /book detail layout -->
          <div class="book-layout">
            <!-- Book Cover -->
            <div class="book-cover-container">
              @if (book()!.hasCover) {
                <img [src]="apiUrl + '/v1/books/' + book()!.id + '/cover'"
                     [alt]="book()!.title + ' cover'"
                     class="book-cover">
              } @else {
        <div class="book-cover-placeholder">
                  <div class="placeholder-content">
          <iconify-icon class="placeholder-icon" icon="material-symbols:menu-book-rounded"></iconify-icon>
                    <div class="placeholder-text">{{ getShortTitle(book()!.title) }}</div>
                  </div>
                </div>
              }

              @if (book()!.formats?.length) {
                <div class="format-badge">{{ book()!.formats!.join(' · ') }}</div>
              }
            </div>

            <!-- Book Information -->
            <div class="book-info-container">
              <!-- Title Section -->
              <div class="title-section">
                <h1 class="book-title">{{ book()!.title }}</h1>
        @if (book()!.series) {
      <div class="book-series">
    <iconify-icon icon="material-symbols:collections-bookmark-rounded"></iconify-icon>
                    @if (book()!.seriesId) {
                      <a class="series-link" [routerLink]="['/series', book()!.seriesId]">{{ book()!.series }}</a>
                    } @else {
                      <span>{{ book()!.series }}</span>
                    }
                    @if (book()!.seriesIndex) {
                      <span class="series-index">#{{ book()!.seriesIndex }}</span>
                    }
                  </div>
                }
                @if (getAuthorsDetailed().length > 0) {
      <div class="book-authors">
    <iconify-icon icon="material-symbols:person-rounded"></iconify-icon>
                    <span class="authors-list">
                      @for (a of getAuthorsDetailed(); track a.id; let last = $last) {
                        <a class="author-link" [routerLink]="['/authors', a.id]">{{ a.name }}</a>
                        @if (!last) {<span>, </span>}
                      }
                    </span>
                  </div>
                }
              </div>

              <!-- Action Buttons -->
              <div class="action-buttons">
                @if (isEpubBook()) {
                  <button mat-raised-button color="primary" (click)="readBook()" class="read-button">
                    <iconify-icon icon="material-symbols:menu-book-rounded"></iconify-icon>
                    Read Book
                  </button>
                }
                <button mat-stroked-button [routerLink]="['/books', book()!.id, 'metadata']" class="metadata-button">
                  <iconify-icon icon="material-symbols:edit-rounded"></iconify-icon>
                  Edit Metadata
                </button>
                <button mat-stroked-button (click)="downloadBook()" class="download-button">
                  <iconify-icon icon="material-symbols:download-rounded"></iconify-icon>
                  Download
                </button>
              </div>

              <!-- Details List -->
              <div class="details-list">
                @if (book()!.description) {
                  <div class="detail-item synopsis">
                    <span class="detail-label">Synopsis</span>
                    <span class="detail-value">{{ book()!.description }}</span>
                  </div>
                }

                @if (book()!.publisher) {
                  <div class="detail-item">
                    <span class="detail-label">Publisher</span>
                    <span class="detail-value">{{ book()!.publisher }}</span>
                  </div>
                }

                @if (book()!.publicationDate) {
                  <div class="detail-item">
                    <span class="detail-label">Published</span>
                    <span class="detail-value">{{ formatDate(book()!.publicationDate!) }}</span>
                  </div>
                }

                @if (book()!.isbn) {
                  <div class="detail-item">
                    <span class="detail-label">ISBN</span>
                    <span class="detail-value">{{ book()!.isbn }}</span>
                  </div>
                }

                @if (book()!.isbn) {
                  <div class="detail-item">
                    <span class="detail-label">ISBN-13</span>
                    <span class="detail-value">{{ formatISBN13(book()!.isbn!) }}</span>
                  </div>
                }

                @if (book()!.language) {
                  <div class="detail-item">
                    <span class="detail-label">Language</span>
                    <span class="detail-value">{{ book()!.language }}</span>
                  </div>
                }

                @if (getBinding()) {
                  <div class="detail-item">
                    <span class="detail-label">Binding</span>
                    <span class="detail-value">{{ getBinding() }}</span>
                  </div>
                }

                @if (getPages()) {
                  <div class="detail-item">
                    <span class="detail-label">Pages</span>
                    <span class="detail-value">{{ getPages() }}</span>
                  </div>
                }

                @if (book()!.publicationYear) {
                  <div class="detail-item">
                    <span class="detail-label">Publication Year</span>
                    <span class="detail-value">{{ book()!.publicationYear }}</span>
                  </div>
                }

                @if (book()!.fileSize) {
                  <div class="detail-item">
                    <span class="detail-label">File Size</span>
                    <span class="detail-value">{{ formatFileSize(book()!.fileSize!) }}</span>
                  </div>
                }

                @if (book()!.createdAt) {
                  <div class="detail-item">
                    <span class="detail-label">Added</span>
                    <span class="detail-value">{{ formatDate(book()!.createdAt!) }}</span>
                  </div>
                }

                @if (book()!.updatedAt) {
                  <div class="detail-item">
                    <span class="detail-label">Updated</span>
                    <span class="detail-value">{{ formatDate(book()!.updatedAt!) }}</span>
                  </div>
                }

                @if (book()!.formats?.length) {
                  <div class="detail-item">
                    <span class="detail-label">Formats</span>
                    <span class="detail-value">
                      <mat-chip-set>
                        @for (f of book()!.formats!; track f) { <mat-chip>{{ f }}</mat-chip> }
                      </mat-chip-set>
                    </span>
                  </div>
                }
              </div>

              @if (getOtherContributors().length > 0) {
                <div class="contributors-section">
                  <h2 class="section-title">
                    <iconify-icon icon="material-symbols:groups-rounded"></iconify-icon>
                    Other contributors
                  </h2>
                  <div class="contributors-list">
                    @for (c of getOtherContributors(); track c.role) {
                      <div class="contrib-item">
                        <span class="contrib-role">{{ c.role }}</span>
                        <span class="contrib-names">{{ c.names.join(', ') }}</span>
                      </div>
                    }
                  </div>
                </div>
              }

              
            </div>
          </div>
        </div>
      } @else {
        <div class="error-state">
          <iconify-icon class="error-icon" icon="material-symbols:error-outline-rounded"></iconify-icon>
          <h2>Book not found</h2>
          <p>The requested book could not be found.</p>
          <button mat-raised-button color="primary" (click)="goBack()">
            <iconify-icon icon="lets-icons:back"></iconify-icon>
            Back
          </button>
        </div>
      }
    </div>
  `,
  styleUrls: ['./book-detail.component.css']
})
export class BookDetailComponent implements OnInit {
  readonly apiUrl = environment.apiUrl;
  book = signal<Book | null>(null);
  loading = signal(true);
  // No local series/author ID caches needed

  constructor(
    private route: ActivatedRoute,
    private router: Router,
  private bookService: BookService,
    private location: Location,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadBookDetails(id);
    } else {
      this.loading.set(false);
    }
  }

  loadBookDetails(id: string) {
    this.loading.set(true);
  this.bookService.getBookDetails(id).subscribe({
      next: (book) => {
        this.book.set(book);
  // Using contributorsDetailed and seriesId from backend; no extra lookups needed
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading book details:', error);
        this.snackBar.open('Failed to load book details.', 'Close', {
          duration: 3000
        });
        this.loading.set(false);
      }
    });
  }

  goBack() {
    if (window.history.length > 1) {
      this.location.back();
    } else {
      this.router.navigate(['/books']);
    }
  }

  formatDate = (d: string) => utilFormatDate(d);

  formatDateTime(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  formatISBN13(isbn: string): string {
    // Simple ISBN-13 formatter - in real app this would be more sophisticated
    if (isbn.length === 13) return isbn;
    if (isbn.length === 10) {
      // Simple conversion for display purposes
      return '978' + isbn.substring(0, 9);
    }
    return isbn;
  }

  // Deprecated; no longer used as we rely solely on contributorsDetailed
  getAuthors(): string[] { return []; }

  getAuthorsDetailed(): Array<{ id: string; name: string }> {
    const contribs = this.book()?.contributorsDetailed?.['author'];
    return Array.isArray(contribs) ? contribs : [];
  }

  getOtherContributors(): Array<{ role: string, names: string[] }> {
    const detailed = this.book()?.contributorsDetailed;
    if (!detailed) return [];
    const result: Array<{ role: string, names: string[] }> = [];
    for (const [role, list] of Object.entries(detailed)) {
      if (role !== 'author' && Array.isArray(list) && list.length > 0) {
        const displayRole = role.charAt(0).toUpperCase() + role.slice(1);
        result.push({ role: displayRole, names: list.map(x => x.name) });
      }
    }
    return result;
  }

  getEdition(): string | null {
    return this.book()?.metadata?.['edition'] || null;
  }

  getBinding(): string | null {
    return this.book()?.metadata?.['binding'] || null;
  }

  getPages(): string | null {
    const pages = this.book()?.metadata?.['pages'];
    return pages ? pages.toString() : null;
  }

  getDimensions(): string | null {
    return this.book()?.metadata?.['dimensions'] || null;
  }

  getWeight(): string | null {
    return this.book()?.metadata?.['weight'] || null;
  }

  hasMetadata(): boolean {
    return !!this.book()?.metadata && Object.keys(this.book()!.metadata!).length > 0;
  }

  getMetadataEntries(): Array<{ key: string, value: any }> {
    if (!this.book()?.metadata) return [];
    return Object.entries(this.book()!.metadata!).map(([key, value]) => ({
      key,
      value: typeof value === 'object' ? JSON.stringify(value) : String(value)
    }));
  }

  getSubjects(): string[] {
    const subjects = this.book()?.metadata?.['subjects'];
    if (Array.isArray(subjects)) {
      return subjects;
    }
    if (typeof subjects === 'string') {
      return [subjects];
    }
    return [];
  }

  getShortTitle = utilGetShortTitle;

  // Check if the book is an EPUB file that can be read
  isEpubBook(): boolean {
    const book = this.book();
    return book?.formats?.some(format => format.toLowerCase() === 'epub') || 
           book?.path?.toLowerCase().endsWith('.epub') || false;
  }

  // Navigate to the ebook reader
  readBook(): void {
    const book = this.book();
    if (book && this.isEpubBook()) {
      this.router.navigate(['/books', book.id, 'read']);
    } else {
      this.snackBar.open('This book format is not supported for reading online.', 'Close', {
        duration: 3000
      });
    }
  }

  // Download the book file
  downloadBook(): void {
    const book = this.book();
    if (book) {
      const downloadUrl = `${this.apiUrl}/v1/books/${book.id}/file`;
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = `${book.title}.epub`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }

  // No author click handler needed; IDs are provided
}