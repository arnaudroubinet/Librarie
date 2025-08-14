import { Component, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';

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
    <div class="plex-container">
      <nav class="plex-sidebar">
        <div class="plex-logo">
          <iconify-icon class="logo-icon" icon="mdi:bookshelf"></iconify-icon>
          <span class="logo-text">Librarie</span>
        </div>
        
        <div class="nav-section">
          <div class="section-title">LIBRARY</div>
          <a mat-button class="nav-item" routerLink="/books" routerLinkActive="active">
            <iconify-icon icon="ph:books-thin"></iconify-icon>
            <span>All Books</span>
          </a>
          <a mat-button class="nav-item" routerLink="/series" routerLinkActive="active">
            <iconify-icon icon="icon-park-outline:bookshelf"></iconify-icon>
            <span>Series</span>
          </a>
          <a mat-button class="nav-item" routerLink="/authors" routerLinkActive="active">
            <iconify-icon icon="ph:users-three-thin"></iconify-icon>
            <span>Authors</span>
          </a>
          <a mat-button class="nav-item" routerLink="/search" routerLinkActive="active">
            <iconify-icon icon="mdi-light:magnify"></iconify-icon>
            <span>Search</span>
          </a>
        </div>

        <div class="nav-section">
          <div class="section-title">MANAGEMENT</div>
          <a mat-button class="nav-item" routerLink="/settings" routerLinkActive="active">
            <iconify-icon icon="mdi:cog"></iconify-icon>
            <span>Settings</span>
          </a>
        </div>

        <div class="nav-section nav-footer">
          <div class="version-info">
            <iconify-icon icon="mdi:information-outline"></iconify-icon>
            <span>Version 1.0.0</span>
          </div>
        </div>
      </nav>

      <main class="plex-content">
        <ng-content></ng-content>
      </main>
    </div>
  `,
  styles: [`
    .plex-container {
      display: flex;
      height: 100vh;
  /* Solid app background color */
  background: #1f1f1f;
      color: #ffffff;
    }

    .plex-sidebar {
      width: 240px;
      /* Transparent to let the root gradient flow across the whole page */
      background: transparent;
  /* Remove hard separation line to blend with content */
  /* border-right: 1px solid #333; */
      display: flex;
      flex-direction: column;
      padding: 0;
  /* Remove drop shadow separation */
  /* box-shadow: 2px 0 10px rgba(0, 0, 0, 0.3); */
    }

    .plex-logo {
      display: flex;
      align-items: center;
      padding: 24px 20px;
  /* Remove bottom border to avoid a visible seam between top-left and bottom-left */
  /* border-bottom: 1px solid #333; */
      margin-bottom: 20px;
    }

  .logo-icon { font-size: 32px; width: 32px; height: 32px; color: #e5a00d; margin-right: 12px; }

    .logo-text {
      font-size: 20px;
      font-weight: 600;
      color: #ffffff;
      letter-spacing: -0.5px;
    }

    .nav-section {
      margin-bottom: 32px;
      padding: 0 12px;
    }

    .section-title {
      font-size: 11px;
      font-weight: 600;
      color: #888;
      text-transform: uppercase;
      letter-spacing: 1px;
      margin-bottom: 12px;
      padding: 0 8px;
    }

    .nav-item {
      width: 100%;
      justify-content: flex-start;
      text-align: left;
      margin-bottom: 4px;
      padding: 12px 8px;
      border-radius: 8px;
      color: #ffffff; /* Menu button text/icon should be white */
      font-weight: 500;
      transition: all 0.3s ease;
      position: relative;
      overflow: hidden;
    }

    /* Ensure links are white in all unselected states */
    .nav-item:link,
    .nav-item:visited,
    .nav-item:hover,
    .nav-item:focus,
    .nav-item:active { color: #ffffff !important; }

    .nav-item:hover { color: #ffffff; transform: translateX(4px); }

    .nav-item.active {
      /* Keep whatever background (often blue) but ensure text stays white */
      color: #ffffff !important;
      font-weight: 600;
    }

    .nav-item.active::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      height: 100%;
      width: 3px;
      background: #fff;
    }

  .nav-item :is(mat-icon, iconify-icon) { margin-right: 12px; font-size: 20px; width: 20px; height: 20px; color: inherit; }
  .nav-item iconify-icon,
  .nav-item mat-icon { color: #ffffff !important; }

    /* Ensure Angular Material button label inside stays white */
    .nav-item .mdc-button__label { color: #ffffff !important; }

    .nav-footer {
      margin-top: auto;
      margin-bottom: 20px;
    }

    .version-info {
      display: flex;
      align-items: center;
      padding: 8px;
      color: #666;
      font-size: 12px;
    }

  .version-info :is(mat-icon, iconify-icon) { margin-right: 8px; font-size: 16px; width: 16px; height: 16px; }

    .plex-content {
      flex: 1;
      /* Transparent so the root gradient shows through */
      background: transparent;
      overflow-y: auto;
      position: relative;
    }
  /* Remove top overlay that visually separates header and content */
  /* .plex-content::before { ... } */

    @media (max-width: 768px) {
      .plex-sidebar {
        width: 60px;
      }
      
      .logo-text,
      .section-title,
      .nav-item span,
      .version-info span {
        display: none;
      }
      
      .nav-item {
        justify-content: center;
      }
      
      .nav-item mat-icon {
        margin-right: 0;
      }
    }
  `]
})
export class NavigationComponent {}