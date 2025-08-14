import { Component, OnInit, signal, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { SettingsService } from '../services/settings.service';
import { SettingsResponse } from '../models/settings.model';
import pkg from '../../../package.json';

@Component({
  selector: 'app-settings',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
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
    <div class="plex-library">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <iconify-icon class="title-icon" icon="mdi:cog"></iconify-icon>
            Settings
          </h1>
          <p class="library-subtitle">Configure and explore your library system</p>
        </div>
      </div>

      @if (loading()) {
        <div class="loading-section">
          <div class="loading-content">
            <mat-progress-spinner mode="indeterminate" diameter="60" color="accent"></mat-progress-spinner>
            <h3>Loading settings...</h3>
            <p>Fetching configuration and system information</p>
          </div>
        </div>
      } @else {
        <div class="library-content">
          <div class="dashboard-grid">
            <!-- Application Information -->
            <mat-card class="info-card dark-card">
              <mat-card-header>
                <div mat-card-avatar class="avatar">
                  <iconify-icon icon="mdi:information-outline"></iconify-icon>
                </div>
                <mat-card-title>Application Information</mat-card-title>
                <mat-card-subtitle>System details and version</mat-card-subtitle>
              </mat-card-header>
              
              <mat-card-content>
                <div class="stat-row">
                  <span class="stat-label">Backend version:</span>
                  <span class="stat-value">{{ settingsData()?.version || 'Unknown' }}</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">Frontend version:</span>
                  <span class="stat-value">{{ frontendVersion }}</span>
                </div>
              </mat-card-content>
            </mat-card>

            <!-- Liveness & Readiness (placed after Application Information) -->
            <mat-card class="health-card dark-card">
              <mat-card-header>
                <div mat-card-avatar class="avatar">
                  <iconify-icon icon="mdi:heart-pulse"></iconify-icon>
                </div>
                <mat-card-title>Liveness & Readiness</mat-card-title>
                <mat-card-subtitle>Backend health status</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content>
                <div class="health-grid">
                  <div class="health-item">
                    <span class="health-label">Liveness</span>
                    <button mat-raised-button [ngClass]="livenessOk() ? 'ok' : 'ko'">
                      {{ livenessOk() ? 'OK' : 'KO' }}
                    </button>
                  </div>
                  <div class="health-item">
                    <span class="health-label">Readiness</span>
                    <button mat-raised-button [ngClass]="readinessOk() ? 'ok' : 'ko'">
                      {{ readinessOk() ? 'OK' : 'KO' }}
                    </button>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>

            <!-- Supported Formats -->
            <mat-card class="formats-card dark-card">
              <mat-card-header>
                <div mat-card-avatar class="avatar">
                  <iconify-icon icon="mdi:file-document-outline"></iconify-icon>
                </div>
                <mat-card-title>Supported Formats</mat-card-title>
                <mat-card-subtitle>{{ settingsData()?.supportedFormats?.length || 0 }} formats supported</mat-card-subtitle>
              </mat-card-header>
              
              <mat-card-content>
                <div class="stats-grid format-grid">
                  @for (format of settingsData()?.supportedFormats; track format) {
                    <div class="stat-item format-item">
                      <iconify-icon class="stat-icon format-icon" icon="mdi:file-document-outline"></iconify-icon>
                      <div class="stat-content">
                        <div class="stat-count format-text">{{ format.toUpperCase() }}</div>
                      </div>
                    </div>
                  }
                </div>
              </mat-card-content>
            </mat-card>

            <!-- Library Statistics -->
            <mat-card class="stats-card dark-card">
              <mat-card-header>
                <div mat-card-avatar class="avatar">
                  <iconify-icon icon="mdi:chart-bar"></iconify-icon>
                </div>
                <mat-card-title>Library Statistics</mat-card-title>
                <mat-card-subtitle>Content overview</mat-card-subtitle>
              </mat-card-header>
              
              <mat-card-content>
                <div class="stats-grid">
                  @for (stat of getEntityStats(); track stat.key) {
                    <div class="stat-item">
                      <iconify-icon class="stat-icon" [icon]="stat.icon"></iconify-icon>
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
        </div>
      }
    </div>
  `,
  styles: [`
    .plex-library {
      min-height: 100vh;
      background: transparent;
      color: #ffffff;
      padding: 0;
    }

    .library-header {
      background: transparent;
      padding: 24px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0;
    }

    .header-content { flex: 1; }

    .library-title {
      font-size: 20px;
      font-weight: 600;
      margin: 0;
      color: #ffffff;
      display: flex;
      align-items: center;
      gap: 12px;
      letter-spacing: -0.5px;
    }

    .title-icon { font-size: 32px; width: 32px; height: 32px; color: #e5a00d; margin-right: 12px; }

    .library-subtitle {
      font-size: 0.95rem;
      margin: 4px 0 0 0;
      opacity: 0.85;
      color: #cfcfcf;
    }

    .loading-section {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 60vh;
      text-align: center;
    }

    .loading-content h3 { margin: 24px 0 8px 0; font-size: 1.5rem; font-weight: 400; }
    .loading-content p { margin: 0; opacity: 0.7; font-size: 1rem; }

    .library-content { padding: 0 20px 24px 20px; }

    /* Masonry-like responsive columns */
    .dashboard-grid {
      column-count: 3;
      column-gap: 20px;
      margin-top: 8px;
    }
    @media (max-width: 1200px) { .dashboard-grid { column-count: 2; } }
    @media (max-width: 768px) { .dashboard-grid { column-count: 1; } }
    .dashboard-grid > * { break-inside: avoid; display: block; margin-bottom: 20px; }

    mat-card.dark-card { background: #222; color: #fff; border-radius: 12px; border: 1px solid rgba(255,255,255,0.06); }
    mat-card.dark-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.35); }
    mat-card-title { font-size: clamp(1.1rem, 2.5vw, 1.25rem); font-weight: 600; }
    mat-card-subtitle { font-size: clamp(0.875rem, 2vw, 0.95rem); opacity: 0.8; color: #cfcfcf; }
    mat-card-header [mat-card-avatar].avatar { background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%); color: #0c1722; display: flex; align-items: center; justify-content: center; border-radius: 8px; }
    mat-card-header [mat-card-avatar].avatar iconify-icon { color: #0c1722; font-size: 22px; }

    .stat-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 12px 0;
      padding: 12px 0;
      border-bottom: 1px solid rgba(255,255,255,0.08);
      min-height: 24px;
    }

    .stat-row:last-child {
      border-bottom: none;
    }

    .stat-label {
      font-weight: 500;
      color: #cfcfcf;
      font-size: clamp(0.875rem, 2vw, 0.95rem);
    }

    .stat-value {
      font-weight: 600;
      color: #ffffff;
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
      background-color: rgba(41, 182, 246, 0.15);
      color: #4fc3f7;
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
      background: linear-gradient(135deg, rgba(79, 195, 247, 0.12) 0%, rgba(29, 124, 187, 0.12) 100%);
      border-radius: 12px;
      transition: transform 0.2s ease, background 0.2s ease;
      min-height: 60px;
    }

    .stat-item:hover {
      transform: translateY(-1px);
      background: linear-gradient(135deg, rgba(79, 195, 247, 0.18) 0%, rgba(29, 124, 187, 0.18) 100%);
    }

    .stat-icon {
      color: #4fc3f7;
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
      color: #4fc3f7;
      line-height: 1.2;
    }

    .stat-name {
      font-size: clamp(0.75rem, 2vw, 0.8rem);
      color: #cfcfcf;
      text-transform: capitalize;
      line-height: 1.2;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

  /* Health section */
  .health-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; }
  .health-item { display: flex; align-items: center; justify-content: space-between; background: rgba(255,255,255,0.04); padding: 12px 14px; border-radius: 10px; }
  .health-label { color: #cfcfcf; font-weight: 500; }
  .health-item button.ok { background: #2e7d32; color: #fff; }
  .health-item button.ko { background: #c62828; color: #fff; }

  /* Formats-specific size reductions */
  .format-grid { grid-template-columns: repeat(auto-fit, minmax(90px, 1fr)); gap: 8px; }
  .format-item { padding: 6px 8px; min-height: 36px; gap: 6px; border-radius: 10px; }
  .format-icon { font-size: 14px; }
  .format-text { font-size: clamp(0.75rem, 2.2vw, 0.9rem); font-weight: 600; line-height: 1.1; }
  .format-item .stat-content { gap: 0; }

    /* Desktop styles (1200px and up) */
    @media (min-width: 1200px) {
  .dashboard-grid { column-count: 3; column-gap: 24px; }
    }

    /* Tablet styles (768px to 1199px) */
    @media (min-width: 768px) and (max-width: 1199px) {
  .dashboard-grid { column-count: 2; column-gap: 18px; margin-top: 8px; }

      .stats-grid {
        grid-template-columns: repeat(3, 1fr);
      }
    }

    /* Mobile styles (480px to 767px) */
    @media (min-width: 480px) and (max-width: 767px) {
  .dashboard-grid { column-count: 1; column-gap: 16px; margin-top: 8px; }

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
  .dashboard-grid { column-count: 1; column-gap: 14px; margin-top: 8px; }

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

  mat-card-content { padding: 12px !important; }
  mat-card-header { padding: 12px 12px 0 12px !important; }
    }

    /* Ultra-wide screen optimization */
    @media (min-width: 1600px) {
  .dashboard-grid { column-count: 3; column-gap: 32px; }
    }

    /* High DPI display optimization */
    @media (min-resolution: 2dppx) {
  mat-card.dark-card { box-shadow: 0 1px 4px rgba(0, 0, 0, 0.32); }
  mat-card.dark-card:hover { box-shadow: 0 2px 12px rgba(0, 0, 0, 0.5); }
    }
  `]
})
export class SettingsComponent implements OnInit {
  settingsData = signal<SettingsResponse | null>(null);
  loading = signal<boolean>(true);
  private _livenessOk = signal<boolean | null>(null);
  private _readinessOk = signal<boolean | null>(null);
  frontendVersion = (pkg as any).version ?? '0.0.0';


  constructor(private settingsService: SettingsService, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.loadSettings();
  this.checkHealth();
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

  private checkHealth() {
    // Liveness
    this.settingsService.getHealthLiveness().subscribe({
      next: (res: any) => {
        this._livenessOk.set(res?.status?.toLowerCase?.() === 'up');
      },
      error: () => this._livenessOk.set(false)
    });

    // Readiness
    this.settingsService.getHealthReadiness().subscribe({
      next: (res: any) => {
        this._readinessOk.set(res?.status?.toLowerCase?.() === 'up');
      },
      error: () => this._readinessOk.set(false)
    });
  }

  livenessOk() { return this._livenessOk() === true; }
  readinessOk() { return this._readinessOk() === true; }



  getEntityStats() {
    const counts = this.settingsData()?.entityCounts;
    if (!counts) return [];

    const iconMap: { [key: string]: string } = {
      books: 'ph:books-thin',
      series: 'icon-park-outline:bookshelf',
      authors: 'ph:users-three-thin',
      publishers: 'mdi:domain',
      languages: 'mdi:translate',
      formats: 'mdi:file-document-outline'
    };

    return Object.entries(counts).map(([key, count]) => ({
      key,
      name: key,
      count,
      icon: iconMap[key] || 'info'
    }));
  }
}