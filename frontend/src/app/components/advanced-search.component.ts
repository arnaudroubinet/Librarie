import { Component, OnDestroy, signal, computed, effect, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormControl } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { trigger, transition, style, animate } from '@angular/animations';
import { MATERIAL_MODULES } from '../shared/materials';
import { BookService } from '../services/book.service';
import { SearchService } from '../services/search.service';
import { Book } from '../models/book.model';
import { Series } from '../models/series.model';
import { Author } from '../models/author.model';
import { UnifiedSearchResult } from '../models/search.model';

@Component({
  selector: 'app-advanced-search',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    ...MATERIAL_MODULES
  ],
  animations: [
    trigger('slideDown', [
      transition(':enter', [
        style({ height: 0, opacity: 0, overflow: 'hidden' }),
        animate('300ms ease-out', style({ height: '*', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ height: 0, opacity: 0, overflow: 'hidden' }))
      ])
    ])
  ],
  template: `
<div class="motspassants-library search-page">
  <div class="library-header">
    <div class="header-content">
      <p class="library-subtitle">Recherchez des livres, séries et auteurs</p>
    </div>
  </div>

  <!-- Quick Search Box -->
  <div class="search-container">
    <div class="search-box">
      <mat-icon class="search-icon">search</mat-icon>
      <input
        type="text"
        placeholder="Recherche rapide..."
        [formControl]="quickSearchControl"
        (keyup.enter)="performQuickSearch()"
      />
      @if (quickSearchControl.value) {
        <button class="clear-btn" (click)="clearQuickSearch()" aria-label="Effacer">
          <mat-icon>close</mat-icon>
        </button>
      }
    </div>

    @if (showSuggestions() && recentSearches().length > 0) {
      <div class="suggestions-panel">
        @for (search of recentSearches(); track search) {
          <div class="suggestion-item" (click)="applySuggestion(search)">
            <mat-icon>history</mat-icon>
            <span>{{ search }}</span>
          </div>
        }
      </div>
    }

    <!-- Filter Toggle -->
    <div class="filter-toggle">
      <button class="toggle-btn" (click)="toggleAdvancedFilters()">
        <mat-icon>{{ showAdvanced() ? 'expand_less' : 'tune' }}</mat-icon>
        <span>{{ showAdvanced() ? 'Masquer' : 'Filtres avancés' }}</span>
      </button>
    </div>
  </div>

  <!-- Advanced Filters -->
  @if (showAdvanced()) {
    <div class="advanced-filters" @slideDown>
      <form [formGroup]="searchForm" class="filters-grid">
        <div class="filter-field">
          <label>Titre</label>
          <mat-form-field appearance="outline">
            <input matInput formControlName="title" placeholder="Titre">
          </mat-form-field>
        </div>

        <div class="filter-field">
          <label>Auteur</label>
          <mat-form-field appearance="outline">
            <input matInput formControlName="author" placeholder="Auteur">
          </mat-form-field>
        </div>

        <div class="filter-field">
          <label>Série</label>
          <mat-form-field appearance="outline">
            <input matInput formControlName="series" placeholder="Série">
          </mat-form-field>
        </div>

        <div class="filter-field">
          <label>ISBN</label>
          <mat-form-field appearance="outline">
            <input matInput formControlName="isbn" placeholder="ISBN">
          </mat-form-field>
        </div>

        <div class="filter-field">
          <label>Année min</label>
          <mat-form-field appearance="outline">
            <input matInput type="number" formControlName="yearMin" placeholder="AAAA">
          </mat-form-field>
        </div>

        <div class="filter-field">
          <label>Année max</label>
          <mat-form-field appearance="outline">
            <input matInput type="number" formControlName="yearMax" placeholder="AAAA">
          </mat-form-field>
        </div>
      </form>

      <div class="filter-actions">
        <button mat-stroked-button (click)="clearFilters()">
          <mat-icon>clear</mat-icon>
          Effacer
        </button>
        <button mat-flat-button color="primary" (click)="applyFilters()">
          <mat-icon>search</mat-icon>
          Rechercher
        </button>
      </div>
    </div>
  }

  <!-- Loading State -->
  @if (isLoading()) {
    <div class="empty-state">
      <mat-spinner diameter="48"></mat-spinner>
      <p>Recherche en cours...</p>
    </div>
  }

  <!-- Results -->
  @if (!isLoading() && searchResults()) {
    <div class="search-results">
      <!-- Books -->
      @if (searchResults()!.books.length > 0) {
        <div class="section-header">
          <h2 class="section-title">
            <mat-icon>menu_book</mat-icon>
            Livres
            <span class="count-badge">{{ searchResults()!.books.length }}</span>
          </h2>
        </div>
        <div class="books-grid">
          @for (book of searchResults()!.books; track book.id) {
            <div class="book-card" (click)="viewBook(book.id)">
              <div class="book-cover">
                @if (book.coverUrl) {
                  <img [src]="book.coverUrl" [alt]="book.title" class="cover-image">
                } @else {
                  <div class="cover-placeholder">
                    <mat-icon>menu_book</mat-icon>
                    <span class="book-title-text">{{ book.title }}</span>
                  </div>
                }
                <div class="book-overlay">
                  <div class="book-actions">
                    <button mat-mini-fab class="action-btn" aria-label="Voir le livre">
                      <mat-icon>visibility</mat-icon>
                    </button>
                  </div>
                </div>
              </div>
              <div class="book-info">
                <h3 class="book-title">{{ book.title }}</h3>
                @if (book.contributorsDetailed?.['author']?.length) {
                  <p class="book-author">{{ book.contributorsDetailed!['author'][0].name }}</p>
                }
                @if (book.series) {
                  <p class="book-series">{{ book.series }}</p>
                }
                @if (book.publicationYear) {
                  <p class="book-date">{{ book.publicationYear }}</p>
                }
              </div>
            </div>
          }
        </div>
      }

      <!-- Series -->
      @if (searchResults()!.series.length > 0) {
        <div class="section-header">
          <h2 class="section-title">
            <mat-icon>library_books</mat-icon>
            Séries
            <span class="count-badge">{{ searchResults()!.series.length }}</span>
          </h2>
        </div>
        <div class="series-grid">
          @for (series of searchResults()!.series; track series.id) {
            <div class="series-card" (click)="viewSeries(series.id)">
              <div class="series-icon">
                <mat-icon>library_books</mat-icon>
              </div>
              <h3>{{ series.name }}</h3>
              @if (series.description) {
                <p>{{ series.description }}</p>
              }
              <p>{{ series.bookCount }} livre(s)</p>
            </div>
          }
        </div>
      }

      <!-- Authors -->
      @if (searchResults()!.authors.length > 0) {
        <div class="section-header">
          <h2 class="section-title">
            <mat-icon>person</mat-icon>
            Auteurs
            <span class="count-badge">{{ searchResults()!.authors.length }}</span>
          </h2>
        </div>
        <div class="authors-grid">
          @for (author of searchResults()!.authors; track author.id) {
            <div class="author-card" (click)="viewAuthor(author.id)">
              <div class="author-avatar">
                <mat-icon>person</mat-icon>
              </div>
              <h3>{{ author.name }}</h3>
              @if (author.birthDate || author.deathDate) {
                <p>{{ formatYear(author.birthDate) || '?' }} - {{ formatYear(author.deathDate) || '?' }}</p>
              }
            </div>
          }
        </div>
      }

      <!-- Empty State -->
      @if (searchResults()!.books.length === 0 && 
           searchResults()!.series.length === 0 && 
           searchResults()!.authors.length === 0) {
        <div class="empty-state">
          <div class="empty-icon">
            <mat-icon>search_off</mat-icon>
          </div>
          <h3>Aucun résultat</h3>
          <p>Essayez d'ajuster vos critères de recherche</p>
        </div>
      }

      <!-- Pagination -->
      @if (hasMore()) {
        <div class="pagination">
          @if (currentCursor()) {
            <button (click)="previousPage()" [disabled]="cursors().length <= 1">
              <mat-icon>chevron_left</mat-icon>
              Précédent
            </button>
          }
          <button (click)="nextPage()">
            Suivant
            <mat-icon>chevron_right</mat-icon>
          </button>
        </div>
      }
    </div>
  }
</div>
  `,
  styleUrls: ['./advanced-search.component.css']
})
export class AdvancedSearchComponent implements OnDestroy {
  quickSearchControl = new FormControl('');
  searchForm: FormGroup;
  
  // Signals
  isLoading = signal(false);
  showAdvanced = signal(false);
  searchResults = signal<UnifiedSearchResult | null>(null);
  recentSearches = signal<string[]>([]);
  showSuggestions = signal(false);
  hasMore = signal(false);
  currentCursor = signal<string | null>(null);
  cursors = signal<string[]>([]);

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private bookService: BookService,
    private searchService: SearchService,
    private snackBar: MatSnackBar
  ) {
    this.searchForm = this.fb.group({
      title: [''],
      author: [''],
      series: [''],
      isbn: [''],
      yearMin: [null],
      yearMax: [null]
    });

    // Load recent searches from localStorage
    this.loadRecentSearches();

    // Show suggestions when input is focused
    effect(() => {
      const value = this.quickSearchControl.value;
      this.showSuggestions.set(value === '' || value === null);
    });
  }

  ngOnDestroy(): void {}

  loadRecentSearches(): void {
    try {
      const searches = localStorage.getItem('recentSearches');
      if (searches) {
        this.recentSearches.set(JSON.parse(searches));
      }
    } catch (error) {
      console.error('Error loading recent searches:', error);
    }
  }

  saveRecentSearch(query: string): void {
    const searches = this.recentSearches();
    const filtered = searches.filter(s => s !== query);
    const updated = [query, ...filtered].slice(0, 5);
    this.recentSearches.set(updated);
    try {
      localStorage.setItem('recentSearches', JSON.stringify(updated));
    } catch (error) {
      console.error('Error saving recent search:', error);
    }
  }

  toggleAdvancedFilters(): void {
    this.showAdvanced.update(v => !v);
  }

  clearQuickSearch(): void {
    this.quickSearchControl.setValue('');
    this.searchResults.set(null);
  }

  applySuggestion(search: string): void {
    this.quickSearchControl.setValue(search);
    this.performQuickSearch();
  }

  performQuickSearch(): void {
    const query = this.quickSearchControl.value?.trim();
    if (!query) {
      this.snackBar.open('Veuillez entrer un terme de recherche', 'Fermer', { duration: 3000 });
      return;
    }

    this.isLoading.set(true);
    this.saveRecentSearch(query);
    this.showSuggestions.set(false);

    this.searchService.unifiedSearch(query).subscribe({
      next: (results) => {
        this.searchResults.set(results);
        this.isLoading.set(false);
        const total = results.books.length + results.series.length + results.authors.length;
        this.snackBar.open(`${total} résultat(s) trouvé(s)`, 'Fermer', { duration: 3000 });
      },
      error: (err) => {
        console.error('Search error:', err);
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors de la recherche', 'Fermer', { duration: 3000 });
      }
    });
  }

  applyFilters(): void {
    const formValue = this.searchForm.value;
    const hasFilters = Object.values(formValue).some(v => v !== null && v !== '');

    if (!hasFilters) {
      this.snackBar.open('Veuillez renseigner au moins un critère', 'Fermer', { duration: 3000 });
      return;
    }

    this.isLoading.set(true);

    const criteria: any = {};
    if (formValue.title) criteria.title = formValue.title;
    if (formValue.author) criteria.authors = [formValue.author];
    if (formValue.series) criteria.series = formValue.series;
    if (formValue.isbn) criteria.isbn = formValue.isbn;
    if (formValue.yearMin) criteria.publicationYearMin = formValue.yearMin;
    if (formValue.yearMax) criteria.publicationYearMax = formValue.yearMax;

    this.bookService.searchBooks(criteria).subscribe({
      next: (response) => {
        const results: UnifiedSearchResult = {
          books: response.content,
          series: [],
          authors: []
        };
        this.searchResults.set(results);
        this.hasMore.set(response.hasNext || false);
        this.currentCursor.set(response.nextCursor || null);
        this.isLoading.set(false);
        this.snackBar.open(`${response.content.length} livre(s) trouvé(s)`, 'Fermer', { duration: 3000 });
      },
      error: (err) => {
        console.error('Search error:', err);
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors de la recherche', 'Fermer', { duration: 3000 });
      }
    });
  }

  clearFilters(): void {
    this.searchForm.reset();
    this.searchResults.set(null);
    this.hasMore.set(false);
    this.currentCursor.set(null);
    this.cursors.set([]);
  }

  nextPage(): void {
    const cursor = this.currentCursor();
    if (!cursor) return;

    const formValue = this.searchForm.value;
    const criteria: any = {};
    if (formValue.title) criteria.title = formValue.title;
    if (formValue.author) criteria.authors = [formValue.author];
    if (formValue.series) criteria.series = formValue.series;
    if (formValue.isbn) criteria.isbn = formValue.isbn;
    if (formValue.yearMin) criteria.publicationYearMin = formValue.yearMin;
    if (formValue.yearMax) criteria.publicationYearMax = formValue.yearMax;

    this.isLoading.set(true);
    this.bookService.searchBooks(criteria, cursor).subscribe({
      next: (response) => {
        const results: UnifiedSearchResult = {
          books: response.content,
          series: [],
          authors: []
        };
        this.searchResults.set(results);
        this.hasMore.set(response.hasNext || false);
        this.cursors.update(c => [...c, cursor]);
        this.currentCursor.set(response.nextCursor || null);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Pagination error:', err);
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors du chargement', 'Fermer', { duration: 3000 });
      }
    });
  }

  previousPage(): void {
    const allCursors = this.cursors();
    if (allCursors.length === 0) return;

    const previousCursor = allCursors[allCursors.length - 2] || null;
    this.cursors.update(c => c.slice(0, -1));

    const formValue = this.searchForm.value;
    const criteria: any = {};
    if (formValue.title) criteria.title = formValue.title;
    if (formValue.author) criteria.authors = [formValue.author];
    if (formValue.series) criteria.series = formValue.series;
    if (formValue.isbn) criteria.isbn = formValue.isbn;
    if (formValue.yearMin) criteria.publicationYearMin = formValue.yearMin;
    if (formValue.yearMax) criteria.publicationYearMax = formValue.yearMax;

    this.isLoading.set(true);
    this.bookService.searchBooks(criteria, previousCursor || undefined).subscribe({
      next: (response) => {
        const results: UnifiedSearchResult = {
          books: response.content,
          series: [],
          authors: []
        };
        this.searchResults.set(results);
        this.hasMore.set(response.hasNext || false);
        this.currentCursor.set(response.nextCursor || null);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Pagination error:', err);
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors du chargement', 'Fermer', { duration: 3000 });
      }
    });
  }

  viewBook(id: string): void {
    this.router.navigate(['/book', id]);
  }

  viewSeries(id: string): void {
    this.router.navigate(['/series', id]);
  }

  viewAuthor(id: string): void {
    this.router.navigate(['/author', id]);
  }

  formatYear(dateStr: string | undefined): string | null {
    if (!dateStr) return null;
    const year = new Date(dateStr).getFullYear();
    return isNaN(year) ? null : year.toString();
  }
}
