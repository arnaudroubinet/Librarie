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
          <a mat-button class="nav-item" routerLink="/search" routerLinkActive="active">
            <iconify-icon icon="material-symbols:search-rounded"></iconify-icon>
            <span>Search</span>
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

  <main class="motspassants-content">
        <ng-content></ng-content>
      </main>
    </div>
  `,
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent {
  frontendVersion = (pkg as any).version ?? '0.0.0';
}