import { Component, OnInit, signal, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MATERIAL_MODULES } from '../shared/materials';
import { getInitials as utilGetInitials, getShortBio as utilGetShortBio, formatDates as utilFormatAuthorDates, getShortTitle as utilGetShortTitle, formatDate as utilFormatDate } from '../utils/author-utils';
import { SeriesService } from '../services/series.service';
import { AuthorService } from '../services/author.service';
import { Series } from '../models/series.model';
import { Book } from '../models/book.model';
import { Author } from '../models/author.model';
import { environment } from '../../environments/environment';
import { forkJoin, of } from 'rxjs';

@Component({
  selector: 'app-series-detail',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    RouterModule,
    ...MATERIAL_MODULES
  ],
  template: `
  <div class="series-detail-container motspassants-library">
      @if (loading()) {
        <div class="loading-container">
          <mat-spinner diameter="50"></mat-spinner>
          <p>Loading series details...</p>
        </div>
      } @else if (series()) {
        <div class="series-detail">
          <div class="back-button">
            <button mat-button (click)="goBack()">
              <iconify-icon icon="lets-icons:back"></iconify-icon>
              Back
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
                    <iconify-icon class="placeholder-icon" icon="material-symbols:books-movies-and-music"></iconify-icon>
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
                <iconify-icon icon="material-symbols:books-movies-and-music"></iconify-icon>
                Books in this Series
                @if (books().length > 0) {
                  <span class="item-count">({{ books().length }})</span>
                }
              </h2>
              
              @if (books().length > 0) {
                <div class="books-grid">
                  @for (book of books(); track book.id) {
                    <div class="book-card" 
                         matRipple
                         [routerLink]="['/books', book.id]">
                      <div class="book-cover">
                        @if (getEffectiveBookImagePath(book)) {
                          <img [src]="getEffectiveBookImagePath(book)!"
                               [alt]="book.title + ' cover'"
                               class="cover-image"
                               loading="lazy"
                               decoding="async"
                               fetchpriority="low"
                               (load)="onImageLoad($event)"
                               (error)="onImageError($event)">
                        } @else {
                          <div class="cover-placeholder">
                            <mat-icon>menu_book</mat-icon>
                            <span class="book-title-text">{{ getShortBookTitle(book.title) }}</span>
                          </div>
                        }
                        <div class="book-overlay">
                          <div class="book-actions">
                            <button mat-icon-button class="action-btn" aria-label="Bookmark" (click)="toggleFavorite($event, book)">
                              <iconify-icon [icon]="getBookmarkIcon(book)"></iconify-icon>
                            </button>
                            <button mat-icon-button class="action-btn" aria-label="Share" (click)="shareBook($event, book)">
                              <iconify-icon icon="material-symbols-light:share"></iconify-icon>
                            </button>
                          </div>
                        </div>
                      </div>
                      <div class="book-info">
                        <h3 class="book-title" [title]="book.title">{{ getShortBookTitle(book.title) }}</h3>
                        @if (book.contributorsDetailed || book.contributors) {
                          <p class="book-author">{{ getShortContributors(book.contributors, book.contributorsDetailed) }}</p>
                        }
                        @if (book.seriesIndex) {
                          <p class="book-series">#{{ book.seriesIndex }}</p>
                        }
                        @if (book.publicationDate) {
                          <p class="book-date">{{ getPublicationYear(book.publicationDate!) }}</p>
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
          @if (contributors().length > 0) {
            <div class="contributors-section">
              <h2 class="section-title">
                <iconify-icon icon="material-symbols:supervised-user-circle"></iconify-icon>
                People who worked on this series
                <span class="item-count">({{ contributors().length }})</span>
              </h2>
              
              <div class="contributors-grid">
                @for (c of contributors(); track c.id ? c.id : c.name) {
                  @if (getAuthorFor(c); as a) {
                    <div class="author-card" matRipple [routerLink]="['/authors', a.id]">
                      <div class="card-container">
                        <div class="author-photo">
                          @if (a.id && !hasAuthorImageError(a.id)) {
                            <img [src]="getAuthorImageUrl(a)" [alt]="a.name + ' photo'" class="photo-image" (error)="onAuthorImageError($event, a.id)">
                          } @else {
                            <div class="photo-placeholder">
                              <mat-icon>person</mat-icon>
                              <span class="name-text">{{ getInitials(a.name) }}</span>
                            </div>
                          }
                          <div class="photo-overlay">
                            <mat-icon class="view-icon">visibility</mat-icon>
                          </div>
                        </div>
                        <div class="author-info">
                          <h3 class="author-name" [title]="a.name">{{ a.name }}</h3>
                          @if (a.bio?.['en']) {
                            <p class="author-bio">{{ getShortBio(a.bio!['en']) }}</p>
                          }
                          @if (a.birthDate || a.deathDate) {
                            <p class="author-dates">{{ formatAuthorDates(a.birthDate, a.deathDate) }}</p>
                          }
                        </div>
                      </div>
                    </div>
                  } @else {
                    <div class="author-card">
                      <div class="card-container">
                        <div class="author-photo">
                          <div class="photo-placeholder">
                            <mat-icon>person</mat-icon>
                            <span class="name-text">{{ getInitials(c.name) }}</span>
                          </div>
                        </div>
                        <div class="author-info">
                          <h3 class="author-name" [title]="c.name">{{ c.name }}</h3>
                        </div>
                      </div>
                    </div>
                  }
                }
              </div>
            </div>
          }
        </div>
      } @else {
        <div class="error-state">
          <mat-icon class="error-icon">error</mat-icon>
          <h2>Series not found</h2>
          <p>The series you are looking for could not be found.</p>
          <button mat-raised-button color="primary" (click)="goBack()">
            <iconify-icon icon="lets-icons:back"></iconify-icon>
            Back
          </button>
        </div>
      }
    </div>
  `,
  styleUrls: ['./series-detail.component.css']
})
export class SeriesDetailComponent implements OnInit {
  readonly apiUrl = environment.apiUrl;
  series = signal<Series | null>(null);
  books = signal<Book[]>([]);
  // Aggregated contributors and loaded author details
  contributors = signal<Array<{ id?: string; name: string; roles: string[]; bookCount: number }>>([]);
  authorsMap = signal<Record<string, Author>>({});
  private authorImageErrors = new Set<string>();
  loading = signal(true);
  booksLoading = signal(true);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
  private seriesService: SeriesService,
  private authorService: AuthorService,
  private snackBar: MatSnackBar,
  private location: Location
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
  this.loadSeriesBooks(series.id);
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

  loadSeriesBooks(seriesId: string) {
    this.booksLoading.set(true);
    this.seriesService.getSeriesBooksById(seriesId).subscribe({
      next: (items: any[]) => {
        // Map API response into Book interface shape used by /books page
        const mapped: Book[] = (items || []).map(it => ({
          id: it.id,
          title: it.title,
          hasCover: it.hasCover,
          seriesIndex: it.seriesIndex,
          publicationDate: it.publicationDate,
          contributors: it.contributors,
          contributorsDetailed: it.contributorsDetailed,
          series: it.series
        }));
        this.books.set(mapped);
        this.booksLoading.set(false);
  // After books are loaded, aggregate contributors and fetch author details
  const agg = this.getAllContributors();
  this.contributors.set(agg);
  this.loadContributorAuthors(agg);
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
    if (window.history.length > 1) {
      this.location.back();
    } else {
      this.router.navigate(['/series']);
    }
  }

  getEffectiveImagePath(series: Series): string | null {
  const base = (series.hasPicture ? 'has' : null) || series.imagePath || null;
  if (!base) return null;
    // Prefer backend endpoint to leverage caching when an image exists
    return `${environment.apiUrl}/v1/books/series/${series.id}/picture`;
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

  getBookAuthorsDetailed(book: Book): Array<{ id: string; name: string }> {
    const contribs = book.contributorsDetailed?.['author'];
    return Array.isArray(contribs) ? contribs : [];
  }

  getAllContributors(): Array<{ id?: string; name: string; roles: string[]; bookCount: number }> {
    const contributorMap = new Map<string, { id?: string; name: string; roles: Set<string>; bookCount: number }>();

    this.books().forEach(book => {
      if (book.contributorsDetailed) {
        Object.entries(book.contributorsDetailed).forEach(([role, list]) => {
          list.forEach(({ id, name }) => {
            const key = id || name;
            if (!contributorMap.has(key)) {
              contributorMap.set(key, { id, name, roles: new Set(), bookCount: 0 });
            }
            const contributor = contributorMap.get(key)!;
            contributor.roles.add(role);
            contributor.bookCount++;
          });
        });
      }
    });

    return Array.from(contributorMap.values())
      .map(c => ({
        id: c.id,
        name: c.name,
        roles: Array.from(c.roles).map(role => role.charAt(0).toUpperCase() + role.slice(1)),
        bookCount: c.bookCount,
      }))
      .sort((a, b) => b.bookCount - a.bookCount || a.name.localeCompare(b.name));
  }

  getInitials = utilGetInitials;

  private loadContributorAuthors(contributors: Array<{ id?: string; name?: string }>) {
    const ids = Array.from(new Set(contributors.map(c => c.id).filter((id): id is string => !!id)));
    const names = Array.from(new Set(contributors.filter(c => !c.id && c.name).map(c => c.name!)));

    const idCalls = ids.map(id => this.authorService.getAuthorById(id));
    const nameCalls = names.map(n => this.authorService.searchAuthorsSimple(n));

    forkJoin([
      idCalls.length ? forkJoin(idCalls) : of([] as Author[]),
      nameCalls.length ? forkJoin(nameCalls) : of([] as Author[][])
    ]).subscribe({
      next: ([authors, searchResults]: [Author[], Author[][]]) => {
        const map: Record<string, Author> = {};
        authors.forEach(a => { map[a.id] = a; });
        searchResults.forEach((arr, idx) => {
          const name = names[idx];
          if (!arr || arr.length === 0) return;
          const match = arr.find(a => a.name.toLowerCase() === name.toLowerCase()) || arr[0];
          if (match) {
            map['name:' + name.toLowerCase()] = match;
          }
        });
        this.authorsMap.set(map);
      },
      error: (err) => {
        console.error('Failed to load author details for contributors', err);
      }
    });
  }

  getAuthorFor(c: { id?: string; name?: string }): Author | null {
    const map = this.authorsMap();
    if (c.id && map[c.id]) return map[c.id];
    if (c.name && map['name:' + c.name.toLowerCase()]) return map['name:' + c.name.toLowerCase()];
    return null;
  }

  hasAuthorImageError(authorId: string): boolean {
    return this.authorImageErrors.has(authorId);
  }

  getAuthorImageUrl(author: Author): string {
    return `${this.apiUrl}/v1/authors/${author.id}/picture`;
  }


  formatAuthorDates = utilFormatAuthorDates;
  getShortBio = utilGetShortBio;

  onAuthorImageError(event: any, authorId?: string) {
    if (authorId) this.authorImageErrors.add(authorId);
    event.target.style.display = 'none';
  }

  // ===== Book card helpers and actions (mirror /books page) =====
  getShortBookTitle(title: string): string {
    return title.length > 30 ? title.substring(0, 30) + '...' : title;
  }

  getPublicationYear(publicationDate: string): number {
    return new Date(publicationDate).getFullYear();
  }

  getShortContributors(
    contributors?: Record<string, string[]>,
    contributorsDetailed?: Record<string, Array<{ id: string; name: string }>>
  ): string {
    let allContributors: string[] = [];
    if (contributorsDetailed) {
      allContributors = Object.values(contributorsDetailed).flat().map(c => c.name);
    } else if (contributors) {
      allContributors = Object.values(contributors).flat();
    }
    if (allContributors.length === 0) return '';
    if (allContributors.length === 1) return allContributors[0];
    return `${allContributors[0]} and ${allContributors.length - 1} more`;
  }

  onImageLoad(event: any) {
    event.target.classList.add('loaded');
  }

  getEffectiveBookImagePath(book: Book): string | null {
    if (book.hasCover && book.id) {
      return `${environment.apiUrl}/v1/books/${book.id}/cover`;
    }
    return null;
  }

  private favorites = new Set<string>();

  toggleFavorite(event: Event, book: Book) {
    event.stopPropagation();
    if (!book?.id) return;
    if (this.favorites.has(book.id)) {
      this.favorites.delete(book.id);
      this.snackBar.open('Removed bookmark', 'Close', { duration: 1500 });
    } else {
      this.favorites.add(book.id);
      this.snackBar.open('Bookmarked', 'Close', { duration: 1500 });
    }
  }

  getBookmarkIcon(book: Book): string {
    return this.isBookmarked(book) ? 'material-symbols:book' : 'material-symbols:book-outline';
  }

  isBookmarked(book: Book): boolean {
    return !!book?.id && this.favorites.has(book.id);
  }

  shareBook(event: Event, book: Book) {
    event.stopPropagation();
    const url = `${window.location.origin}/books/${book.id}`;
    const done = () => this.snackBar.open('Book link copied to clipboard', 'Close', { duration: 2000 });
    const fail = () => this.snackBar.open('Failed to copy link', 'Close', { duration: 2500 });

    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(url).then(done).catch(() => {
        this.legacyCopy(url) ? done() : fail();
      });
    } else {
      this.legacyCopy(url) ? done() : fail();
    }
  }

  private legacyCopy(text: string): boolean {
    try {
      const textarea = document.createElement('textarea');
      textarea.value = text;
      textarea.style.position = 'fixed';
      textarea.style.left = '-9999px';
      document.body.appendChild(textarea);
      textarea.focus();
      textarea.select();
      const success = document.execCommand('copy');
      document.body.removeChild(textarea);
      return success;
    } catch {
      return false;
    }
  }
}