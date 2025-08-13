import { Component, OnInit, signal } from '@angular/core';
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
import { Author, AuthorPageResponse } from '../models/author.model';

@Component({
  selector: 'app-author-list',
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
    MatRippleModule,
    MatBadgeModule
  ],
  template: `
    <div class="plex-library">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <mat-icon class="title-icon">people</mat-icon>
            Authors Library
            @if (authors().length > 0) {
              <span class="author-count">{{ authors().length }} authors</span>
            }
          </h1>
          <p class="library-subtitle">Discover and explore your favorite authors</p>
        </div>
        <div class="header-actions">
          <button mat-fab color="accent" routerLink="/search" class="fab-search">
            <mat-icon>search</mat-icon>
          </button>
        </div>
      </div>
      
      @if (loading()) {
        <div class="loading-section">
          <div class="loading-content">
            <mat-spinner diameter="60" color="accent"></mat-spinner>
            <h3>Loading authors...</h3>
            <p>Gathering your authors from the digital shelves</p>
          </div>
        </div>
      } @else {
        @if (authors().length === 0) {
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
            @for (author of authors(); track author.id) {
              <div class="author-card" matRipple [routerLink]="['/authors', author.id]">
                <div class="card-container">
                  <div class="author-photo">
                    @if (author.metadata?.['imageUrl']) {
                      <img [src]="author.metadata!['imageUrl']" 
                           [alt]="author.name + ' photo'"
                           class="photo-image"
                           (error)="onImageError($event)">
                    } @else {
                      <div class="photo-placeholder">
                        <mat-icon>person</mat-icon>
                        <span class="name-text">{{ getInitials(author.name) }}</span>
                      </div>
                    }
                    <div class="photo-overlay">
                      <mat-icon class="view-icon">visibility</mat-icon>
                    </div>
                  </div>
                  
                  <div class="author-info">
                    <h3 class="author-name" [title]="author.name">{{ author.name }}</h3>
                    @if (author.bio?.['en']) {
                      <p class="author-bio">{{ getShortBio(author.bio!['en']) }}</p>
                    }
                    @if (author.birthDate || author.deathDate) {
                      <p class="author-dates">
                        {{ formatDates(author.birthDate, author.deathDate) }}
                      </p>
                    }
                  </div>
                </div>
                
                <div class="author-actions">
                  <button mat-icon-button class="action-button" (click)="openAuthorDetails($event, author.id)">
                    <mat-icon>info</mat-icon>
                  </button>
                  @if (author.websiteUrl) {
                    <button mat-icon-button class="action-button" (click)="openWebsite($event, author.websiteUrl!)">
                      <mat-icon>language</mat-icon>
                    </button>
                  }
                </div>
              </div>
            }
          </div>
          
          <!-- Pagination controls -->
          @if (hasNext() || hasPrevious()) {
            <div class="pagination-controls">
              <button mat-button 
                      [disabled]="!hasPrevious()" 
                      (click)="loadPreviousPage()"
                      class="pagination-button">
                <mat-icon>chevron_left</mat-icon>
                Previous
              </button>
              
              <span class="page-info">
                Showing {{ authors().length }} authors
                @if (totalCount() !== undefined) {
                  of {{ totalCount() }}
                }
              </span>
              
              <button mat-button 
                      [disabled]="!hasNext()" 
                      (click)="loadNextPage()"
                      class="pagination-button">
                Next
                <mat-icon>chevron_right</mat-icon>
              </button>
            </div>
          }
        }
      }
    </div>
  `,
  styles: [`
    .plex-library {
      padding: 0;
      background: linear-gradient(135deg, #1e1e1e 0%, #2a2a2a 100%);
      min-height: 100vh;
      color: #ffffff;
    }

    .library-header {
      background: linear-gradient(135deg, rgba(25, 118, 210, 0.1) 0%, rgba(255, 64, 129, 0.1) 100%);
      backdrop-filter: blur(10px);
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      padding: 32px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      position: sticky;
      top: 0;
      z-index: 10;
    }

    .header-content h1.library-title {
      font-size: 2.5rem;
      font-weight: 700;
      margin: 0 0 8px 0;
      display: flex;
      align-items: center;
      gap: 16px;
      background: linear-gradient(45deg, #1976d2, #ff4081);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .title-icon {
      font-size: 2.5rem !important;
      width: 2.5rem !important;
      height: 2.5rem !important;
      background: linear-gradient(45deg, #1976d2, #ff4081);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .author-count {
      font-size: 1rem;
      font-weight: 400;
      opacity: 0.8;
      background: rgba(255, 255, 255, 0.1);
      padding: 4px 12px;
      border-radius: 16px;
    }

    .library-subtitle {
      font-size: 1.1rem;
      margin: 0;
      opacity: 0.8;
    }

    .fab-search {
      width: 56px !important;
      height: 56px !important;
      background: linear-gradient(45deg, #1976d2, #ff4081) !important;
    }

    .loading-section, .empty-library {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 60vh;
      padding: 64px 32px;
    }

    .loading-content, .empty-content {
      text-align: center;
      max-width: 400px;
    }

    .loading-content h3, .empty-content h2 {
      margin: 24px 0 16px 0;
      font-size: 1.5rem;
      font-weight: 600;
    }

    .empty-icon {
      font-size: 4rem !important;
      width: 4rem !important;
      height: 4rem !important;
      opacity: 0.5;
      margin-bottom: 16px;
    }

    .cta-button {
      margin-top: 24px;
      background: linear-gradient(45deg, #1976d2, #ff4081) !important;
      color: white !important;
    }

    .authors-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 24px;
      padding: 32px;
    }

    .author-card {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 16px;
      overflow: hidden;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border: 1px solid rgba(255, 255, 255, 0.1);
      cursor: pointer;
      position: relative;
    }

    .author-card:hover {
      transform: translateY(-8px) scale(1.02);
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
      background: rgba(255, 255, 255, 0.08);
      border-color: rgba(255, 255, 255, 0.2);
    }

    .card-container {
      padding: 24px;
      min-height: 200px;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
    }

    .author-photo {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      overflow: hidden;
      margin-bottom: 16px;
      position: relative;
      background: linear-gradient(45deg, #1976d2, #ff4081);
      padding: 2px;
    }

    .photo-image {
      width: calc(100% - 4px);
      height: calc(100% - 4px);
      border-radius: 50%;
      object-fit: cover;
      background: #2a2a2a;
    }

    .photo-placeholder {
      width: calc(100% - 4px);
      height: calc(100% - 4px);
      border-radius: 50%;
      background: #2a2a2a;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: rgba(255, 255, 255, 0.7);
    }

    .photo-placeholder mat-icon {
      font-size: 2rem;
      width: 2rem;
      height: 2rem;
      margin-bottom: 4px;
    }

    .name-text {
      font-size: 0.75rem;
      font-weight: 600;
    }

    .photo-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s ease;
      border-radius: 50%;
    }

    .author-card:hover .photo-overlay {
      opacity: 1;
    }

    .view-icon {
      color: white;
      font-size: 1.5rem !important;
    }

    .author-info {
      flex: 1;
    }

    .author-name {
      font-size: 1.25rem;
      font-weight: 600;
      margin: 0 0 8px 0;
      line-height: 1.4;
      color: #ffffff;
    }

    .author-bio {
      font-size: 0.9rem;
      line-height: 1.5;
      opacity: 0.8;
      margin: 0 0 8px 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .author-dates {
      font-size: 0.85rem;
      opacity: 0.6;
      margin: 0;
      font-style: italic;
    }

    .author-actions {
      position: absolute;
      top: 8px;
      right: 8px;
      display: flex;
      gap: 4px;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    .author-card:hover .author-actions {
      opacity: 1;
    }

    .action-button {
      background: rgba(255, 255, 255, 0.1) !important;
      color: white !important;
      width: 32px !important;
      height: 32px !important;
      line-height: 32px !important;
    }

    .action-button mat-icon {
      font-size: 1rem !important;
    }

    .pagination-controls {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 24px;
      padding: 32px;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
    }

    .pagination-button {
      color: #ffffff !important;
      font-weight: 500 !important;
    }

    .page-info {
      opacity: 0.8;
      font-size: 0.9rem;
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

      .pagination-controls {
        padding: 24px 16px;
        flex-direction: column;
        gap: 16px;
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
  authors = signal<Author[]>([]);
  loading = signal(true);
  nextCursor = signal<string | undefined>(undefined);
  previousCursor = signal<string | undefined>(undefined);
  hasNext = signal(false);
  hasPrevious = signal(false);
  totalCount = signal<number | undefined>(undefined);
  limit = signal(20);

  constructor(
    private authorService: AuthorService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadAuthors();
  }

  loadAuthors(cursor?: string) {
    this.loading.set(true);
    this.authorService.getAllAuthors(cursor, this.limit()).subscribe({
      next: (response: AuthorPageResponse) => {
        this.authors.set(response.content);
        this.nextCursor.set(response.nextCursor);
        this.previousCursor.set(response.previousCursor);
        this.hasNext.set(response.hasNext || false);
        this.hasPrevious.set(response.hasPrevious || false);
        this.totalCount.set(response.totalCount);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading authors:', error);
        this.snackBar.open('Failed to load authors', 'Close', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  loadNextPage() {
    if (this.hasNext() && this.nextCursor()) {
      this.loadAuthors(this.nextCursor());
    }
  }

  loadPreviousPage() {
    if (this.hasPrevious() && this.previousCursor()) {
      this.loadAuthors(this.previousCursor());
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