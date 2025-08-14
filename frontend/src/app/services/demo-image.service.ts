import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, shareReplay } from 'rxjs';

/**
 * DemoImageService loads a client-side mapping of series names/ids to image URLs
 * from a static JSON file placed under public/demo/series-images.json.
 * This is intended for demo environments only and avoids hardcoding any
 * copyrighted image URLs in the codebase.
 */
@Injectable({ providedIn: 'root' })
export class DemoImageService {
  private readonly mappingUrl = '/demo/series-images.json';

  // Cache the mapping to avoid repeated HTTP requests
  private mapping$?: Observable<Record<string, string>>;

  constructor(private http: HttpClient) {}

  /**
   * Returns the demo mapping as a dictionary { key: imageUrl }.
   * Keys can be series id or series name (case-insensitive).
   */
  getMapping(): Observable<Record<string, string>> {
    if (!this.mapping$) {
      this.mapping$ = this.http.get<Record<string, string>>(this.mappingUrl).pipe(
        // If file is missing or invalid, fall back to empty mapping
        map((data) => (data && typeof data === 'object' ? data : {})),
        shareReplay(1)
      );
    }
    return this.mapping$;
  }

  /**
   * Given a series identifier and name, returns a mapped image URL if available.
   */
  getSeriesImage(id?: string, name?: string): Observable<string | undefined> {
    return this.getMapping().pipe(
      map((mapping) => {
        if (!mapping) return undefined;
        // Try id first
        if (id && mapping[id]) return mapping[id];

        // Try name exact match
        if (name && mapping[name]) return mapping[name];

        // Try normalized name (lowercase, trimmed)
        if (name) {
          const normalized = name.trim().toLowerCase();
          for (const key of Object.keys(mapping)) {
            if (key.trim().toLowerCase() === normalized) {
              return mapping[key];
            }
          }
        }
        return undefined;
      })
    );
  }
}
