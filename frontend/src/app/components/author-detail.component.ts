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
  <div class="author-detail-container motspassants-library">
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
              @if (author()!.id) {
                <img [src]="apiUrl + '/v1/authors/' + author()!.id + '/picture'" 
                     [alt]="author()!.name + ' photo'" 
                     class="author-photo"
                     (error)="onImageError($event)" />
              } @else {
        <div class="author-photo-placeholder">
                  <div class="placeholder-content">
          <iconify-icon class="placeholder-iconify" icon="material-symbols:supervised-user-circle"></iconify-icon>
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
                    <iconify-icon icon="material-symbols:books-movies-and-music"></iconify-icon>
                    Series
                  </h2>
                  <div class="series-grid">
                    @for (s of authorSeries(); track s.key) {
                      @if (s.id) {
                        <a class="series-card" [routerLink]="['/series', s.id]">
                          <div class="cover-wrap">
                            @if (s.imagePath || s.fallbackImagePath) {
                              <img class="book-cover" [src]="getSeriesImageUrl(s)" [alt]="s.name + ' cover'" />
                            } @else if (s.coverFromBookId) {
                              <img class="book-cover" [src]="apiUrl + '/v1/books/' + s.coverFromBookId + '/cover'" [alt]="s.name + ' cover'" />
                            } @else {
                              <div class="series-cover-placeholder">
                                <iconify-icon class="placeholder-iconify" icon="material-symbols:books-movies-and-music"></iconify-icon>
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
                                <iconify-icon class="placeholder-iconify" icon="material-symbols:books-movies-and-music"></iconify-icon>
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
                    <iconify-icon icon="material-symbols:book-2-rounded"></iconify-icon>
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
                              <iconify-icon class="placeholder-iconify" icon="material-symbols:book-2-rounded"></iconify-icon>
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
  styleUrls: ['./author-detail.component.css']
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

  getSeriesImageUrl(s: any): string | null {
    const base: string | undefined = s?.imagePath || s?.fallbackImagePath || undefined;
    if (!base) return null;
    if (/^https?:\/\//i.test(base)) return base;
    return `${this.apiUrl}${base}`;
  }
}