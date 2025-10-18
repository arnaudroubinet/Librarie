import { Injectable, LOCALE_ID, inject } from '@angular/core';

export interface Language {
  code: string;
  name: string;
  nativeName: string;
}

@Injectable({
  providedIn: 'root'
})
export class LanguageService {
  private readonly localeId = inject(LOCALE_ID);
  
  readonly availableLanguages: Language[] = [
    { code: 'en-US', name: 'English', nativeName: 'English' },
    { code: 'fr', name: 'French', nativeName: 'Français' }
  ];

  getCurrentLocale(): string {
    return this.localeId;
  }

  getCurrentLanguage(): Language {
    return this.availableLanguages.find(lang => lang.code === this.localeId) 
      || this.availableLanguages[0];
  }

  switchLanguage(localeCode: string): void {
    const language = this.availableLanguages.find(lang => lang.code === localeCode);
    if (!language) {
      console.error(`Language ${localeCode} not found`);
      return;
    }

    // Store preference in localStorage
    localStorage.setItem('preferredLocale', localeCode);

    // Redirect to the same path but with the new locale
    const currentPath = window.location.pathname;
    const currentLocale = this.getCurrentLocale();
    
    // Build the new URL with the selected locale
    let newPath: string;
    if (currentPath.startsWith(`/${currentLocale}/`)) {
      // Replace existing locale in path
      newPath = currentPath.replace(`/${currentLocale}/`, `/${localeCode}/`);
    } else if (currentLocale === 'en-US' && !currentPath.startsWith('/en-US/')) {
      // en-US is default, so it might not be in the path
      newPath = `/${localeCode}${currentPath}`;
    } else {
      // Fallback: just prepend the new locale
      newPath = `/${localeCode}${currentPath}`;
    }

    window.location.href = newPath;
  }

  static getPreferredLocale(): string | null {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      return localStorage.getItem('preferredLocale');
    }
    return null;
  }
}
