import { Component, CUSTOM_ELEMENTS_SCHEMA, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, NavigationEnd, Router, RouterModule } from '@angular/router';
import { HeaderActionsService } from '../../app/services/header-actions.service';
import { MatButtonModule } from '@angular/material/button';
import pkg from '../../../package.json';

@Component({
  selector: 'app-navigation',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    
  ],
  template: `
    <div class="motspassants-container">
      <a class="sr-only" href="#maincontent">Skip to main content</a>
      <nav class="motspassants-sidebar">
        <div class="motspassants-logo">
          <iconify-icon class="logo-icon" icon="mdi:bookshelf"></iconify-icon>
          <span class="logo-text">MotsPassants</span>
        </div>
        
        <div class="nav-section">
          <div class="section-title">LIBRARY</div>
          <a mat-button class="nav-item" routerLink="/books" routerLinkActive="active">
            <iconify-icon icon="material-symbols:book-2-rounded"></iconify-icon>
            <span>All Books</span>
          </a>
          <a mat-button class="nav-item" routerLink="/series" routerLinkActive="active">
            <iconify-icon icon="material-symbols:books-movies-and-music"></iconify-icon>
            <span>Series</span>
          </a>
          <a mat-button class="nav-item" routerLink="/authors" routerLinkActive="active">
            <iconify-icon icon="material-symbols:supervised-user-circle"></iconify-icon>
            <span>Authors</span>
          </a>
        </div>

        <div class="nav-section">
          <div class="section-title">MANAGEMENT</div>
          <a mat-button class="nav-item" routerLink="/settings" routerLinkActive="active">
            <iconify-icon icon="material-symbols:settings-outline"></iconify-icon>
            <span>Settings</span>
          </a>
        </div>

    <div class="nav-section nav-footer">
          <div class="version-info">
            <iconify-icon icon="mdi:information-outline"></iconify-icon>
      <span>Version {{ frontendVersion }}</span>
          </div>
        </div>
      </nav>

  <main id="maincontent" role="main" class="motspassants-content">
        <header class="shell-header">
          <div class="shell-header-left">
            <iconify-icon class="title-icon" [icon]="headerIcon()" aria-hidden="true"></iconify-icon>
            <h1 class="shell-title ngx-h1">{{ pageTitle() }}</h1>
            @if (actions.refresh()) {
              <button class="refresh-btn" type="button" aria-label="Refresh" (click)="actions.triggerRefresh()">
                <iconify-icon icon="material-symbols-light:refresh-rounded"></iconify-icon>
              </button>
            }
          </div>
          <div class="shell-header-right">
            <button class="search-btn" type="button" (click)="goToSearch()" aria-label="Search">
              <iconify-icon icon="material-symbols:search-rounded" aria-hidden="true"></iconify-icon>
              <span class="search-label">Search</span>
            </button>
          </div>
        </header>
        <ng-content></ng-content>
      </main>
    </div>
  `,
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent {
  frontendVersion = (pkg as any).version ?? '0.0.0';

  private currentTitle = signal('Books Library');
  pageTitle = computed(() => this.currentTitle());
  headerIcon = computed(() => this.computeHeaderIcon());
  // Motion removed: no rotation state

  constructor(private router: Router, private route: ActivatedRoute, public actions: HeaderActionsService) {
    // Update title on navigation end
    this.router.events.subscribe((evt) => {
      if (evt instanceof NavigationEnd) {
        const title = this.findDeepestTitle(this.route);
        this.currentTitle.set(title ?? 'Books Library');
      }
    });
  }

  private findDeepestTitle(route: ActivatedRoute): string | undefined {
    let r: ActivatedRoute | null = route.firstChild ?? route;
    let lastTitle: string | undefined;
    while (r) {
      const dataTitle = r.snapshot.data?.['title'];
      if (dataTitle) lastTitle = dataTitle;
      r = r.firstChild ?? null;
    }
    return lastTitle;
  }

  private computeHeaderIcon(): string {
    const url = this.router.url || '';
    if (url.startsWith('/settings')) return 'material-symbols:settings-outline';
    if (url.startsWith('/series')) return 'material-symbols:books-movies-and-music';
    if (url.startsWith('/authors')) return 'material-symbols:supervised-user-circle';
    if (url.startsWith('/search')) return 'material-symbols:search-rounded';
    if (url.startsWith('/books')) return 'material-symbols:book-2-rounded';
    return 'material-symbols:book-2-rounded';
  }

  goToSearch() {
    // If already on search, do nothing
    if ((this.router.url || '').startsWith('/search')) return;
    // Navigate directly without motion
    this.router.navigate(['/search']);
  }
}