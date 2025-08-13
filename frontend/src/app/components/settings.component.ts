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
      padding: 24px;
    }

    .settings-container h1 {
      font-size: clamp(1.8rem, 4vw, 2.5rem);
      margin-bottom: 24px;
      font-weight: 600;
      color: var(--text-primary);
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
      grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
      gap: 20px;
      margin-top: 32px;
    }

    mat-card {
      height: fit-content;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transition: transform 0.2s ease, box-shadow 0.2s ease;
    }

    mat-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    }

    mat-card-header [mat-card-avatar] {
      background-color: var(--primary-color);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
    }

    mat-card-title {
      font-size: clamp(1.1rem, 2.5vw, 1.25rem);
      font-weight: 600;
    }

    mat-card-subtitle {
      font-size: clamp(0.875rem, 2vw, 0.95rem);
      opacity: 0.8;
    }

    .stat-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 12px 0;
      padding: 12px 0;
      border-bottom: 1px solid #eee;
      min-height: 24px;
    }

    .stat-row:last-child {
      border-bottom: none;
    }

    .stat-label {
      font-weight: 500;
      color: var(--text-secondary);
      font-size: clamp(0.875rem, 2vw, 0.95rem);
    }

    .stat-value {
      font-weight: 600;
      color: var(--text-primary);
      font-size: clamp(0.875rem, 2vw, 0.95rem);
      text-align: right;
    }

    .formats-container {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      max-height: 220px;
      overflow-y: auto;
      padding: 4px;
    }

    .format-chip {
      font-size: clamp(10px, 2vw, 11px);
      height: 28px;
      line-height: 28px;
      padding: 0 12px;
      background-color: #e3f2fd;
      color: #1976d2;
      border-radius: 14px;
      font-weight: 500;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 16px;
    }

    .stat-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      border-radius: 12px;
      transition: transform 0.2s ease, background 0.2s ease;
      min-height: 60px;
    }

    .stat-item:hover {
      transform: translateY(-1px);
      background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
    }

    .stat-icon {
      color: #1976d2;
      font-size: clamp(20px, 3vw, 24px);
      flex-shrink: 0;
    }

    .stat-content {
      display: flex;
      flex-direction: column;
      gap: 2px;
      min-width: 0;
    }

    .stat-count {
      font-size: clamp(1.25rem, 3vw, 1.5rem);
      font-weight: bold;
      color: #1976d2;
      line-height: 1.2;
    }

    .stat-name {
      font-size: clamp(0.75rem, 2vw, 0.8rem);
      color: #666;
      text-transform: capitalize;
      line-height: 1.2;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    /* Desktop styles (1200px and up) */
    @media (min-width: 1200px) {
      .settings-container {
        padding: 32px;
      }
      
      .dashboard-grid {
        grid-template-columns: repeat(3, 1fr);
        gap: 24px;
      }
    }

    /* Tablet styles (768px to 1199px) */
    @media (min-width: 768px) and (max-width: 1199px) {
      .settings-container {
        padding: 20px;
      }
      
      .dashboard-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 18px;
        margin-top: 28px;
      }

      .stats-grid {
        grid-template-columns: repeat(3, 1fr);
      }
    }

    /* Mobile styles (480px to 767px) */
    @media (min-width: 480px) and (max-width: 767px) {
      .settings-container {
        padding: 16px;
      }
      
      .dashboard-grid {
        grid-template-columns: 1fr;
        gap: 16px;
        margin-top: 24px;
      }

      .stats-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 12px;
      }

      .stat-item {
        padding: 14px;
        min-height: 56px;
      }

      .format-chip {
        height: 26px;
        line-height: 26px;
        padding: 0 10px;
      }
    }

    /* Small mobile styles (up to 479px) */
    @media (max-width: 479px) {
      .settings-container {
        padding: 12px;
      }
      
      .dashboard-grid {
        grid-template-columns: 1fr;
        gap: 14px;
        margin-top: 20px;
      }

      .stats-grid {
        grid-template-columns: 1fr;
        gap: 10px;
      }

      .stat-item {
        padding: 12px;
        min-height: 52px;
        gap: 10px;
      }

      .stat-row {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
        padding: 10px 0;
      }

      .stat-value {
        text-align: left;
        font-weight: 700;
      }

      .format-chip {
        height: 24px;
        line-height: 24px;
        padding: 0 8px;
        font-size: 10px;
      }

      .formats-container {
        max-height: 180px;
      }

      mat-card-content {
        padding: 12px !important;
      }

      mat-card-header {
        padding: 12px 12px 0 12px !important;
      }
    }

    /* Ultra-wide screen optimization */
    @media (min-width: 1600px) {
      .settings-container {
        max-width: 1400px;
        padding: 40px;
      }
      
      .dashboard-grid {
        grid-template-columns: repeat(3, 1fr);
        gap: 32px;
      }
    }

    /* High DPI display optimization */
    @media (min-resolution: 2dppx) {
      mat-card {
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
      }
      
      mat-card:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
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