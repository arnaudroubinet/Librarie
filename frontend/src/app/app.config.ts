import { ApplicationConfig, ErrorHandler, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

import { routes } from './app.routes';

/**
 * Custom ErrorHandler that suppresses ResizeObserver errors from third-party libraries like Readium Navigator.
 * These errors don't break functionality and are common with EPUB readers using ResizeObserver APIs.
 */
class ResizeObserverErrorHandler implements ErrorHandler {
  handleError(error: Error): void {
    // Suppress ResizeObserver loop errors - these are non-critical and come from Readium Navigator
    if (error?.message?.includes('ResizeObserver loop')) {
      console.debug('[ResizeObserver] Non-critical error suppressed:', error.message);
      return;
    }
    
    // Log all other errors normally
    console.error('ERROR', error);
  }
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    provideAnimationsAsync(),
    { provide: ErrorHandler, useClass: ResizeObserverErrorHandler }
  ]
};
