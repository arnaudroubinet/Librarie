# Librarie - System Architecture Documentation

## Table of Contents

1. [System Overview](#system-overview)
2. [C4 Model - System Context](#c4-model---system-context)
3. [C4 Model - Container Architecture](#c4-model---container-architecture)
4. [Backend Hexagonal Architecture](#backend-hexagonal-architecture)
5. [Frontend Component Structure](#frontend-component-structure)
6. [Data Flow Examples](#data-flow-examples)
7. [Architectural Decision Records](#architectural-decision-records)

## System Overview

Librarie is a modern library management system built with a clear separation between backend and frontend concerns. The system allows users to manage books, authors, and series, with support for reading eBooks directly in the browser.

### Key Technologies

- **Backend**: Quarkus 3.25 (Java 21), PostgreSQL, Flyway
- **Frontend**: Angular 20, TypeScript 5.8, Readium Web Reader
- **Architecture Pattern**: Hexagonal Architecture (Ports & Adapters)
- **API Communication**: RESTful JSON API
- **Build Tools**: Maven (backend), Angular CLI (frontend)

## C4 Model - System Context

The following diagram shows the system context and external actors interacting with Librarie.

```mermaid
C4Context
    title System Context diagram for Librarie Library Management System

    Person(user, "Library User", "A person who wants to browse, search, and read books from the library")
    
    System(librarie, "Librarie System", "Allows users to manage and read their digital book collection")
    
    System_Ext(filesystem, "File System", "Stores book files (EPUB, PDF, etc.) and cover images")
    System_Ext(postgres, "PostgreSQL Database", "Stores book metadata, authors, series, and reading progress")
    System_Ext(oidc, "OIDC Provider", "Handles user authentication and authorization (optional)")
    
    Rel(user, librarie, "Uses", "HTTPS")
    Rel(librarie, filesystem, "Reads book files from", "File I/O")
    Rel(librarie, postgres, "Reads from and writes to", "SQL/JDBC")
    Rel(librarie, oidc, "Authenticates via", "OIDC")
    
    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

### System Boundaries

- **Internal System**: Librarie (Backend + Frontend)
- **External Dependencies**: 
  - File System for storing book files and assets
  - PostgreSQL for persistent data storage
  - OIDC Provider for authentication (optional, configurable)

## C4 Model - Container Architecture

The container diagram shows the high-level architecture of the Librarie system.

```mermaid
C4Container
    title Container diagram for Librarie Library Management System

    Person(user, "Library User", "A person browsing and reading books")

    System_Boundary(librarie, "Librarie System") {
        Container(spa, "Single-Page Application", "Angular 20, TypeScript", "Provides the user interface for browsing books, authors, series, and reading eBooks")
        Container(api, "API Application", "Quarkus 3, Java 21", "Handles business logic, book management, search, and serves book files")
        ContainerDb(db, "Database", "PostgreSQL 16", "Stores book metadata, authors, series, reading progress")
        Container(storage, "File Storage", "File System", "Stores book files (EPUB, PDF) and cover images")
    }

    System_Ext(oidc, "OIDC Provider", "Authentication service")

    Rel(user, spa, "Uses", "HTTPS")
    Rel(spa, api, "Makes API calls to", "JSON/HTTPS")
    Rel(api, db, "Reads/Writes", "SQL/JDBC")
    Rel(api, storage, "Reads files from", "File I/O")
    Rel(api, oidc, "Authenticates users", "OIDC")
    
    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```

### Container Descriptions

#### Single-Page Application (SPA)
- **Technology**: Angular 20 with TypeScript
- **Purpose**: Provides the user interface for all library operations
- **Key Features**:
  - Book browsing and searching
  - Author and series management
  - Integrated eBook reader using Readium
  - Responsive design with Angular Material
  - Lazy-loaded routes for optimal performance

#### API Application
- **Technology**: Quarkus 3.25 with Java 21
- **Purpose**: Backend business logic and API endpoints
- **Key Features**:
  - RESTful API endpoints
  - Hexagonal architecture implementation
  - Book file serving and processing
  - Search functionality
  - Health checks and metrics (OpenTelemetry)

#### Database
- **Technology**: PostgreSQL 16
- **Purpose**: Persistent storage of structured data
- **Key Data**:
  - Books, Authors, Series
  - Reading progress
  - User settings
  - Metadata and relationships

#### File Storage
- **Technology**: File system (local or mounted)
- **Purpose**: Storage of binary assets
- **Contents**:
  - Book files (EPUB, PDF, MOBI, etc.)
  - Cover images
  - Author pictures

## Backend Hexagonal Architecture

The backend follows the Hexagonal Architecture (also known as Ports and Adapters) pattern to maintain clear separation of concerns and ensure testability.

### Architecture Layers

```mermaid
graph TB
    subgraph "Infrastructure Layer (Adapters)"
        REST[REST Controllers<br/>Inbound Adapters]
        PERSIST[JPA Repositories<br/>Outbound Adapters]
        CONFIG[Configuration<br/>Adapters]
        SECURITY[Security<br/>Adapters]
        LOGGING[Logging<br/>Adapters]
    end
    
    subgraph "Application Layer (Use Cases)"
        BOOKSERVICE[BookService]
        AUTHORSERVICE[AuthorService]
        SERIESSERVICE[SeriesService]
        SEARCHSERVICE[UnifiedSearchService]
        INGESTSERVICE[IngestService]
    end
    
    subgraph "Domain Layer (Core Business Logic)"
        subgraph "Ports (Interfaces)"
            INPORTS[Inbound Ports<br/>Use Case Interfaces]
            OUTPORTS[Outbound Ports<br/>Repository Interfaces]
        end
        subgraph "Core"
            MODEL[Domain Models<br/>Book, Author, Series]
        end
    end
    
    REST --> INPORTS
    PERSIST --> OUTPORTS
    CONFIG --> OUTPORTS
    SECURITY --> OUTPORTS
    LOGGING --> OUTPORTS
    
    INPORTS --> BOOKSERVICE
    INPORTS --> AUTHORSERVICE
    INPORTS --> SERIESSERVICE
    INPORTS --> SEARCHSERVICE
    INPORTS --> INGESTSERVICE
    
    BOOKSERVICE --> MODEL
    AUTHORSERVICE --> MODEL
    SERIESSERVICE --> MODEL
    SEARCHSERVICE --> MODEL
    INGESTSERVICE --> MODEL
    
    BOOKSERVICE --> OUTPORTS
    AUTHORSERVICE --> OUTPORTS
    SERIESSERVICE --> OUTPORTS
    SEARCHSERVICE --> OUTPORTS
    INGESTSERVICE --> OUTPORTS
    
    style MODEL fill:#f9f,stroke:#333,stroke-width:4px
    style INPORTS fill:#bbf,stroke:#333,stroke-width:2px
    style OUTPORTS fill:#bbf,stroke:#333,stroke-width:2px
```

### Package Structure

```
org.motpassants/
├── domain/                          # Domain Layer (Core)
│   ├── core/
│   │   └── model/                   # Domain entities (Book, Author, Series)
│   └── port/
│       ├── in/                      # Inbound ports (Use Case interfaces)
│       │   ├── BookUseCase.java
│       │   ├── AuthorUseCase.java
│       │   └── SeriesUseCase.java
│       └── out/                     # Outbound ports (Repository interfaces)
│           ├── BookRepository.java
│           ├── AuthorRepositoryPort.java
│           └── SeriesRepositoryPort.java
├── application/                     # Application Layer (Use Cases)
│   └── service/
│       ├── BookService.java         # Implements BookUseCase
│       ├── AuthorService.java       # Implements AuthorUseCase
│       └── SeriesService.java       # Implements SeriesUseCase
└── infrastructure/                  # Infrastructure Layer (Adapters)
    ├── adapter/
    │   ├── in/                      # Inbound adapters
    │   │   └── rest/                # REST Controllers
    │   │       ├── BookController.java
    │   │       ├── AuthorController.java
    │   │       └── SeriesController.java
    │   └── out/                     # Outbound adapters
    │       ├── persistence/         # JPA implementations
    │       ├── config/              # Configuration adapters
    │       ├── security/            # Security adapters
    │       └── logging/             # Logging adapters
    ├── config/                      # Framework configuration
    ├── security/                    # Security configuration
    ├── media/                       # Media processing
    └── readium/                     # Readium integration
```

### Dependency Rules

The hexagonal architecture enforces strict dependency rules (validated by ArchUnit tests):

1. **Domain Core** → No external dependencies (only Java standard library)
2. **Domain Ports** → Only Domain Core
3. **Application Layer** → Domain Ports + Domain Core
4. **Infrastructure Layer** → Can access all layers (implements ports)

```mermaid
graph LR
    INFRA[Infrastructure<br/>Layer] --> APP[Application<br/>Layer]
    APP --> PORTS[Domain<br/>Ports]
    PORTS --> CORE[Domain<br/>Core]
    INFRA -.implements.-> PORTS
    
    style CORE fill:#f9f,stroke:#333,stroke-width:4px
    style PORTS fill:#bbf,stroke:#333,stroke-width:2px
    style APP fill:#bfb,stroke:#333,stroke-width:2px
    style INFRA fill:#fbb,stroke:#333,stroke-width:2px
```

### Key Architectural Principles

1. **Dependency Inversion**: Core business logic (domain) doesn't depend on infrastructure
2. **Interface Segregation**: Ports are focused interfaces defining specific use cases
3. **Separation of Concerns**: Each layer has a clear, single responsibility
4. **Testability**: Domain logic can be tested without infrastructure
5. **Framework Independence**: Domain logic is independent of Quarkus or any framework

## Frontend Component Structure

The frontend follows Angular best practices with standalone components, lazy loading, and signal-based state management.

### Component Architecture

```mermaid
graph TB
    subgraph "Application Shell"
        APP[App Component<br/>app.component.ts]
        NAV[Navigation Component<br/>navigation.component.ts]
    end
    
    subgraph "Feature Components (Lazy Loaded)"
        BOOKLIST[Book List<br/>book-list.component.ts]
        BOOKDETAIL[Book Detail<br/>book-detail.component.ts]
        READER[eBook Reader<br/>ebook-reader.component.ts]
        AUTHORLIST[Author List<br/>author-list.component.ts]
        AUTHORDETAIL[Author Detail<br/>author-detail.component.ts]
        SERIESLIST[Series List<br/>series-list.component.ts]
        SERIESDETAIL[Series Detail<br/>series-detail.component.ts]
        SEARCH[Search<br/>search.component.ts]
        SETTINGS[Settings<br/>settings.component.ts]
    end
    
    subgraph "Services (Shared)"
        BOOKSERVICE[BookService]
        AUTHORSERVICE[AuthorService]
        SERIESSERVICE[SeriesService]
        SEARCHSERVICE[SearchService]
        PROGRESSSERVICE[ReadingProgressService]
        MANIFESTSERVICE[ManifestService]
    end
    
    subgraph "Backend API"
        API[REST API<br/>Quarkus Backend]
    end
    
    APP --> NAV
    APP --> BOOKLIST
    APP --> BOOKDETAIL
    APP --> READER
    
    BOOKLIST --> BOOKSERVICE
    BOOKDETAIL --> BOOKSERVICE
    BOOKDETAIL --> PROGRESSSERVICE
    READER --> BOOKSERVICE
    READER --> PROGRESSSERVICE
    READER --> MANIFESTSERVICE
    
    AUTHORLIST --> AUTHORSERVICE
    AUTHORDETAIL --> AUTHORSERVICE
    SERIESLIST --> SERIESSERVICE
    SERIESDETAIL --> SERIESSERVICE
    SEARCH --> SEARCHSERVICE
    
    BOOKSERVICE --> API
    AUTHORSERVICE --> API
    SERIESSERVICE --> API
    SEARCHSERVICE --> API
    PROGRESSSERVICE --> API
    MANIFESTSERVICE --> API
```

### Frontend Structure

```
frontend/src/app/
├── components/                      # Feature components
│   ├── book-list.component.ts       # Book browsing
│   ├── book-detail.component.ts     # Book details
│   ├── ebook-reader.component.ts    # Readium-based reader
│   ├── author-list.component.ts     # Author browsing
│   ├── author-detail.component.ts   # Author details
│   ├── series-list.component.ts     # Series browsing
│   ├── series-detail.component.ts   # Series details
│   ├── search.component.ts          # Unified search
│   ├── settings.component.ts        # User settings
│   └── navigation.component.ts      # Navigation bar
├── services/                        # Shared services
│   ├── book.service.ts              # Book API client
│   ├── author.service.ts            # Author API client
│   ├── series.service.ts            # Series API client
│   ├── search.service.ts            # Search API client
│   ├── reading-progress.service.ts  # Progress tracking
│   └── manifest.service.ts          # Readium manifest
├── models/                          # TypeScript interfaces
├── directives/                      # Custom directives
├── shared/                          # Shared components
├── utils/                           # Utility functions
└── app.routes.ts                    # Route configuration
```

### Key Frontend Patterns

1. **Standalone Components**: All components are standalone (no NgModules)
2. **Lazy Loading**: Routes use dynamic imports for code splitting
3. **Signal-based State**: Using Angular signals for reactive state management
4. **Service Layer**: Services encapsulate API communication
5. **Type Safety**: Strong TypeScript typing throughout
6. **Responsive Design**: Angular Material for consistent UI

## Data Flow Examples

### Use Case 1: Viewing Book Details

This diagram shows the complete flow when a user views book details.

```mermaid
sequenceDiagram
    actor User
    participant SPA as Angular SPA
    participant Router as Angular Router
    participant Component as BookDetailComponent
    participant Service as BookService
    participant API as REST API
    participant UseCase as BookService (Backend)
    participant Port as BookRepository
    participant DB as PostgreSQL

    User->>SPA: Navigate to /books/{id}
    SPA->>Router: Route to BookDetailComponent
    Router->>Component: Lazy load component
    activate Component
    Component->>Service: getBook(id)
    activate Service
    Service->>API: GET /api/books/{id}
    activate API
    API->>UseCase: getBookById(id)
    activate UseCase
    UseCase->>Port: findById(id)
    activate Port
    Port->>DB: SELECT * FROM books WHERE id = ?
    DB-->>Port: Book data
    Port-->>UseCase: Optional<Book>
    deactivate Port
    UseCase-->>API: Book
    deactivate UseCase
    API-->>Service: JSON response
    deactivate API
    Service-->>Component: Book object
    deactivate Service
    Component->>Component: Update view with book details
    Component-->>User: Display book information
    deactivate Component
```

### Use Case 2: Reading an eBook

This diagram shows the flow for opening and reading an eBook.

```mermaid
sequenceDiagram
    actor User
    participant Component as EbookReaderComponent
    participant ManifestService as ManifestService
    participant ProgressService as ReadingProgressService
    participant API as REST API
    participant Storage as File Storage
    participant Readium as Readium Navigator

    User->>Component: Click "Read Book"
    activate Component
    Component->>ManifestService: loadManifest(bookId)
    activate ManifestService
    ManifestService->>API: GET /api/books/{id}/manifest
    API->>Storage: Read book file
    Storage-->>API: EPUB data
    API-->>ManifestService: Readium manifest JSON
    deactivate ManifestService
    
    Component->>Readium: Initialize reader with manifest
    activate Readium
    Readium-->>Component: Reader ready
    deactivate Readium
    
    Component->>ProgressService: loadProgress(bookId)
    activate ProgressService
    ProgressService->>API: GET /api/books/{id}/progress
    API-->>ProgressService: Reading position
    ProgressService-->>Component: Locator
    deactivate ProgressService
    
    Component->>Readium: Navigate to saved position
    Component-->>User: Display book content
    
    User->>Component: Read and navigate
    Component->>ProgressService: saveProgress(bookId, locator)
    ProgressService->>API: POST /api/books/{id}/progress
    API-->>ProgressService: Success
    deactivate Component
```

### Use Case 3: Searching Books

```mermaid
sequenceDiagram
    actor User
    participant Component as SearchComponent
    participant Service as SearchService
    participant API as REST API
    participant Search as UnifiedSearchService
    participant BookRepo as BookRepository
    participant AuthorRepo as AuthorRepositoryPort
    participant SeriesRepo as SeriesRepositoryPort
    participant DB as PostgreSQL

    User->>Component: Enter search query
    Component->>Service: search(query)
    activate Service
    Service->>API: GET /api/search?q={query}
    activate API
    API->>Search: search(query)
    activate Search
    
    par Parallel search across entities
        Search->>BookRepo: findByTitleContaining(query)
        BookRepo->>DB: SELECT FROM books WHERE...
        DB-->>BookRepo: Books
        BookRepo-->>Search: List<Book>
    and
        Search->>AuthorRepo: findByNameContaining(query)
        AuthorRepo->>DB: SELECT FROM authors WHERE...
        DB-->>AuthorRepo: Authors
        AuthorRepo-->>Search: List<Author>
    and
        Search->>SeriesRepo: findByNameContaining(query)
        SeriesRepo->>DB: SELECT FROM series WHERE...
        DB-->>SeriesRepo: Series
        SeriesRepo-->>Search: List<Series>
    end
    
    Search-->>API: Unified search results
    deactivate Search
    API-->>Service: JSON response
    deactivate API
    Service-->>Component: Search results
    deactivate Service
    Component-->>User: Display results
```

### Use Case 4: Ingesting New Books

```mermaid
sequenceDiagram
    actor Admin
    participant API as REST API
    participant Ingest as IngestService
    participant FileProcessor as SecureFileProcessingPort
    participant BookService as BookService
    participant Storage as File Storage
    participant DB as PostgreSQL

    Admin->>API: POST /api/ingest (multipart/form-data)
    activate API
    API->>Ingest: processBookFile(file)
    activate Ingest
    
    Ingest->>FileProcessor: validateAndExtractMetadata(file)
    activate FileProcessor
    FileProcessor->>FileProcessor: Check file type
    FileProcessor->>FileProcessor: Validate file size
    FileProcessor->>FileProcessor: Extract metadata (EPUB)
    FileProcessor-->>Ingest: BookMetadata
    deactivate FileProcessor
    
    Ingest->>Storage: Save book file
    Storage-->>Ingest: File path
    
    Ingest->>BookService: createBook(bookData)
    activate BookService
    BookService->>DB: INSERT INTO books...
    DB-->>BookService: Book ID
    BookService-->>Ingest: Created Book
    deactivate BookService
    
    Ingest-->>API: Ingestion result
    deactivate Ingest
    API-->>Admin: Success response
    deactivate API
```

## Architectural Decision Records

### ADR-001: Hexagonal Architecture for Backend

**Status**: Accepted

**Context**: 
We needed an architecture that provides clear separation of concerns, testability, and independence from frameworks and external dependencies.

**Decision**: 
Implement hexagonal architecture (ports and adapters pattern) with three main layers:
- Domain layer (core business logic, pure Java)
- Application layer (use cases, orchestration)
- Infrastructure layer (adapters for REST, persistence, etc.)

**Consequences**:
- **Positive**: 
  - Clear dependency rules enforced by ArchUnit tests
  - Business logic independent of frameworks
  - Easy to test without infrastructure
  - Can swap implementations without changing core logic
- **Negative**: 
  - More boilerplate code (interfaces for ports)
  - Steeper learning curve for developers unfamiliar with the pattern
  - More files and packages to navigate

**Enforcement**: 
ArchUnit tests in `HexagonalArchitectureTest.java` automatically validate architecture rules.

### ADR-002: Angular Standalone Components

**Status**: Accepted

**Context**: 
Angular 14+ introduced standalone components as a simpler alternative to NgModules. We needed to decide on the component architecture for a new application.

**Decision**: 
Use standalone components exclusively without NgModules, with lazy loading for all feature routes.

**Consequences**:
- **Positive**: 
  - Simpler mental model (no need to understand NgModules)
  - Better tree-shaking and smaller bundles
  - Easier to maintain and understand
  - Lazy loading becomes simpler
- **Negative**: 
  - Less familiar to developers from older Angular versions
  - Some third-party libraries may not fully support standalone components

### ADR-003: Quarkus Framework for Backend

**Status**: Accepted

**Context**: 
We needed a modern Java framework optimized for cloud-native applications with fast startup and low memory footprint.

**Decision**: 
Use Quarkus 3.x as the backend framework instead of Spring Boot or Jakarta EE.

**Consequences**:
- **Positive**: 
  - Fast startup time (~1 second)
  - Low memory footprint
  - Native compilation support with GraalVM
  - Developer-friendly with hot reload
  - Built-in support for modern standards (MicroProfile, OpenTelemetry)
- **Negative**: 
  - Smaller ecosystem compared to Spring
  - Less mature tooling and documentation
  - Some reflection-based libraries may not work in native mode

### ADR-004: PostgreSQL for Data Storage

**Status**: Accepted

**Context**: 
We needed a relational database that supports complex queries, full-text search, and JSONB for flexible metadata storage.

**Decision**: 
Use PostgreSQL 16 as the primary database with Flyway for migrations.

**Consequences**:
- **Positive**: 
  - Excellent support for complex queries and relationships
  - JSONB support for flexible metadata
  - Strong ACID guarantees
  - Mature, well-documented, and widely adopted
  - Full-text search capabilities
- **Negative**: 
  - Requires separate database server (more complex deployment)
  - Horizontal scaling requires additional tools (vs NoSQL)

### ADR-005: Readium for eBook Reading

**Status**: Accepted

**Context**: 
We needed a robust, standards-compliant eBook reader that works in modern browsers.

**Decision**: 
Integrate Readium Web Reader (@readium/navigator) for EPUB reading functionality.

**Consequences**:
- **Positive**: 
  - Industry-standard EPUB support
  - Actively maintained by EDRLab
  - Supports Readium Web Publication Manifest
  - Good accessibility features
  - Works across modern browsers
- **Negative**: 
  - Complex integration and configuration
  - Large library size impacts bundle size
  - Limited documentation for advanced features

### ADR-006: REST API over GraphQL

**Status**: Accepted

**Context**: 
We needed to choose an API style for client-server communication.

**Decision**: 
Use RESTful JSON API with cursor-based pagination instead of GraphQL.

**Consequences**:
- **Positive**: 
  - Simple, well-understood pattern
  - Easy to cache with HTTP
  - Standard tooling and libraries
  - Lower learning curve
- **Negative**: 
  - Potential for over-fetching or under-fetching data
  - Multiple requests may be needed for related data
  - No built-in schema introspection

### ADR-007: Flyway for Database Migrations

**Status**: Accepted

**Context**: 
We needed a reliable way to manage database schema changes across environments.

**Decision**: 
Use Flyway for versioned database migrations with SQL scripts.

**Consequences**:
- **Positive**: 
  - Version-controlled database schema
  - Automatic migration on startup
  - Rollback support
  - Works across all environments consistently
  - Simple SQL scripts (no ORM-specific DSL)
- **Negative**: 
  - Must be careful with migration order
  - Failed migrations can be tricky to recover from
  - Requires discipline in writing migrations

### ADR-008: UUID Primary Keys

**Status**: Accepted

**Context**: 
We needed to choose between auto-incrementing integers and UUIDs for primary keys.

**Decision**: 
Use UUIDs (UUID v4) as primary keys for all entities.

**Consequences**:
- **Positive**: 
  - Globally unique without coordination
  - Safe for distributed systems
  - No sequential guessing of IDs
  - Easier to merge data from different sources
- **Negative**: 
  - Larger index size (16 bytes vs 4/8 bytes)
  - Less human-friendly URLs
  - Slightly slower joins compared to integers
  - No inherent ordering

### ADR-009: Angular Signals for State Management

**Status**: Accepted

**Context**: 
Angular 16+ introduced signals as a new reactive primitive. We needed to choose between RxJS, signals, or a state management library.

**Decision**: 
Use Angular signals as the primary state management approach, with RxJS for asynchronous operations.

**Consequences**:
- **Positive**: 
  - Fine-grained reactivity
  - Better performance than zone.js
  - Simpler mental model than RxJS for simple state
  - First-class support from Angular team
  - Can interop with RxJS when needed
- **Negative**: 
  - New concept for Angular developers
  - RxJS still needed for complex async flows
  - Less mature ecosystem compared to RxJS

### ADR-010: Demo Mode for Development

**Status**: Accepted

**Context**: 
Developers need sample data to test the application without setting up a full book library.

**Decision**: 
Implement a demo mode that generates realistic sample data on startup using the DemoDataService.

**Consequences**:
- **Positive**: 
  - Quick onboarding for new developers
  - Easier to test features without real data
  - Demonstrates system capabilities
  - Configurable via application.properties
- **Negative**: 
  - Extra code to maintain
  - Could be accidentally enabled in production
  - Generated data may not represent all edge cases

---

## Conclusion

This architecture document provides a comprehensive overview of the Librarie system. The hexagonal architecture in the backend ensures clean separation of concerns and testability, while the modern Angular frontend provides a responsive and performant user experience.

For implementation details, refer to:
- Backend code: `/backend/src/main/java/org/motpassants/`
- Frontend code: `/frontend/src/app/`
- Database schema: `/backend/src/main/resources/db/migration/`
- Architecture tests: `/backend/src/test/java/org/motpassants/architecture/`

For questions or clarifications, please refer to the README.md or reach out to the development team.
