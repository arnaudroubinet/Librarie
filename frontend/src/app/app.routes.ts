import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/books', pathMatch: 'full' },
  { 
    path: 'books', 
    loadComponent: () => import('./components/book-list.component').then(c => c.BookListComponent)
  },
  { 
    path: 'books/:id', 
    loadComponent: () => import('./components/book-detail.component').then(c => c.BookDetailComponent)
  },
  { 
    path: 'series', 
    loadComponent: () => import('./components/series-list.component').then(c => c.SeriesListComponent)
  },
  { 
    path: 'search', 
    loadComponent: () => import('./components/search.component').then(c => c.SearchComponent)
  },
  { 
    path: 'settings', 
    loadComponent: () => import('./components/settings.component').then(c => c.SettingsComponent)
  },
  { path: '**', redirectTo: '/books' }
];
