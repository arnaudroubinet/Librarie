import { 
  Component, 
  OnInit, 
  OnDestroy, 
  ElementRef, 
  ViewChild, 
  signal, 
  computed,
  inject,
  ChangeDetectionStrategy
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MATERIAL_MODULES } from '../shared/materials';
import { MatSelectChange } from '@angular/material/select';
import { environment } from '../../environments/environment';
import { BookService } from '../services/book.service';
import { ReadingProgressService, ReadingProgressData, ReadingProgressRequest } from '../services/reading-progress.service';
import { Book } from '../models/book.model';

// Import epub.js - we'll declare it for TypeScript
declare const ePub: any;

@Component({
  selector: 'app-ebook-reader',
  imports: [
    CommonModule,
    ...MATERIAL_MODULES
  ],
  template: `
    <div class="reader-container">
      <!-- Top toolbar -->
      <mat-toolbar class="reader-toolbar">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>
        
        <span class="book-title">{{ book()?.title || 'Loading...' }}</span>
        
        <span class="spacer"></span>
        
        <!-- Font size selector -->
        <mat-select 
          [(value)]="fontSize" 
          (selectionChange)="onFontSizeChange($event)"
          class="font-size-select">
          <mat-option value="14">Small</mat-option>
          <mat-option value="16">Medium</mat-option>
          <mat-option value="18">Large</mat-option>
          <mat-option value="20">Extra Large</mat-option>
        </mat-select>
        
        <!-- Theme toggle -->
        <button mat-icon-button (click)="toggleTheme()">
          <mat-icon>{{ isDarkTheme() ? 'light_mode' : 'dark_mode' }}</mat-icon>
        </button>
        
        <!-- Settings menu -->
        <button mat-icon-button>
          <mat-icon>settings</mat-icon>
        </button>
      </mat-toolbar>

      <!-- Reader content area -->
      <div class="reader-content" #readerContainer></div>

      <!-- Bottom controls -->
      <div class="reader-controls">
        <button mat-icon-button (click)="previousPage()" [disabled]="!canGoPrevious()">
          <mat-icon>chevron_left</mat-icon>
        </button>
        
        <div class="progress-section">
          <input 
            type="range"
            class="progress-slider"
            [min]="0" 
            [max]="100" 
            [step]="0.1"
            [value]="currentProgress()?.progress || 0"
            (input)="onProgressSliderChange($event)">
          
          <div class="progress-text">
            @if (currentProgress(); as progress) {
              <span>{{ progress.progress.toFixed(1) }}% 
                @if (progress.currentPage && progress.totalPages) {
                  ({{ progress.currentPage }} / {{ progress.totalPages }})
                }
              </span>
            } @else {
              <span>0%</span>
            }
          </div>
        </div>
        
        <button mat-icon-button (click)="nextPage()" [disabled]="!canGoNext()">
          <mat-icon>chevron_right</mat-icon>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .reader-container {
      display: flex;
      flex-direction: column;
      height: 100vh;
      background: var(--reader-bg, #ffffff);
      color: var(--reader-text, #000000);
    }

    .reader-toolbar {
      flex-shrink: 0;
      background: var(--toolbar-bg, #f5f5f5);
      border-bottom: 1px solid var(--border-color, #e0e0e0);
    }

    .book-title {
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .spacer {
      flex: 1;
    }

    .font-size-select {
      width: 100px;
      margin-right: 8px;
    }

    .reader-content {
      flex: 1;
      overflow: hidden;
      padding: 20px;
      background: var(--reader-bg, #ffffff);
    }

    .reader-controls {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      padding: 12px 16px;
      background: var(--toolbar-bg, #f5f5f5);
      border-top: 1px solid var(--border-color, #e0e0e0);
      gap: 16px;
    }

    .progress-section {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .progress-slider {
      width: 100%;
      height: 4px;
      -webkit-appearance: none;
      appearance: none;
      background: rgba(255, 255, 255, 0.3);
      border-radius: 2px;
      outline: none;
    }
    
    .progress-slider::-webkit-slider-thumb {
      -webkit-appearance: none;
      appearance: none;
      width: 16px;
      height: 16px;
      background: #4fc3f7;
      border-radius: 50%;
      cursor: pointer;
    }
    
    .progress-slider::-moz-range-thumb {
      width: 16px;
      height: 16px;
      background: #4fc3f7;
      border-radius: 50%;
      cursor: pointer;
      border: none;
    }

    .progress-text {
      text-align: center;
      font-size: 12px;
      color: var(--text-secondary, #666);
    }

    /* Dark theme */
    :host(.dark-theme) {
      --reader-bg: #1e1e1e;
      --reader-text: #e0e0e0;
      --toolbar-bg: #2d2d2d;
      --border-color: #404040;
      --text-secondary: #a0a0a0;
    }

    /* Mobile responsive */
    @media (max-width: 768px) {
      .reader-toolbar {
        padding: 0 8px;
      }
      
      .book-title {
        font-size: 14px;
      }
      
      .reader-content {
        padding: 12px;
      }
      
      .reader-controls {
        padding: 8px 12px;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EbookReaderComponent implements OnInit, OnDestroy {
  @ViewChild('readerContainer', { static: true }) readerContainer!: ElementRef;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private bookService = inject(BookService);
  private readingProgressService = inject(ReadingProgressService);
  private snackBar = inject(MatSnackBar);

  // Signals for component state
  book = signal<Book | null>(null);
  currentProgress = signal<ReadingProgressData | null>(null);
  isDarkTheme = signal<boolean>(false);
  isLoading = signal<boolean>(true);
  
  // Computed properties
  canGoPrevious = computed(() => {
    const progress = this.currentProgress();
    return progress ? progress.progress > 0 : false;
  });
  
  canGoNext = computed(() => {
    const progress = this.currentProgress();
    return progress ? progress.progress < 100 : false;
  });

  // epub.js properties
  private epubBook: any = null;
  private rendition: any = null;
  
  // UI properties
  fontSize = 16;
  private baseUrl = environment.production ? '' : 'http://localhost:8080';

  ngOnInit(): void {
    const bookId = this.route.snapshot.paramMap.get('id');
    if (!bookId) {
      this.router.navigate(['/books']);
      return;
    }

    this.loadBook(bookId);
  }

  ngOnDestroy(): void {
    if (this.rendition) {
      this.rendition.destroy();
    }
  }

  private async loadBook(bookId: string): Promise<void> {
    try {
      // Load book metadata
      this.bookService.getBookById(bookId).subscribe({
        next: (book) => {
          this.book.set(book);
          this.loadEpub(bookId);
        },
        error: (error) => {
          console.error('Failed to load book metadata:', error);
          this.snackBar.open('Failed to load book', 'Close', { duration: 3000 });
        }
      });

      // Load reading progress
      this.readingProgressService.getReadingProgress(bookId).subscribe({
        next: (progress) => {
          this.currentProgress.set(progress);
        },
        error: () => {
          // No existing progress, start from beginning
          this.currentProgress.set({
            progress: 0,
            currentPage: 0,
            totalPages: 0,
            isCompleted: false
          });
        }
      });

    } catch (error) {
      console.error('Failed to load book:', error);
      this.snackBar.open('Failed to load book', 'Close', { duration: 3000 });
    }
  }

  private async loadEpub(bookId: string): Promise<void> {
    try {
      // Load epub.js script if not already loaded
      if (typeof ePub === 'undefined') {
        await this.loadEpubScript();
      }

      const bookUrl = `${this.baseUrl}/v1/books/${bookId}/file`;
      
      // Initialize epub.js
      this.epubBook = ePub(bookUrl);
      
      // Render the book
      this.rendition = this.epubBook.renderTo(this.readerContainer.nativeElement, {
        width: '100%',
        height: '100%',
        spread: 'auto'
      });

      // Display the book
      await this.rendition.display();

      // Apply initial settings
      this.applyTheme();
      this.changeFontSize(this.fontSize);

      // Set up event listeners
      this.setupEventListeners();

      // Restore reading position
      this.restoreReadingPosition();

      this.isLoading.set(false);

    } catch (error) {
      console.error('Failed to load EPUB:', error);
      this.snackBar.open('Failed to load EPUB file', 'Close', { duration: 3000 });
      this.isLoading.set(false);
    }
  }

  private loadEpubScript(): Promise<void> {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://cdn.jsdelivr.net/npm/epubjs@0.3/dist/epub.min.js';
      script.onload = () => resolve();
      script.onerror = () => reject(new Error('Failed to load epub.js'));
      document.head.appendChild(script);
    });
  }

  private setupEventListeners(): void {
    // Navigation with keyboard
    document.addEventListener('keydown', (e) => {
      if (e.key === 'ArrowLeft') {
        this.previousPage();
      } else if (e.key === 'ArrowRight') {
        this.nextPage();
      }
    });

    // Track location changes
    this.rendition.on('locationChanged', (location: any) => {
      this.updateProgress(location);
    });
  }

  private updateProgress(location: any): void {
    if (!location || !this.book()) return;

    try {
      const progress = this.epubBook.locations.percentageFromCfi(location.start.cfi) * 100;
      const currentLocation = this.epubBook.locations.locationFromCfi(location.start.cfi);
      
      const progressData: ReadingProgressRequest = {
        progress: Math.round(progress * 10) / 10, // Round to 1 decimal
        currentPage: currentLocation || undefined,
        totalPages: this.epubBook.locations.total || undefined
      };

      // Update local state
      this.currentProgress.set({
        ...progressData,
        isCompleted: progress >= 100,
        lastReadAt: new Date().toISOString()
      });

      // Auto-save progress
      if (this.book()) {
        this.readingProgressService.autoSaveProgress(this.book()!.id, progressData);
      }

    } catch (error) {
      console.warn('Failed to update reading progress:', error);
    }
  }

  private restoreReadingPosition(): void {
    const progress = this.currentProgress();
    if (progress && progress.progress > 0) {
      // Try to restore position based on percentage
      if (this.epubBook.locations.length > 0) {
        const cfi = this.epubBook.locations.cfiFromPercentage(progress.progress / 100);
        if (cfi) {
          this.rendition.display(cfi);
        }
      }
    }
  }

  // Navigation methods
  previousPage(): void {
    if (this.rendition && this.canGoPrevious()) {
      this.rendition.prev();
    }
  }

  nextPage(): void {
    if (this.rendition && this.canGoNext()) {
      this.rendition.next();
    }
  }

  // UI controls
  changeFontSize(size: number): void {
    this.fontSize = size;
    if (this.rendition) {
      this.rendition.themes.fontSize(size + 'px');
    }
  }

  onFontSizeChange(event: MatSelectChange): void {
    this.changeFontSize(event.value);
  }

  toggleTheme(): void {
    this.isDarkTheme.update(dark => !dark);
    this.applyTheme();
  }

  private applyTheme(): void {
    if (!this.rendition) return;

    const isDark = this.isDarkTheme();
    
    if (isDark) {
      this.rendition.themes.register('dark', {
        body: {
          background: '#1e1e1e !important',
          color: '#e0e0e0 !important'
        },
        p: {
          color: '#e0e0e0 !important'
        }
      });
      this.rendition.themes.select('dark');
      document.body.classList.add('dark-theme');
    } else {
      this.rendition.themes.select('default');
      document.body.classList.remove('dark-theme');
    }
  }

  onProgressSliderChange(event: Event): void {
    const value = parseFloat((event.target as HTMLInputElement).value);
    if (this.epubBook && this.epubBook.locations.length > 0) {
      const cfi = this.epubBook.locations.cfiFromPercentage(value / 100);
      if (cfi) {
        this.rendition.display(cfi);
      }
    }
  }

  goBack(): void {
    // Save current progress before leaving
    const progress = this.currentProgress();
    if (progress && this.book()) {
      this.readingProgressService.saveProgress(this.book()!.id, {
        progress: progress.progress,
        currentPage: progress.currentPage,
        totalPages: progress.totalPages
      }).subscribe();
    }

    this.router.navigate(['/books', this.book()?.id]);
  }
}