# Frontend Coding Style and Conventions

## TypeScript Style
- **TypeScript Version**: 5.8.2
- **Strict Mode**: Enabled (all strict compiler options)
- **Style Guide**: Angular TypeScript Style Guide

## File Naming Conventions
### Components
- **Pattern**: `<feature-name>.component.ts`
- **Examples**: 
  - `book-list.component.ts`
  - `book-detail.component.ts`
  - `ebook-reader.component.ts`
  - `author-detail.component.ts`

### Services
- **Pattern**: `<service-name>.service.ts`
- **Examples**:
  - `book.service.ts`
  - `author.service.ts`
  - `reading-progress.service.ts`
  - `manifest.service.ts`

### Models
- **Pattern**: `<entity-name>.model.ts`
- **Examples**:
  - `book.model.ts`
  - `author.model.ts`
  - `series.model.ts`
  - `search.model.ts`

### Directives
- **Pattern**: `<directive-name>.directive.ts`
- **Example**: `infinite-scroll.directive.ts`

### Utils
- **Pattern**: `<utility-name>-utils.ts`
- **Example**: `author-utils.ts`

## Class and Interface Naming
### Components
- **Pattern**: `<FeatureName>Component`
- **Examples**:
  - `BookListComponent`
  - `BookDetailComponent`
  - `EbookReaderComponent`
  - `NavigationComponent`

### Services
- **Pattern**: `<ServiceName>Service`
- **Examples**:
  - `BookService`
  - `AuthorService`
  - `ReadingProgressService`
  - `ManifestService`

### Models/Interfaces
- **No suffix** for domain models
- **Examples**:
  - `Book`
  - `Author`
  - `Series`
  - `SearchResult`
  - `Settings`

### Response/Request Types
- **Pattern**: `<Entity>Response`, `<Entity>Request`, `<Entity>Details`, `<Entity>ListItem`
- **Examples**:
  - `BookResponse`
  - `BookDetails`
  - `BookListItem`
  - `PageResponse<T>`

## Code Structure
### Standalone Components
All components MUST use the standalone component API:
```typescript
@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [
    CommonModule,
    MaterialModule,
    // other imports
  ],
  templateUrl: './book-list.component.html',
  styleUrls: ['./book-list.component.css']
})
export class BookListComponent {
  // component code
}
```

### Component Structure
Components should follow this order:
1. **Decorators**: `@Component`
2. **Public properties**: Inputs, outputs, signals
3. **Private properties**: Internal state
4. **Constructor**: Dependency injection
5. **Lifecycle hooks**: ngOnInit, ngOnDestroy, etc. (in order)
6. **Public methods**: Template-accessible methods
7. **Private methods**: Internal helper methods

### Service Structure
Services should follow this order:
1. **Decorator**: `@Injectable({ providedIn: 'root' })`
2. **Private properties**: HttpClient, configuration
3. **Constructor**: Dependency injection
4. **Public methods**: API methods
5. **Private methods**: Helper methods

## TypeScript Features
### Type Safety
- **Always use explicit types** for function parameters and return types
- **Use interfaces** for object shapes
- **Use type aliases** for unions and complex types
- **Avoid `any`**: Use `unknown` if type is truly unknown

### Modern TypeScript
- **Optional chaining**: `book?.author?.name`
- **Nullish coalescing**: `book.title ?? 'Unknown'`
- **Template literals**: Use for string interpolation
- **Async/await**: Prefer over `.then()` chains
- **Arrow functions**: For callbacks and short functions

### Readonly
- Use `readonly` for properties that shouldn't change after initialization
- Use `as const` for literal types

## Angular-Specific Conventions
### Signals
- Use Angular signals for reactive state:
```typescript
books = signal<Book[]>([]);
selectedBook = signal<Book | null>(null);
```

### Dependency Injection
- **Constructor injection** for services
- Use `inject()` function in functional contexts
- Mark services as `providedIn: 'root'`

### Observables
- Use RxJS operators properly
- **Always unsubscribe** from observables (use `takeUntil`, `async` pipe, or `toSignal`)
- Prefer `async` pipe in templates over manual subscriptions
- Use `toSignal()` to convert observables to signals

### Template Syntax
- **Use signals in templates**: `{{ books().length }}`
- **Structural directives**: `@if`, `@for`, `@switch` (new control flow)
- **Event binding**: `(click)="onBookClick(book)"`
- **Property binding**: `[book]="selectedBook()"`
- **Two-way binding**: `[(ngModel)]="searchQuery"`

### CSS/Styles
- **Component-scoped styles**: Use component CSS files
- **BEM naming**: Consider BEM for complex components
- **Material Design**: Leverage Angular Material theming
- **Responsive design**: Mobile-first approach

## Naming Conventions
### Variables and Properties
- **camelCase** for all variables and properties
- **Descriptive names**: `selectedBook`, not `book`
- **Boolean prefixes**: `isLoading`, `hasError`, `canEdit`
- **Collections**: Plural names (`books`, `authors`, `series`)

### Methods
- **camelCase** for all methods
- **Verb-based names**: `loadBooks()`, `deleteAuthor()`, `updateSeries()`
- **Event handlers**: Prefix with `on` (`onClick`, `onBookSelected`)
- **Async methods**: Consider `async` prefix or suffix (`fetchBooks()`, `loadBooksAsync()`)

### Constants
- **UPPER_SNAKE_CASE** for constants
- **Define at file or class level**
```typescript
const DEFAULT_PAGE_SIZE = 20;
const API_BASE_URL = '/api';
```

### Enums
- **PascalCase** for enum names
- **PascalCase** for enum values
```typescript
enum BookFormat {
  Epub = 'EPUB',
  Pdf = 'PDF',
  Mobi = 'MOBI'
}
```

## Code Organization
### Imports
Order imports as follows:
1. Angular core imports
2. Angular Material imports
3. Third-party imports (RxJS, Readium, etc.)
4. Application imports (services, models, utils)

```typescript
import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { Observable } from 'rxjs';
import { BookService } from '../services/book.service';
import { Book } from '../models/book.model';
```

### File Size
- **Keep components focused**: < 400 lines
- **Extract logic**: Move complex logic to services
- **Split large templates**: Use child components
- **Reusable components**: Create shared components

## Comments and Documentation
### TSDoc
Use TSDoc for public APIs:
```typescript
/**
 * Fetches a book by its ID.
 * @param id - The unique identifier of the book
 * @returns An observable of the book or null if not found
 */
getBookById(id: string): Observable<Book | null> {
  // implementation
}
```

### Inline Comments
- **Use sparingly**: Code should be self-documenting
- **Explain "why", not "what"**
- **TODO comments**: Use for planned improvements
```typescript
// TODO: Add pagination support
// FIXME: Handle edge case when book has no authors
```

## Error Handling
### HTTP Errors
- Use RxJS `catchError` operator
- Provide user-friendly error messages
- Log errors for debugging

### Null/Undefined Checks
- Use optional chaining: `book?.author?.name`
- Use nullish coalescing: `book.title ?? 'Unknown'`
- Type guards for complex checks

## Performance Best Practices
### Change Detection
- Use `OnPush` strategy for performance
- Use signals for reactive updates
- Avoid unnecessary re-renders

### Lazy Loading
- All feature routes use lazy loading
- Code splitting for large dependencies

### Bundle Size
- **Limit**: 500 kB for main application bundle
- **Tree-shaking**: Remove unused imports
- **Dynamic imports**: For large libraries

### Optimization
- **TrackBy functions**: Always use with `@for`
- **Memoization**: Cache expensive computations
- **Virtual scrolling**: For long lists

## Accessibility
- **Semantic HTML**: Use proper HTML elements
- **ARIA labels**: Add for screen readers
- **Keyboard navigation**: Support tab navigation
- **Focus management**: Manage focus for modals and dynamic content

## Testing Conventions
### Test File Naming
- **Pattern**: `<component-name>.component.spec.ts`
- **Example**: `book-list.component.spec.ts`

### Test Structure
- **Describe blocks**: Group related tests
- **It blocks**: Single assertion per test
- **AAA pattern**: Arrange, Act, Assert

### Test Coverage
- **Unit tests**: For components, services, utilities
- **Integration tests**: For component interactions
- **E2E tests**: For critical user flows

## Prettier Configuration
Prettier is configured in `package.json`:
```json
{
  "prettier": {
    "overrides": [
      {
        "files": "*.html",
        "options": {
          "parser": "angular"
        }
      }
    ]
  }
}
```

## Git Commit Conventions
Follow conventional commits:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code style changes (formatting)
- `refactor:` Code refactoring
- `test:` Adding or updating tests
- `chore:` Maintenance tasks

Examples:
- `feat: add book search with filters`
- `fix: correct reading progress calculation`
- `refactor: simplify book service API calls`
