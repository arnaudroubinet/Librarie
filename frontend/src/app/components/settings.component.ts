import { Component, OnInit, signal, CUSTOM_ELEMENTS_SCHEMA, Inject } from '@angular/core';
import { CommonModule, DOCUMENT } from '@angular/common';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
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
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
  <div class="ngx-container settings-scope iot-scope">
    @if (loading()) {
      <section class="loading-section" aria-busy="true" aria-live="polite">
        <div class="loading-content">
          <mat-progress-spinner mode="indeterminate" diameter="60" color="accent" aria-hidden="true"></mat-progress-spinner>
          <h2 class="ngx-h2">Loading settings…</h2>
          <p class="ngx-muted">Fetching configuration and system information</p>
        </div>
      </section>
    } @else {
      @if (usingCachedWarning()) {
        <div class="warning-banner" role="status" aria-live="polite">
          <iconify-icon icon="mdi:alert" aria-hidden="true"></iconify-icon>
          The settings service is unreachable. Showing last known data.
        </div>
      }

      <!-- IoT-style control tiles -->
      <div class="iot-controls-grid" aria-label="Primary indicators">
        <div class="iot-control" role="group" aria-label="Backend">
          <div class="iot-icon" aria-hidden="true"><iconify-icon icon="mdi:server"></iconify-icon></div>
          <div class="iot-meta">
            <div class="iot-title">Backend</div>
            <div class="iot-state on">{{ (settingsData()?.version || 'Unknown') }}</div>
          </div>
        </div>
        <div class="iot-control" role="group" aria-label="Frontend">
          <div class="iot-icon" aria-hidden="true"><iconify-icon icon="mdi:webpack"></iconify-icon></div>
          <div class="iot-meta">
            <div class="iot-title">Frontend</div>
            <div class="iot-state on">{{ frontendVersion }}</div>
          </div>
        </div>
        <button class="iot-control iot-action" type="button" (click)="healthRefresh()" aria-label="Refresh and show liveness" [attr.aria-pressed]="livenessOk()">
          <div class="iot-icon" aria-hidden="true"><iconify-icon icon="mdi:heart-pulse"></iconify-icon></div>
          <div class="iot-meta">
            <div class="iot-title">Liveness</div>
            <div class="iot-state" [class.on]="livenessOk()" [class.off]="!livenessOk()">{{ livenessOk() ? 'ON' : 'OFF' }}</div>
          </div>
        </button>
        <button class="iot-control iot-action" type="button" (click)="healthRefresh()" aria-label="Refresh and show readiness" [attr.aria-pressed]="readinessOk()">
          <div class="iot-icon" aria-hidden="true"><iconify-icon icon="mdi:check-network"></iconify-icon></div>
          <div class="iot-meta">
            <div class="iot-title">Readiness</div>
            <div class="iot-state" [class.on]="readinessOk()" [class.off]="!readinessOk()">{{ readinessOk() ? 'ON' : 'OFF' }}</div>
          </div>
        </button>
      </div>

      <!-- IoT-style two-column main area -->
      <div class="iot-main-grid" aria-label="Settings sections">
        <!-- Left: Tabbed overview -->
        <section class="ngx-card hoverable iot-tab-card" aria-labelledby="overview-title">
          <div class="ngx-card-header">
            <div class="avatar" aria-hidden="true">
              <iconify-icon icon="mdi:information-outline"></iconify-icon>
            </div>
            <div class="title-group">
              <h2 class="ngx-h2" id="overview-title">System Overview</h2>
              <div class="ngx-card-subtitle">Application & Health</div>
            </div>
            <div class="iot-tab-actions" role="tablist" aria-label="Overview tabs">
              <button type="button" role="tab" [attr.aria-selected]="tab() === 'app'" class="iot-tab" (click)="selectTab('app')">Application</button>
              <button type="button" role="tab" [attr.aria-selected]="tab() === 'health'" class="iot-tab" (click)="selectTab('health')">Health</button>
            </div>
            <button class="refresh-btn btn-ghost" aria-label="Refresh" (click)="reloadSettings(true)">
              <iconify-icon icon="material-symbols-light:refresh-rounded" aria-hidden="true"></iconify-icon>
            </button>
          </div>
          <div class="ngx-card-content">
            @if (tab() === 'app') {
              <div class="stat-row">
                <span class="stat-label">Backend version</span>
                <span class="stat-value">{{ settingsData()?.version || 'Unknown' }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">Frontend version</span>
                <span class="stat-value">{{ frontendVersion }}</span>
              </div>
              <div class="formats-wrap">
                <div class="formats-title">Supported formats</div>
                <div class="chip-list" role="list">
                  @for (format of settingsData()?.supportedFormats; track format) {
                    <span class="chip" role="listitem" [attr.aria-label]="'Format ' + format">{{ format.toUpperCase() }}</span>
                  }
                </div>
              </div>
            } @else {
              <div class="health-grid">
                <div class="health-item">
                  <span class="health-label">Liveness</span>
                  <span class="status-badge" [ngClass]="livenessOk() ? 'ok' : 'ko'">{{ livenessOk() ? 'UP' : 'DOWN' }}</span>
                </div>
                <div class="health-item">
                  <span class="health-label">Readiness</span>
                  <span class="status-badge" [ngClass]="readinessOk() ? 'ok' : 'ko'">{{ readinessOk() ? 'UP' : 'DOWN' }}</span>
                </div>
              </div>
              @if (readinessChecks().length) {
                <div class="readiness-list" aria-label="Readiness checks">
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
            }
          </div>
        </section>

        <!-- Right: Quick stats -->
        <section class="ngx-card hoverable iot-quick-card" aria-labelledby="quick-stats-title">
          <div class="ngx-card-header">
            <div class="avatar" aria-hidden="true">
              <iconify-icon icon="mdi:view-dashboard"></iconify-icon>
            </div>
            <div class="title-group">
              <h2 class="ngx-h2" id="quick-stats-title">Quick Stats</h2>
              <div class="ngx-card-subtitle">Library overview</div>
            </div>
            <button class="refresh-btn btn-ghost" aria-label="Refresh statistics" (click)="reloadSettings(true)">
              <iconify-icon icon="material-symbols-light:refresh-rounded" aria-hidden="true"></iconify-icon>
            </button>
          </div>
          <div class="ngx-card-content">
            <ul class="quick-list" role="list">
              @for (stat of getEntityStats(); track stat.key) {
                <li class="quick-item" role="listitem">
                  <iconify-icon class="quick-icon" [icon]="stat.icon" aria-hidden="true"></iconify-icon>
                  <div class="quick-meta">
                    <div class="quick-name">{{ stat.name }}</div>
                    <div class="quick-count">{{ stat.count }}</div>
                  </div>
                </li>
              }
            </ul>
          </div>
        </section>
      </div>

      <!-- Bottom row: Formats as dedicated card (compact) -->
      <div class="iot-bottom-grid">
        <section class="ngx-card hoverable" aria-labelledby="formats-title">
          <div class="ngx-card-header">
            <div class="avatar" aria-hidden="true">
              <iconify-icon icon="mdi:file-document-outline"></iconify-icon>
            </div>
            <div class="title-group">
              <h2 class="ngx-h2" id="formats-title">Supported Formats</h2>
              <div class="ngx-card-subtitle">{{ settingsData()?.supportedFormats?.length || 0 }} formats supported</div>
            </div>
            <button class="refresh-btn btn-ghost" aria-label="Refresh formats" (click)="reloadSettings(true)">
              <iconify-icon icon="material-symbols-light:refresh-rounded" aria-hidden="true"></iconify-icon>
            </button>
          </div>
          <div class="ngx-card-content">
            <div class="chip-list" role="list">
              @for (format of settingsData()?.supportedFormats; track format) {
                <span class="chip" role="listitem" [attr.aria-label]="'Format ' + format">{{ format.toUpperCase() }}</span>
              }
            </div>
          </div>
        </section>
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
  // IoT-style tab selection: 'app' or 'health'
  tab = signal<'app' | 'health'>('app');
  frontendVersion = (pkg as any).version ?? '0.0.0';
  private healthIntervalId: any;


  constructor(
    private settingsService: SettingsService,
    private snackBar: MatSnackBar,
    @Inject(DOCUMENT) private document: Document
  ) {}

  ngOnInit() {
    // Apply ngx-admin-like dark theme to the whole page while on Settings
    this.document.body.classList.add('theme-ngx');
    this.loadSettings();
    this.checkHealth();
    // Poll health probes every minute
    this.healthIntervalId = setInterval(() => this.checkHealth(), 60_000);
  }

  ngOnDestroy() {
    // Remove theme class on leave to avoid leaking styles to other pages
    this.document.body.classList.remove('theme-ngx');
    if (this.healthIntervalId) {
      clearInterval(this.healthIntervalId);
      this.healthIntervalId = null;
    }
  }

  selectTab(next: 'app' | 'health') {
    this.tab.set(next);
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