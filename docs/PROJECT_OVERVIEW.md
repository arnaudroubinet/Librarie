# Librarie - Project Overview

## Table of Contents

1. [Business Context](#business-context)
2. [System Purpose and Scope](#system-purpose-and-scope)
3. [Core Features](#core-features)
4. [Technology Stack](#technology-stack)
5. [System Architecture Overview](#system-architecture-overview)
6. [Key Concepts and Domain Model](#key-concepts-and-domain-model)
7. [User Flows](#user-flows)
8. [Project Status](#project-status)
9. [Deployment and Operations](#deployment-and-operations)

---

## Business Context

### What is Librarie?

Librarie (also known as MotsPassants) is a digital library management system designed for individuals and small organizations to organize, browse, and read their digital book collections. Unlike commercial ebook platforms that lock users into proprietary ecosystems, Librarie gives users full control over their digital libraries while providing a modern, web-based reading experience.

### Target Users

- **Individual Book Collectors**: People with large personal digital book collections who want better organization than file systems provide
- **Small Libraries**: Community libraries, book clubs, or small organizations managing digital collections
- **Readers**: Anyone who wants to read EPUB books directly in their browser without installing separate reader software

### Business Value

1. **Organization**: Manage books, authors, and series with rich metadata
2. **Accessibility**: Read books from any device with a web browser
3. **Ownership**: Full control over book files and data (no vendor lock-in)
4. **Discoverability**: Powerful search across books, authors, and series
5. **Progress Tracking**: Resume reading where you left off across devices

---

## System Purpose and Scope

### Primary Purpose

Provide a self-hosted, web-based platform for managing and reading digital books with a focus on:
- **User Experience**: Modern, intuitive interface matching commercial ebook platforms
- **Performance**: Fast search and navigation with efficient caching
- **Standards Compliance**: Support for industry-standard EPUB format via Readium
- **Extensibility**: Clean architecture enabling future enhancements

### What Librarie Does

✅ **Book Management**
- Import and organize EPUB, PDF, and other ebook formats
- Manage book metadata (title, authors, series, publisher, ISBN, etc.)
- Display book covers and author pictures
- Track reading progress

✅ **Content Discovery**
- Browse books by title, author, or series
- Unified search across all entities
- Filter and sort book collections
- View library statistics

✅ **Reading Experience**
- In-browser EPUB reader powered by Readium
- Customizable reading settings
- Bookmark and navigation support
- Resume reading across sessions

✅ **User Management**
- Authentication via OIDC (Keycloak)
- Role-based access control (future)
- Multi-user support (future)

### What Librarie Does NOT Do

❌ DRM management or encrypted content
❌ Social features (reviews, ratings, recommendations)
❌ Book purchasing or marketplace integration
❌ Format conversion (expects pre-converted files)
❌ Mobile native apps (web-only currently)

---

## Core Features

### 1. Book Library Management

**Browse Books**
- Grid view with book covers
- List view with detailed information
- Pagination and infinite scroll support
- Quick actions (read, view details, download)

**Book Details**
- Complete metadata display
- Cover image and description
- Author and series information
- Publication details and ISBN
- Reading progress indicator

**Import Books**
- File upload with validation
- Automatic metadata extraction from EPUB
- Cover image extraction
- Duplicate detection

### 2. Author Management

**Author Profiles**
- Name and biographical information
- Profile pictures
- List of books by author
- Author statistics

**Author Discovery**
- Browse all authors
- Search by name
- View books by specific author

### 3. Series Management

**Series Organization**
- Group books into series
- Maintain reading order
- Series metadata (name, description)
- Series cover images

**Series Browsing**
- View all series
- See books in series order
- Track series completion

### 4. Unified Search

**Search Capabilities**
- Quick search across all entities
- Advanced search with filters:
  - Title contains
  - Author name
  - Series name
  - Publisher
  - Publication date range
  - Language
  - Format
- Sort options (title, author, date, rating)

**Search Results**
- Grouped by entity type (books, authors, series)
- Highlighted matching terms
- Instant results for quick search

### 5. EPUB Reader

**Reading Interface**
- Full-screen reading mode
- Table of contents navigation
- Pagination or scroll view
- Zoom and font controls
- Theme selection (light, dark, sepia)

**Progress Tracking**
- Automatic bookmark at last position
- Resume reading across sessions
- Progress percentage indicator
- Sync across devices (same user)

**Navigation**
- Previous/next chapter
- Jump to specific page or chapter
- Search within book (future)

### 6. Statistics and Monitoring

**Library Statistics**
- Total books count
- Total authors count
- Total series count
- Storage usage
- Recent additions

**System Health**
- Backend health checks
- Database connection status
- File storage status
- Application metrics via Micrometer

---

## Technology Stack

### Frontend Stack

```
Angular 20 (TypeScript 5.8)
├── @angular/material 20       # UI components
├── @angular/cdk 20             # Component dev kit
├── @readium/navigator 2.0      # EPUB rendering
├── rxjs 7.8                    # Reactive programming
├── zone.js 0.15                # Change detection
└── iconify-icon 3.0            # Icon library
```

**Build Tools:**
- Angular CLI 20
- esbuild (via @angular/build)
- TypeScript compiler
- Karma + Jasmine (unit tests)
- Playwright (E2E tests)

**Key Frontend Patterns:**
- Standalone components (no NgModules)
- Signal-based state management
- Lazy-loaded routes
- Reactive forms
- HTTP client with interceptors

### Backend Stack

```
Quarkus 3.25.2 (Java 21)
├── quarkus-rest                # REST endpoints
├── quarkus-rest-jackson        # JSON serialization
├── quarkus-jdbc-postgresql     # Database driver
├── quarkus-flyway              # Database migrations
├── quarkus-oidc                # Authentication
├── quarkus-smallrye-openapi    # API documentation
├── quarkus-smallrye-health     # Health checks
├── quarkus-micrometer          # Metrics
└── quarkus-opentelemetry       # Observability
```

**Build Tools:**
- Maven 3.9+
- JUnit 5 (unit tests)
- REST Assured (integration tests)
- ArchUnit (architecture tests)
- Mockito (mocking)

**Key Backend Patterns:**
- Hexagonal architecture (ports & adapters)
- Domain-driven design
- Repository pattern
- Dependency injection (CDI)
- RESTful API design

### Infrastructure Stack

**Database:**
- PostgreSQL 17
- Flyway for schema migrations
- Connection pooling (Agroal)

**Authentication:**
- Keycloak 26.3.0 (OIDC provider)
- JWT token validation
- Role-based access control

**Development Services:**
- Quarkus Dev Services (auto-configured containers)
- TestContainers for integration tests
- Hot reload for both frontend and backend

**Deployment (Future):**
- Docker containers
- Kubernetes manifests
- CI/CD via GitHub Actions

---

## System Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         User Browser                         │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         Angular SPA (Port 4200)                      │   │
│  │  - Book list/detail views                            │   │
│  │  - Author/Series management                          │   │
│  │  - Unified search                                    │   │
│  │  - EPUB reader (Readium)                             │   │
│  └────────────┬────────────────────────────────────────┘   │
└───────────────┼──────────────────────────────────────────────┘
                │ HTTPS/JSON
                │
┌───────────────┼──────────────────────────────────────────────┐
│  ┌────────────▼────────────────────────────────────────┐    │
│  │     REST API (Quarkus - Port 8080)                   │    │
│  │  - Book/Author/Series endpoints                      │    │
│  │  - Search API                                        │    │
│  │  - File upload/download                              │    │
│  │  - EPUB manifest generation                          │    │
│  └────────────┬────────────────────────────────────────┘    │
│               │                                               │
│  ┌────────────┼────────────────────────────────────────┐    │
│  │         Business Logic Layer                         │    │
│  │  - BookService, AuthorService, SeriesService         │    │
│  │  - UnifiedSearchService                              │    │
│  │  - IngestService                                     │    │
│  └────────────┬────────────────────────────────────────┘    │
│               │                                               │
│  ┌────────────┼────────────────────────────────────────┐    │
│  │         Domain Layer (Core)                          │    │
│  │  - Entities: Book, Author, Series                    │    │
│  │  - Ports: Interfaces for repositories                │    │
│  └────────────┬────────────────────────────────────────┘    │
│               │                                               │
│  ┌────────────┼────────────────────────────────────────┐    │
│  │      Infrastructure Layer (Adapters)                 │    │
│  │  - JPA Repositories                                  │    │
│  │  - File Storage Service                              │    │
│  │  - OIDC Security                                     │    │
│  └────────────┬────────────────────────────────────────┘    │
└───────────────┼──────────────────────────────────────────────┘
                │
     ┌──────────┼──────────┐
     │          │           │
┌────▼───┐  ┌──▼─────┐  ┌─▼──────────┐
│PostgreSQL│  │File    │  │Keycloak   │
│Database  │  │Storage │  │(OIDC)     │
│(Port     │  │        │  │(Port 9080)│
│5432)     │  │        │  │           │
└──────────┘  └────────┘  └───────────┘
```

### Component Interaction

**Frontend to Backend:**
- HTTP requests via Angular HttpClient
- JSON payload format
- Bearer token authentication (JWT)
- Proxy configuration for dev mode (port 4200 → 8080)

**Backend Layers:**
1. **REST Controllers** (Infrastructure): Handle HTTP, validate input
2. **Application Services**: Orchestrate business logic
3. **Domain Core**: Pure business rules, no framework dependencies
4. **Repository Adapters**: Persist data to database

**External Dependencies:**
- **PostgreSQL**: Persistent storage of metadata
- **File System**: Store book files and images
- **Keycloak**: User authentication and authorization

---

## Key Concepts and Domain Model

### Core Domain Entities

#### Book
The central entity representing a digital book.

**Key Attributes:**
- `id` (UUID): Unique identifier
- `title` (String): Book title
- `subtitle` (String, optional): Subtitle or additional title info
- `authors` (List<Author>): Book authors (many-to-many)
- `series` (List<BookSeries>): Series this book belongs to
- `isbn` (String, optional): International Standard Book Number
- `publisher` (Publisher, optional): Publishing information
- `publishedDate` (LocalDate, optional): Publication date
- `language` (Language): Book language (BCP 47 code)
- `format` (Format): File format (EPUB, PDF, MOBI, etc.)
- `description` (String, optional): Book description/synopsis
- `pageCount` (Integer, optional): Number of pages
- `rating` (Rating, optional): User rating
- `tags` (List<Tag>): Categorization tags
- `coverPath` (String, optional): Path to cover image
- `filePath` (String): Path to book file
- `fileSize` (Long): File size in bytes

**Relationships:**
- Many-to-Many with Author
- Many-to-Many with Series (via BookSeries join entity)
- Many-to-One with Publisher
- One-to-Many with ReadingProgress

#### Author
Represents a book author.

**Key Attributes:**
- `id` (UUID): Unique identifier
- `name` (String): Author full name
- `biography` (String, optional): Author bio
- `birthDate` (LocalDate, optional): Date of birth
- `deathDate` (LocalDate, optional): Date of death
- `nationality` (String, optional): Author nationality
- `picturePath` (String, optional): Path to author picture
- `books` (List<Book>): Books by this author

**Relationships:**
- Many-to-Many with Book

#### Series
Represents a book series.

**Key Attributes:**
- `id` (UUID): Unique identifier
- `name` (String): Series name
- `description` (String, optional): Series description
- `totalBooks` (Integer, optional): Expected total books in series
- `completed` (Boolean): Whether series is complete
- `coverPath` (String, optional): Series cover image
- `books` (List<BookSeries>): Books in this series

**Relationships:**
- Many-to-Many with Book (via BookSeries join entity)

#### BookSeries
Join entity connecting Book and Series with additional metadata.

**Key Attributes:**
- `id` (UUID): Unique identifier
- `book` (Book): Reference to book
- `series` (Series): Reference to series
- `volumeNumber` (Float): Book position in series (1.0, 2.0, 2.5, etc.)

**Purpose:**
- Maintains reading order within series
- Allows books to belong to multiple series
- Supports decimal ordering for prequels, side stories

#### ReadingProgress
Tracks user reading progress for a book.

**Key Attributes:**
- `id` (UUID): Unique identifier
- `bookId` (UUID): Reference to book
- `userId` (String): User identifier
- `locator` (String): JSON locator (Readium format)
- `progress` (Float): Percentage complete (0.0 - 100.0)
- `lastReadAt` (Instant): Last reading timestamp

**Purpose:**
- Resume reading where user left off
- Track completion percentage
- Sync across devices (same user)

### Supporting Value Objects

#### Language
- BCP 47 language codes (e.g., "en", "fr", "de")
- Multilingual book support

#### Format
- EPUB, PDF, MOBI, AZW3, etc.
- File format identification

#### Publisher
- Name, location
- Publication details

#### Rating
- User rating (1-5 stars)
- Review text (optional)

#### Tag
- Categorization (genre, topic, etc.)
- Hierarchical tagging support

---

## User Flows

### Flow 1: Discovering and Reading a Book

```
1. User lands on homepage
2. User browses book library (grid/list view)
3. User clicks on a book cover
4. System displays book detail page with:
   - Cover image
   - Title, authors, series
   - Description
   - Metadata (ISBN, publisher, etc.)
   - "Read Book" button
5. User clicks "Read Book"
6. System:
   a. Loads book file
   b. Generates Readium manifest
   c. Initializes EPUB reader
   d. Restores last reading position
7. User reads book with:
   - Page navigation
   - Table of contents
   - Bookmark support
8. System auto-saves reading progress every 30 seconds
9. User closes reader
10. Progress saved for next session
```

### Flow 2: Searching for Books

```
1. User navigates to search page
2. User enters search query (quick search)
   OR
   User expands advanced search
3. Advanced search options:
   - Title contains
   - Author name
   - Series name
   - Publisher
   - Publication date range
   - Language filter
   - Format filter
4. User clicks "Search"
5. System queries database across all matching fields
6. Results displayed in sections:
   - Books (with covers)
   - Authors (with pictures)
   - Series (with covers)
7. User clicks on result
8. System navigates to detail page
```

### Flow 3: Uploading New Books

```
1. Administrator navigates to upload page
2. User selects book file (EPUB, PDF, etc.)
3. System validates:
   - File type (whitelist)
   - File size (max 100MB)
   - MIME type verification
4. System extracts metadata:
   - Title from EPUB metadata
   - Authors from EPUB
   - Cover image from EPUB
   - ISBN if available
5. System checks for duplicates:
   - By ISBN (if present)
   - By file path
6. If duplicate found:
   - Warn user, offer to skip or overwrite
7. If unique:
   - Save file to storage
   - Create database records
   - Generate thumbnail
8. Success message displayed
9. Book appears in library
```

### Flow 4: Browsing Authors

```
1. User navigates to Authors page
2. System displays all authors in grid view:
   - Profile picture or placeholder
   - Author name
   - Book count
3. User can:
   - Sort by name or book count
   - Search by name
4. User clicks on author
5. System displays author detail:
   - Picture and bio
   - List of all books by author
   - Statistics
6. User clicks on book
7. System navigates to book detail
```

### Flow 5: Viewing Series

```
1. User navigates to Series page
2. System displays all series:
   - Series cover
   - Series name
   - Book count / total expected
   - Completion status
3. User clicks on series
4. System displays:
   - Series description
   - Books in reading order
   - Missing volumes highlighted
5. User can start reading from volume 1
   OR
   User can continue from last read volume
```

---

## Project Status

### Current Phase: **Beta / Pre-Production**

The application is functional and feature-complete for core use cases, but requires additional work before production deployment.

### Feature Completeness

✅ **Completed Features:**
- Book library management (browse, view details)
- Author and series management
- EPUB reader with Readium
- Unified search (basic)
- Reading progress tracking
- Demo data generation
- OIDC authentication configuration
- Health checks and metrics
- API documentation (Swagger/OpenAPI)

⚠️ **Partially Complete:**
- Advanced search (UI broken, see T-001)
- Settings management (form errors, see T-002)
- Author pictures (HTTP 406 errors, see T-003)
- Multi-user support (configured but not tested)

❌ **Not Yet Implemented:**
- CI/CD pipeline (see T-012)
- End-to-end tests (see T-015)
- HTTP caching for images (see T-013)
- Internationalization (see T-014)
- Accessibility compliance (see T-015)
- Production deployment guides
- User documentation/help

### Quality Status

**Testing:**
- Unit tests: Exist but coverage unknown
- Integration tests: Limited
- E2E tests: None
- Manual testing: Extensive during audit

**Security:**
- Authentication: OIDC configured ✅
- Authorization: Basic RBAC configured ✅
- Input validation: Enabled ✅
- File upload security: Basic validation ✅
- Security scanning: Not automated ❌
- Secrets management: Not documented ❌

**Performance:**
- Initial bundle size: 120KB gzipped ✅
- Lazy loading: Implemented ✅
- HTTP caching: Not implemented ❌
- Database queries: Not optimized ❌

**Documentation:**
- README: Good ✅
- Architecture: Excellent ✅
- API docs: Auto-generated (Swagger) ✅
- Development guide: Missing ❌
- Deployment guide: Missing ❌
- User guide: Missing ❌

### Known Issues

See [audit_report.md](../audit_report.md) for complete list. Critical issues:

1. **Advanced search broken** (NG01050 form error) - CRITICAL
2. **Author pictures failing** (HTTP 406) - HIGH
3. **Demo data risk** (could run in production) - MEDIUM
4. **No CI/CD** (manual quality checks) - MEDIUM
5. **Missing E2E tests** (regression risk) - MEDIUM

### Roadmap

**Phase 1 (Week 1): Critical Fixes**
- Fix advanced search
- Fix author picture endpoint
- Add demo data safeguard
- Fix security vulnerabilities

**Phase 2 (Weeks 2-3): Infrastructure**
- Implement CI/CD pipeline
- Add E2E test suite
- Create architecture documentation
- Setup bundle size monitoring

**Phase 3 (Week 4): Quality & Performance**
- Implement HTTP caching
- Add demo data idempotency
- Improve test coverage
- Performance optimization

**Phase 4 (Month 2+): Enhancement**
- Internationalization
- Accessibility compliance
- Advanced features
- Production deployment

---

## Deployment and Operations

### Development Deployment

**Requirements:**
- Java 21+
- Node.js 18+
- PostgreSQL 16+ (or use Dev Services)
- Keycloak 26+ (or use Dev Services)

**Quick Start:**
```bash
# Backend
cd backend
./mvnw quarkus:dev
# Access: http://localhost:8080

# Frontend (separate terminal)
cd frontend
npm install
npm start
# Access: http://localhost:4200
```

**Dev Services:**
Quarkus automatically starts:
- PostgreSQL container (port 5432)
- Keycloak container (port 9080)

**Configuration:**
- Backend: `backend/src/main/resources/application.properties`
- Frontend: `frontend/src/environments/environment.ts`
- Proxy: `frontend/proxy.conf.json`

### Production Deployment (Future)

**Not yet documented.** Planned approaches:

**Option 1: Docker Compose**
- Frontend: nginx container
- Backend: Quarkus JVM mode
- PostgreSQL: dedicated container
- Keycloak: dedicated container

**Option 2: Kubernetes**
- Helm charts for deployment
- Horizontal pod autoscaling
- Persistent volumes for storage
- Ingress for routing

**Option 3: Native Build**
- Frontend: Static files on CDN
- Backend: GraalVM native image
- PostgreSQL: Managed service
- Keycloak: Managed service

### Monitoring and Observability

**Health Checks:**
- Liveness: `GET /q/health/live`
- Readiness: `GET /q/health/ready`
- Startup: `GET /q/health/started`

**Metrics:**
- Endpoint: `/q/metrics`
- Format: Prometheus
- Integration: Micrometer

**Logging:**
- Backend: JBoss Logging
- Frontend: Browser console
- Structured logging: JSON format (configurable)

**Tracing:**
- OpenTelemetry configured
- OTLP exporter
- Jaeger/Zipkin compatible

### Backup and Recovery

**Database Backups:**
- PostgreSQL pg_dump recommended
- Frequency: Daily minimum
- Retention: 30 days recommended

**File Storage Backups:**
- Book files in `backend/assets/books/`
- Cover images in `backend/assets/books/covers/`
- Author pictures in `backend/assets/authors/pictures/`
- Recommended: rsync or cloud storage sync

**Configuration Backups:**
- application.properties
- Database schema (Flyway migrations)
- Keycloak realm export

### Security Considerations

**Authentication:**
- OIDC via Keycloak
- JWT token validation
- Token refresh handling

**File Security:**
- File type validation (whitelist)
- File size limits (100MB default)
- Secure file path handling
- No directory traversal

**Network Security:**
- HTTPS required in production
- CORS configuration
- CSP headers (recommended)

**Data Security:**
- Database credentials in env vars
- No sensitive data in logs
- Secrets management (vault recommended)

---

## For LLMs: Understanding This Project

### Quick Context for Code Generation

When generating code for this project, keep in mind:

1. **Backend is Hexagonal Architecture:**
   - Domain layer: Pure business logic (no framework dependencies)
   - Application layer: Use cases and orchestration
   - Infrastructure layer: Adapters for REST, persistence, etc.
   - Always create ports (interfaces) before adapters

2. **Frontend Uses Modern Angular:**
   - Standalone components only (no NgModules)
   - Signals for state management
   - Lazy loading for routes
   - Reactive forms for complex forms

3. **File Organization:**
   - Backend: `backend/src/main/java/org/motpassants/`
   - Frontend: `frontend/src/app/`
   - Both follow package-by-feature structure

4. **Testing:**
   - Backend: JUnit 5, Mockito, ArchUnit
   - Frontend: Jasmine, Karma, Playwright
   - Always write tests for new features

5. **Common Tasks:**
   - Adding new entity: Domain model → Repository port → Service → REST controller
   - Adding UI feature: Service → Component → Route
   - Database change: Flyway migration script

### Architectural Constraints

**DO:**
✅ Follow hexagonal architecture in backend
✅ Use dependency injection (CDI backend, DI frontend)
✅ Write clean, tested code
✅ Document public APIs
✅ Use typed models (Java records, TypeScript interfaces)

**DON'T:**
❌ Put business logic in controllers
❌ Access database directly from REST layer
❌ Use NgModules (Angular)
❌ Hardcode configuration values
❌ Skip writing tests

### Common Patterns

**Backend Service Pattern:**
```java
@ApplicationScoped
public class BookService implements BookUseCase {
    @Inject BookRepository repository;
    
    @Override
    @Transactional
    public Book createBook(Book book) {
        // Business logic here
        return repository.save(book);
    }
}
```

**Frontend Service Pattern:**
```typescript
@Injectable({ providedIn: 'root' })
export class BookService {
  private http = inject(HttpClient);
  
  getBooks(): Observable<Book[]> {
    return this.http.get<Book[]>('/api/v1/books');
  }
}
```

**Frontend Component Pattern:**
```typescript
@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [CommonModule, MaterialModule],
  templateUrl: './book-list.component.html'
})
export class BookListComponent {
  books = signal<Book[]>([]);
  
  constructor() {
    this.loadBooks();
  }
}
```

---

## Conclusion

Librarie is a well-architected, modern web application for digital library management. It demonstrates clean architecture principles, uses current framework versions, and provides a solid foundation for future enhancements.

**For Developers:**
- See [DEVELOPMENT.md](./DEVELOPMENT.md) for setup and workflows
- See [ARCHITECTURE.md](../ARCHITECTURE.md) for detailed architecture
- See [CONTRIBUTING.md](../CONTRIBUTING.md) for contribution guidelines

**For Users:**
- User documentation is pending (see roadmap)
- For now, refer to UI tooltips and Swagger API docs

**For Project Managers:**
- See [audit_report.md](../audit_report.md) for status and risks
- See [tasks.md](../tasks.md) for prioritized backlog
- Current phase: Beta, approaching production readiness
