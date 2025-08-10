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
import { LibraryService } from '../services/library.service';
import { LibraryStats, SupportedFormatsResponse, ScanResult } from '../models/book.model';

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
      
      <!-- Library Statistics -->
      <mat-card class="stats-card">
        <mat-card-header>
          <mat-card-title>
            <mat-icon>analytics</mat-icon>
            Library Statistics
          </mat-card-title>
        </mat-card-header>
        <mat-card-content>
          @if (statsLoading()) {
            <div class="loading-inline">
              <mat-spinner diameter="30"></mat-spinner>
              <span>Loading statistics...</span>
            </div>
          } @else if (libraryStats()) {
            <div class="stats-grid">
              <div class="stat-item">
                <mat-icon>book_online</mat-icon>
                <div>
                  <strong>Version</strong>
                  <p>{{ libraryStats()!.version }}</p>
                </div>
              </div>
              <div class="stat-item">
                <mat-icon>file_present</mat-icon>
                <div>
                  <strong>Supported Formats</strong>
                  <p>{{ libraryStats()!.supportedFormats }} formats</p>
                </div>
              </div>
            </div>
            
            <mat-divider class="my-3"></mat-divider>
            
            <h3>Features</h3>
            <div class="features-list">
              @for (feature of libraryStats()!.features; track feature) {
                <mat-chip class="feature-chip">{{ feature }}</mat-chip>
              }
            </div>
          }
        </mat-card-content>
      </mat-card>

      <!-- Supported Formats -->
      <mat-card class="formats-card">
        <mat-card-header>
          <mat-card-title>
            <mat-icon>description</mat-icon>
            Supported File Formats
          </mat-card-title>
          <mat-card-subtitle>
            eBook formats that can be automatically ingested
          </mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          @if (formatsLoading()) {
            <div class="loading-inline">
              <mat-spinner diameter="30"></mat-spinner>
              <span>Loading formats...</span>
            </div>
          } @else if (supportedFormats()) {
            <div class="formats-grid">
              @for (format of supportedFormats()!.supportedFormats; track format) {
                <mat-chip class="format-chip">.{{ format }}</mat-chip>
              }
            </div>
            <p class="format-count">
              <strong>{{ supportedFormats()!.count }}</strong> formats supported
            </p>
          }
        </mat-card-content>
        <mat-card-actions>
          <button mat-button (click)="loadSupportedFormats()">
            <mat-icon>refresh</mat-icon>
            Refresh
          </button>
        </mat-card-actions>
      </mat-card>

      <!-- Library Actions -->
      <mat-card class="actions-card">
        <mat-card-header>
          <mat-card-title>
            <mat-icon>build</mat-icon>
            Library Actions
          </mat-card-title>
          <mat-card-subtitle>
            Manage and maintain your library
          </mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div class="actions-grid">
            <div class="action-item">
              <h3>Scan Ingest Directory</h3>
              <p>Scan for new eBook files in the ingest directory and add them to the library.</p>
              <button 
                mat-raised-button 
                color="primary" 
                (click)="scanIngestDirectory()"
                [disabled]="scanLoading()">
                @if (scanLoading()) {
                  <mat-spinner diameter="20"></mat-spinner>
                } @else {
                  <mat-icon>folder_open</mat-icon>
                }
                Scan Directory
              </button>
            </div>
            
            <div class="action-item">
              <h3>Refresh Library</h3>
              <p>Manually trigger a complete refresh of the library database.</p>
              <button 
                mat-raised-button 
                color="accent" 
                (click)="refreshLibrary()"
                [disabled]="refreshLoading()">
                @if (refreshLoading()) {
                  <mat-spinner diameter="20"></mat-spinner>
                } @else {
                  <mat-icon>refresh</mat-icon>
                }
                Refresh Library
              </button>
            </div>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Recent Actions Results -->
      @if (lastScanResult()) {
        <mat-card class="result-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon [style.color]="lastScanResult()!.status === 'success' ? 'green' : 'red'">
                {{ lastScanResult()!.status === 'success' ? 'check_circle' : 'error' }}
              </mat-icon>
              Last Scan Result
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p><strong>Status:</strong> {{ lastScanResult()!.status }}</p>
            <p><strong>Message:</strong> {{ lastScanResult()!.message }}</p>
            @if (lastScanResult()!.count !== undefined) {
              <p><strong>Files Processed:</strong> {{ lastScanResult()!.count }}</p>
            }
            @if (lastScanResult()!.processedFiles !== undefined) {
              <p><strong>Total Processed:</strong> {{ lastScanResult()!.processedFiles }}</p>
            }
            @if (lastScanResult()!.ingestedBooks && lastScanResult()!.ingestedBooks!.length > 0) {
              <h4>Ingested Books:</h4>
              <mat-list>
                @for (book of lastScanResult()!.ingestedBooks!; track book) {
                  <mat-list-item>
                    <mat-icon matListItemIcon>book</mat-icon>
                    {{ book }}
                  </mat-list-item>
                }
              </mat-list>
            }
          </mat-card-content>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .library-container {
      max-width: 1000px;
      margin: 0 auto;
      padding: 16px;
    }

    .stats-card, .formats-card, .actions-card, .result-card {
      margin-bottom: 24px;
    }

    .loading-inline {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin-bottom: 16px;
    }

    .stat-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background-color: #f5f5f5;
      border-radius: 8px;
    }

    .stat-item mat-icon {
      color: var(--primary-color);
      font-size: 32px;
      width: 32px;
      height: 32px;
    }

    .stat-item p {
      margin: 4px 0 0 0;
      color: var(--text-secondary);
    }

    .features-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .feature-chip {
      background-color: #e3f2fd;
      color: #1976d2;
    }

    .formats-grid {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 16px;
    }

    .format-chip {
      background-color: #f3e5f5;
      color: #7b1fa2;
      font-family: monospace;
    }

    .format-count {
      margin: 16px 0 0 0;
      color: var(--text-secondary);
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 24px;
    }

    .action-item {
      padding: 16px;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
    }

    .action-item h3 {
      margin: 0 0 8px 0;
      color: var(--text-primary);
    }

    .action-item p {
      margin: 0 0 16px 0;
      color: var(--text-secondary);
      font-size: 14px;
    }

    .action-item button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .my-3 {
      margin: 24px 0;
    }

    .result-card {
      border-left: 4px solid #4caf50;
    }

    .result-card[data-status="error"] {
      border-left-color: #f44336;
    }

    @media (max-width: 768px) {
      .library-container {
        padding: 8px;
      }
      
      .stats-grid {
        grid-template-columns: 1fr;
      }
      
      .actions-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class LibraryManagementComponent implements OnInit {
  libraryStats = signal<LibraryStats | null>(null);
  supportedFormats = signal<SupportedFormatsResponse | null>(null);
  lastScanResult = signal<ScanResult | null>(null);
  
  statsLoading = signal(false);
  formatsLoading = signal(false);
  scanLoading = signal(false);
  refreshLoading = signal(false);

  constructor(
    private libraryService: LibraryService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadLibraryStats();
    this.loadSupportedFormats();
  }

  loadLibraryStats() {
    this.statsLoading.set(true);
    this.libraryService.getLibraryStats().subscribe({
      next: (stats) => {
        this.libraryStats.set(stats);
        this.statsLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading library stats:', error);
        this.snackBar.open('Failed to load library statistics.', 'Close', {
          duration: 3000
        });
        this.statsLoading.set(false);
      }
    });
  }

  loadSupportedFormats() {
    this.formatsLoading.set(true);
    this.libraryService.getSupportedFormats().subscribe({
      next: (formats) => {
        this.supportedFormats.set(formats);
        this.formatsLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading supported formats:', error);
        this.snackBar.open('Failed to load supported formats.', 'Close', {
          duration: 3000
        });
        this.formatsLoading.set(false);
      }
    });
  }

  scanIngestDirectory() {
    this.scanLoading.set(true);
    this.libraryService.scanIngestDirectory().subscribe({
      next: (result) => {
        this.lastScanResult.set(result);
        this.scanLoading.set(false);
        this.snackBar.open('Ingest directory scan completed!', 'Close', {
          duration: 3000
        });
      },
      error: (error) => {
        console.error('Error scanning ingest directory:', error);
        this.snackBar.open('Failed to scan ingest directory.', 'Close', {
          duration: 3000
        });
        this.scanLoading.set(false);
      }
    });
  }

  refreshLibrary() {
    this.refreshLoading.set(true);
    this.libraryService.refreshLibrary().subscribe({
      next: (result) => {
        this.lastScanResult.set(result);
        this.refreshLoading.set(false);
        this.snackBar.open('Library refresh completed!', 'Close', {
          duration: 3000
        });
        // Reload stats after refresh
        this.loadLibraryStats();
      },
      error: (error) => {
        console.error('Error refreshing library:', error);
        this.snackBar.open('Failed to refresh library.', 'Close', {
          duration: 3000
        });
        this.refreshLoading.set(false);
      }
    });
  }
}