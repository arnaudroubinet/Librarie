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
import { AuthorService } from '../services/author.service';
import { Author } from '../models/author.model';
import { BookService } from '../services/book.service';
import { Book } from '../models/book.model';
import { SeriesService } from '../services/series.service';
import { environment } from '../../environments/environment';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-author-detail',
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
  <div class="author-detail-container plex-library">
      @if (loading()) {
        <div class="loading-container">
          <mat-spinner diameter="50"></mat-spinner>
          <p>Loading author details...</p>
        </div>
      } @else if (author()) {
        <div class="author-detail">
          <div class="back-button">
            <button mat-button (click)="goBack()">
              <iconify-icon icon="lets-icons:back"></iconify-icon>
              Back
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
                    <iconify-icon class="placeholder-iconify" icon="ph:users-three-thin"></iconify-icon>
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
          <a [href]="author()!.websiteUrl" target="_blank" class="website-link">{{ author()!.websiteUrl }}</a>
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

                @if (author()!.createdAt) {
                  <div class="detail-item">
                    <span class="detail-label">Added</span>
                    <span class="detail-value">{{ formatDate(author()!.createdAt!) }}</span>
                  </div>
                }

                @if (author()!.updatedAt) {
                  <div class="detail-item">
                    <span class="detail-label">Updated</span>
                    <span class="detail-value">{{ formatDate(author()!.updatedAt!) }}</span>
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

              <!-- Series Section -->
              @if (authorSeries().length > 0) {
                <div class="works-section">
                  <h2 class="section-title">
                    <iconify-icon icon="icon-park-outline:bookshelf"></iconify-icon>
                    Series
                  </h2>
                  <div class="series-grid">
                    @for (s of authorSeries(); track s.key) {
                      @if (s.id) {
                        <a class="series-card" [routerLink]="['/series', s.id]">
                          <div class="cover-wrap">
                            @if (s.imagePath || s.fallbackImagePath) {
                              <img class="book-cover" [src]="s.imagePath || s.fallbackImagePath" [alt]="s.name + ' cover'" />
                            } @else if (s.coverFromBookId) {
                              <img class="book-cover" [src]="apiUrl + '/v1/books/' + s.coverFromBookId + '/cover'" [alt]="s.name + ' cover'" />
                            } @else {
                              <div class="series-cover-placeholder">
                                <iconify-icon class="placeholder-iconify" icon="icon-park-outline:bookshelf"></iconify-icon>
                                <div class="placeholder-text">{{ getShortTitle(s.name) }}</div>
                              </div>
                            }
                          </div>
                          <div class="series-title">{{ s.name }}</div>
                          <div class="series-count">{{ s.count }} {{ s.count === 1 ? 'book' : 'books' }}</div>
                        </a>
                      } @else {
                        <div class="series-card">
                          <div class="cover-wrap">
                            @if (s.coverFromBookId) {
                              <img class="book-cover" [src]="apiUrl + '/v1/books/' + s.coverFromBookId + '/cover'" [alt]="s.name + ' cover'" />
                            } @else {
                              <div class="series-cover-placeholder">
                                <iconify-icon class="placeholder-iconify" icon="icon-park-outline:bookshelf"></iconify-icon>
                                <div class="placeholder-text">{{ getShortTitle(s.name) }}</div>
                              </div>
                            }
                          </div>
                          <div class="series-title">{{ s.name }}</div>
                          <div class="series-count">{{ s.count }} {{ s.count === 1 ? 'book' : 'books' }}</div>
                        </div>
                      }
                    }
                  </div>
                </div>
              }

              <!-- Books Section -->
              @if (authorBooks().length > 0) {
                <div class="works-section">
                  <h2 class="section-title">
                    <iconify-icon icon="ph:books-thin"></iconify-icon>
                    Books
                  </h2>
                  <div class="books-grid">
                    @for (b of authorBooks(); track b.id) {
                      <a class="book-card" [routerLink]="['/books', b.id]">
                        <div class="cover-wrap">
                          @if (b.hasCover) {
                            <img class="book-cover" [src]="apiUrl + '/v1/books/' + b.id + '/cover'" [alt]="b.title + ' cover'" />
                          } @else {
                            <div class="book-cover-placeholder">
                              <iconify-icon class="placeholder-iconify" icon="ph:books-thin"></iconify-icon>
                              <div class="placeholder-text">{{ getShortTitle(b.title) }}</div>
                            </div>
                          }
                        </div>
                        <div class="book-title">{{ b.title }}</div>
                        @if (b.series) { <div class="book-series-sub">{{ b.series }} @if (b.seriesIndex) { <span>#{{ b.seriesIndex }}</span> }</div> }
                      </a>
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
          <h2>Author not found</h2>
          <p>The author you're looking for could not be found.</p>
          <button mat-raised-button color="primary" (click)="goBack()">
      <iconify-icon icon="lets-icons:back"></iconify-icon>
      Back
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .author-detail-container {
      min-height: 100vh;
      /* Match list pages: inherit app background */
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
      /* Align with list page header */
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
      background: linear-gradient(135deg, #2a2a2a 0%, #1a1a1a 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      border: 4px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    }

  .placeholder-content { text-align: center; color: white; }

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
  font-size: 2.2rem;
  font-weight: 700;
  margin: 0 0 8px 0;
  line-height: 1.2;
  color: #ffffff;
    }

  /* Removed sort name display */

  .author-dates { font-size: 1.1rem; color: #81d4fa; font-weight: 500; }

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

  .detail-label { font-weight: 600; color: #4fc3f7; white-space: nowrap; }

    .detail-value {
      word-break: break-word;
    }

  .website-link { color: #b3e5fc; text-decoration: none; display: inline-flex; align-items: center; gap: 4px; transition: color 0.3s ease; }
  .website-link:hover { color: #e1f5fe; }
    /* Removed external-link icon styling */

  mat-chip-set { --mdc-chip-container-color: rgba(79, 195, 247, 0.2); --mdc-chip-label-text-color: #ffffff; }

    .biography-section, .works-section {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 32px;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

  .section-title { font-size: 1.25rem; font-weight: 600; margin: 0 0 16px 0; color: #4fc3f7; display: flex; align-items: center; gap: 8px; }

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

  .series-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 16px; }
  .series-card { display: block; text-decoration: none; color: #ffffff; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); border-radius: 10px; padding: 12px; transition: transform .2s ease, background .2s ease; }
  .series-card:hover { transform: translateY(-2px); background: rgba(255,255,255,0.06); }
  .series-title { font-size: .95rem; font-weight: 600; line-height: 1.2; margin-bottom: 4px; }
  .series-count { font-size: .8rem; color: #b3e5fc; }
  .series-cover-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; text-align: center; padding: 8px; background: linear-gradient(135deg, #2a2a2a 0%, #1a1a1a 100%); color: #cfcfcf; }

  .books-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 16px; }
  .book-card { display: block; text-decoration: none; color: #ffffff; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); border-radius: 10px; padding: 12px; transition: transform .2s ease, background .2s ease; }
  .book-card:hover { transform: translateY(-2px); background: rgba(255,255,255,0.06); }
  .cover-wrap { width: 100%; aspect-ratio: 2/3; border-radius: 8px; overflow: hidden; margin-bottom: 8px; border: 1px solid rgba(255,255,255,0.1); }
  .book-cover { width: 100%; height: 100%; object-fit: cover; display: block; }
  .book-cover-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; text-align: center; padding: 8px; background: linear-gradient(135deg, #2a2a2a 0%, #1a1a1a 100%); color: #cfcfcf; }
  .placeholder-iconify { font-size: 3rem; width: 3rem; height: 3rem; margin-bottom: 8px; }
  .book-title { font-size: .95rem; font-weight: 600; line-height: 1.2; margin-bottom: 4px; }
  .book-series-sub { font-size: .8rem; color: #b3e5fc; }

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

  .author-name { font-size: 1.8rem; text-align: center; }

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
  `]
})
export class AuthorDetailComponent implements OnInit {
  readonly apiUrl = environment.apiUrl;
  author = signal<Author | null>(null);
  loading = signal(true);
  authorBooks = signal<Book[]>([]);
  authorSeries = signal<Array<{ key: string; id?: string; name: string; count: number; imagePath?: string; fallbackImagePath?: string; coverFromBookId?: string }>>([]);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
  private authorService: AuthorService,
  private bookService: BookService,
  private seriesService: SeriesService,
  private snackBar: MatSnackBar,
  private location: Location
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
        this.loadAuthorWorks(id);
      },
      error: (error) => {
        console.error('Error loading author details:', error);
        this.snackBar.open('Failed to load author details', 'Close', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  private loadAuthorWorks(authorId: string) {
    this.bookService.getAllBooks(undefined, 500).subscribe({
      next: (page) => {
        const name = this.author()?.name;
        const books = (page.content || []).filter(b => {
          const detailed = b.contributorsDetailed?.['author'];
          const simple = b.contributors?.['author'];
          const matchDetailed = Array.isArray(detailed) && detailed.some(a => a.id === authorId || (name && a.name === name));
          const matchSimple = Array.isArray(simple) && name ? simple.includes(name) : false;
          return matchDetailed || matchSimple;
        });
        this.authorBooks.set(books);

    const map = new Map<string, { key: string; id?: string; name: string; count: number; coverFromBookId?: string }>();
        for (const b of books) {
          if (b.series) {
            const key = b.seriesId || `name:${b.series}`;
            const existing = map.get(key);
            if (existing) {
      existing.count += 1;
      if (!existing.coverFromBookId && b.hasCover) existing.coverFromBookId = b.id;
            } else {
      map.set(key, { key, id: b.seriesId, name: b.series, count: 1, coverFromBookId: b.hasCover ? b.id : undefined });
            }
          }
        }
    const seriesEntries = Array.from(map.values()).sort((a, b) => a.name.localeCompare(b.name));
    this.authorSeries.set(seriesEntries);
    this.populateSeriesImages(seriesEntries);
      },
      error: (err) => {
        console.error('Error loading author works:', err);
        this.loading.set(false);
      }
    });
  }

  private populateSeriesImages(seriesEntries: Array<{ id?: string }>) {
    const ids = Array.from(new Set(seriesEntries.map(s => s.id).filter(Boolean))) as string[];
    if (ids.length === 0) {
      this.loading.set(false);
      return;
    }
    forkJoin(ids.map(id => this.seriesService.getSeriesById(id))).subscribe({
      next: (seriesList) => {
        const byId = new Map(seriesList.filter(s => !!s?.id).map(s => [s.id!, s]));
        this.authorSeries.update(arr => arr.map(s => {
          if (s.id && byId.has(s.id)) {
            const ser = byId.get(s.id)!;
            return { ...s, imagePath: (ser as any).imagePath, fallbackImagePath: (ser as any).fallbackImagePath };
          }
          return s;
        }));
        this.loading.set(false);
      },
      error: (e) => {
        console.warn('Failed to fetch some series details', e);
        this.loading.set(false);
      }
    });
  }

  goBack() {
    if (window.history.length > 1) {
      this.location.back();
    } else {
      this.router.navigate(['/authors']);
    }
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

  getShortTitle(title: string): string {
    return title && title.length > 30 ? title.substring(0, 30) + '...' : title;
  }

  isArray(value: any): boolean {
    return Array.isArray(value);
  }
}