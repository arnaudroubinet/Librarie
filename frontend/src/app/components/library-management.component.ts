import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-library-management',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSnackBarModule,
    MatListModule,
    MatDividerModule
  ],
  template: `
    <div class="library-container">
      <h1>⚙️ Library Management</h1>
      
      <div class="dashboard-grid">
        <!-- Library Information -->
        <mat-card class="info-card">
          <mat-card-header>
            <div mat-card-avatar>
              <mat-icon>info</mat-icon>
            </div>
            <mat-card-title>System Information</mat-card-title>
            <mat-card-subtitle>Library management system details</mat-card-subtitle>
          </mat-card-header>
          
          <mat-card-content>
            <div class="stat-row">
              <span class="stat-label">Version:</span>
              <span class="stat-value">1.0.0-SNAPSHOT</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">API Version:</span>
              <span class="stat-value">v1</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">Architecture:</span>
              <span class="stat-value">Hexagonal</span>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Supported Formats -->
        <mat-card class="formats-card">
          <mat-card-header>
            <div mat-card-avatar>
              <mat-icon>description</mat-icon>
            </div>
            <mat-card-title>Supported Formats</mat-card-title>
            <mat-card-subtitle>{{ supportedFormats().length }} formats supported</mat-card-subtitle>
          </mat-card-header>
          
          <mat-card-content>
            <div class="formats-container">
              @for (format of supportedFormats(); track format) {
                <mat-chip class="format-chip">{{ format.toUpperCase() }}</mat-chip>
              }
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Features -->
        <mat-card class="features-card">
          <mat-card-header>
            <div mat-card-avatar>
              <mat-icon>star</mat-icon>
            </div>
            <mat-card-title>System Features</mat-card-title>
            <mat-card-subtitle>Available functionality</mat-card-subtitle>
          </mat-card-header>
          
          <mat-card-content>
            <mat-list>
              @for (feature of systemFeatures(); track feature) {
                <mat-list-item>
                  <mat-icon matListItemIcon>check_circle</mat-icon>
                  <div matListItemTitle>{{ feature }}</div>
                </mat-list-item>
              }
            </mat-list>
          </mat-card-content>
        </mat-card>

        <!-- Security Features -->
        <mat-card class="security-card">
          <mat-card-header>
            <div mat-card-avatar>
              <mat-icon>security</mat-icon>
            </div>
            <mat-card-title>Security Features</mat-card-title>
            <mat-card-subtitle>Enterprise-grade protection</mat-card-subtitle>
          </mat-card-header>
          
          <mat-card-content>
            <mat-list>
              @for (security of securityFeatures(); track security) {
                <mat-list-item>
                  <mat-icon matListItemIcon>shield</mat-icon>
                  <div matListItemTitle>{{ security }}</div>
                </mat-list-item>
              }
            </mat-list>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .library-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 16px;
    }

    .dashboard-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 16px;
      margin-top: 24px;
    }

    mat-card {
      height: fit-content;
    }

    mat-card-header [mat-card-avatar] {
      background-color: var(--primary-color);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .stat-row {
      display: flex;
      justify-content: space-between;
      margin: 12px 0;
      padding: 8px 0;
      border-bottom: 1px solid #eee;
    }

    .stat-row:last-child {
      border-bottom: none;
    }

    .stat-label {
      font-weight: 500;
      color: var(--text-secondary);
    }

    .stat-value {
      font-weight: 600;
      color: var(--text-primary);
    }

    .formats-container {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      max-height: 200px;
      overflow-y: auto;
    }

    .format-chip {
      font-size: 11px;
      height: 24px;
      line-height: 24px;
      background-color: #e3f2fd;
      color: #1976d2;
    }

    mat-list {
      padding: 0;
    }

    mat-list-item {
      font-size: 14px;
    }

    mat-icon[matListItemIcon] {
      color: #4caf50;
    }

    @media (max-width: 768px) {
      .library-container {
        padding: 8px;
      }
      
      .dashboard-grid {
        grid-template-columns: 1fr;
        gap: 12px;
      }
    }
  `]
})
export class LibraryManagementComponent implements OnInit {
  supportedFormats = signal<string[]>([]);
  systemFeatures = signal<string[]>([]);
  securityFeatures = signal<string[]>([]);

  constructor(private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.loadStaticData();
  }

  loadStaticData() {
    // Static data since we removed the library service
    this.supportedFormats.set([
      'epub', 'mobi', 'azw', 'azw3', 'pdf', 'cbz', 'cbr', 'fb2', 
      'txt', 'rtf', 'doc', 'docx', 'odt', 'html', 'lit', 'lrf', 
      'pdb', 'pml', 'rb', 'snb', 'tcr', 'txtz'
    ]);

    this.systemFeatures.set([
      'Multi-language title sorting (6 languages)',
      'Cursor-based pagination for performance',
      'REST API with OpenAPI documentation',
      'Hexagonal architecture design',
      'Book management with CRUD operations',
      'Advanced search functionality',
      'Reading progress tracking',
      'Type-safe configuration system'
    ]);

    this.securityFeatures.set([
      'OWASP-compliant input sanitization',
      'SQL injection protection',
      'XSS attack prevention',
      'Path traversal protection',
      'File validation with magic bytes',
      'MIME type verification',
      'Virus scanning integration points',
      'Rate limiting and request controls'
    ]);
  }
}