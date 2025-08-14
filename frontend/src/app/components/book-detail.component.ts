import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { BookService } from '../services/book.service';
import { Book } from '../models/book.model';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSnackBarModule,
    MatDividerModule
  ],
  template: `
    <div class="book-detail-container">
      @if (loading()) {
        <div class="loading-container">
          <mat-spinner diameter="50"></mat-spinner>
          <p>Loading book details...</p>
        </div>
      } @else if (book()) {
        <!-- Mobile Layout -->
        <div class="mobile-book-detail">
          <!-- Status Bar -->
          <div class="status-bar">
            <div class="time">9:41</div>
            <div class="right-icons">
              <div class="wifi-icon"></div>
              <div class="signal-icon"></div>
              <div class="battery-icon">
                <div class="battery-base"></div>
                <div class="battery-charge"></div>
              </div>
            </div>
            <div class="camera-cutout"></div>
          </div>

          <!-- App Bar -->
          <div class="app-bar">
            <button class="leading-icon" (click)="goBack()">
              <mat-icon>arrow_back</mat-icon>
            </button>
            <div class="text-content">
              <h1 class="headline">{{ book()!.title }}</h1>
            </div>
            <div class="trailing-elements">
              <button class="trailing-action">
                <mat-icon>share</mat-icon>
              </button>
              <button class="trailing-action">
                <mat-icon>more_vert</mat-icon>
              </button>
            </div>
          </div>

          <!-- Header -->
          <div class="header">
            <div class="book-image">
              @if (book()!.hasCover) {
                <img src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTM2IiBoZWlnaHQ9IjIwNyIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iIzM0NDk1ZSIvPgo8dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IlJvYm90byIgZm9udC1zaXplPSIxMiIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIwLjNlbSI+Qm9vayBDb3ZlcjwvdGV4dD4KPC9zdmc+" 
                     alt="Book Cover" />
              } @else {
                <div class="image-placeholder">
                  <div class="placeholder-icon">📖</div>
                  <div class="placeholder-text">No Image</div>
                </div>
              }
            </div>
            <div class="text-column">
              <div class="headline-supporting">
                <h2 class="book-headline">{{ book()!.title }}</h2>
                @if (getAuthors().length > 0) {
                  <div class="supporting-text">{{ getAuthors().join(', ') }}</div>
                }
              </div>
              <button class="read-button" mat-flat-button color="primary">
                Read
              </button>
            </div>
          </div>

          <!-- Text Content -->
          <div class="text-content-section">
            @if (book()!.publicationDate) {
              <div class="published-date">Published {{ formatDate(book()!.publicationDate!) }}</div>
            }
            @if (book()!.description) {
              <div class="detailed-paragraph">{{ book()!.description }}</div>
            }
          </div>

          <!-- Simple Card Grid -->
          <div class="simple-card-grid">
            <div class="title-header">
              <h3 class="grid-title">Related</h3>
              <button class="icon-button" mat-icon-button>
                <mat-icon>arrow_forward</mat-icon>
              </button>
            </div>
            <div class="column-01">
              <div class="list-item-01">
                <div class="text-and-image">
                  <div class="related-image"></div>
                  <div class="content">
                    <div class="title-description">
                      <div class="related-title">Similar Books</div>
                      <div class="related-description">Discover more books by this author and in similar genres...</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Gesture Bar -->
          <div class="gesture-bar">
            <div class="handle"></div>
          </div>
        </div>

        <!-- Desktop Layout (existing) -->
        <div class="desktop-book-detail">
          <div class="back-button">
            <button mat-button (click)="goBack()">
              <mat-icon>arrow_back</mat-icon>
              Back to Books
            </button>
          </div>

          <!-- ISBNdb-inspired layout -->
          <div class="isbndb-layout">
            <!-- Book Cover -->
            <div class="book-cover-container">
              @if (book()!.hasCover) {
                <img src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iIzM0NDk1ZSIvPgo8dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjE0IiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9IjAuM2VtIj5Db3ZlciBBdmFpbGFibGU8L3RleHQ+Cjwvc3ZnPg==" 
                     alt="Book Cover" 
                     class="book-cover" />
              } @else {
                <div class="book-cover-placeholder">
                  <div class="placeholder-content">
                    <div class="placeholder-icon">📖</div>
                    <div class="placeholder-text">No Image Available</div>
                  </div>
                </div>
              }
            </div>

            <!-- Book Information -->
            <div class="book-info-container">
              <!-- Title Section -->
              <div class="title-section">
                <h1 class="book-title">{{ book()!.title }}</h1>
                @if (book()!.titleSort && book()!.titleSort !== book()!.title) {
                  <div class="book-subtitle">({{ book()!.titleSort }})</div>
                }
              </div>

              <!-- Details List -->
              <div class="details-list">
                <div class="detail-item">
                  <span class="detail-label">Full Title:</span>
                  <span class="detail-value">{{ book()!.title }}</span>
                </div>

                @if (book()!.isbn) {
                  <div class="detail-item">
                    <span class="detail-label">ISBN:</span>
                    <span class="detail-value">{{ book()!.isbn }}</span>
                  </div>
                }

                @if (book()!.isbn) {
                  <div class="detail-item">
                    <span class="detail-label">ISBN13:</span>
                    <span class="detail-value">{{ formatISBN13(book()!.isbn!) }}</span>
                  </div>
                }

                @if (getAuthors().length > 0) {
                  <div class="detail-item">
                    <span class="detail-label">Authors:</span>
                    <span class="detail-value author-links">
                      @for (author of getAuthors(); track author; let last = $last) {
                        <span class="author-link">{{ author }}</span>@if (!last) {<span>, </span>}
                      }
                    </span>
                  </div>
                }

                @if (book()!.publisher) {
                  <div class="detail-item">
                    <span class="detail-label">Publisher:</span>
                    <span class="detail-value">{{ book()!.publisher }}</span>
                  </div>
                }

                @if (getEdition()) {
                  <div class="detail-item">
                    <span class="detail-label">Edition:</span>
                    <span class="detail-value">{{ getEdition() }}</span>
                  </div>
                }

                @if (book()!.publicationDate) {
                  <div class="detail-item">
                    <span class="detail-label">Publish Date:</span>
                    <span class="detail-value">{{ formatDate(book()!.publicationDate!) }}</span>
                  </div>
                }

                @if (getBinding()) {
                  <div class="detail-item">
                    <span class="detail-label">Binding:</span>
                    <span class="detail-value">{{ getBinding() }}</span>
                  </div>
                }

                @if (getPages()) {
                  <div class="detail-item">
                    <span class="detail-label">Pages:</span>
                    <span class="detail-value">{{ getPages() }}</span>
                  </div>
                }

                @if (book()!.description) {
                  <div class="detail-item synopsis">
                    <span class="detail-label">Synopsis:</span>
                    <span class="detail-value">{{ book()!.description }}</span>
                  </div>
                }

                @if (book()!.language) {
                  <div class="detail-item">
                    <span class="detail-label">Language:</span>
                    <span class="detail-value">{{ book()!.language }}</span>
                  </div>
                }

                @if (getDimensions()) {
                  <div class="detail-item">
                    <span class="detail-label">Dimensions:</span>
                    <span class="detail-value">{{ getDimensions() }}</span>
                  </div>
                }

                @if (getWeight()) {
                  <div class="detail-item">
                    <span class="detail-label">Weight:</span>
                    <span class="detail-value">{{ getWeight() }}</span>
                  </div>
                }

                @if (getSubjects().length > 0) {
                  <div class="detail-item">
                    <span class="detail-label">Subjects:</span>
                    <span class="detail-value">{{ getSubjects().join(', ') }}</span>
                  </div>
                }
              </div>
            </div>
          </div>
        </div>
      } @else {
        <div class="error-state">
          <mat-icon style="font-size: 64px; height: 64px; width: 64px;">error</mat-icon>
          <h2>Book not found</h2>
          <p>The requested book could not be found.</p>
          <button mat-raised-button color="primary" (click)="goBack()">
            <mat-icon>arrow_back</mat-icon>
            Back to Books
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .book-detail-container {
      max-width: 1000px;
      margin: 0 auto;
      padding: 0;
      font-family: 'Roboto', sans-serif;
      background-color: #ffffff;
    }

    .loading-container, .error-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 400px;
      text-align: center;
    }

    .loading-container p, .error-state p {
      margin-top: 16px;
      color: #666;
    }

    .error-state h2 {
      margin: 16px 0;
      color: #333;
    }

    /* Mobile Layout - Material Design 3 */
    .mobile-book-detail {
      position: relative;
      width: 100%;
      max-width: 412px;
      margin: 0 auto;
      background: var(--m3-surface);
      border: 1px solid var(--m3-outline);
      border-radius: 28px;
      min-height: 100vh;
      box-sizing: border-box;
      overflow: hidden;
    }

    /* Status Bar */
    .status-bar {
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      align-items: flex-end;
      padding: 10px 24px;
      gap: 286px;
      position: relative;
      width: 100%;
      height: 52px;
      box-sizing: border-box;
    }

    .time {
      width: 29px;
      height: 20px;
      font-family: 'Roboto';
      font-weight: 500;
      font-size: 14px;
      line-height: 20px;
      color: var(--m3-on-surface);
      display: flex;
      align-items: center;
    }

    .right-icons {
      display: flex;
      gap: 8px;
      width: 46px;
      height: 17px;
    }

    .wifi-icon, .signal-icon {
      width: 17px;
      height: 17px;
      background: var(--m3-on-surface);
      opacity: 0.6;
      border-radius: 2px;
    }

    .battery-icon {
      position: relative;
      width: 8px;
      height: 15px;
    }

    .battery-base {
      width: 8px;
      height: 15px;
      background: var(--m3-on-surface);
      opacity: 0.3;
      border-radius: 2px;
    }

    .battery-charge {
      position: absolute;
      width: 8px;
      height: 7px;
      left: 0;
      bottom: 0;
      background: var(--m3-on-surface);
      border-radius: 0 0 2px 2px;
    }

    .camera-cutout {
      position: absolute;
      width: 24px;
      height: 24px;
      left: calc(50% - 12px);
      top: 18px;
      background: var(--m3-on-surface);
      border-radius: 50%;
    }

    /* App Bar */
    .app-bar {
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      align-items: center;
      padding: 8px 4px;
      gap: 4px;
      width: 100%;
      height: 64px;
      box-sizing: border-box;
    }

    .leading-icon, .trailing-action {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 48px;
      height: 48px;
      border-radius: 50%;
      border: none;
      background: transparent;
      cursor: pointer;
      transition: background-color 0.2s;
    }

    .leading-icon:hover, .trailing-action:hover {
      background-color: rgba(0, 0, 0, 0.04);
    }

    .leading-icon mat-icon, .trailing-action mat-icon {
      color: var(--m3-on-surface);
      width: 24px;
      height: 24px;
      font-size: 24px;
    }

    .text-content {
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 0 8px;
    }

    .headline {
      font-family: 'Roboto';
      font-weight: 400;
      font-size: 22px;
      line-height: 28px;
      color: var(--m3-on-surface);
      margin: 0;
      text-align: center;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 300px;
    }

    .trailing-elements {
      display: flex;
      gap: 4px;
    }

    /* Header */
    .header {
      display: flex;
      flex-direction: row;
      align-items: flex-start;
      padding: 8px 16px;
      gap: 24px;
      background: var(--m3-surface);
      box-sizing: border-box;
    }

    .book-image {
      width: 136px;
      height: 207px;
      border-radius: 28px;
      overflow: hidden;
      background: var(--m3-outline-variant);
      flex-shrink: 0;
    }

    .book-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #ECE6F0 0%, #F3EDF7 100%);
      color: #777;
    }

    .placeholder-icon {
      font-size: 48px;
      margin-bottom: 8px;
    }

    .placeholder-text {
      font-size: 12px;
      font-weight: 500;
    }

    .text-column {
      display: flex;
      flex-direction: column;
      gap: 11px;
      flex: 1;
      min-width: 0;
    }

    .headline-supporting {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .book-headline {
      font-family: 'Roboto';
      font-weight: 400;
      font-size: 24px;
      line-height: 32px;
      color: var(--m3-on-surface);
      margin: 0;
    }

    .supporting-text {
      font-family: 'Roboto';
      font-weight: 500;
      font-size: 16px;
      line-height: 24px;
      color: var(--m3-on-surface-variant);
      letter-spacing: 0.15px;
    }

    .read-button {
      width: 95px;
      height: 40px;
      background: var(--m3-primary);
      color: var(--m3-on-primary);
      border-radius: 100px;
      border: none;
      font-family: 'Roboto';
      font-weight: 500;
      font-size: 14px;
      letter-spacing: 0.1px;
      cursor: pointer;
    }

    /* Text Content */
    .text-content-section {
      display: flex;
      flex-direction: column;
      padding: 8px 16px;
      gap: 8px;
      background: var(--m3-surface);
    }

    .published-date {
      font-family: 'Roboto';
      font-weight: 500;
      font-size: 11px;
      line-height: 16px;
      color: var(--m3-on-surface-variant);
      letter-spacing: 0.5px;
    }

    .detailed-paragraph {
      font-family: 'Roboto';
      font-weight: 400;
      font-size: 14px;
      line-height: 20px;
      color: var(--m3-on-surface);
      letter-spacing: 0.25px;
    }

    /* Simple Card Grid */
    .simple-card-grid {
      display: flex;
      flex-direction: column;
      padding: 0 0 32px;
      background: var(--m3-surface);
    }

    .title-header {
      display: flex;
      flex-direction: row;
      align-items: center;
      justify-content: space-between;
      padding: 0 16px;
      height: 48px;
    }

    .grid-title {
      font-family: 'Roboto';
      font-weight: 400;
      font-size: 24px;
      line-height: 32px;
      color: var(--m3-on-surface);
      margin: 0;
    }

    .icon-button {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      border: none;
      background: transparent;
      cursor: pointer;
    }

    .icon-button mat-icon {
      color: var(--m3-on-surface-variant);
    }

    .column-01 {
      display: flex;
      flex-direction: column;
      padding: 0 16px;
      gap: 16px;
    }

    .list-item-01 {
      border-radius: 12px;
    }

    .text-and-image {
      display: flex;
      gap: 16px;
      align-items: flex-start;
    }

    .related-image {
      width: 120px;
      height: 120px;
      background: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0VDRTZGMCIvPgo8dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IlJvYm90byIgZm9udC1zaXplPSIxMiIgZmlsbD0iIzc3NyIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9IjAuM2VtIj5SZWxhdGVkPC90ZXh0Pgo8L3N2Zz4='), var(--m3-outline-variant);
      border-radius: 16px;
      flex-shrink: 0;
    }

    .content {
      flex: 1;
      min-width: 0;
    }

    .title-description {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .related-title {
      font-family: 'Roboto';
      font-weight: 400;
      font-size: 22px;
      line-height: 28px;
      color: var(--m3-on-surface);
    }

    .related-description {
      font-family: 'Roboto';
      font-weight: 400;
      font-size: 14px;
      line-height: 20px;
      color: #000000;
      letter-spacing: 0.25px;
    }

    /* Gesture Bar */
    .gesture-bar {
      position: absolute;
      bottom: 0;
      width: 100%;
      height: 24px;
      background: var(--m3-surface-container);
      display: flex;
      justify-content: center;
      align-items: center;
    }

    .handle {
      width: 108px;
      height: 4px;
      background: var(--m3-on-surface);
      border-radius: 12px;
    }

    /* Desktop Layout (hidden on mobile) */
    .desktop-book-detail {
      display: none;
    }

    /* Desktop styles */
    @media (min-width: 769px) {
      .mobile-book-detail {
        display: none;
      }

      .desktop-book-detail {
        display: block;
      }

      .book-detail-container {
        padding: 20px;
      }

      .back-button {
        margin-bottom: 20px;
      }

      /* ISBNdb-inspired layout */
      .isbndb-layout {
        display: flex;
        gap: 30px;
        align-items: flex-start;
      }

      /* Book Cover */
      .book-cover-container {
        flex: 0 0 200px;
      }

      .book-cover {
        width: 200px;
        height: 300px;
        border: 1px solid #e0e0e0;
        border-radius: 3px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      }

      .book-cover-placeholder {
        width: 200px;
        height: 300px;
        border: 1px solid #e0e0e0;
        border-radius: 3px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f8f9fa;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      }

      .placeholder-content {
        text-align: center;
        color: #6c757d;
      }

      .desktop-book-detail .placeholder-icon {
        font-size: 48px;
        margin-bottom: 8px;
      }

      .desktop-book-detail .placeholder-text {
        font-size: 12px;
        font-weight: 500;
      }

      /* Book Information */
      .book-info-container {
        flex: 1;
        min-width: 0;
      }

      .title-section {
        margin-bottom: 24px;
      }

      .book-title {
        font-size: 26px;
        font-weight: 600;
        color: #333;
        margin: 0 0 8px 0;
        line-height: 1.3;
      }

      .book-subtitle {
        font-size: 18px;
        color: #666;
        font-weight: 400;
        margin: 0;
      }

      /* Details List - ISBNdb style */
      .details-list {
        background-color: #ffffff;
      }

      .detail-item {
        display: flex;
        padding: 10px 0;
        border-bottom: 1px solid #f0f0f0;
        align-items: flex-start;
      }

      .detail-item:first-child {
        padding-top: 0;
      }

      .detail-item:last-child {
        border-bottom: none;
        padding-bottom: 0;
      }

      .detail-item.synopsis {
        align-items: flex-start;
      }

      .detail-item.synopsis .detail-value {
        margin-top: 0;
      }

      .detail-label {
        flex: 0 0 120px;
        font-weight: 600;
        color: #555;
        font-size: 14px;
        padding-right: 16px;
      }

      .detail-value {
        flex: 1;
        color: #333;
        font-size: 14px;
        line-height: 1.5;
        word-wrap: break-word;
      }

      /* Author links styling */
      .author-links .author-link {
        color: #0066cc;
        text-decoration: none;
        cursor: pointer;
      }

      .author-links .author-link:hover {
        text-decoration: underline;
      }
    }

    /* Tablet breakpoint */
    @media (max-width: 1024px) and (min-width: 769px) {
      .isbndb-layout {
        gap: 24px;
      }
      
      .book-cover-container {
        flex: 0 0 160px;
      }
      
      .book-cover, .book-cover-placeholder {
        width: 160px;
        height: 240px;
      }
    }
  `]
})
export class BookDetailComponent implements OnInit {
  book = signal<Book | null>(null);
  loading = signal(true);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService,
    private snackBar: MatSnackBar
  ) {}

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
    this.router.navigate(['/books']);
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

  getAuthors(): string[] {
    if (!this.book()?.contributors) return [];
    return this.book()!.contributors!['author'] || [];
  }

  getOtherContributors(): Array<{role: string, names: string[]}> {
    if (!this.book()?.contributors) return [];
    
    const result: Array<{role: string, names: string[]}> = [];
    const contributors = this.book()!.contributors!;
    
    for (const [role, names] of Object.entries(contributors)) {
      if (role !== 'author' && names.length > 0) {
        // Capitalize first letter of role
        const displayRole = role.charAt(0).toUpperCase() + role.slice(1);
        result.push({ role: displayRole, names });
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

  getMetadataEntries(): Array<{key: string, value: any}> {
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
}