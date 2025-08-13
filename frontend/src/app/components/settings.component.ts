import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { SettingsService } from '../services/settings.service';
import { SettingsResponse } from '../models/settings.model';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSnackBarModule
  ],
  template: `
    <div class="settings-container">
      <h1>⚙️ Settings</h1>
      
      @if (loading()) {
        <div class="loading-container">
          <mat-progress-spinner mode="indeterminate"></mat-progress-spinner>
          <p>Loading settings...</p>
        </div>
      } @else {
        <div class="dashboard-grid">
          <!-- Application Information -->
          <mat-card class="info-card">
            <mat-card-header>
              <div mat-card-avatar>
                <mat-icon>info</mat-icon>
              </div>
              <mat-card-title>Application Information</mat-card-title>
              <mat-card-subtitle>System details and version</mat-card-subtitle>
            </mat-card-header>
            
            <mat-card-content>
              <div class="stat-row">
                <span class="stat-label">Version:</span>
                <span class="stat-value">{{ settingsData()?.version || 'Unknown' }}</span>
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
              <mat-card-subtitle>{{ settingsData()?.supportedFormats?.length || 0 }} formats supported</mat-card-subtitle>
            </mat-card-header>
            
            <mat-card-content>
              <div class="formats-container">
                @for (format of settingsData()?.supportedFormats; track format) {
                  <mat-chip class="format-chip">{{ format.toUpperCase() }}</mat-chip>
                }
              </div>
            </mat-card-content>
          </mat-card>

          <!-- Library Statistics -->
          <mat-card class="stats-card">
            <mat-card-header>
              <div mat-card-avatar>
                <mat-icon>analytics</mat-icon>
              </div>
              <mat-card-title>Library Statistics</mat-card-title>
              <mat-card-subtitle>Content overview</mat-card-subtitle>
            </mat-card-header>
            
            <mat-card-content>
              <div class="stats-grid">
                @for (stat of getEntityStats(); track stat.key) {
                  <div class="stat-item">
                    <mat-icon class="stat-icon">{{ stat.icon }}</mat-icon>
                    <div class="stat-content">
                      <div class="stat-count">{{ stat.count }}</div>
                      <div class="stat-name">{{ stat.name }}</div>
                    </div>
                  </div>
                }
              </div>
            </mat-card-content>
          </mat-card>


        </div>
      }
    </div>
  `,
  styles: [`
    .settings-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 16px;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px;
      gap: 16px;
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

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
      gap: 16px;
    }

    .stat-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 8px;
    }

    .stat-icon {
      color: #1976d2;
    }

    .stat-content {
      display: flex;
      flex-direction: column;
    }

    .stat-count {
      font-size: 24px;
      font-weight: bold;
      color: #1976d2;
    }

    .stat-name {
      font-size: 12px;
      color: #666;
      text-transform: capitalize;
    }

    @media (max-width: 768px) {
      .settings-container {
        padding: 8px;
      }
      
      .dashboard-grid {
        grid-template-columns: 1fr;
        gap: 12px;
      }

      .stats-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }
  `]
})
export class SettingsComponent implements OnInit {
  settingsData = signal<SettingsResponse | null>(null);
  loading = signal<boolean>(true);


  constructor(private settingsService: SettingsService, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.loadSettings();
  }

  loadSettings() {
    this.loading.set(true);
    this.settingsService.getSettings().subscribe({
      next: (data) => {
        this.settingsData.set(data);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading settings:', error);
        this.snackBar.open('Failed to load settings from API, showing demo data', 'Close', { duration: 3000 });
        
        // Fallback to demo data for demonstration
        const demoData = {
          version: "1.0.0-SNAPSHOT",
          supportedFormats: ["epub", "pdf", "mobi", "azw", "azw3", "cbz", "cbr", "txt"],
          entityCounts: {
            books: 42,
            series: 8,
            authors: 15,
            publishers: 6,
            languages: 3,
            formats: 8
          }
        };
        this.settingsData.set(demoData);
        this.loading.set(false);
      }
    });
  }



  getEntityStats() {
    const counts = this.settingsData()?.entityCounts;
    if (!counts) return [];

    const iconMap: { [key: string]: string } = {
      books: 'menu_book',
      series: 'library_books',
      authors: 'person',
      publishers: 'business',
      languages: 'language',
      formats: 'description'
    };

    return Object.entries(counts).map(([key, count]) => ({
      key,
      name: key,
      count,
      icon: iconMap[key] || 'info'
    }));
  }
}