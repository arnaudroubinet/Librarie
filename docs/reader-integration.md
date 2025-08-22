# Reader Integration Notes

This document maps the RWPM-based reader spec to our Angular app and highlights implementation steps for contributors.

## Components and Services

- `EbookReaderComponent` (standalone): Main reader UI. Supports Readium EpubNavigator when available; falls back to a sandboxed iframe that loads RWPM reading order items.
- `ManifestService`: Loads `/v1/books/:id/manifest.json` and normalizes `readingOrder`, `resources`, and `toc` hrefs to absolute URLs under `/v1/books/:id/resources/`.
- `ReadingProgressService`: Persists progress to backend and exposes a signal + observable for live updates. Supports Readium `Locator` objects.

## Data Flow

1. Route `/books/:id/read` resolves book metadata, progress, then manifest via `ManifestService`.
2. Reader tries to initialize `EpubNavigator` with a `Publication` built from the manifest. Preferences (theme, flow, spread, font) are applied.
3. If navigator fails or renders nothing within a grace period, the fallback iframe renders the first spine item and provides scroll-based progress and simple TOC navigation.

## Settings and Themes

Reader settings are stored per-book in `localStorage` with key `reader:settings:<bookId>`. Supported:
- fontSize, theme (default|sepia|night), paginated/scroll, spread (auto|none), serif/sans, publisherStyles, hyphenate.

## Security Considerations

- Fallback iframe uses `sandbox` with `allow-scripts allow-same-origin` so internal anchors and JS in EPUB resources work. Consider CSP and strict resource origins at the backend level.
- Navigator path uses Readium fetcher; URLs are normalized to our resources endpoint.

## Next Steps

- Add loading indicator and error states in `EbookReaderComponent` template.
- Improve accessibility (ARIA on controls, focus management for TOC panel).
- Keyboard shortcuts are implemented; consider a help overlay.
- Optional: Service Worker + IndexedDB for offline cache of resources.
