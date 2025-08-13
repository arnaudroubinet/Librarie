import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatBadgeModule } from '@angular/material/badge';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatSidenavModule,
    MatListModule,
    MatBadgeModule
  ],
  template: `
    <div class="plex-container">
      <nav class="plex-sidebar">
        <div class="plex-logo">
          <mat-icon class="logo-icon">collections</mat-icon>
          <span class="logo-text">Librarie</span>
        </div>
        
        <div class="nav-section">
          <div class="section-title">LIBRARY</div>
          <a mat-button class="nav-item" routerLink="/books" routerLinkActive="active">
            <mat-icon>collections</mat-icon>
            <span>All Books</span>
          </a>
          <a mat-button class="nav-item" routerLink="/series" routerLinkActive="active">
            <mat-icon>library_books</mat-icon>
            <span>Series</span>
          </a>
          <a mat-button class="nav-item" routerLink="/search" routerLinkActive="active">
            <mat-icon>search</mat-icon>
            <span>Search</span>
          </a>
        </div>

        <div class="nav-section">
          <div class="section-title">MANAGEMENT</div>
          <a mat-button class="nav-item" routerLink="/library" routerLinkActive="active">
            <mat-icon>settings</mat-icon>
            <span>Library Settings</span>
          </a>
        </div>

        <div class="nav-section nav-footer">
          <div class="version-info">
            <mat-icon>info</mat-icon>
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
      background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
      color: #ffffff;
    }

    .plex-sidebar {
      width: 240px;
      background: linear-gradient(180deg, #1f1f1f 0%, #0f0f0f 100%);
      border-right: 1px solid #333;
      display: flex;
      flex-direction: column;
      padding: 0;
      box-shadow: 2px 0 10px rgba(0, 0, 0, 0.3);
    }

    .plex-logo {
      display: flex;
      align-items: center;
      padding: 24px 20px;
      border-bottom: 1px solid #333;
      margin-bottom: 20px;
    }

    .logo-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #e5a00d;
      margin-right: 12px;
    }

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
      color: #cccccc;
      font-weight: 500;
      transition: all 0.3s ease;
      position: relative;
      overflow: hidden;
    }

    .nav-item:hover {
      background: rgba(229, 160, 13, 0.1);
      color: #e5a00d;
      transform: translateX(4px);
    }

    .nav-item.active {
      background: linear-gradient(135deg, #e5a00d 0%, #cc9000 100%);
      color: #000000;
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

    .nav-item mat-icon {
      margin-right: 12px;
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

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

    .version-info mat-icon {
      margin-right: 8px;
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .plex-content {
      flex: 1;
      background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
      overflow-y: auto;
      position: relative;
    }

    .plex-content::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 100px;
      background: linear-gradient(180deg, rgba(0,0,0,0.3) 0%, transparent 100%);
      pointer-events: none;
      z-index: 1;
    }

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