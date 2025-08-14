import { Component, OnInit, signal, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
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
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSnackBarModule,
    MatDividerModule
  ],
  template: `
  <div class="book-detail-container plex-library">
      @if (loading()) {
        <div class="loading-container">
          <mat-spinner diameter="50"></mat-spinner>
          <p>Loading book details...</p>
        </div>
      } @else if (book()) {
        <div class="book-detail">
          <div class="back-button">
            <button mat-button (click)="goBack()">
              <iconify-icon icon="lets-icons:back"></iconify-icon>
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
                    <mat-icon class="placeholder-icon">menu_book</mat-icon>
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
                    <iconify-icon icon="icon-park-outline:bookshelf"></iconify-icon>
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
                    <iconify-icon icon="ph:users-three-thin"></iconify-icon>
                    <span class="authors-list">
                      @for (a of getAuthorsDetailed(); track a.id; let last = $last) {
                        <a class="author-link" [routerLink]="['/authors', a.id]">{{ a.name }}</a>
                        @if (!last) {<span>, </span>}
                      }
                    </span>
                  </div>
                }
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
                    <mat-icon>people</mat-icon>
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
          <mat-icon class="error-icon">error</mat-icon>
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
  styles: [`
    .book-detail-container {
      min-height: 100vh;
      /* Match all books page: keep page-level gradient from parent, not here */
      background: transparent;
      color: #ffffff;
      padding: 0;
    }

    .loading-container, .error-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 60vh;
      padding: 64px 32px;
      text-align: center;
    }

    .error-icon {
      font-size: 4rem !important;
      width: 4rem !important;
      height: 4rem !important;
      color: #ff4444;
      margin-bottom: 16px;
    }

    .back-button {
      /* Match all books page header spacing and remove separator */
      padding: 24px 20px;
      position: sticky;
      top: 0;
      background: transparent;
      backdrop-filter: none;
      z-index: 10;
      border-bottom: none;
    }

    .back-button button {
      color: #ffffff !important;
      font-weight: 500 !important;
    }

    /* Iconify icons inside buttons spacing/size */
    .back-button iconify-icon,
    .actions-section iconify-icon,
    .error-state iconify-icon {
      margin-right: 8px;
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .book-layout {
      display: flex;
      gap: 48px;
      padding: 48px 32px;
      max-width: 1200px;
      margin: 0 auto;
    }

    /* Book Cover */
    .book-cover-container {
      flex: 0 0 240px;
      position: relative;
    }

    .book-cover {
      width: 240px;
      height: 360px;
      object-fit: cover;
      border-radius: 12px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
    }

    .book-cover-placeholder {
      width: 240px;
      height: 360px;
      border-radius: 12px;
      background: linear-gradient(135deg, #2a2a2a 0%, #1a1a1a 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
      text-align: center;
      padding: 16px;
      color: #cfcfcf;
    }

    .placeholder-icon {
      font-size: 3rem !important;
      width: 3rem !important;
      height: 3rem !important;
      margin-bottom: 8px;
    }

    .placeholder-text {
      font-size: 1rem;
      line-height: 1.2;
      word-break: break-word;
    }

    .format-badge {
      position: absolute;
      top: 10px;
      right: 10px;
      background: rgba(0,0,0,0.8);
      color: white;
      padding: 4px 8px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      backdrop-filter: blur(6px);
      border: 1px solid rgba(255,255,255,0.15);
    }

    /* Book Information */
    .book-info-container { flex: 1; min-width: 0; }

    .title-section { margin-bottom: 24px; }

    .book-title {
      font-size: 2.2rem;
      font-weight: 700;
      margin: 0 0 8px 0;
      line-height: 1.2;
      color: #ffffff;
    }

    .book-series, .book-authors {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #b3e5fc;
      margin-top: 6px;
    }

  .book-series .series-index { color: #81d4fa; font-style: italic; margin-left: 4px; }
  .book-series .series-link { color: #b3e5fc; text-decoration: none; }
  .book-series .series-link:hover { text-decoration: underline; }
  .authors-list .author-link { color: #b3e5fc; text-decoration: none; }
  .authors-list .author-link:hover { text-decoration: underline; }

    .details-list {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 24px;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

    .detail-item {
      display: grid;
      grid-template-columns: 1fr 2fr;
      gap: 16px;
      align-items: start;
      padding: 12px 0;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }

    .detail-item:last-child { border-bottom: none; }
    .detail-item.synopsis { grid-template-columns: 1fr; }

    .detail-label { font-weight: 600; color: #4fc3f7; white-space: nowrap; }
    .detail-value { word-break: break-word; }

    mat-chip-set {
      --mdc-chip-container-color: rgba(79, 195, 247, 0.2);
      --mdc-chip-label-text-color: #ffffff;
    }

    .contributors-section { margin-top: 24px; }
    .section-title { font-size: 1.25rem; margin: 0 0 12px 0; display: flex; align-items: center; gap: 8px; color: #4fc3f7; }
    .contributors-list { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; padding: 16px; }
    .contrib-item { display: grid; grid-template-columns: 1fr 2fr; gap: 12px; padding: 8px 0; border-bottom: 1px solid rgba(255,255,255,0.08); }
    .contrib-item:last-child { border-bottom: none; }
    .contrib-role { color: #81d4fa; font-weight: 600; }
    .contrib-names { color: #ffffff; opacity: 0.95; }

    .actions-section { display: flex; gap: 12px; margin-top: 16px; }

    /* Responsive */
    @media (max-width: 768px) {
      .book-layout { flex-direction: column; gap: 32px; padding: 32px 16px; }
      .book-cover-container { align-self: center; }
      .book-cover, .book-cover-placeholder { width: 200px; height: 300px; }
      .book-title { font-size: 1.8rem; text-align: center; }
      .book-subtitle, .book-series, .book-authors { justify-content: center; }
      .detail-item { grid-template-columns: 1fr; gap: 6px; }
    }

    @media (max-width: 1024px) and (min-width: 769px) {
      .book-layout { gap: 32px; }
      .book-cover, .book-cover-placeholder { width: 200px; height: 300px; }
    }
  `]
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
    this.bookService.getBookById(id).subscribe({
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

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

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

  getShortTitle(title: string): string {
    return title && title.length > 30 ? title.substring(0, 30) + '...' : title;
  }

  // No author click handler needed; IDs are provided
}