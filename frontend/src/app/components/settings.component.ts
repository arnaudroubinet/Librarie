import { Component, OnInit, signal, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MATERIAL_MODULES } from '../shared/materials';
import { SettingsService } from '../services/settings.service';
import { SettingsResponse } from '../models/settings.model';
import pkg from '../../../package.json';

@Component({
  selector: 'app-settings',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    ...MATERIAL_MODULES
  ],
  template: `
  <div class="motspassants-library">
      <div class="library-header">
        <div class="header-content">
          <h1 class="library-title">
            <iconify-icon class="title-icon" icon="material-symbols:settings-outline"></iconify-icon>
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
          @if (usingCachedWarning()) {
            <div class="warning-banner">
              <iconify-icon icon="mdi:alert"></iconify-icon>
              The settings service is unreachable. Showing last known data.
            </div>
          }
          <div class="dashboard-grid">
            <!-- Left column stack: keep Application info and Health together -->
            <div class="stacked-column">
              <!-- Application Information -->
              <mat-card class="info-card dark-card">
                <mat-card-header>
                  <div mat-card-avatar class="avatar">
                    <iconify-icon icon="mdi:information-outline"></iconify-icon>
                  </div>
                  <mat-card-title>
                    Application Information
                    <button mat-icon-button class="refresh-btn" aria-label="Refresh application info" (click)="reloadSettings(true)">
                      <iconify-icon icon="material-symbols-light:refresh-rounded"></iconify-icon>
                    </button>
                  </mat-card-title>
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

              <!-- Liveness & Readiness -->
              <mat-card class="health-card dark-card">
                <mat-card-header>
                  <div mat-card-avatar class="avatar">
                    <iconify-icon icon="mdi:heart-pulse"></iconify-icon>
                  </div>
                  <mat-card-title>
                    Liveness & Readiness
                    <button mat-icon-button class="refresh-btn" aria-label="Refresh health" (click)="healthRefresh()">
                      <iconify-icon icon="material-symbols-light:refresh-rounded"></iconify-icon>
                    </button>
                  </mat-card-title>
                  <mat-card-subtitle>Backend health status</mat-card-subtitle>
                </mat-card-header>
                <mat-card-content>
                  <div class="health-grid">
                    <div class="health-item">
                      <span class="health-label">Liveness</span>
                      <span class="status-badge" [ngClass]="livenessOk() ? 'ok' : 'ko'">
                        {{ livenessOk() ? 'OK' : 'KO' }}
                      </span>
                    </div>
                    <div class="health-item">
                      <span class="health-label">Readiness</span>
                      <span class="status-badge" [ngClass]="readinessOk() ? 'ok' : 'ko'">
                        {{ readinessOk() ? 'OK' : 'KO' }}
                      </span>
                    </div>
                  </div>

                  <!-- Readiness checks (dynamic submodules) -->
                  @if (readinessChecks().length) {
                    <div class="readiness-list">
                      @for (chk of readinessChecks(); track chk.name) {
                        <div class="health-item">
                          <span class="health-label">{{ chk.name }}</span>
                          <span class="status-badge" [ngClass]="(chk.status || '').toLowerCase() === 'up' ? 'ok' : 'ko'">
                            {{ (chk.status || 'UNKNOWN').toUpperCase() }}
                          </span>
                        </div>
                      }
                    </div>
                  }
                </mat-card-content>
              </mat-card>
            </div>

            <!-- Supported Formats -->
            <mat-card class="formats-card dark-card">
              <mat-card-header>
                <div mat-card-avatar class="avatar">
                  <iconify-icon icon="mdi:file-document-outline"></iconify-icon>
                </div>
                <mat-card-title>
                  Supported Formats
                  <button mat-icon-button class="refresh-btn" aria-label="Refresh formats" (click)="reloadSettings(true)">
                    <iconify-icon icon="material-symbols-light:refresh-rounded"></iconify-icon>
                  </button>
                </mat-card-title>
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
                <mat-card-title>
                  Library Statistics
                  <button mat-icon-button class="refresh-btn" aria-label="Refresh statistics" (click)="reloadSettings(true)">
                    <iconify-icon icon="material-symbols-light:refresh-rounded"></iconify-icon>
                  </button>
                </mat-card-title>
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
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  settingsData = signal<SettingsResponse | null>(null);
  loading = signal<boolean>(true);
  usingCachedWarning = signal<{ since: number } | null>(null);
  private _livenessOk = signal<boolean | null>(null);
  private _readinessOk = signal<boolean | null>(null);
  private _readinessChecks = signal<Array<{ name: string; status: string }>>([]);
  frontendVersion = (pkg as any).version ?? '0.0.0';
  private healthIntervalId: any;


  constructor(private settingsService: SettingsService, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.loadSettings();
    this.checkHealth();
    // Poll health probes every minute
    this.healthIntervalId = setInterval(() => this.checkHealth(), 60_000);
  }

  ngOnDestroy() {
    if (this.healthIntervalId) {
      clearInterval(this.healthIntervalId);
      this.healthIntervalId = null;
    }
  }

  loadSettings(force = false) {
    this.loading.set(true);
    this.settingsService.getSettings(force).subscribe({
      next: (data) => {
        this.settingsData.set(data);
        this.loading.set(false);
        this.usingCachedWarning.set(null);
      },
      error: (error) => {
        console.error('Error loading settings:', error);
  // Keep old data (if any) and show a temporary banner; if no prior data, try cache
  const cached = this.settingsService.getCacheEntry();
  if (!this.settingsData() && cached) {
    this.settingsData.set(cached.data);
  }
  this.loading.set(false);
  this.usingCachedWarning.set({ since: Date.now() });
  this.snackBar.open('Settings API unavailable. Showing last known data.', 'Close', { duration: 3000 });
      }
    });
  }

  reloadSettings(force = false) {
    const wasForce = !!force;
    this.loadSettings(force);
    if (wasForce) {
      this.snackBar.open('Refreshing settings…', undefined, { duration: 1500 });
    }
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
        const checks = Array.isArray(res?.checks) ? res.checks : [];
        // Quarkus readiness probe usually returns an array of { name, status, data? }
        const parsed = checks
          .filter((c: any) => c && typeof c === 'object')
          .map((c: any) => ({
            name: (c.name ?? 'Unnamed check') as string,
            status: (c.status ?? 'UNKNOWN') as string
          }));
        this._readinessChecks.set(parsed);
      },
      error: () => {
        this._readinessOk.set(false);
        this._readinessChecks.set([]);
      }
    });
  }

  livenessOk() { return this._livenessOk() === true; }
  readinessOk() { return this._readinessOk() === true; }
  healthRefresh() {
    this.checkHealth();
  }
  readinessChecks() { return this._readinessChecks(); }



  getEntityStats() {
    const counts = this.settingsData()?.entityCounts;
    if (!counts) return [];

    const iconMap: { [key: string]: string } = {
      books: 'material-symbols:book-2-rounded',
      series: 'material-symbols:books-movies-and-music',
      authors: 'material-symbols:supervised-user-circle',
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