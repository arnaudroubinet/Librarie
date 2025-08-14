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
import { AuthorService } from '../services/author.service';
import { BookService } from '../services/book.service';
import { SeriesService } from '../services/series.service';
import { Author } from '../models/author.model';
import { Book } from '../models/book.model';
import { Series } from '../models/series.model';

@Component({
  selector: 'app-author-detail',
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
    <div class="author-detail-container">
      @if (loading()) {
        <div class="loading-container">
          <mat-spinner diameter="50"></mat-spinner>
          <p>Loading author details...</p>
        </div>
      } @else if (author()) {
        <div class="author-detail">
          <div class="back-button">
            <button mat-button (click)="goBack()">
              <mat-icon>arrow_back</mat-icon>
              Back to Authors
            </button>
          </div>

          <!-- Author detail layout -->
          <div class="author-layout">
            <!-- Author Photo -->
            <div class="author-photo-container">
              @if (author()!.metadata?.['imageUrl']) {
                <img [src]="author()!.metadata!['imageUrl']" 
                     [alt]="author()!.name + ' photo'" 
                     class="author-photo"
                     (error)="onImageError($event)" />
              } @else {
                <div class="author-photo-placeholder">
                  <div class="placeholder-content">
                    <mat-icon class="placeholder-icon">person</mat-icon>
                    <div class="placeholder-text">{{ getInitials(author()!.name) }}</div>
                  </div>
                </div>
              }
            </div>

            <!-- Author Information -->
            <div class="author-info-container">
              <!-- Name Section -->
              <div class="name-section">
                <h1 class="author-name">{{ author()!.name }}</h1>
                @if (author()!.sortName && author()!.sortName !== author()!.name) {
                  <div class="author-sortname">({{ author()!.sortName }})</div>
                }
                @if (author()!.birthDate || author()!.deathDate) {
                  <div class="author-dates">
                    {{ formatDates(author()!.birthDate, author()!.deathDate) }}
                  </div>
                }
              </div>

              <!-- Details List -->
              <div class="details-list">
                <div class="detail-item">
                  <span class="detail-label">Full Name:</span>
                  <span class="detail-value">{{ author()!.name }}</span>
                </div>

                @if (author()!.birthDate) {
                  <div class="detail-item">
                    <span class="detail-label">Birth Date:</span>
                    <span class="detail-value">{{ formatDate(author()!.birthDate!) }}</span>
                  </div>
                }

                @if (author()!.deathDate) {
                  <div class="detail-item">
                    <span class="detail-label">Death Date:</span>
                    <span class="detail-value">{{ formatDate(author()!.deathDate!) }}</span>
                  </div>
                }

                @if (author()!.metadata?.['nationality']) {
                  <div class="detail-item">
                    <span class="detail-label">Nationality:</span>
                    <span class="detail-value">{{ author()!.metadata!['nationality'] }}</span>
                  </div>
                }

                @if (author()!.metadata?.['genres'] && isArray(author()!.metadata!['genres'])) {
                  <div class="detail-item">
                    <span class="detail-label">Genres:</span>
                    <span class="detail-value">
                      <mat-chip-set>
                        @for (genre of author()!.metadata!['genres']; track genre) {
                          <mat-chip>{{ genre }}</mat-chip>
                        }
                      </mat-chip-set>
                    </span>
                  </div>
                }

                @if (author()!.websiteUrl) {
                  <div class="detail-item">
                    <span class="detail-label">Website:</span>
                    <span class="detail-value">
                      <a [href]="author()!.websiteUrl" target="_blank" class="website-link">
                        {{ author()!.websiteUrl }}
                        <mat-icon class="external-link">open_in_new</mat-icon>
                      </a>
                    </span>
                  </div>
                }

                @if (author()!.metadata?.['education']) {
                  <div class="detail-item">
                    <span class="detail-label">Education:</span>
                    <span class="detail-value">{{ author()!.metadata!['education'] }}</span>
                  </div>
                }

                @if (author()!.metadata?.['profession']) {
                  <div class="detail-item">
                    <span class="detail-label">Profession:</span>
                    <span class="detail-value">{{ author()!.metadata!['profession'] }}</span>
                  </div>
                }
              </div>

              <!-- Biography Section -->
              @if (author()!.bio?.['en']) {
                <div class="biography-section">
                  <h2 class="section-title">Biography</h2>
                  <p class="biography-text">{{ author()!.bio!['en'] }}</p>
                </div>
              }

              <!-- Series & Works Section -->
              <div class="works-section">
                <h2 class="section-title">
                  <mat-icon>library_books</mat-icon>
                  Series & Works
                  <span class="item-count">
                    {{ (authorSeries().length || 0) + (authorBooks().length || 0) }} items
                  </span>
                </h2>
                
                @if (loadingWorks()) {
                  <div class="loading-works">
                    <mat-spinner diameter="30"></mat-spinner>
                    <p>Loading author's works...</p>
                  </div>
                } @else {
                  @if (authorSeries().length > 0 || authorBooks().length > 0) {
                    <!-- Series Section -->
                    @if (authorSeries().length > 0) {
                      <div class="series-subsection">
                        <h3 class="subsection-title">
                          <mat-icon>collections_bookmark</mat-icon>
                          Series ({{ authorSeries().length }})
                        </h3>
                        <div class="series-grid">
                          @for (series of authorSeries(); track series.name) {
                            <div class="series-card">
                              @if (series.fallbackImagePath) {
                                <img [src]="series.fallbackImagePath" 
                                     [alt]="series.name + ' cover'" 
                                     class="series-cover"
                                     (error)="onImageError($event)" />
                              } @else {
                                <div class="series-cover-placeholder">
                                  <mat-icon>auto_stories</mat-icon>
                                </div>
                              }
                              <div class="series-info">
                                <h4 class="series-title">{{ series.name }}</h4>
                                <p class="series-book-count">{{ series.bookCount }} {{ series.bookCount === 1 ? 'book' : 'books' }}</p>
                              </div>
                            </div>
                          }
                        </div>
                      </div>
                    }

                    <!-- Individual Books Section -->
                    @if (authorBooks().length > 0) {
                      <div class="books-subsection">
                        <h3 class="subsection-title">
                          <mat-icon>book</mat-icon>
                          Books ({{ authorBooks().length }})
                        </h3>
                        <div class="books-grid">
                          @for (book of authorBooks(); track book.id) {
                            <div class="book-card">
                              @if (book.hasCover && book.path) {
                                <img [src]="book.path + '/cover'" 
                                     [alt]="book.title + ' cover'" 
                                     class="book-cover"
                                     (error)="onImageError($event)" />
                              } @else {
                                <div class="book-cover-placeholder">
                                  <mat-icon>book</mat-icon>
                                </div>
                              }
                              <div class="book-info">
                                <h4 class="book-title">{{ book.title }}</h4>
                                @if (book.series) {
                                  <p class="book-series">{{ book.series }}
                                    @if (book.seriesIndex) {
                                      #{{ book.seriesIndex }}
                                    }
                                  </p>
                                }
                                @if (book.publicationDate) {
                                  <p class="book-date">{{ formatBookDate(book.publicationDate) }}</p>
                                }
                              </div>
                            </div>
                          }
                        </div>
                      </div>
                    }
                  } @else {
                    <div class="no-works">
                      <mat-icon class="no-works-icon">auto_stories</mat-icon>
                      <p>No books or series found for this author.</p>
                      <p class="no-works-description">
                        This could mean the author's works haven't been added to the library yet,
                        or they might be listed under a different name.
                      </p>
                    </div>
                  }
                }
              </div>

              <!-- Actions Section -->
              <div class="actions-section">
                @if (author()!.websiteUrl) {
                  <button mat-raised-button color="primary" (click)="openWebsite(author()!.websiteUrl!)">
                    <mat-icon>language</mat-icon>
                    Visit Website
                  </button>
                }
                <button mat-button (click)="goBack()">
                  <mat-icon>arrow_back</mat-icon>
                  Back to Authors
                </button>
              </div>
            </div>
          </div>
        </div>
      } @else {
        <div class="error-container">
          <mat-icon class="error-icon">error</mat-icon>
          <h2>Author not found</h2>
          <p>The author you're looking for could not be found.</p>
          <button mat-raised-button color="primary" (click)="goBack()">
            <mat-icon>arrow_back</mat-icon>
            Back to Authors
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .author-detail-container {
      min-height: 100vh;
      background: linear-gradient(135deg, #1e1e1e 0%, #2a2a2a 100%);
      color: #ffffff;
      padding: 0;
    }

    .loading-container, .error-container {
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
      padding: 24px 32px 0 32px;
      position: sticky;
      top: 0;
      background: rgba(30, 30, 30, 0.95);
      backdrop-filter: blur(10px);
      z-index: 10;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }

    .back-button button {
      color: #ffffff !important;
      font-weight: 500 !important;
    }

    .author-layout {
      display: flex;
      gap: 48px;
      padding: 48px 32px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .author-photo-container {
      flex: 0 0 200px;
    }

    .author-photo {
      width: 200px;
      height: 200px;
      border-radius: 50%;
      object-fit: cover;
      border: 4px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    }

    .author-photo-placeholder {
      width: 200px;
      height: 200px;
      border-radius: 50%;
      background: linear-gradient(45deg, #1976d2, #ff4081);
      display: flex;
      align-items: center;
      justify-content: center;
      border: 4px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    }

    .placeholder-content {
      text-align: center;
      color: white;
    }

    .placeholder-icon {
      font-size: 3rem !important;
      width: 3rem !important;
      height: 3rem !important;
      margin-bottom: 8px;
    }

    .placeholder-text {
      font-size: 1.5rem;
      font-weight: 600;
    }

    .author-info-container {
      flex: 1;
      min-width: 0;
    }

    .name-section {
      margin-bottom: 32px;
    }

    .author-name {
      font-size: 2.5rem;
      font-weight: 700;
      margin: 0 0 8px 0;
      line-height: 1.2;
      background: linear-gradient(45deg, #1976d2, #ff4081);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .author-sortname {
      font-size: 1.1rem;
      opacity: 0.7;
      margin-bottom: 8px;
    }

    .author-dates {
      font-size: 1.1rem;
      color: #ff4081;
      font-weight: 500;
    }

    .details-list {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 32px;
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

    .detail-item:last-child {
      border-bottom: none;
    }

    .detail-label {
      font-weight: 600;
      color: #1976d2;
      white-space: nowrap;
    }

    .detail-value {
      word-break: break-word;
    }

    .website-link {
      color: #ff4081;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 4px;
      transition: color 0.3s ease;
    }

    .website-link:hover {
      color: #ff6090;
    }

    .external-link {
      font-size: 1rem !important;
      width: 1rem !important;
      height: 1rem !important;
    }

    mat-chip-set {
      --mdc-chip-container-color: rgba(25, 118, 210, 0.2);
      --mdc-chip-label-text-color: #ffffff;
    }

    .biography-section, .works-section {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 32px;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

    .section-title {
      font-size: 1.5rem;
      font-weight: 600;
      margin: 0 0 16px 0;
      color: #1976d2;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .biography-text {
      line-height: 1.6;
      font-size: 1rem;
      margin: 0;
    }

    .placeholder-description {
      opacity: 0.7;
      font-size: 0.9rem;
      margin-top: 8px;
    }

    .actions-section {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
    }

    .actions-section button {
      font-weight: 500 !important;
    }

    /* Responsive design */
    @media (max-width: 768px) {
      .author-layout {
        flex-direction: column;
        gap: 32px;
        padding: 32px 16px;
      }

      .author-photo-container {
        flex: none;
        align-self: center;
      }

      .author-photo, .author-photo-placeholder {
        width: 150px;
        height: 150px;
      }

      .author-name {
        font-size: 2rem;
      }

      .detail-item {
        grid-template-columns: 1fr;
        gap: 4px;
      }

      .detail-label {
        font-weight: 600;
        margin-bottom: 4px;
      }
      
      .detail-value {
        padding-left: 0;
      }
    }

    /* Tablet breakpoint */
    @media (max-width: 1024px) and (min-width: 769px) {
      .author-layout {
        gap: 32px;
      }
      
      .author-photo-container {
        flex: 0 0 160px;
      }
      
      .author-photo, .author-photo-placeholder {
        width: 160px;
        height: 160px;
      }
    }

    .item-count {
      font-size: 0.9rem;
      color: #ff4081;
      font-weight: normal;
      margin-left: 8px;
    }

    .loading-works {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 32px;
      color: rgba(255, 255, 255, 0.7);
    }

    .loading-works mat-spinner {
      margin-bottom: 16px;
    }

    .subsection-title {
      font-size: 1.2rem;
      font-weight: 600;
      margin: 24px 0 16px 0;
      color: #ff4081;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .series-subsection, .books-subsection {
      margin-bottom: 32px;
    }

    .series-grid, .books-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 16px;
      margin-top: 16px;
    }

    .series-card, .book-card {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      padding: 16px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      transition: all 0.3s ease;
      cursor: pointer;
    }

    .series-card:hover, .book-card:hover {
      background: rgba(255, 255, 255, 0.08);
      border-color: rgba(25, 118, 210, 0.3);
      transform: translateY(-2px);
    }

    .series-cover, .book-cover {
      width: 100%;
      height: 120px;
      object-fit: cover;
      border-radius: 4px;
      margin-bottom: 12px;
    }

    .series-cover-placeholder, .book-cover-placeholder {
      width: 100%;
      height: 120px;
      background: linear-gradient(45deg, #1976d2, #ff4081);
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12px;
    }

    .series-cover-placeholder mat-icon, .book-cover-placeholder mat-icon {
      font-size: 3rem !important;
      width: 3rem !important;
      height: 3rem !important;
      color: white;
    }

    .series-title, .book-title {
      font-size: 1rem;
      font-weight: 600;
      margin: 0 0 8px 0;
      line-height: 1.3;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .series-book-count, .book-series, .book-date {
      font-size: 0.85rem;
      color: rgba(255, 255, 255, 0.7);
      margin: 4px 0;
    }

    .book-series {
      color: #1976d2;
      font-weight: 500;
    }

    .no-works {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 48px 24px;
      text-align: center;
      color: rgba(255, 255, 255, 0.7);
    }

    .no-works-icon {
      font-size: 4rem !important;
      width: 4rem !important;
      height: 4rem !important;
      color: rgba(255, 255, 255, 0.3);
      margin-bottom: 16px;
    }

    .no-works-description {
      font-size: 0.9rem;
      margin-top: 8px;
      opacity: 0.8;
    }

    /* Additional responsive updates for works section */
    @media (max-width: 768px) {
      .series-grid, .books-grid {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
        gap: 12px;
      }

      .series-cover, .book-cover {
        height: 100px;
      }

      .series-cover-placeholder, .book-cover-placeholder {
        height: 100px;
      }

      .series-cover-placeholder mat-icon, .book-cover-placeholder mat-icon {
        font-size: 2.5rem !important;
        width: 2.5rem !important;
        height: 2.5rem !important;
      }
    }
  `]
})
export class AuthorDetailComponent implements OnInit {
  author = signal<Author | null>(null);
  loading = signal(true);
  authorBooks = signal<Book[]>([]);
  authorSeries = signal<Series[]>([]);
  loadingWorks = signal(false);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authorService: AuthorService,
    private bookService: BookService,
    private seriesService: SeriesService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadAuthorDetails(id);
    } else {
      this.snackBar.open('Invalid author ID', 'Close', { duration: 3000 });
      this.goBack();
    }
  }

  loadAuthorDetails(id: string) {
    this.loading.set(true);
    this.authorService.getAuthorById(id).subscribe({
      next: (author: Author) => {
        this.author.set(author);
        this.loading.set(false);
        this.loadAuthorWorks(author.name);
      },
      error: (error) => {
        console.error('Error loading author details:', error);
        this.snackBar.open('Failed to load author details', 'Close', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  loadAuthorWorks(authorName: string) {
    this.loadingWorks.set(true);
    
    // Load books by author
    this.bookService.getBooksByAuthor(authorName, undefined, 50).subscribe({
      next: (booksResponse) => {
        this.authorBooks.set(booksResponse.content);
      },
      error: (error) => {
        console.error('Error loading author books:', error);
      }
    });

    // Load series by author
    this.seriesService.getSeriesByAuthor(authorName).subscribe({
      next: (series) => {
        this.authorSeries.set(series);
        this.loadingWorks.set(false);
      },
      error: (error) => {
        console.error('Error loading author series:', error);
        this.loadingWorks.set(false);
      }
    });
  }

  goBack() {
    this.router.navigate(['/authors']);
  }

  onImageError(event: any) {
    event.target.style.display = 'none';
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .map(part => part.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  formatDates(birthDate?: string, deathDate?: string): string {
    const birth = birthDate ? new Date(birthDate).getFullYear() : '?';
    const death = deathDate ? new Date(deathDate).getFullYear() : '';
    
    if (death) {
      return `${birth} - ${death}`;
    } else {
      return `Born ${birth}`;
    }
  }

  openWebsite(url: string) {
    window.open(url, '_blank');
  }

  isArray(value: any): boolean {
    return Array.isArray(value);
  }

  formatBookDate(dateString: string): string {
    const date = new Date(dateString);
    return date.getFullYear().toString();
  }
}