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
import { MatGridListModule } from '@angular/material/grid-list';
import { MatRippleModule } from '@angular/material/core';
import { SeriesService } from '../services/series.service';
import { AuthorService } from '../services/author.service';
import { Series } from '../models/series.model';
import { Book, CursorPageResponse } from '../models/book.model';
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
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSnackBarModule,
    MatDividerModule,
  MatGridListModule,
  MatRippleModule
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
                    <iconify-icon class="placeholder-icon" icon="icon-park-outline:bookshelf"></iconify-icon>
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
                <iconify-icon icon="icon-park-outline:bookshelf"></iconify-icon>
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
                        @if (getBookAuthorsDetailed(book).length > 0) {
                          <p class="book-authors">
                            @for (a of getBookAuthorsDetailed(book); track a.id; let last = $last) {
                              <a class="author-link" [routerLink]="['/authors', a.id]" (click)="$event.stopPropagation()">{{ a.name }}</a>
                              @if (!last) {<span>, </span>}
                            }
                          </p>
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
          @if (contributors().length > 0) {
            <div class="contributors-section">
              <h2 class="section-title">
                <iconify-icon icon="ph:users-three-thin"></iconify-icon>
                People who worked on this series
                <span class="item-count">({{ contributors().length }})</span>
              </h2>
              
              <div class="contributors-grid">
                @for (c of contributors(); track c.id ? c.id : c.name) {
                  @if (getAuthorFor(c); as a) {
                    <div class="author-card" matRipple [routerLink]="['/authors', a.id]">
                      <div class="card-container">
                        <div class="author-photo">
                          @if (a.metadata?.['imageUrl'] && !hasAuthorImageError(a.id)) {
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

    .loading-container p, .error-state p, .section-loading p { margin-top: 16px; color: #cfcfcf; }
    .error-state h2 { margin: 16px 0; color: #ffffff; }
    .error-icon { font-size: 4rem !important; width: 4rem !important; height: 4rem !important; color: #ff4444; margin-bottom: 16px; }

    .back-button {
      /* Match all books page header spacing and remove separator */
      padding: 24px 20px;
      position: sticky;
      top: 0;
      background: transparent;
      backdrop-filter: none;
      z-index: 10;
      border-bottom: none;
      margin-bottom: 0;
    }

    .back-button button { color: #ffffff !important; font-weight: 500 !important; }

    /* Iconify icons inside buttons spacing/size */
    .back-button iconify-icon,
    .section-title iconify-icon,
    .error-state iconify-icon {
      margin-right: 8px;
      font-size: 20px;
      width: 20px;
      height: 20px;
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
  .book-authors .author-link { color: #b3e5fc; text-decoration: none; }
  .book-authors .author-link:hover { text-decoration: underline; }

  /* Contributors Grid using author-list styles */
  .contributors-grid { display: grid; grid-template-columns: repeat(auto-fill, 240px); gap: 16px; justify-content: center; }
  .author-card { background: linear-gradient(135deg, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.04) 100%); border: 1px solid rgba(255, 255, 255, 0.12); border-radius: 16px; overflow: hidden; transition: all 0.3s ease; cursor: pointer; position: relative; backdrop-filter: blur(10px); width: 240px; }
  .author-card:hover { transform: translateY(-4px); box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3); border-color: rgba(33, 150, 243, 0.5); }
  .card-container { padding: 20px; }
  .author-photo { position: relative; width: 100%; height: 200px; border-radius: 12px; overflow: hidden; margin-bottom: 16px; background: linear-gradient(135deg, #333 0%, #555 100%); }
  .photo-image { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s ease; }
  .author-card:hover .photo-image { transform: scale(1.05); }
  .photo-placeholder { width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #888; background: linear-gradient(135deg, #2a2a2a 0%, #1e1e1e 100%); }
  .photo-placeholder mat-icon { font-size: 4rem; margin-bottom: 8px; }
  .name-text { font-size: 1.2rem; font-weight: 600; }
  .photo-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.6); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.3s ease; }
  .author-card:hover .photo-overlay { opacity: 1; }
  .view-icon { color: white; font-size: 2rem; }
  .author-info { text-align: center; }
  .author-name { font-size: 1.3rem; font-weight: 500; margin: 0 0 8px 0; color: #ffffff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .author-bio { font-size: 0.9rem; line-height: 1.4; margin: 0 0 8px 0; opacity: 0.8; color: #e3f2fd; height: 2.8em; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
  .author-dates { font-size: 0.85rem; margin: 0; opacity: 0.8; color: #e3f2fd; }

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
      
  .contributors-grid { grid-template-columns: repeat(auto-fill, 200px); gap: 16px; justify-content: center; }
    }
  `]
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

  getInitials(name: string): string {
    return name
      .split(' ')
      .map(part => part.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

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
    const raw = author.metadata?.['imageUrl'];
    if (!raw) return '';
    // If it's an absolute URL (http/https/data), return as-is
    if (/^(https?:)?\/\//i.test(raw) || raw.startsWith('data:')) return raw;
    // Otherwise treat as relative to API base
    return `${this.apiUrl}${raw.startsWith('/') ? '' : '/'}${raw}`;
  }

  formatAuthorDates(birthDate?: string, deathDate?: string): string {
    const birth = birthDate ? new Date(birthDate).getFullYear() : '?';
    const death = deathDate ? new Date(deathDate).getFullYear() : '';
    return death ? `${birth} - ${death}` : `Born ${birth}`;
  }

  getShortBio(bio: string): string {
    if (!bio) return '';
    const maxLength = 120;
    return bio.length > maxLength ? bio.substring(0, maxLength) + '...' : bio;
  }

  onAuthorImageError(event: any, authorId?: string) {
    if (authorId) this.authorImageErrors.add(authorId);
    event.target.style.display = 'none';
  }
}