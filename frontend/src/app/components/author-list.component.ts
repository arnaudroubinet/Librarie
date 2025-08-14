import { Component, OnInit, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatRippleModule } from '@angular/material/core';
import { MatBadgeModule } from '@angular/material/badge';
import { AuthorService } from '../services/author.service';
import { Author } from '../models/author.model';
import { InfiniteScrollService, AlphabeticalSeparator } from '../services/infinite-scroll.service';
import { InfiniteScrollDirective } from '../directives/infinite-scroll.directive';

@Component({
  selector: 'app-author-list',
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
    MatRippleModule,
    MatBadgeModule,
    InfiniteScrollDirective
  ],
  template: `
    <div class="plex-library" appInfiniteScroll (scrolled)="onScroll()" [disabled]="scrollState.loading()">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <iconify-icon class="title-icon" icon="ph:users-three-thin"></iconify-icon>
            Authors Library
            @if (scrollState.items().length > 0) {
              <span class="author-count">{{ getAuthorCount() }} authors</span>
            }
          </h1>
          <p class="library-subtitle">Discover and explore your favorite authors</p>
        </div>
        
      </div>
      
      @if (scrollState.loading() && scrollState.items().length === 0) {
        <div class="loading-section">
          <div class="loading-content">
            <mat-spinner diameter="60" color="accent"></mat-spinner>
            <h3>Loading authors...</h3>
            <p>Gathering your authors from the digital shelves</p>
          </div>
        </div>
      } @else {
        @if (scrollState.isEmpty()) {
          <div class="empty-library">
            <div class="empty-content">
              <mat-icon class="empty-icon">people</mat-icon>
              <h2>No authors found</h2>
              <p>No authors found in your collection. Start building your digital library by scanning your book directory.</p>
              <button mat-raised-button color="accent" routerLink="/library" class="cta-button">
                <mat-icon>add</mat-icon>
                Manage Library
              </button>
            </div>
          </div>
        } @else {
          <div class="authors-grid">
            @for (item of scrollState.items(); track trackByFn($index, item)) {
              @if (infiniteScrollService.isSeparator(item)) {
                <div class="alphabetical-separator">
                  <div class="separator-line"></div>
                  <span class="separator-letter">{{ item.letter }}</span>
                  <div class="separator-line"></div>
                </div>
              } @else {
                <div class="author-card" matRipple [routerLink]="['/authors', item.id]">
                  <div class="card-container">
                    <div class="author-photo">
                      @if (item.metadata?.['imageUrl']) {
                        <img [src]="item.metadata!['imageUrl']" 
                             [alt]="item.name + ' photo'"
                             class="photo-image"
                             (error)="onImageError($event)">
                      } @else {
                        <div class="photo-placeholder">
                          <mat-icon>person</mat-icon>
                          <span class="name-text">{{ getInitials(item.name) }}</span>
                        </div>
                      }
                      <div class="photo-overlay">
                        <mat-icon class="view-icon">visibility</mat-icon>
                      </div>
                    </div>
                    
                    <div class="author-info">
                      <h3 class="author-name" [title]="item.name">{{ item.name }}</h3>
                      @if (item.bio?.['en']) {
                        <p class="author-bio">{{ getShortBio(item.bio!['en']) }}</p>
                      }
                      @if (item.birthDate || item.deathDate) {
                        <p class="author-dates">
                          {{ formatDates(item.birthDate, item.deathDate) }}
                        </p>
                      }
                    </div>
                  </div>
                  
                  
                </div>
              }
            }
          </div>
          
          <!-- Loading indicator for more items -->
          @if (scrollState.loading() && scrollState.items().length > 0) {
            <div class="load-more-container">
              <mat-spinner diameter="30"></mat-spinner>
              <p>Loading more authors...</p>
            </div>
          }
          
          <!-- Error indicator for loading more -->
          @if (scrollState.error() && scrollState.items().length > 0) {
            <div class="load-more-error">
              <p>{{ scrollState.error() }}</p>
              <button mat-button color="primary" (click)="scrollState.loadMore()">
                Try Again
              </button>
            </div>
          }
          
          <!-- End of list indicator -->
          @if (!scrollState.hasMore() && scrollState.items().length > 0) {
            <div class="end-of-list">
              <p>You've reached the end of the author list</p>
            </div>
          }
        }
      }
    </div>
  `,
  styles: [`
    .plex-library {
      padding: 0;
      background: transparent;
      min-height: 100vh;
      color: #ffffff;
    }

    .library-header {
      background: transparent;
      padding: 24px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      /* Remove separator line and gap */
      /* border-bottom: 1px solid #333; */
      margin-bottom: 0;
    }

    .header-content {
      flex: 1;
    }

    .library-title {
      font-size: 20px;
      font-weight: 600;
      margin: 0;
      color: #ffffff;
      display: flex;
      align-items: center;
      gap: 12px;
      letter-spacing: -0.5px;
    }

  .title-icon { font-size: 32px; width: 32px; height: 32px; color: #e5a00d; margin-right: 12px; }

    .author-count {
      font-size: 1.1rem;
      opacity: 0.7;
      font-weight: 400;
      margin-left: 16px;
    }

  .library-subtitle { font-size: 0.95rem; margin: 4px 0 0 0; opacity: 0.9; color: #888; }

    .header-actions {
      display: flex;
      gap: 16px;
      align-items: center;
    }

    .fab-search {
      background: linear-gradient(135deg, #2196f3 0%, #ff4081 100%) !important;
      color: white !important;
      box-shadow: 0 8px 32px rgba(33, 150, 243, 0.3) !important;
      transition: all 0.3s ease !important;
    }

    .fab-search:hover {
      transform: translateY(-2px) !important;
      box-shadow: 0 12px 40px rgba(33, 150, 243, 0.4) !important;
    }

    .loading-section {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 60vh;
      text-align: center;
    }

    .loading-content h3 {
      margin: 24px 0 8px 0;
      font-size: 1.5rem;
      font-weight: 400;
    }

    .loading-content p {
      margin: 0;
      opacity: 0.7;
      font-size: 1rem;
    }

    .empty-library {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 60vh;
      text-align: center;
    }

    .empty-content {
      max-width: 500px;
      padding: 48px 24px;
    }

    .empty-icon {
      font-size: 6rem;
      color: #555;
      margin-bottom: 24px;
    }

    .empty-content h2 {
      font-size: 2rem;
      font-weight: 300;
      margin: 0 0 16px 0;
      color: #ffffff;
    }

    .empty-content p {
      font-size: 1.1rem;
      line-height: 1.6;
      opacity: 0.8;
      margin: 0 0 32px 0;
    }

    .cta-button {
      background: linear-gradient(135deg, #2196f3 0%, #ff4081 100%) !important;
      color: white !important;
      padding: 12px 32px !important;
      font-size: 1.1rem !important;
    }

    .authors-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 24px;
      padding: 32px;
      max-width: 1600px;
      margin: 0 auto;
    }

    .alphabetical-separator {
      grid-column: 1 / -1;
      display: flex;
      align-items: center;
      gap: 20px;
      margin: 30px 0 20px 0;
      padding: 0 20px;
    }

    .separator-line {
      flex: 1;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    }

    .separator-letter {
      font-size: 1.5rem;
      font-weight: 600;
      color: #2196f3;
      padding: 8px 16px;
      background: rgba(33, 150, 243, 0.1);
      border-radius: 25px;
      border: 2px solid rgba(33, 150, 243, 0.3);
      min-width: 50px;
      text-align: center;
    }

    .author-card {
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.04) 100%);
      border: 1px solid rgba(255, 255, 255, 0.12);
      border-radius: 16px;
      overflow: hidden;
      transition: all 0.3s ease;
      cursor: pointer;
      position: relative;
      backdrop-filter: blur(10px);
    }

    .author-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
      border-color: rgba(33, 150, 243, 0.5);
    }

    .card-container {
      padding: 20px;
    }

    .author-photo {
      position: relative;
      width: 100%;
      height: 200px;
      border-radius: 12px;
      overflow: hidden;
      margin-bottom: 16px;
      background: linear-gradient(135deg, #333 0%, #555 100%);
    }

    .photo-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .author-card:hover .photo-image {
      transform: scale(1.05);
    }

    .photo-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #888;
      background: linear-gradient(135deg, #2a2a2a 0%, #1e1e1e 100%);
    }

    .photo-placeholder mat-icon {
      font-size: 4rem;
      margin-bottom: 8px;
    }

    .name-text {
      font-size: 1.2rem;
      font-weight: 600;
    }

    .photo-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    .author-card:hover .photo-overlay {
      opacity: 1;
    }

    .view-icon {
      color: white;
      font-size: 2rem;
    }

    .author-info {
      text-align: center;
    }

    .author-name {
      font-size: 1.3rem;
      font-weight: 500;
      margin: 0 0 8px 0;
      color: #ffffff;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .author-bio {
      font-size: 0.9rem;
      line-height: 1.4;
      margin: 0 0 8px 0;
      opacity: 0.8;
      color: #e3f2fd;
      height: 2.8em;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .author-dates {
      font-size: 0.85rem;
      margin: 0;
  opacity: 0.8;
  color: #e3f2fd;
    }

    .author-actions {
      display: flex;
      justify-content: center;
      gap: 8px;
      padding: 16px 20px;
      background: rgba(0, 0, 0, 0.2);
      border-top: 1px solid rgba(255, 255, 255, 0.08);
    }

    .action-button {
      color: #ffffff !important;
      transition: all 0.3s ease !important;
    }

    .action-button:hover {
      background: rgba(33, 150, 243, 0.2) !important;
      color: #2196f3 !important;
    }

    .load-more-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 40px 20px;
      gap: 16px;
    }

    .load-more-container p {
      margin: 0;
      opacity: 0.7;
    }

    .load-more-error {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 40px 20px;
      gap: 16px;
    }

    .load-more-error p {
      margin: 0;
      color: #f44336;
    }

    .end-of-list {
      display: flex;
      justify-content: center;
      padding: 40px 20px;
      opacity: 0.6;
    }

    .end-of-list p {
      margin: 0;
      font-style: italic;
    }

    /* Responsive design */
    @media (max-width: 768px) {
      .library-header {
        padding: 24px 16px;
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }

      .library-title {
        font-size: 2rem !important;
      }

      .authors-grid {
        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
        gap: 16px;
        padding: 16px;
      }
    }

    @media (max-width: 480px) {
      .authors-grid {
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 12px;
        padding: 12px;
      }

      .library-title {
        font-size: 1.75rem !important;
      }

      .fab-search {
        width: 48px !important;
        height: 48px !important;
        min-width: 48px !important;
      }
    }
  `]
})
export class AuthorListComponent implements OnInit {
  scrollState;

  constructor(
    private authorService: AuthorService,
    private snackBar: MatSnackBar,
    protected infiniteScrollService: InfiniteScrollService
  ) {
    // Initialize infinite scroll state with alphabetical separators
    this.scrollState = this.infiniteScrollService.createInfiniteScrollState(
      (cursor, limit) => this.authorService.getAllAuthors(cursor, limit),
      {
        limit: 20,
        enableAlphabeticalSeparators: true,
        sortProperty: 'sortName'
      }
    );
  }

  ngOnInit() {
    // Initialization is handled by the infinite scroll service
  }

  onScroll() {
    this.scrollState.loadMore();
  }

  getAuthorCount(): number {
    return this.scrollState.items().filter(item => !this.infiniteScrollService.isSeparator(item)).length;
  }

  trackByFn(index: number, item: Author | AlphabeticalSeparator): string {
    return this.infiniteScrollService.isSeparator(item) ? item.id : item.id;
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

  getShortBio(bio: string): string {
    if (!bio) return '';
    const maxLength = 120;
    return bio.length > maxLength ? bio.substring(0, maxLength) + '...' : bio;
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

  openAuthorDetails(event: Event, authorId: string) {
    event.stopPropagation();
    // Router navigation will be handled by the template
  }
  
  openWebsite(event: Event, url: string) {
    event.stopPropagation();
    window.open(url, '_blank');
  }
}