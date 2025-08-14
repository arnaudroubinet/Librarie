import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatGridListModule } from '@angular/material/grid-list';
import { SeriesService } from '../services/series.service';
import { Series } from '../models/series.model';
import { Book, CursorPageResponse } from '../models/book.model';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-series-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSnackBarModule,
    MatDividerModule,
    MatGridListModule
  ],
  template: `
    <div class="series-detail-container plex-library">
      @if (loading()) {
        <div class="loading-container">
          <mat-spinner diameter="50"></mat-spinner>
          <p>Loading series details...</p>
        </div>
      } @else if (series()) {
        <div class="series-detail">
          <div class="back-button">
            <button mat-button (click)="goBack()">
              <mat-icon>arrow_back</mat-icon>
              Back to Series
            </button>
          </div>

          <!-- Series layout inspired by book detail -->
          <div class="series-layout">
            <!-- Series Cover -->
            <div class="series-cover-container">
              @if (getEffectiveImagePath(series()!)) {
                <img [src]="getEffectiveImagePath(series()!)" 
                     [alt]="series()!.name + ' series'"
                     class="series-cover" 
                     (error)="onImageError($event)">
              } @else {
                <div class="series-cover-placeholder">
                  <div class="placeholder-content">
                    <div class="placeholder-icon">📚</div>
                    <div class="placeholder-text">{{ getShortTitle(series()!.name) }}</div>
                  </div>
                </div>
              }
              
              <!-- Book count badge -->
              <div class="book-count-badge">
                {{ series()!.bookCount }} {{ series()!.bookCount === 1 ? 'book' : 'books' }}
              </div>
            </div>

            <!-- Series Information -->
            <div class="series-info-container">
              <!-- Title Section -->
              <div class="title-section">
                <h1 class="series-title">{{ series()!.name }}</h1>
                @if (series()!.sortName && series()!.sortName !== series()!.name) {
                  <div class="series-subtitle">({{ series()!.sortName }})</div>
                }
              </div>

              <!-- Details List -->
              <div class="details-list">
                @if (series()!.description) {
                  <div class="detail-item synopsis">
                    <span class="detail-label">Summary:</span>
                    <span class="detail-value">{{ series()!.description }}</span>
                  </div>
                }

                <div class="detail-item">
                  <span class="detail-label">Book Count:</span>
                  <span class="detail-value">{{ series()!.bookCount }} {{ series()!.bookCount === 1 ? 'book' : 'books' }}</span>
                </div>

                @if (series()!.createdAt) {
                  <div class="detail-item">
                    <span class="detail-label">Added to Library:</span>
                    <span class="detail-value">{{ formatDate(series()!.createdAt!) }}</span>
                  </div>
                }

                @if (series()!.updatedAt) {
                  <div class="detail-item">
                    <span class="detail-label">Last Updated:</span>
                    <span class="detail-value">{{ formatDate(series()!.updatedAt!) }}</span>
                  </div>
                }
              </div>
            </div>
          </div>

          <!-- Books in this Series -->
          @if (booksLoading()) {
            <div class="section-loading">
              <mat-spinner diameter="30"></mat-spinner>
              <p>Loading books in series...</p>
            </div>
          } @else {
            <div class="books-section">
              <h2 class="section-title">
                <mat-icon>collections</mat-icon>
                Books in this Series
                @if (books().length > 0) {
                  <span class="item-count">({{ books().length }})</span>
                }
              </h2>
              
              @if (books().length > 0) {
                <div class="books-grid">
                  @for (book of books(); track book.id) {
                    <div class="book-card" [routerLink]="['/books', book.id]">
                      <div class="book-cover">
                        @if (book.hasCover) {
                          <img [src]="apiUrl + '/v1/books/' + book.id + '/cover'"
                               [alt]="book.title + ' cover'"
                               class="cover-image">
                        } @else {
                          <div class="cover-placeholder">
                            <mat-icon>book</mat-icon>
                          </div>
                        }
                      </div>
                      <div class="book-info">
                        <h3 class="book-title">{{ book.title }}</h3>
                        @if (book.seriesIndex) {
                          <p class="book-index">Book {{ book.seriesIndex }}</p>
                        }
                        @if (getBookAuthors(book).length > 0) {
                          <p class="book-authors">{{ getBookAuthors(book).join(', ') }}</p>
                        }
                        @if (book.publicationDate) {
                          <p class="book-date">{{ formatDate(book.publicationDate!) }}</p>
                        }
                      </div>
                    </div>
                  }
                </div>
              } @else {
                <div class="empty-state">
                  <mat-icon>library_books</mat-icon>
                  <p>No books found in this series</p>
                </div>
              }
            </div>
          }

          <!-- Contributors Section -->
          @if (getAllContributors().length > 0) {
            <div class="contributors-section">
              <h2 class="section-title">
                <mat-icon>people</mat-icon>
                People who worked on this series
                <span class="item-count">({{ getAllContributors().length }})</span>
              </h2>
              
              <div class="contributors-grid">
                @for (contributor of getAllContributors(); track contributor.name) {
                  <div class="contributor-card">
                    <div class="contributor-info">
                      <h3 class="contributor-name">{{ contributor.name }}</h3>
                      <p class="contributor-roles">{{ contributor.roles.join(', ') }}</p>
                      <p class="contributor-count">{{ contributor.bookCount }} {{ contributor.bookCount === 1 ? 'book' : 'books' }}</p>
                    </div>
                  </div>
                }
              </div>
            </div>
          }
        </div>
      } @else {
        <div class="error-state">
          <mat-icon>error</mat-icon>
          <h2>Series not found</h2>
          <p>The series you are looking for could not be found.</p>
          <button mat-raised-button color="primary" (click)="goBack()">
            Back to Series
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .series-detail-container {
      min-height: 100vh;
      background: transparent;
      color: #ffffff;
      padding: 0;
    }

    .loading-container, .error-state, .section-loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 400px;
      text-align: center;
    }

    .section-loading {
      min-height: 200px;
    }

    .loading-container p, .error-state p, .section-loading p {
      margin-top: 16px;
      color: #666;
    }

    .error-state h2 {
      margin: 16px 0;
      color: #333;
    }

    .back-button {
      padding: 24px 20px;
      position: sticky;
      top: 0;
      background: transparent;
      backdrop-filter: none;
      z-index: 10;
      border-bottom: none;
      margin-bottom: 0;
    }

    /* Series layout */
    .series-layout {
      display: flex;
      gap: 48px;
      align-items: flex-start;
      padding: 48px 32px;
      max-width: 1200px;
      margin: 0 auto;
    }

    /* Series Cover */
    .series-cover-container {
      flex: 0 0 240px;
      position: relative;
    }

    .series-cover {
      width: 240px;
      height: 360px;
      object-fit: cover;
      border-radius: 12px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
    }

    .series-cover-placeholder {
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

    .placeholder-content {
      text-align: center;
    }

    .placeholder-icon {
      font-size: 48px;
      margin-bottom: 8px;
    }

    .placeholder-text {
      font-size: 12px;
      font-weight: 500;
      padding: 0 10px;
      word-wrap: break-word;
    }

    .book-count-badge {
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

    /* Series Information */
    .series-info-container {
      flex: 1;
      min-width: 0;
    }

  .title-section { margin-bottom: 24px; }

    .series-title {
      font-size: 2.2rem;
      font-weight: 700;
      margin: 0 0 8px 0;
      line-height: 1.2;
      color: #ffffff;
    }

  .series-subtitle { font-size: 1.1rem; color: #cfcfcf; font-weight: 400; margin: 0; }

    /* Details List */
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

  .detail-label { font-weight: 600; color: #4fc3f7; white-space: nowrap; }

  .detail-value { color: #ffffff; opacity: 0.95; word-break: break-word; }

    .synopsis .detail-value {
      line-height: 1.6;
    }

    /* Sections */
    .books-section, .contributors-section {
      margin-top: 40px;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 1.25rem;
      font-weight: 600;
      color: #4fc3f7;
      margin-bottom: 16px;
    }

  .item-count { color: #cfcfcf; font-weight: 400; }

    /* Books Grid */
    .books-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 20px;
    }

    .book-card {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      overflow: hidden;
      border: 1px solid rgba(255, 255, 255, 0.1);
      transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
      cursor: pointer;
      text-decoration: none;
      color: inherit;
    }

    .book-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 24px rgba(0,0,0,0.3);
      border-color: rgba(79, 195, 247, 0.5);
    }

    .book-cover {
      height: 180px;
      position: relative;
      overflow: hidden;
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
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #2a2a2a 0%, #1a1a1a 100%);
      color: #cfcfcf;
    }

    .cover-placeholder mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
    }

    .book-info {
      padding: 12px;
    }

    .book-title {
      font-size: 14px;
      font-weight: 600;
      margin: 0 0 8px 0;
      line-height: 1.3;
      color: #ffffff;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .book-index, .book-authors, .book-date {
      font-size: 12px;
      color: #cfcfcf;
      margin: 4px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

  .book-index { font-weight: 500; color: #4fc3f7; }

    /* Contributors Grid */
    .contributors-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 16px;
    }

    .contributor-card {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      padding: 16px;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

  .contributor-name { font-size: 16px; font-weight: 600; margin: 0 0 8px 0; color: #ffffff; }

  .contributor-roles { font-size: 14px; color: #cfcfcf; margin: 4px 0; font-style: italic; }

  .contributor-count { font-size: 12px; color: #cfcfcf; opacity: 0.8; margin: 4px 0 0 0; }

    /* Empty State */
  .empty-state { text-align: center; padding: 40px; color: #cfcfcf; }

    .empty-state mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
      opacity: 0.5;
    }

    /* Responsive */
    @media (max-width: 768px) {
      .series-layout {
        flex-direction: column;
        gap: 20px;
      }
      
      .series-cover-container {
        flex: none;
        align-self: center;
      }
      
      .books-grid {
        grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
        gap: 16px;
      }
      
      .contributors-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class SeriesDetailComponent implements OnInit {
  readonly apiUrl = environment.apiUrl;
  series = signal<Series | null>(null);
  books = signal<Book[]>([]);
  loading = signal(true);
  booksLoading = signal(true);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private seriesService: SeriesService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadSeriesDetails(id);
    } else {
      this.loading.set(false);
    }
  }

  loadSeriesDetails(id: string) {
    this.loading.set(true);
    this.seriesService.getSeriesById(id).subscribe({
      next: (series) => {
        this.series.set(series);
        this.loading.set(false);
        this.loadSeriesBooks(series.name);
      },
      error: (error) => {
        console.error('Error loading series details:', error);
        this.snackBar.open('Failed to load series details.', 'Close', {
          duration: 3000
        });
        this.loading.set(false);
      }
    });
  }

  loadSeriesBooks(seriesName: string) {
    this.booksLoading.set(true);
    this.seriesService.getSeriesBooks(seriesName).subscribe({
      next: (response: CursorPageResponse<Book>) => {
        this.books.set(response.content || []);
        this.booksLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading series books:', error);
        this.snackBar.open('Failed to load books in series.', 'Close', {
          duration: 3000
        });
        this.booksLoading.set(false);
      }
    });
  }

  goBack() {
    this.router.navigate(['/series']);
  }

  getEffectiveImagePath(series: Series): string | null {
    if (series.imagePath) {
      return series.imagePath;
    }
    if (series.fallbackImagePath) {
      return series.fallbackImagePath;
    }
    return null;
  }

  onImageError(event: any) {
    // Hide the image if it fails to load
    event.target.style.display = 'none';
  }

  getShortTitle(title: string): string {
    return title.length > 20 ? title.substring(0, 20) + '...' : title;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  getBookAuthors(book: Book): string[] {
    if (book.contributorsDetailed?.['author']?.length) {
      return book.contributorsDetailed['author'].map(a => a.name);
    }
    return [];
  }

  getAllContributors(): Array<{name: string, roles: string[], bookCount: number}> {
    const contributorMap = new Map<string, {roles: Set<string>, bookCount: number}>();
    
    this.books().forEach(book => {
  if (book.contributorsDetailed) {
        Object.entries(book.contributorsDetailed).forEach(([role, list]) => {
          list.forEach(({ name }) => {
            if (!contributorMap.has(name)) {
              contributorMap.set(name, { roles: new Set(), bookCount: 0 });
            }
            const contributor = contributorMap.get(name)!;
            contributor.roles.add(role);
            contributor.bookCount++;
          });
        });
  }
    });

    return Array.from(contributorMap.entries()).map(([name, data]) => ({
      name,
      roles: Array.from(data.roles).map(role => 
        role.charAt(0).toUpperCase() + role.slice(1)
      ),
      bookCount: data.bookCount
    })).sort((a, b) => b.bookCount - a.bookCount);
  }
}