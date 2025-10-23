# Quick Terminology Reference

## Purpose
This memory provides Serena with quick access to the most important terminology from GLOSSARY.md for faster context retrieval.

## Core Domain Terms (Must Know)

### Book
- **DB**: `book` table
- **Backend**: `org.rlh.models.Book`
- **Frontend**: `Book` interface
- **API**: `/api/books`
- **Key Fields**: `title`, `sortableTitle`, `volumeNumber`, `isbn`, `publicationDate`

### Author
- **DB**: `author` table
- **Backend**: `org.rlh.models.Author`
- **Frontend**: `Author` interface
- **API**: `/api/authors`
- **Key Fields**: `firstName`, `lastName`, `sortableName`, `biography`, `profilePictureUrl`

### Series
- **DB**: `series` table
- **Backend**: `org.rlh.models.Series`
- **Frontend**: `Series` interface
- **API**: `/api/series`
- **Key Fields**: `title`, `bookCount`, `coverUrl`

### ReadingProgress
- **DB**: `reading_progress` table
- **Backend**: `org.rlh.models.ReadingProgress`
- **Frontend**: `ReadingProgress` interface
- **API**: `/api/reading-progress`
- **Key Fields**: `userId`, `bookId`, `locator`, `progressPercentage`, `lastReadAt`

### Locator
- **Type**: JSON object (RWPM spec)
- **Fields**: `href`, `type`, `title`, `locations{progression, position, totalProgression}`
- **Purpose**: Stores precise reading position in EPUB

## Critical Acronyms

| Acronym | Meaning | Context |
|---------|---------|---------|
| **ADR** | Architecture Decision Record | Docs architectural choices |
| **EPUB** | Electronic Publication | E-book format |
| **OIDC** | OpenID Connect | Auth protocol (Keycloak) |
| **RWPM** | Readium Web Publication Manifest | Locator spec |
| **JPA** | Java Persistence API | ORM spec (backend) |
| **CDI** | Contexts & Dependency Injection | Java EE DI |
| **JWT** | JSON Web Token | Auth token format |
| **PKCE** | Proof Key for Code Exchange | OAuth security |

## Architecture Terms

### Hexagonal Architecture (Backend)
- **Layers**: Domain → Application → Infrastructure
- **Domain**: Pure Java, no framework deps
- **Application**: Use cases, orchestration
- **Infrastructure**: REST, DB, file I/O

### Standalone Component (Frontend)
- **Definition**: Angular component with own imports (no NgModules)
- **Pattern**: `@Component({ imports: [...] })`
- **Usage**: All components in this project

### Signal (Angular)
- **Definition**: Reactive state primitive
- **Pattern**: `signal<T>(value)`, `computed(() => ...)`, `effect(() => ...)`
- **Purpose**: Fine-grained change detection

## Package Structure Map

### Backend
```
org.rlh/
├── domain/
│   ├── core/model/          # Entities (Book, Author, Series)
│   ├── port/in/             # Use case interfaces
│   └── port/out/            # Repository interfaces
├── application/service/     # Use case implementations
└── infrastructure/
    ├── adapter/in/rest/     # REST controllers
    └── adapter/out/persistence/  # JPA repositories
```

### Frontend
```
src/app/
├── components/              # Feature components (book-list, reader, etc.)
├── services/                # HTTP services (BookService, AuthService)
├── models/                  # TypeScript interfaces (Book, Author, Series)
└── shared/                  # Shared components & pipes
```

## Common Patterns Quick Reference

### Pagination
- **Query**: `?page=0&size=20`
- **Response Header**: `X-Total-Count`

### Sorting
- **Query**: `?sort=title,asc` or `?sort=publicationDate,desc`

### Filtering
- **Query**: `?filter=title:contains:potter`

## Database Tables

| Table | PK | FK | Purpose |
|-------|----|----|---------|
| `book` | `id` (UUID) | `series_id` | Books |
| `author` | `id` (UUID) | - | Authors |
| `series` | `id` (UUID) | - | Series |
| `book_author` | Composite | `book_id`, `author_id` | M:N |
| `reading_progress` | `id` (UUID) | `book_id` | Progress |

## File Locations

- **Book covers**: `backend/assets/books/covers/{uuid}`
- **EPUB files**: `backend/assets/books/{uuid}`
- **Author photos**: `backend/assets/authors/pictures/{uuid}`
- **Series covers**: `backend/assets/series/covers/{uuid}`
- **Demo data CSV**: `backend/data/` and `data/`

## Environment Modes

### Dev Mode
- **Profile**: `%dev` in `application.properties`
- **Database**: PostgreSQL via TestContainers (auto-provisioned)
- **OIDC**: Keycloak via TestContainers
- **Ports**: Backend 8080, Frontend 4200

### Prod Mode
- **Profile**: `%prod`
- **Database**: External PostgreSQL
- **OIDC**: External Keycloak
- **Build**: Optional GraalVM native

## When to Use Full GLOSSARY.md

Consult the full [GLOSSARY.md](../../docs/GLOSSARY.md) when:
- Need detailed explanations of concepts
- Learning new domain terminology
- Writing documentation
- Explaining to users

This memory provides **quick lookups only** - always verify critical details in the full glossary.
