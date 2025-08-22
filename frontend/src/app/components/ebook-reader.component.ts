import { 
  Component, 
  OnInit, 
  OnDestroy, 
  ElementRef, 
  ViewChild, 
  signal, 
  computed,
  inject,
  ChangeDetectionStrategy,
  HostListener,
  CUSTOM_ELEMENTS_SCHEMA
} from '@angular/core';
import type { Locator as LocatorType } from '@readium/shared';
import { Manifest, Publication, HttpFetcher, Locator as LocatorModel, Link } from '@readium/shared';
import { EpubNavigator, EpubPreferences } from '@readium/navigator';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MATERIAL_MODULES } from '../shared/materials';
import { environment } from '../../environments/environment';
import { BookService } from '../services/book.service';
import { ManifestService, LoadedManifest } from '../services/manifest.service';
import { ReadingProgressService, ReadingProgressData, ReadingProgressRequest } from '../services/reading-progress.service';
import { Book } from '../models/book.model';
import type { RwpmLink } from '../models/rwpm.model';

@Component({
  selector: 'app-ebook-reader',
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    ...MATERIAL_MODULES
  ],
  template: `
    <div class="reader-container">
      <!-- Top toolbar -->
      <mat-toolbar class="reader-toolbar">
        <button mat-icon-button (click)="goBack()" aria-label="Back to book details">
          <iconify-icon icon="material-symbols:arrow-back-rounded"></iconify-icon>
        </button>
        
        <span class="book-title">{{ book()?.title || 'Loading...' }}</span>
        
        <span class="spacer"></span>
        
        <!-- TOC toggle -->
        <button mat-icon-button (click)="toggleToc()" [disabled]="toc().length === 0" aria-label="Open table of contents">
          <iconify-icon icon="material-symbols:toc-rounded"></iconify-icon>
        </button>

        <!-- Reading mode: paginated/scrolled -->
  <button mat-icon-button (click)="toggleFlow()" [disabled]="!usingNavigator()" aria-label="Toggle reading flow">
          <iconify-icon [icon]="isPaginated() ? 'material-symbols:view-day-rounded' : 'material-symbols:view-agenda-rounded'"></iconify-icon>
        </button>

        <!-- Spread toggle: auto/none -->
  <button mat-icon-button (click)="toggleSpread()" [disabled]="!usingNavigator() || !isPaginated()" aria-label="Toggle page spread">
          <iconify-icon [icon]="spreadMode() === 'auto' ? 'material-symbols:view-column-rounded' : 'material-symbols:view-stream-rounded'"></iconify-icon>
        </button>
        
        <!-- Font size selector -->
        <div class="font-size-controls" role="group" aria-label="Font size">
          <button mat-icon-button (click)="decreaseFont()" aria-label="Decrease font size">
            <iconify-icon icon="material-symbols:zoom-out-rounded"></iconify-icon>
          </button>
          <span class="font-size-display">{{ fontSize }}px</span>
          <button mat-icon-button (click)="increaseFont()" aria-label="Increase font size">
            <iconify-icon icon="material-symbols:zoom-in-rounded"></iconify-icon>
          </button>
        </div>

        <!-- Font family toggle: serif/sans -->
        <button mat-icon-button (click)="toggleSerif()" aria-label="Toggle serif font">
          <iconify-icon [icon]="isSerif() ? 'material-symbols:title-rounded' : 'material-symbols:font-download-rounded'"></iconify-icon>
        </button>
        
        <!-- Theme toggle -->
        <button mat-icon-button (click)="cycleTheme()" aria-label="Cycle theme">
          <iconify-icon [icon]="theme() === 'night' ? 'material-symbols:light-mode-rounded' : (theme() === 'sepia' ? 'material-symbols:tonality-rounded' : 'material-symbols:dark-mode-rounded')"></iconify-icon>
        </button>

        <!-- Publisher styles on/off -->
        <button mat-icon-button (click)="togglePublisherStyles()" aria-label="Toggle publisher styles">
          <iconify-icon [icon]="publisherStyles() ? 'material-symbols:style-rounded' : 'material-symbols:format-paint-rounded'"></iconify-icon>
        </button>

        <!-- Hyphenation on/off -->
        <button mat-icon-button (click)="toggleHyphenation()" aria-label="Toggle hyphenation">
          <iconify-icon [icon]="hyphenate() ? 'material-symbols:segment-rounded' : 'material-symbols:horizontal-rule-rounded'"></iconify-icon>
        </button>

        <!-- Fullscreen -->
        <button mat-icon-button (click)="toggleFullscreen()" aria-label="Toggle fullscreen">
          <iconify-icon [icon]="isFullscreen() ? 'material-symbols:fullscreen-exit-rounded' : 'material-symbols:fullscreen-rounded'"></iconify-icon>
        </button>
        
        <!-- Settings menu -->
        <button mat-icon-button aria-label="Reader settings">
          <iconify-icon icon="material-symbols:video-settings"></iconify-icon>
        </button>
      </mat-toolbar>

      <!-- Reader content area -->
  <div class="reader-content">
        <!-- TOC panel (supports nested TOC like Playground) -->
        @if (tocOpen()) {
          <div class="toc-panel" role="navigation" aria-label="Table of contents">
            <div class="toc-title">Contents</div>
            <ng-template #tocTpl let-items>
              <ul>
                <li *ngFor="let item of items">
                  <button class="toc-link" (click)="goToHref(item.href)" [disabled]="!item.href">
                    {{ displayTocTitle(item) }}
                  </button>
                  <ng-container *ngIf="item.children?.length">
                    <ng-container [ngTemplateOutlet]="tocTpl" [ngTemplateOutletContext]="{ $implicit: item.children }"></ng-container>
                  </ng-container>
                </li>
              </ul>
            </ng-template>

            <ng-container [ngTemplateOutlet]="tocTpl" [ngTemplateOutletContext]="{ $implicit: toc() }"></ng-container>
          </div>
        }
  <div #readerContainer class="reader-stage" title="EPUB Reader" role="region" aria-label="Reader" tabindex="-1"></div>
      </div>

  <!-- Bottom controls (available for Navigator and fallback) -->
  <div class="reader-controls">
        <button mat-icon-button (click)="previousPage()" [disabled]="!canGoPrevious()" aria-label="Previous page">
          <iconify-icon icon="material-symbols:chevron-left-rounded"></iconify-icon>
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
        
        <button mat-icon-button (click)="nextPage()" [disabled]="!canGoNext()" aria-label="Next page">
          <iconify-icon icon="material-symbols:chevron-right-rounded"></iconify-icon>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .reader-container {
      display: flex;
      flex-direction: column;
  height: 100vh;
  height: 100dvh; /* modern browsers: fill dynamic viewport */
      background: var(--reader-bg, #ffffff);
      color: var(--reader-text, #000000);
    }

    /* Toolbar: match main theme */
    .mat-toolbar.reader-toolbar.mat-toolbar-single-row,
    .reader-toolbar {
      flex-shrink: 0;
      background: var(--app-bg) !important;
      color: var(--content-fg) !important;
      border-bottom: 1px solid var(--border-color) !important;
      position: relative; /* ensure own stacking context */
      z-index: 5; /* sit above reader iframe */
      overflow: hidden; /* clip enlarged touch-targets so they don't overlap content */
    }
    .reader-toolbar iconify-icon,
    .reader-toolbar .mat-mdc-icon-button,
    .reader-toolbar .mat-mdc-icon-button iconify-icon {
      color: var(--content-fg) !important;
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

    .font-size-controls {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      margin: 0 8px;
    }
    .font-size-display { font-size: 12px; min-width: 42px; text-align: center; opacity: 0.8; }

    .reader-content {
      flex: 1;
      overflow: hidden;
      padding: 0;
      background: var(--reader-bg, #ffffff);
      min-height: 0; /* allow child to size fully within flex */
      position: relative;
    }

    /* Stage hosts the navigator or the fallback iframe */
    .reader-stage {
      position: relative;
      width: 100%;
      height: 100%;
  z-index: 0;
  overflow: hidden;
  isolation: isolate; /* new stacking context */
  transform: translateZ(0); /* ensure fixed children are contained */
    }
    /* Ensure any navigator/fallback child fills its stage */
    .reader-stage > *,
    .reader-stage > .reader-frame,
    iframe.reader-frame {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      border: 0;
      display: block;
      z-index: 0;
    }

  /* Readium navigator can set inline sizes on its iframe; force full bleed (sizing only) */
  .reader-stage > iframe,
  .reader-stage .navigator-root,
  .reader-stage .navigator-root > iframe,
  .reader-stage [data-readium-view],
  .reader-stage [data-readium-view] > iframe {
      position: absolute !important;
      inset: 0 !important;
      width: 100% !important;
      height: 100% !important;
      border: 0 !important;
      max-width: 100% !important;
      max-height: 100% !important;
    }

    .toc-panel {
      position: absolute;
      z-index: 10;
      top: 64px;
      left: 16px;
      width: min(320px, 70vw);
      max-height: calc(100vh - 128px);
      overflow: auto;
      background: var(--toolbar-bg, #f5f5f5);
      border: 1px solid var(--border-color, #e0e0e0);
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      padding: 8px 0;
    }
    .toc-title {
      font-weight: 600;
      padding: 8px 12px;
      border-bottom: 1px solid var(--border-color, #e0e0e0);
    }
    .toc-panel ul {
      list-style: none;
      margin: 0;
      padding: 0;
    }
    .toc-link {
      display: block;
      width: 100%;
      text-align: left;
      padding: 8px 12px;
      background: transparent;
      border: none;
      cursor: pointer;
      color: inherit;
    }
    .toc-link:hover { background: rgba(0,0,0,0.05); }

    /* Bottom controls: match main theme */
    .reader-controls {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
      background: var(--app-bg) !important;
      color: var(--content-fg) !important;
      border-top: 1px solid var(--border-color) !important;
      gap: 16px;
      position: relative;
      z-index: 5; /* keep above navigator view */
    }
    .reader-controls iconify-icon,
    .reader-controls .mat-mdc-icon-button,
    .reader-controls .mat-mdc-icon-button iconify-icon {
      color: var(--content-fg) !important;
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

  .progress-text { text-align: center; font-size: 12px; color: var(--muted-fg); }

    /* Prevent clicks on oversized Angular Material touch/ripple spans from passing through */
    .reader-toolbar .mat-mdc-button-touch-target,
    .reader-toolbar .mat-mdc-button-persistent-ripple,
    .reader-toolbar .mat-mdc-button-ripple,
    .reader-toolbar .mdc-icon-button__ripple {
      pointer-events: none;
    }

    /* Dark theme (inherits from body.dark-theme via :host-context) */
    :host-context(.dark-theme) {
      --reader-bg: #1e1e1e;
      --reader-text: #e0e0e0;
      --toolbar-bg: #2d2d2d;
      --border-color: #404040;
      --text-secondary: #a0a0a0;
    }

    /* Sepia theme */
    :host-context(.sepia-theme) {
      --reader-bg: #f7f0e1;
      --reader-text: #3b2f1b;
      --toolbar-bg: #efe6d6;
      --border-color: #d8c9ad;
      --text-secondary: #6f624d;
    }

    /* Mobile responsive */
    @media (max-width: 768px) {
      .reader-toolbar {
        padding: 0 8px;
      }
      
      .book-title {
        font-size: 14px;
      }
      
  .reader-content { padding: 0; }
      
      .reader-controls {
        padding: 8px 12px;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EbookReaderComponent implements OnInit, OnDestroy {
  @ViewChild('readerContainer', { static: true }) readerContainer!: ElementRef<HTMLDivElement>;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private bookService = inject(BookService);
  private readingProgressService = inject(ReadingProgressService);
  private snackBar = inject(MatSnackBar);
  private manifestService = inject(ManifestService);

  // Signals for component state
  book = signal<Book | null>(null);
  currentProgress = signal<ReadingProgressData | null>(null);
  // theme: default | sepia | night
  theme = signal<'default'|'sepia'|'night'>('default');
  isLoading = signal<boolean>(true);
  tocOpen = signal<boolean>(false);
  toc = signal<RwpmLink[]>([]);
  private manifest: any = null;
  usingNavigator = signal<boolean>(false);
  // reading presentation
  isPaginated = signal<boolean>(true);
  spreadMode = signal<'auto'|'none'>('auto');
  isSerif = signal<boolean>(false);
  publisherStyles = signal<boolean>(true);
  hyphenate = signal<boolean>(true);
  isFullscreen = signal<boolean>(false);
  
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
  // Removed epub.js; using a minimal iframe preview for spine HTML for now.
  
  // UI properties
  fontSize = 16;
  private baseUrl = environment.apiUrl || '';
  private lastAutoSavePct: number | null = null;
  private bookSettingsKey(id: string) { return `reader:settings:${id}`; }
  private _fallbackIframe: HTMLIFrameElement | null = null;
  private _resourceBase: string | null = null; // base URL for publication resources
  private _spineHrefs: string[] = [];
  private _currentSpineIndex: number = 0;
  private debug = !environment.production;
  private _prefetched: Set<string> = new Set();
  // Track navigator DOM readiness to avoid premature fallback
  private _navObserver: MutationObserver | null = null;
  private _navDomReady = false;

  ngOnInit(): void {
    const bookId = this.route.snapshot.paramMap.get('id');
    if (!bookId) {
      this.router.navigate(['/books']);
      return;
    }

    // Initialize theme preference from system if not set
    try {
      const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
      if (prefersDark) this.theme.set('night');
    } catch {}

    this.loadBook(bookId);
  }

  ngOnDestroy(): void {
    // destroy navigator if available
    const nav: any = (this as any)._navigator;
    try { nav?.destroy?.(); } catch {}
  try { this._navObserver?.disconnect(); this._navObserver = null; } catch {}
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
          // If locator is a JSON string, parse it once for client-side use
          let locator = (progress as any).locator;
          if (locator && typeof locator === 'string') {
            try { locator = JSON.parse(locator); } catch {}
            (progress as any).locator = locator;
          }
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
      // Prefer Readium navigator when available; fallback to iframe loading first spine.
      const loaded: LoadedManifest = await new Promise((resolve, reject) => {
        this.manifestService.getManifest(bookId).subscribe({
          next: resolve,
          error: reject
        });
      });
  const { manifest, manifestUrl, resourcesBase } = loaded;
      this.manifest = manifest;
      this._resourceBase = resourcesBase;
      // Extract TOC and spine
  const tocLinks: RwpmLink[] = Array.isArray(manifest.toc) ? manifest.toc : [];
      this.toc.set(tocLinks);
      try { this._spineHrefs = Array.isArray(manifest.readingOrder) ? manifest.readingOrder.map((it: any) => it?.href).filter(Boolean) : []; } catch { this._spineHrefs = []; }

      let navigatorInitOk = false;
      try {
        // Build Publication from manifest using Readium Shared models
        const manifestModel = Manifest.deserialize(this.manifest);
        if (!manifestModel) throw new Error('Manifest parse failed');
        const host = this.readerContainer.nativeElement;
        // Ensure stage is clean (remove any existing fallback iframe)
        try { host.innerHTML = ''; this._fallbackIframe = null; } catch {}
        // Ensure a dedicated container exists
        let container = host.querySelector(':scope > .navigator-root') as HTMLDivElement | null;
        if (!container) {
          container = document.createElement('div');
          container.className = 'navigator-root';
          container.style.position = 'absolute';
          container.style.inset = '0';
          container.style.width = '100%';
          container.style.height = '100%';
          host.appendChild(container);
        }
        // Helper to detect whether navigator has actually rendered something (best-effort)
        const hasNavigatorDom = () => {
          try {
            const anyDesc = container!.querySelector('*');
            const hasIFrame = !!container!.querySelector('iframe');
            const visible = container!.offsetHeight > 0 && container!.offsetWidth > 0;
            return !!(anyDesc || hasIFrame) && visible;
          } catch { return false; }
        };
  // Point fetcher base to the resources endpoint so relative HREFs resolve
  const baseForFetcher = resourcesBase;
        let hasResourceFetch = false;
        const loggedFetch = async (input: RequestInfo | URL, init?: RequestInit) => {
          if (this.debug) console.info('[Reader] fetching', input);
          hasResourceFetch = true;
          const resp = await window.fetch(input, init);
          if (this.debug) console.info('[Reader] fetched', input, resp.status);
          return resp;
        };
        const fetcher = new HttpFetcher(loggedFetch as any, baseForFetcher);
  try { (manifestModel as any).setSelfLink?.(manifestUrl); } catch {}
  const publication = new Publication({ manifest: manifestModel, fetcher });

        // Restore initial position if available and valid for Readium
        const saved = this.currentProgress();
        // Build a Readium Locator with a guaranteed media type.
        const resolveAbs = (h: string) => this.resolvePublicationHref(h);
        const stripHash = (u: string) => {
          try { return u.split('#')[0]; } catch { return u; }
        };
        const findLinkTypeForHref = (href: string): string => {
          try {
            const target = stripHash(resolveAbs(href));
            const ro: any[] = Array.isArray(this.manifest?.readingOrder) ? this.manifest.readingOrder : [];
            const match = ro.find((it: any) => stripHash(resolveAbs(it?.href || '')) === target);
            return typeof match?.type === 'string' && match.type.length > 0 ? match.type : 'application/xhtml+xml';
          } catch { return 'application/xhtml+xml'; }
        };
        const makeLocator = (href: string, locations?: any): LocatorType => {
          try {
            const link = new Link({ href, type: findLinkTypeForHref(href) });
            let locator = (link as any).locator as LocatorType;
            if (locations && typeof locations === 'object') {
              try { locator = (locator as any).copyWithLocations(locations); } catch { (locator as any).locations = locations; }
            }
            return locator;
          } catch {
            return { href, type: findLinkTypeForHref(href), locations: locations || { progression: 0 } } as any as LocatorType;
          }
        };
        const sanitizeInitialLocator = (loc: any): LocatorType | undefined => {
          try {
            if (!loc || typeof loc !== 'object') return undefined;
            const href = typeof loc.href === 'string' && loc.href.length > 0 ? loc.href : undefined;
            if (!href) return undefined;
            const locations = (loc.locations && typeof loc.locations === 'object') ? loc.locations : { progression: 0 };
            return makeLocator(href, locations);
          } catch { return undefined; }
        };
        // Provide coarse positions from readingOrder using proper Locator instances
        const ro: any[] = Array.isArray(this.manifest?.readingOrder) ? this.manifest.readingOrder : [];
        const positionsArray: LocatorType[] = ro.map((lnk: any, i: number) => makeLocator(lnk?.href || '', { position: i + 1, total: ro.length, progression: 0 }));
        const initialPosition: LocatorType | undefined = sanitizeInitialLocator((saved?.locator as any)) || positionsArray[0];
        if (this.debug) console.info('[Reader] Initial position:', initialPosition ? { href: (initialPosition as any).href, locations: (initialPosition as any).locations } : 'none');

        // Create EpubNavigator with listeners
  let frameReady = false;
  const nav = new EpubNavigator(
          container,
          publication,
          {
      frameLoaded: () => { frameReady = true; this._navDomReady = true; },
            positionChanged: (locator: LocatorType) => this.updateProgressFromNavigatorLocator(locator as any),
            tap: () => false,
            click: () => false,
            zoom: () => {},
            miscPointer: () => {},
            scroll: () => {},
            customEvent: () => {},
            handleLocator: () => false,
            textSelected: () => {}
          },
          // positions (optional)
          positionsArray,
          // initial position (optional)
          initialPosition
        );

  await nav.load();
        if (this.debug) {
          const shadowCount = (container as any).shadowRoot?.childElementCount ?? 0;
          console.info('[Reader] Navigator loaded. Container children:', container.childNodes.length, 'shadow children:', shadowCount);
        }

  // Post-load sanity: if DOM is already present, mark ready
  if (hasNavigatorDom()) this._navDomReady = true;

        // Apply preferences (flow, font, theme...)
        await this.submitEpubPreferences(nav);

        // Persist/restore settings
        const settingsRaw = localStorage.getItem(this.bookSettingsKey(bookId));
        if (settingsRaw) {
          try {
            const s = JSON.parse(settingsRaw);
            if (typeof s.fontSize === 'number') { this.fontSize = s.fontSize; }
            if (typeof s.theme === 'string') { this.theme.set(s.theme); }
            if (typeof s.isPaginated === 'boolean') { this.isPaginated.set(s.isPaginated); }
            if (typeof s.spread === 'string') { this.spreadMode.set(s.spread); }
            if (typeof s.isSerif === 'boolean') { this.isSerif.set(s.isSerif); }
            if (typeof s.publisherStyles === 'boolean') { this.publisherStyles.set(s.publisherStyles); }
            if (typeof s.hyphenate === 'boolean') { this.hyphenate.set(s.hyphenate); }
            await this.submitEpubPreferences(nav);
          } catch {}
        } else {
          this.persistSettings(bookId);
        }

        // Expose controls
        (this as any)._navigator = nav;
        this.usingNavigator.set(true);
        navigatorInitOk = true;

  // Note: We no longer auto-fallback after load(); if load() resolved we trust the navigator.
      } catch (e) {
        if (this.debug) console.warn('[Reader] Failed to initialize EpubNavigator, falling back.', e);
      }

      if (!navigatorInitOk) {
        // Fallback: load first spine resource into an iframe clone
        this.loadFallbackFromManifest(manifest);
      }

  this.applyTheme();
  this.isLoading.set(false);

    } catch (error) {
      console.error('Failed to load EPUB:', error);
      this.snackBar.open('Failed to load EPUB file', 'Close', { duration: 3000 });
      this.isLoading.set(false);
    }
  }

  // No pagination without a full navigator; stub controls

  // Navigation methods
  previousPage(): void {
  const nav: any = (this as any)._navigator;
  console.debug('[Reader] previousPage invoked');
  if (nav?.goBackward) { try { nav.goBackward(true, () => {}); return; } catch {} }
  if (nav?.goLeft) { nav.goLeft(true, () => {}); return; }
  if (nav?.goPrevious) { nav.goPrevious(); return; }
  if (nav?.prev) { nav.prev(); return; }
  if (nav?.previous) { nav.previous(); return; }
  if (typeof nav?.move === 'function') { try { nav.move('previous'); return; } catch {} }
  // Navigator fallback: step progression backwards if supported
  if (nav?.publication && typeof nav?.go === 'function') {
    try {
      const ro: any[] = (nav.publication?.readingOrder as any)?.items ?? [];
      if (ro.length > 0) {
        const curPct = this.currentProgress()?.progress ?? 0;
        const targetPct = Math.max(0, curPct - this.progressStepPercent()) / 100;
        const idx = Math.max(0, Math.min(ro.length - 1, Math.floor(targetPct * ro.length)));
        const within = Math.min(0.999, Math.max(0, targetPct * ro.length - idx));
        const href = ro[idx]?.href as string | undefined;
        if (href) {
          // Use the same locator factory as during init
          const locator = ((): any => {
            try {
              const link = new Link({ href, type: (ro[idx]?.type || 'application/xhtml+xml') });
              return (link as any).locator.copyWithLocations({ progression: within });
            } catch { return { href, type: (ro[idx]?.type || 'application/xhtml+xml'), locations: { progression: within } }; }
          })();
          nav.go(locator, true, () => {});
        }
        return;
      }
    } catch {}
  }
  // Fallback: scroll iframe up by viewport height
  if (this._fallbackIframe?.contentWindow) {
    try {
      const doc = this._fallbackIframe.contentDocument;
      const scroller = (doc?.scrollingElement || doc?.documentElement || doc?.body) as HTMLElement | undefined;
      const atTop = scroller ? scroller.scrollTop <= 2 : false;
      if (atTop && this._currentSpineIndex > 0) {
        // Go to previous chapter and scroll to bottom
        this.navigateFallbackToSpine(this._currentSpineIndex - 1, 'bottom');
        return;
      }
      this._fallbackIframe.contentWindow.scrollBy({ top: -this._fallbackIframe.contentWindow.innerHeight, behavior: 'smooth' });
    } catch {}
  }
  }

  nextPage(): void {
  const nav: any = (this as any)._navigator;
  console.debug('[Reader] nextPage invoked');
  if (nav?.goForward) { try { nav.goForward(true, () => {}); return; } catch {} }
  if (nav?.goRight) { nav.goRight(true, () => {}); return; }
  if (nav?.goNext) { nav.goNext(); return; }
  if (nav?.next) { nav.next(); return; }
  if (nav?.forward) { nav.forward(); return; }
  if (typeof nav?.move === 'function') { try { nav.move('next'); return; } catch {} }
  // Navigator fallback: step progression forward if supported
  if (nav?.publication && typeof nav?.go === 'function') {
    try {
      const ro: any[] = (nav.publication?.readingOrder as any)?.items ?? [];
      if (ro.length > 0) {
        const curPct = this.currentProgress()?.progress ?? 0;
        const targetPct = Math.min(1, (curPct + this.progressStepPercent()) / 100);
        const idx = Math.max(0, Math.min(ro.length - 1, Math.floor(targetPct * ro.length)));
        const within = Math.min(0.999, Math.max(0, targetPct * ro.length - idx));
        const href = ro[idx]?.href as string | undefined;
        if (href) {
          const locator = ((): any => {
            try {
              const link = new Link({ href, type: (ro[idx]?.type || 'application/xhtml+xml') });
              return (link as any).locator.copyWithLocations({ progression: within });
            } catch { return { href, type: (ro[idx]?.type || 'application/xhtml+xml'), locations: { progression: within } }; }
          })();
          nav.go(locator, true, () => {});
        }
        return;
      }
    } catch {}
  }
  if (this._fallbackIframe?.contentWindow) {
    try {
      const doc = this._fallbackIframe.contentDocument;
      const scroller = (doc?.scrollingElement || doc?.documentElement || doc?.body) as HTMLElement | undefined;
      const max = scroller ? (scroller.scrollHeight - scroller.clientHeight) : 0;
      const atBottom = scroller ? scroller.scrollTop >= (max - 2) : false;
      if (atBottom && this._currentSpineIndex < this._spineHrefs.length - 1) {
        // Go to next chapter at top
        this.navigateFallbackToSpine(this._currentSpineIndex + 1, 'top');
        return;
      }
      this._fallbackIframe.contentWindow.scrollBy({ top: this._fallbackIframe.contentWindow.innerHeight, behavior: 'smooth' });
    } catch {}
  }
  }

  // UI controls
  changeFontSize(size: number): void {
    this.fontSize = Math.max(10, Math.min(32, size));
  this.updateNavigatorSettings();
  this.updateFallbackStyles();
    const id = this.book()?.id; if (id) this.persistSettings(id);
  }

  increaseFont(): void { this.changeFontSize(this.fontSize + 1); }
  decreaseFont(): void { this.changeFontSize(this.fontSize - 1); }

  cycleTheme(): void {
    const next = this.theme() === 'default' ? 'sepia' : (this.theme() === 'sepia' ? 'night' : 'default');
    this.theme.set(next);
    this.applyTheme();
    const id = this.book()?.id; if (id) this.persistSettings(id);
  }

  private applyTheme(): void {
  const t = this.theme();
  document.body.classList.toggle('dark-theme', t === 'night');
  document.body.classList.toggle('sepia-theme', t === 'sepia');

    const nav: any = (this as any)._navigator;
    if (nav?.setTheme) {
      nav.setTheme(t);
    }
  }

  private persistSettings(bookId: string): void {
    try {
      localStorage.setItem(this.bookSettingsKey(bookId), JSON.stringify({ 
        fontSize: this.fontSize, 
        theme: this.theme(),
        isPaginated: this.isPaginated(),
        spread: this.spreadMode(),
        isSerif: this.isSerif(),
        publisherStyles: this.publisherStyles(),
        hyphenate: this.hyphenate()
      }));
    } catch {}
  }

  onProgressSliderChange(event: Event): void {
    const value = parseFloat((event.target as HTMLInputElement).value);
    const nav: any = (this as any)._navigator;
    if (!Number.isNaN(value)) {
      // If navigator is available, approximate a locator by mapping percentage to readingOrder index + intra-doc progression
      if (nav?.publication && typeof nav?.go === 'function') {
        try {
          const ro: any[] = (nav.publication?.readingOrder as any)?.items ?? [];
          if (ro.length > 0) {
            const pct = Math.max(0, Math.min(100, value)) / 100;
            const idx = Math.max(0, Math.min(ro.length - 1, Math.floor(pct * ro.length)));
            const within = Math.min(0.999, Math.max(0, pct * ro.length - idx));
            const href = ro[idx]?.href as string | undefined;
            if (href) {
              const locator = ((): any => {
                try {
                  const link = new Link({ href, type: (ro[idx]?.type || 'application/xhtml+xml') });
                  return (link as any).locator.copyWithLocations({ progression: within });
                } catch { return { href, type: (ro[idx]?.type || 'application/xhtml+xml'), locations: { progression: within } }; }
              })();
              nav.go(locator, true, () => {});
              return;
            }
          }
        } catch {}
      }
    }
    // Fallback: smooth scroll to the corresponding percentage
    if (!Number.isNaN(value)) this.scrollFallbackToProgress(value);
  }

  goBack(): void {
    // Save current progress before leaving
    const progress = this.currentProgress();
  if (progress && this.book()) {
      this.readingProgressService.saveProgress(this.book()!.id, {
        progress: progress.progress,
        currentPage: progress.currentPage,
    totalPages: progress.totalPages,
    locator: (progress as any).locator
      }).subscribe();
    }

    this.router.navigate(['/books', this.book()?.id]);
  }

  toggleToc(): void { this.tocOpen.update(o => !o); }
  async goToHref(href: string): Promise<void> {
    const nav: any = (this as any)._navigator;
    if (nav?.go || nav?.goTo) {
      try {
        const ro: any[] = (nav.publication?.readingOrder as any)?.items ?? [];
        const match = ro.find((it: any) => (it?.href || '').split('#')[0] === href.split('#')[0]);
        const locator = ((): any => {
          try {
            const link = new Link({ href, type: (match?.type || 'application/xhtml+xml') });
            return (link as any).locator.copyWithLocations({ progression: 0 });
          } catch { return { href, type: (match?.type || 'application/xhtml+xml'), locations: { progression: 0 } }; }
        })();
        if (typeof nav.go === 'function') {
          await nav.go(locator as LocatorType, true, () => {});
        } else {
          await nav.goTo(locator as LocatorType);
        }
        this.tocOpen.set(false);
      } catch {}
      return;
    }
    // Fallback: navigate the iframe to the resource URL
    try {
      const { base, fragment } = this.normalizeHref(href);
      const url = this.resolvePublicationHref(base);
      if (!this._fallbackIframe) return;

      // Same-document fragment navigation
      const current = this._fallbackIframe.getAttribute('src') || '';
      if (current && current.split('#')[0] === url && fragment) {
        this.scrollToFragment(fragment);
        this.tocOpen.set(false);
        return;
      }

      // Load new spine item, then scroll to fragment (if any)
      const onLoad = () => {
        this._fallbackIframe?.removeEventListener('load', onLoad);
        if (fragment) this.scrollToFragment(fragment);
        // Prefetch neighbors when landing on a new spine
        this.prefetchNeighborSpine();
      };
      this._fallbackIframe.addEventListener('load', onLoad);
      this._fallbackIframe.src = fragment ? `${url}#${fragment}` : url;
      this.tocOpen.set(false);
      // Update spine index
      const idx = this._spineHrefs.findIndex(h => this.resolvePublicationHref(h) === url || h === base || h === href);
      if (idx >= 0) this._currentSpineIndex = idx;
    } catch {}
  }

  // Derive a readable TOC label when the source item lacks a title
  displayTocTitle(item: { href: string; title?: string } | null | undefined): string {
    if (!item) return '';
    if (item.title && item.title.trim().length > 0) return item.title.trim();
    const href = item.href || '';
    try {
      // Strip query/hash and leading slashes
      const clean = href.split('#')[0].split('?')[0].replace(/^\/+/, '');
      // Take last path segment and remove extension
      const seg = clean.split('/').filter(Boolean).pop() || clean;
      const base = seg.replace(/\.[a-zA-Z0-9]+$/, '');
      // Humanize: replace separators with spaces and capitalize words
      const spaced = base.replace(/[\-_]+/g, ' ').trim();
      return spaced.replace(/\b\w/g, (c) => c.toUpperCase()) || href;
    } catch {
      return href;
    }
  }

  // Resolve publication-relative or absolute resource href to a fully qualified URL
  private resolvePublicationHref(href: string): string {
    if (!href) return href;
    // If already absolute URL, use as-is
    if (/^https?:\/\//i.test(href)) return href;
    // If absolute path, prefix backend base URL
    if (href.startsWith('/')) return `${this.baseUrl}${href}`;
    // Otherwise, resolve relative to the resources base
    if (this._resourceBase) {
      try { return new URL(href, this._resourceBase).toString(); } catch {}
    }
    return href;
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyDown(ev: KeyboardEvent) {
  if (ev.key === 'ArrowRight' || ev.key === 'PageDown' || (ev.key === ' ' && !ev.shiftKey)) { ev.preventDefault(); this.nextPage(); }
  if (ev.key === 'ArrowLeft' || ev.key === 'PageUp' || (ev.key === ' ' && ev.shiftKey)) { ev.preventDefault(); this.previousPage(); }
    if (ev.key.toLowerCase() === 't') { this.toggleToc(); }
    if (ev.key === '+' || ev.key === '=') { ev.preventDefault(); this.increaseFont(); }
    if (ev.key === '-' || ev.key === '_') { ev.preventDefault(); this.decreaseFont(); }
    if (ev.key.toLowerCase() === 'f') { ev.preventDefault(); this.toggleFullscreen(); }
    if (ev.key.toLowerCase() === 's') { ev.preventDefault(); this.toggleFlow(); }
  }

  // Readium-inspired presentation toggles
  toggleFlow(): void {
    this.isPaginated.update(v => !v);
  this.updateNavigatorSettings();
    const id = this.book()?.id; if (id) this.persistSettings(id);
  }

  toggleSpread(): void {
    const next = this.spreadMode() === 'auto' ? 'none' : 'auto';
    this.spreadMode.set(next);
  this.updateNavigatorSettings();
    const id = this.book()?.id; if (id) this.persistSettings(id);
  }

  toggleSerif(): void {
    this.isSerif.update(v => !v);
  this.updateNavigatorSettings();
  this.updateFallbackStyles();
    const id = this.book()?.id; if (id) this.persistSettings(id);
  }

  togglePublisherStyles(): void {
    this.publisherStyles.update(v => !v);
  this.updateNavigatorSettings();
    const id = this.book()?.id; if (id) this.persistSettings(id);
  }

  toggleHyphenation(): void {
    this.hyphenate.update(v => !v);
  this.updateNavigatorSettings();
  this.updateFallbackStyles();
    const id = this.book()?.id; if (id) this.persistSettings(id);
  }

  async toggleFullscreen(): Promise<void> {
    try {
      const container = this.readerContainer?.nativeElement?.parentElement ?? document.documentElement;
      if (!document.fullscreenElement) {
        await container.requestFullscreen?.();
        this.isFullscreen.set(true);
      } else {
        await document.exitFullscreen?.();
        this.isFullscreen.set(false);
      }
    } catch {}
  }

  // Try multiple APIs to update presentation regardless of navigator version
  private updateNavigatorSettings(): void {
    const nav: any = (this as any)._navigator;
    if (!nav) return;
    // Preferred API for v2: submitPreferences
    if (typeof nav.submitPreferences === 'function') {
      this.submitEpubPreferences(nav as EpubNavigator).catch(() => {});
      return;
    }
    // Fallback for any legacy APIs
    const settings = {
      flow: this.isPaginated() ? 'paginated' : 'scrolled',
      spread: this.spreadMode(),
      theme: this.theme(),
      fontSize: this.fontSize,
      hyphenate: this.hyphenate(),
      publisherStyles: this.publisherStyles(),
      fontFamily: this.isSerif() ? 'serif' : 'sans-serif',
    } as any;
    nav.setSettings?.(settings);
    nav.updateSettings?.(settings);
    nav.setViewSettings?.(settings);
    nav.setFlow?.(settings.flow);
    nav.setSpread?.(settings.spread);
    nav.setTheme?.(settings.theme);
    nav.setFontSize?.(settings.fontSize);
    nav.setHyphenation?.(settings.hyphenate);
    nav.setPublisherStyles?.(settings.publisherStyles);
    nav.setFontFamily?.(settings.fontFamily);
  }

  private async submitEpubPreferences(nav: EpubNavigator): Promise<void> {
    const theme = this.theme();
    const prefs = new EpubPreferences({
      scroll: !this.isPaginated(),
      fontSize: this.fontSize,
      fontFamily: this.isSerif() ? 'serif' : 'sans-serif',
      hyphens: this.hyphenate(),
      // Readium v2 themes: 'sepia' | 'night' | 'custom' (we'll map default->custom with colors untouched)
      theme: theme === 'default' ? ('custom' as any) : (theme as any)
    });
    await nav.submitPreferences(prefs);
  }

  // Build the simple iframe fallback from a manifest
  private loadFallbackFromManifest(manifest: any): void {
    try {
      const spine = Array.isArray(manifest?.readingOrder) ? manifest.readingOrder : [];
      const firstHrefRaw = spine.length > 0 ? spine[0].href : null;
      const firstHref = firstHrefRaw ? this.resolvePublicationHref(firstHrefRaw) : null;
      if (!firstHref) {
  if (this.debug) console.warn('[Reader] No spine item to load in fallback.');
        return;
      }
      const iframe = document.createElement('iframe');
      iframe.className = 'reader-frame';
      iframe.title = 'EPUB Reader';
      iframe.style.position = 'absolute';
      iframe.style.inset = '0';
      iframe.style.width = '100%';
      iframe.style.height = '100%';
      iframe.style.border = '0';
  // Allow scripts and same-origin so EPUB content can run JS and we can style/scroll the document
  // Note: This combination reduces sandbox isolation. Consider CSP on /resources if stricter security is needed.
  iframe.sandbox.add('allow-same-origin');
  iframe.sandbox.add('allow-scripts');
      iframe.src = firstHref;
      const host = this.readerContainer.nativeElement;
      host.innerHTML = '';
      host.appendChild(iframe);
      this._fallbackIframe = iframe;
      // Reset spine index to first item
      this._currentSpineIndex = 0;
    iframe.addEventListener('load', () => {
        // Track scroll to update progress while using iframe fallback
        try {
          const doc = this._fallbackIframe?.contentDocument;
          if (doc) {
            const onScroll = () => requestAnimationFrame(() => this.updateProgressFromScrollDocument(doc));
            doc.addEventListener('scroll', onScroll, { passive: true });
            // Capture arrow keys inside iframe too
            doc.addEventListener('keydown', (ev: KeyboardEvent) => {
              if (ev.key === 'ArrowRight' || ev.key === 'PageDown' || (ev.key === ' ' && !ev.shiftKey)) { ev.preventDefault(); this.nextPage(); }
              if (ev.key === 'ArrowLeft' || ev.key === 'PageUp' || (ev.key === ' ' && ev.shiftKey)) { ev.preventDefault(); this.previousPage(); }
            });
            // Intercept internal links for in-place navigation
            doc.addEventListener('click', (ev: any) => {
              try {
                const a = (ev.target as HTMLElement)?.closest?.('a[href]') as HTMLAnchorElement | null;
                if (!a) return;
                const href = a.getAttribute('href') || '';
                if (!href) return;
                // Let external links open normally
                const isExternal = /^(https?:)?\/\//i.test(href);
                if (isExternal) return;
                ev.preventDefault();
                this.goToHref(href);
              } catch {}
            }, { capture: true });
            // Initial progress calculation
            this.updateProgressFromScrollDocument(doc);
          }
        } catch {}
        // Prefetch neighbors when first page is ready
        this.prefetchNeighborSpine();
      });
    } catch (e) {
      console.error('[Reader] Failed to build fallback iframe:', e);
    }
  }

  // No-op: we deliberately avoid injecting styles into the fallback iframe to prevent altering ebook content.
  private updateFallbackStyles(): void { /* intentionally empty */ }

  // Navigate fallback iframe to a specific spine item and position
  private navigateFallbackToSpine(index: number, position: 'top'|'bottom' = 'top'): void {
    try {
      if (!this._spineHrefs?.length) return;
      const clamped = Math.max(0, Math.min(this._spineHrefs.length - 1, index));
      const href = this._spineHrefs[clamped];
      if (!href) return;
      const url = this.resolvePublicationHref(href);
      this._currentSpineIndex = clamped;
      if (this._fallbackIframe) {
        const onLoad = () => {
          try {
            const doc = this._fallbackIframe?.contentDocument;
            const el = (doc?.scrollingElement || doc?.documentElement || doc?.body) as HTMLElement;
            if (position === 'bottom') {
              const max = (el.scrollHeight - el.clientHeight);
              (el as any).scrollTo?.({ top: max, behavior: 'instant' as any });
            } else {
              (el as any).scrollTo?.({ top: 0, behavior: 'instant' as any });
            }
          } catch {}
          this._fallbackIframe?.removeEventListener('load', onLoad);
          // Prefetch next/prev chapters after navigation
          this.prefetchNeighborSpine();
        };
        this._fallbackIframe.addEventListener('load', onLoad);
        this._fallbackIframe.src = url;
      }
    } catch {}
  }

  // Determine a reasonable step in percent for next/previous when using goToProgress fallback
  private progressStepPercent(): number {
    const p = this.currentProgress();
    if (p?.totalPages && p.totalPages > 0) {
      return Math.max(0.5, Math.min(10, 100 / p.totalPages));
    }
    // Default to 2% per step if total pages unknown
    return 2;
  }

  // Update reading progress signal (and autosave) from a navigator locator
  private updateProgressFromNavigatorLocator(locator: any): void {
    try {
      if (!locator || typeof locator !== 'object') return;
      const locations = (locator as any).locations && typeof (locator as any).locations === 'object' ? (locator as any).locations : undefined;
      const prog = (locations && typeof locations.progression === 'number')
        ? locations.progression
        : (typeof (locator as any).progression === 'number' ? (locator as any).progression : 0);
      const pct = prog ? Math.max(0, Math.min(1, Number(prog))) * 100 : 0;
      const req: ReadingProgressRequest = {
        progress: Math.round(pct * 10) / 10,
        locator
      };
      if (this.lastAutoSavePct === null || Math.abs(req.progress - this.lastAutoSavePct) >= 0.5) {
        this.lastAutoSavePct = req.progress;
        if (this.book()) this.readingProgressService.autoSaveProgress(this.book()!.id, req);
      }
  const pos = (locations && typeof locations.position === 'number') ? locations.position : (typeof (locator as any).position === 'number' ? (locator as any).position : 0);
  const total = (locations && typeof locations.total === 'number') ? locations.total : (typeof (locator as any).total === 'number' ? (locator as any).total : 0);
      this.currentProgress.set({
        progress: req.progress,
        currentPage: pos ?? 0,
        totalPages: total ?? 0,
        isCompleted: req.progress >= 100,
        lastReadAt: new Date().toISOString(),
        locator
      });
    } catch {}
  }

  // Compute and set progress based on fallback iframe scroll position
  private updateProgressFromScrollDocument(doc: Document): void {
    try {
      const el = (doc.scrollingElement || doc.documentElement || doc.body) as HTMLElement;
      const maxScroll = (el.scrollHeight - el.clientHeight) || 1;
      const pct = Math.max(0, Math.min(1, el.scrollTop / maxScroll)) * 100;
      const rounded = Math.round(pct * 10) / 10;
      const prev = this.currentProgress();
      if (!prev || Math.abs((prev.progress || 0) - rounded) >= 0.5) {
        const next = {
          progress: rounded,
          currentPage: prev?.currentPage || 0,
          totalPages: prev?.totalPages || 0,
          isCompleted: rounded >= 100,
          lastReadAt: new Date().toISOString(),
          locator: prev?.locator
        } as ReadingProgressData;
        this.currentProgress.set(next);
        if (this.book()) {
          const req: ReadingProgressRequest = { progress: rounded, locator: next.locator as any };
          this.readingProgressService.autoSaveProgress(this.book()!.id, req);
        }
      }
    } catch {}
  }

  // Smoothly scroll fallback iframe to a given percent [0..100]
  private scrollFallbackToProgress(percent: number): void {
    const doc = this._fallbackIframe?.contentDocument;
    if (!doc) return;
    try {
      const el = (doc.scrollingElement || doc.documentElement || doc.body) as HTMLElement;
      const maxScroll = (el.scrollHeight - el.clientHeight);
      const target = Math.max(0, Math.min(100, percent)) / 100 * maxScroll;
      (el as any).scrollTo?.({ top: target, behavior: this.scrollBehavior() as any });
    } catch {}
  }

  // Helpers: anchor navigation and URL normalization for fallback
  private normalizeHref(href: string): { base: string; fragment: string | null } {
    try {
      const [b, frag] = href.split('#');
      return { base: b || '', fragment: frag || null };
    } catch {
      return { base: href, fragment: null };
    }
  }

  private scrollToFragment(fragment: string): void {
    try {
      const doc = this._fallbackIframe?.contentDocument;
      if (!doc) return;
      // ID first, then named anchor fallback
      const el = doc.getElementById(fragment) || doc.querySelector(`[name="${CSS.escape(fragment)}"]`);
      if (el && 'scrollIntoView' in el) {
        (el as any).scrollIntoView({ behavior: this.scrollBehavior(), block: 'start', inline: 'nearest' });
      }
    } catch {}
  }

  private scrollBehavior(): ScrollBehavior {
    try {
      const reduced = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      return reduced ? 'auto' : 'smooth';
    } catch { return 'smooth'; }
  }

  // Prefetch neighboring spine resources to improve perceived performance
  private prefetchNeighborSpine(): void {
    try {
      const prev = this._spineHrefs[this._currentSpineIndex - 1];
      const next = this._spineHrefs[this._currentSpineIndex + 1];
      if (prev) this.prefetchResource(this.resolvePublicationHref(prev));
      if (next) this.prefetchResource(this.resolvePublicationHref(next));
    } catch {}
  }

  private prefetchResource(url: string): void {
    try {
      if (!url || this._prefetched.has(url)) return;
      this._prefetched.add(url);
      const link = document.createElement('link');
      link.rel = 'prefetch';
      link.as = 'document';
      link.href = url;
      document.head.appendChild(link);
    } catch {}
  }
}