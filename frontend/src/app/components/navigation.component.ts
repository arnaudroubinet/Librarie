import { Component, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
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
      <nav class="motspassants-sidebar" role="navigation" aria-label="Main navigation">
        <div class="motspassants-logo">
          <iconify-icon class="logo-icon" icon="mdi:bookshelf" role="img" aria-label="MotsPassants logo"></iconify-icon>
          <span class="logo-text">MotsPassants</span>
        </div>
        
        <div class="nav-section">
          <div class="section-title" role="heading" aria-level="2">LIBRARY</div>
          <a mat-button class="nav-item" routerLink="/books" routerLinkActive="active" aria-label="All Books">
            <iconify-icon icon="material-symbols:book-2-rounded" role="img" aria-hidden="true"></iconify-icon>
            <span>All Books</span>
          </a>
          <a mat-button class="nav-item" routerLink="/series" routerLinkActive="active" aria-label="Series">
            <iconify-icon icon="material-symbols:books-movies-and-music" role="img" aria-hidden="true"></iconify-icon>
            <span>Series</span>
          </a>
          <a mat-button class="nav-item" routerLink="/authors" routerLinkActive="active" aria-label="Authors">
            <iconify-icon icon="material-symbols:supervised-user-circle" role="img" aria-hidden="true"></iconify-icon>
            <span>Authors</span>
          </a>
          <a mat-button class="nav-item" routerLink="/search" routerLinkActive="active" aria-label="Search">
            <iconify-icon icon="material-symbols:search-rounded" role="img" aria-hidden="true"></iconify-icon>
            <span>Search</span>
          </a>
        </div>

        <div class="nav-section">
          <div class="section-title" role="heading" aria-level="2">MANAGEMENT</div>
          <a mat-button class="nav-item" routerLink="/settings" routerLinkActive="active" aria-label="Settings">
            <iconify-icon icon="material-symbols:settings-outline" role="img" aria-hidden="true"></iconify-icon>
            <span>Settings</span>
          </a>
        </div>

    <div class="nav-section nav-footer">
          <div class="version-info">
            <iconify-icon icon="mdi:information-outline" role="img" aria-hidden="true"></iconify-icon>
      <span>Version {{ frontendVersion }}</span>
          </div>
        </div>
      </nav>

  <main id="main-content" class="motspassants-content" role="main" tabindex="-1">
        <ng-content></ng-content>
      </main>
    </div>
  `,
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent {
  frontendVersion = (pkg as any).version ?? '0.0.0';
}