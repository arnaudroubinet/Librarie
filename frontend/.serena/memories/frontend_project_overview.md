# Frontend Project Overview

## Purpose
Librarie Frontend is an Angular-based single-page application (SPA) for browsing and reading digital books. It provides:
- Book, author, and series browsing and management
- Unified search across all entities
- EPUB reader integration using Readium Navigator
- Reading progress tracking
- Responsive Material Design UI
- Advanced filtering and pagination

## Tech Stack
- **Framework**: Angular 20.3.6
- **Language**: TypeScript 5.8.2
- **UI Library**: Angular Material 20.2.9
- **eBook Reader**: Readium Navigator 2.0.0 (EPUB support)
- **State Management**: Angular Signals (reactive primitives)
- **HTTP Client**: Angular HttpClient with RxJS
- **Icons**: Iconify Icon
- **Build Tool**: Angular CLI with esbuild
- **Testing**: Jasmine 5.8, Karma 6.4, Playwright 1.55
- **Code Quality**: Prettier (formatting), TypeScript strict mode

## Project Structure
```
frontend/
├── package.json                      # Dependencies and scripts
├── angular.json                      # Angular CLI configuration
├── tsconfig.json                     # TypeScript configuration
├── proxy.conf.json                   # Dev proxy to backend
├── src/
│   ├── index.html                    # Main HTML entry point
│   ├── main.ts                       # Application bootstrap
│   ├── styles.css                    # Global styles
│   └── app/
│       ├── app.component.ts          # Root component
│       ├── app.config.ts             # App configuration
│       ├── app.routes.ts             # Route definitions
│       ├── components/               # Feature components
│       │   ├── book-list.component.ts
│       │   ├── book-detail.component.ts
│       │   ├── ebook-reader.component.ts
│       │   ├── author-list.component.ts
│       │   ├── author-detail.component.ts
│       │   ├── series-list.component.ts
│       │   ├── series-detail.component.ts
│       │   ├── search.component.ts
│       │   ├── advanced-search.component.ts
│       │   ├── dashboard.component.ts
│       │   ├── settings.component.ts
│       │   └── navigation.component.ts
│       ├── services/                 # Shared services
│       │   ├── book.service.ts       # Book API client
│       │   ├── author.service.ts     # Author API client
│       │   ├── series.service.ts     # Series API client
│       │   ├── search.service.ts     # Search API client
│       │   ├── manifest.service.ts   # Readium manifest
│       │   ├── reading-progress.service.ts
│       │   ├── settings.service.ts
│       │   └── header-actions.service.ts
│       ├── models/                   # TypeScript interfaces
│       │   ├── book.model.ts
│       │   ├── author.model.ts
│       │   ├── series.model.ts
│       │   ├── search.model.ts
│       │   ├── settings.model.ts
│       │   └── rwpm.model.ts         # Readium Web Publication Manifest
│       ├── directives/               # Custom directives
│       │   └── infinite-scroll.directive.ts
│       ├── shared/                   # Shared components and utilities
│       │   └── materials.ts          # Material imports
│       └── utils/                    # Utility functions
│           └── author-utils.ts
├── public/                           # Static assets
└── dist/                             # Build output
```

## Key Features
### Standalone Components
- All components use Angular standalone API (no NgModules)
- Explicit imports in each component
- Better tree-shaking and smaller bundles

### Routing
- Lazy loading for all feature routes
- Route configuration in `app.routes.ts`
- Navigation handled by `NavigationComponent`

### State Management
- Angular signals for reactive state
- RxJS for asynchronous operations and HTTP
- Services encapsulate state and API calls

### Material Design
- Angular Material components throughout
- Responsive layout with CSS Grid and Flexbox
- Custom theme in `styles.css`

### Readium Integration
- EPUB reading capability with Readium Navigator
- Manifest service for Readium Web Publication Manifest
- Reading progress persistence across devices

### API Communication
- Services use `HttpClient` for backend communication
- Proxy configuration for dev mode (`proxy.conf.json`)
- Backend API at: `http://localhost:8080/api`

## Configuration
### Development Proxy
```json
// proxy.conf.json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

### TypeScript
- **Strict mode enabled**: All strict compiler options
- **Target**: ES2022
- **Isolated modules**: True
- **Experimental decorators**: True
- **Resolve JSON module**: True

### Bundle Size
- **Limit**: 500 kB for main application bundle
- **Tool**: size-limit for bundle size monitoring
- **Budgets**: Configured in `angular.json`
  - Initial: 500 kB warning, 1 MB error
  - Component styles: 6 kB warning, 10 kB error

## Development Workflow
1. **Start dev server**: `npm start` (with proxy to backend)
2. **Run tests**: `npm test`
3. **Build**: `npm run build`
4. **Check bundle size**: `npm run size`
5. **Analyze bundle**: `npm run analyze`

## Testing
- **Unit tests**: Jasmine + Karma
- **E2E tests**: Playwright
- **Coverage**: Karma coverage reporter
- **Continuous testing**: `ng test` (watch mode)

## Code Quality
- **Prettier**: Configured in `package.json`
- **TypeScript strict mode**: All strict options enabled
- **Angular strict templates**: Enabled in `tsconfig.json`
- **Linting**: ESLint (via Angular CLI)
