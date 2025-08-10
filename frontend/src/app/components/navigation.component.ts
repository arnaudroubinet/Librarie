import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';

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
    MatListModule
  ],
  template: `
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav 
        #drawer 
        class="sidenav" 
        fixedInViewport="true"
        [attr.role]="'navigation'"
        [mode]="'over'"
        [opened]="false">
        <mat-nav-list>
          <a mat-list-item routerLink="/books" routerLinkActive="active">
            <mat-icon>book</mat-icon>
            <span class="ml-2">Books</span>
          </a>
          <a mat-list-item routerLink="/search" routerLinkActive="active">
            <mat-icon>search</mat-icon>
            <span class="ml-2">Search</span>
          </a>
          <a mat-list-item routerLink="/library" routerLinkActive="active">
            <mat-icon>dashboard</mat-icon>
            <span class="ml-2">Library Management</span>
          </a>
        </mat-nav-list>
      </mat-sidenav>
      
      <mat-sidenav-content>
        <mat-toolbar color="primary">
          <button
            type="button"
            aria-label="Toggle sidenav"
            mat-icon-button
            (click)="drawer.toggle()">
            <mat-icon aria-label="Side nav toggle icon">menu</mat-icon>
          </button>
          <span>📚 Librarie</span>
          <div class="flex-spacer"></div>
          <button mat-icon-button routerLink="/books" matTooltip="Books">
            <mat-icon>book</mat-icon>
          </button>
          <button mat-icon-button routerLink="/search" matTooltip="Search">
            <mat-icon>search</mat-icon>
          </button>
          <button mat-icon-button routerLink="/library" matTooltip="Library">
            <mat-icon>dashboard</mat-icon>
          </button>
        </mat-toolbar>
        
        <div class="content">
          <ng-content></ng-content>
        </div>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .sidenav-container {
      height: 100vh;
    }

    .sidenav {
      width: 200px;
    }

    .sidenav .mat-toolbar {
      background: inherit;
    }

    .mat-toolbar.mat-primary {
      position: sticky;
      top: 0;
      z-index: 1;
    }

    .content {
      padding: 16px;
      min-height: calc(100vh - 64px);
      background-color: var(--background-color);
    }

    .active {
      background-color: rgba(0, 0, 0, 0.04);
    }

    .ml-2 {
      margin-left: 16px;
    }

    @media (max-width: 768px) {
      .content {
        padding: 8px;
      }
    }
  `]
})
export class NavigationComponent {}