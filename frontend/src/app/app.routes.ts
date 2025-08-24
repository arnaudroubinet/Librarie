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
    path: 'books/:id/read', 
    loadComponent: () => import('./components/ebook-reader.component').then(c => c.EbookReaderComponent)
  },
  { 
    path: 'books/:id/metadata', 
    loadComponent: () => import('./components/metadata-editor.component').then(c => c.MetadataEditorComponent)
  },
  { 
    path: 'series', 
    loadComponent: () => import('./components/series-list.component').then(c => c.SeriesListComponent)
  },
  { 
    path: 'series/:id', 
    loadComponent: () => import('./components/series-detail.component').then(c => c.SeriesDetailComponent)
  },
  { 
    path: 'authors', 
    loadComponent: () => import('./components/author-list.component').then(c => c.AuthorListComponent)
  },
  { 
    path: 'authors/:id', 
    loadComponent: () => import('./components/author-detail.component').then(c => c.AuthorDetailComponent)
  },
  { 
    path: 'search', 
    loadComponent: () => import('./components/search.component').then(c => c.SearchComponent)
  },
  { 
    path: 'upload', 
    loadComponent: () => import('./components/upload.component').then(c => c.UploadComponent)
  },
  { 
    path: 'batch', 
    loadComponent: () => import('./components/batch-operations.component').then(c => c.BatchOperationsComponent)
  },
  { 
    path: 'settings', 
    loadComponent: () => import('./components/settings.component').then(c => c.SettingsComponent)
  },
  { 
    path: 'admin/anomalies', 
    loadComponent: () => import('./components/book-anomalies.component').then(c => c.BookAnomaliesComponent)
  },
  // Friendly alias
  { path: 'anomalies', redirectTo: '/admin/anomalies', pathMatch: 'full' },
  { path: '**', redirectTo: '/books' }
];
