import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RwpmManifest, RwpmLink } from '../models/rwpm.model';

export interface LoadedManifest {
  manifest: RwpmManifest;
  manifestUrl: string;     // absolute URL to the manifest
  resourcesBase: string;   // absolute base URL to resolve relative resource hrefs
}

@Injectable({ providedIn: 'root' })
export class ManifestService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl || '';

  getManifest(bookId: string): Observable<LoadedManifest> {
    const manifestUrl = `${this.baseUrl}/v1/books/${bookId}/manifest.json`;
    return this.http.get<RwpmManifest>(manifestUrl).pipe(
      map((mf) => this.normalize(mf, manifestUrl))
    );
  }

  private normalize(mf: RwpmManifest, manifestUrl: string): LoadedManifest {
    const origin = (typeof window !== 'undefined' && (window as any).location?.origin) || '';
    const resourcesBaseRel = manifestUrl.replace(/\/manifest\.json.*$/, '/resources/');
    const manifestUrlAbs = /^https?:\/\//i.test(manifestUrl) ? manifestUrl : `${origin}${manifestUrl}`;
    const resourcesBase = /^https?:\/\//i.test(resourcesBaseRel) ? resourcesBaseRel : `${origin}${resourcesBaseRel}`;
    const absolutize = (href?: string) => {
      if (!href) return href as any;
      if (/^https?:\/\//i.test(href)) return href;
      if (href.startsWith('/')) return `${origin}${href}`; // make absolute to current origin
      try { return new URL(href, resourcesBase).toString(); } catch { return href; }
    };

    // Absolutize helper only used for resources; keep readingOrder/toc HREFs as server provided
    const mapLinksAbs = (arr?: RwpmLink[]): RwpmLink[] => Array.isArray(arr)
      ? arr.map((l) => ({
          ...l,
          href: absolutize(l.href),
          children: mapLinksAbs(l.children)
        }))
      : [];

    const manifest: RwpmManifest = {
      ...mf,
      readingOrder: Array.isArray(mf.readingOrder) ? mf.readingOrder : [],
      resources: mapLinksAbs(mf.resources),
      toc: Array.isArray(mf.toc) ? mf.toc : []
    };

  return { manifest, manifestUrl: manifestUrlAbs, resourcesBase };
  }
}
