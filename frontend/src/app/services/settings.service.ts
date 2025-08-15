import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { SettingsResponse } from '../models/settings.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  private baseUrl = environment.apiUrl || 'http://localhost:8080';
  private static readonly CACHE_KEY = 'settingsCache:v1';
  private static readonly TTL_MS = 60 * 60 * 1000; // 1 hour

  constructor(private http: HttpClient) {}

  getSettings(forceRefresh = false): Observable<SettingsResponse> {
    if (!forceRefresh) {
      const cached = this.safeReadCache();
      if (cached && Date.now() - cached.timestamp < SettingsService.TTL_MS) {
        return of(cached.data as SettingsResponse);
      }
    }

    return this.http
      .get<SettingsResponse>(`${this.baseUrl}/v1/settings`)
      .pipe(tap((data) => this.safeWriteCache(data)));
  }

  private safeReadCache(): { timestamp: number; data: SettingsResponse } | null {
    try {
      const raw = localStorage.getItem(SettingsService.CACHE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  private safeWriteCache(data: SettingsResponse): void {
    try {
      localStorage.setItem(
        SettingsService.CACHE_KEY,
        JSON.stringify({ timestamp: Date.now(), data })
      );
    } catch {
      // ignore storage errors
    }
  }

  getHealthLiveness(): Observable<any> {
    return this.http.get(`${this.baseUrl}/q/health/live`);
  }

  getHealthReadiness(): Observable<any> {
    return this.http.get(`${this.baseUrl}/q/health/ready`);
  }

  // Expose cache entry for consumers to display cached timestamp/data when API is unavailable
  getCacheEntry(): { timestamp: number; data: SettingsResponse } | null {
    return this.safeReadCache();
  }
}