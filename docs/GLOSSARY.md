# Glossary

This glossary defines key terms, concepts, and acronyms used throughout the Librarie project. It ensures consistent vocabulary across documentation, code, and communication between developers, LLMs, and users.

## Domain Concepts

### Book
**Definition**: A digital publication in the library. Each book has metadata (title, description, language), at least one file (EPUB, PDF, etc.), and can belong to a series.

**Database**: `book` table  
**Backend Entity**: `org.rlh.models.Book`  
**Frontend Model**: `Book` interface  
**REST Endpoint**: `/api/books`

**Key Attributes**:
- `title`: Display name
- `sortableTitle`: Normalized title for sorting (without articles like "The", "A")
- `volumeNumber`: Position in series (if applicable)
- `isbn`: International Standard Book Number (optional)
- `publicationDate`: Original publication date

### Author
**Definition**: A person who wrote or contributed to one or more books. Each author has a name, optional biography, and optional profile picture.

**Database**: `author` table  
**Backend Entity**: `org.rlh.models.Author`  
**Frontend Model**: `Author` interface  
**REST Endpoint**: `/api/authors`

**Key Attributes**:
- `firstName`, `middleName`, `lastName`: Name components
- `biography`: Optional biographical information
- `sortableName`: Normalized name for sorting (usually "LastName, FirstName")
- `profilePictureUrl`: Optional author photo

### Series
**Definition**: A collection of related books with a specific order. A series has a title and contains multiple books ordered by volume number.

**Database**: `series` table  
**Backend Entity**: `org.rlh.models.Series`  
**Frontend Model**: `Series` interface  
**REST Endpoint**: `/api/series`

**Key Attributes**:
- `title`: Series name
- `bookCount`: Total number of books in the series
- `coverUrl`: Optional cover image for the series

### Reading Progress (Progression de lecture)
**Definition**: Tracks a user's current position in a book. Uses Locators to store the precise reading position.

**Database**: `reading_progress` table  
**Backend Entity**: `org.rlh.models.ReadingProgress`  
**Frontend Model**: `ReadingProgress` interface  
**REST Endpoint**: `/api/reading-progress`

**Key Attributes**:
- `userId`: User identifier from Keycloak
- `bookId`: Reference to the book being read
- `locator`: JSON object storing the precise reading position
- `progressPercentage`: 0-100 value representing completion
- `lastReadAt`: Timestamp of last reading session

### Locator
**Definition**: A standardized JSON object that stores a precise location within a digital publication. Based on the Readium Web Publication Manifest (RWPM) specification.

**Format**: JSON object with properties like `href`, `type`, `title`, `locations` (containing `progression`, `position`, `totalProgression`)

**Example**:
```json
{
  "href": "chapter3.xhtml",
  "type": "application/xhtml+xml",
  "title": "Chapter 3",
  "locations": {
    "progression": 0.45,
    "position": 23,
    "totalProgression": 0.45
  }
}
```

### Cover
**Definition**: Image representation of a book or series. Stored as files in the backend and served via `/api/books/{id}/cover` or `/api/series/{id}/cover`.

**Storage Location**: `backend/assets/books/covers/` and `backend/assets/series/covers/`  
**Supported Formats**: JPEG, PNG, WebP  
**REST Endpoint**: `/api/books/{id}/cover`, `/api/series/{id}/cover`

### Demo Data
**Definition**: Pre-loaded sample books, authors, and series used for development and testing. Loaded from CSV files at application startup if the database is empty.

**Location**: `backend/data/` and `data/`  
**Files**: `books.csv`, `authors.csv`, `series.csv`  
**Behavior**: Idempotent loading (see `backend/DEMO_DATA_IDEMPOTENCY.md`)

## Technical Terms

### Hexagonal Architecture (Ports & Adapters)
**Definition**: Architectural pattern used in the backend that isolates the core business logic (domain) from external concerns (infrastructure).

**Layers**:
- **Domain**: Business logic, entities, domain services (`org.rlh.domain.*`)
- **Application**: Use cases, orchestration (`org.rlh.application.*`)
- **Infrastructure**: Database, REST, file I/O (`org.rlh.infrastructure.*`)

**Purpose**: Makes the code testable, maintainable, and independent of frameworks.

### Standalone Component (Angular)
**Definition**: Angular component that declares its own dependencies instead of relying on NgModules. Used throughout the frontend for better tree-shaking and lazy loading.

**Declaration**: Uses `imports: [...]` in the `@Component` decorator  
**Example**: `BookListComponent`, `AuthorCardComponent`

### Signal (Angular)
**Definition**: Reactive primitive in Angular for state management. Provides fine-grained change detection and automatic dependency tracking.

**Usage**: `signal<T>(initialValue)`, `computed(() => ...)`, `effect(() => ...)`  
**Example**: `selectedBook = signal<Book | null>(null);`

### Quarkus Dev Services
**Definition**: Automatic provisioning of development dependencies (database, OIDC) using TestContainers. Simplifies local development setup.

**Provides**: PostgreSQL, Keycloak (OIDC provider)  
**Configuration**: `application-dev.properties`, `quarkus.datasource.devservices.enabled=true`

### OpenID Connect (OIDC)
**Definition**: Authentication protocol used to secure the application. Keycloak acts as the identity provider.

**Flow**: Authorization Code Flow with PKCE  
**Tokens**: Access Token (JWT), ID Token, Refresh Token  
**Library**: `@auth0/angular-jwt` (frontend), `quarkus-oidc` (backend)

### TestContainers
**Definition**: Java library that provides throwaway Docker containers for integration testing. Used extensively in the backend tests.

**Usage**: Automatic database provisioning for tests  
**Optimization**: Reusable containers (see T-006 in `tasks.md`)  
**Configuration**: `.testcontainers.properties` file

## Acronyms & Abbreviations

| Acronym | Full Name | Context |
|---------|-----------|---------|
| **ADR** | Architecture Decision Record | Documents important architectural choices |
| **API** | Application Programming Interface | RESTful endpoints for backend services |
| **CDI** | Contexts and Dependency Injection | Java EE/Jakarta EE dependency injection spec |
| **CLI** | Command Line Interface | Angular CLI tool for project management |
| **CSV** | Comma-Separated Values | Format for demo data files |
| **DTO** | Data Transfer Object | Objects used for API requests/responses |
| **EPUB** | Electronic Publication | E-book file format |
| **HTTP** | Hypertext Transfer Protocol | Network protocol for REST API |
| **JAX-RS** | Java API for RESTful Web Services | Specification for building REST APIs |
| **JPA** | Java Persistence API | ORM specification for database access |
| **JSON** | JavaScript Object Notation | Data interchange format |
| **JWT** | JSON Web Token | Token format for authentication |
| **OIDC** | OpenID Connect | Authentication protocol |
| **ORM** | Object-Relational Mapping | Database abstraction layer |
| **PDF** | Portable Document Format | Document file format |
| **PKCE** | Proof Key for Code Exchange | OAuth 2.0 security extension |
| **REST** | Representational State Transfer | API architectural style |
| **RWPM** | Readium Web Publication Manifest | Specification for digital publications |
| **SSR** | Server-Side Rendering | Not used in this project (CSR only) |
| **UUID** | Universally Unique Identifier | Primary key format for entities |

## Frontend-Backend Mapping

This table shows how the same concepts are named in different parts of the codebase:

| Concept | Frontend (TypeScript) | Backend (Java) | Database (PostgreSQL) | REST API |
|---------|----------------------|----------------|----------------------|----------|
| Book entity | `Book` interface | `Book` class | `book` table | `/api/books` |
| Author entity | `Author` interface | `Author` class | `author` table | `/api/authors` |
| Series entity | `Series` interface | `Series` class | `series` table | `/api/series` |
| Progress tracking | `ReadingProgress` | `ReadingProgress` | `reading_progress` | `/api/reading-progress` |
| Book service | `BookService` | `BookService` | N/A | N/A |
| HTTP client | `HttpClient` (Angular) | JAX-RS `@Path` | N/A | N/A |
| Authentication | `AuthService` | `@RolesAllowed` | N/A | Bearer token |

## Database Tables

| Table Name | Primary Key | Foreign Keys | Purpose |
|------------|-------------|--------------|---------|
| `book` | `id` (UUID) | `series_id` (nullable) | Stores book metadata and files |
| `author` | `id` (UUID) | None | Stores author information |
| `series` | `id` (UUID) | None | Stores series metadata |
| `book_author` | Composite | `book_id`, `author_id` | Many-to-many relationship |
| `reading_progress` | `id` (UUID) | `book_id`, `user_id` | Tracks user reading positions |

## Common Patterns

### Pagination
**Definition**: Mechanism for retrieving large datasets in chunks.

**Query Parameters**: `page` (0-indexed), `size` (items per page)  
**Response Headers**: `X-Total-Count` (total items)  
**Frontend Service**: `PaginationService`

### Sorting
**Definition**: Ordering results by specific fields.

**Query Parameter**: `sort` (e.g., `sort=title,asc` or `sort=publicationDate,desc`)  
**Backend**: JPA `Sort` object  
**Common Fields**: `title`, `sortableTitle`, `publicationDate`, `volumeNumber`

### Filtering
**Definition**: Narrowing results based on criteria.

**Query Parameter**: `filter` (e.g., `filter=title:contains:potter`)  
**Backend**: JPA Criteria API  
**Common Filters**: `title`, `author.name`, `series.title`, `language`

## File Formats

### EPUB
**Definition**: Open e-book standard format based on XHTML and CSS.

**MIME Type**: `application/epub+zip`  
**Storage**: `backend/assets/books/`  
**Support**: Primary format for reading in the application

### PDF
**Definition**: Portable document format.

**MIME Type**: `application/pdf`  
**Storage**: `backend/assets/books/`  
**Support**: Secondary format (viewing only)

## Environment & Configuration

### Development Mode (`dev`)
**Definition**: Local development environment with hot reload, Dev Services, and debug logging.

**Profile**: `%dev` properties in `application.properties`  
**Database**: PostgreSQL via TestContainers  
**OIDC**: Keycloak via TestContainers  
**Port**: Backend 8080, Frontend 4200

### Production Mode (`prod`)
**Definition**: Optimized environment for deployment with external database and OIDC provider.

**Profile**: `%prod` properties in `application.properties`  
**Database**: External PostgreSQL instance  
**OIDC**: External Keycloak instance  
**Build**: Native compilation with GraalVM (optional)

## Related Documentation

- **Architecture Details**: See [ARCHITECTURE.md](../ARCHITECTURE.md)
- **API Reference**: See OpenAPI/Swagger UI at `http://localhost:8080/q/swagger-ui`
- **Project Overview**: See [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md)
- **Contributing**: See [CONTRIBUTING.md](../CONTRIBUTING.md)

---

> **Note for LLMs**: This glossary is the authoritative source for terminology. When generating code or documentation, use these exact terms and follow the naming conventions specified here.
