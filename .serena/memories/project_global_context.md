# Librarie - Project Global Overview

## What is Librarie?

Librarie (MotsPassants) is a **full-stack digital library management system** for organizing and reading digital books (EPUB, PDF) with an integrated web-based ebook reader.

**Key Purpose**: Provide individuals and small organizations with a self-hosted alternative to commercial ebook platforms, giving users full control over their digital book collections.

## Technology Stack

### Frontend
- **Framework**: Angular 20.3.6
- **Language**: TypeScript 5.8.2
- **UI**: Angular Material 20.2.9
- **Reader**: Readium Navigator 2.0.0 (EPUB support)
- **State**: Angular Signals
- **Build**: Angular CLI + esbuild

### Backend
- **Framework**: Quarkus 3.25.2
- **Language**: Java 21
- **Database**: PostgreSQL 17
- **Migrations**: Flyway
- **Architecture**: Hexagonal (Ports & Adapters)
- **Testing**: JUnit 5, ArchUnit, Mockito

### Infrastructure
- **Auth**: Keycloak 26.3.0 (OIDC)
- **Dev Services**: Auto-configured containers (PostgreSQL, Keycloak)
- **CI/CD**: GitHub Actions
- **Observability**: OpenTelemetry, Micrometer

## Project Structure

```
Librarie/
├── llms.txt                    # LLM entry point
├── README.md                   # Main readme
├── ARCHITECTURE.md             # Architecture details
├── CONTRIBUTING.md             # Contribution guide
├── audit_report.md             # System audit
├── tasks.md                    # Task backlog
├── docs/
│   └── PROJECT_OVERVIEW.md     # Complete overview
├── backend/                    # Quarkus backend
│   ├── src/
│   │   └── main/java/org/motpassants/
│   │       ├── domain/         # Core business logic
│   │       ├── application/    # Use cases
│   │       └── infrastructure/ # Adapters
│   └── .serena/memories/       # Backend-specific Serena memories
├── frontend/                   # Angular frontend
│   ├── src/app/
│   │   ├── components/         # UI components
│   │   ├── services/           # API clients
│   │   └── models/             # TypeScript interfaces
│   └── .serena/memories/       # Frontend-specific Serena memories
└── .github/
    ├── workflows/              # CI/CD pipelines
    └── instructions/           # Copilot instructions
```

## Core Domain Entities

1. **Book** - Digital book with metadata (title, authors, ISBN, format, etc.)
2. **Author** - Book author with biography and profile picture
3. **Series** - Book series with reading order
4. **BookSeries** - Join entity connecting books to series with volume number
5. **ReadingProgress** - User reading progress tracking with Readium locators

## Architectural Patterns

### Backend - Hexagonal Architecture

**Layers (dependency direction →):**
```
Infrastructure → Application → Domain (Ports) → Domain (Core)
```

**Key Principles:**
- Domain core has zero framework dependencies (pure Java)
- All external interactions through ports (interfaces)
- Business logic in domain and application layers
- Infrastructure provides adapters implementing ports
- ArchUnit tests enforce these rules automatically

**Package Structure:**
- `domain.core.model` - Entities (Book, Author, Series)
- `domain.port.in` - Use case interfaces (BookUseCase, AuthorUseCase)
- `domain.port.out` - Repository interfaces (BookRepository)
- `application.service` - Use case implementations (BookService)
- `infrastructure.adapter.in.rest` - REST controllers
- `infrastructure.adapter.out.persistence` - JPA repositories

### Frontend - Modern Angular

**Key Patterns:**
- **Standalone components** (no NgModules)
- **Signals** for reactive state management
- **Lazy loading** for all feature routes
- **Reactive forms** with proper FormGroup binding
- **Service layer** for API communication and state

## Development Workflow

### Starting the Application

**Backend:**
```powershell
cd backend
./mvnw quarkus:dev
# Access: http://localhost:8080
# Swagger UI: http://localhost:8080/q/swagger-ui
```

**Frontend:**
```powershell
cd frontend
npm install
npm start
# Access: http://localhost:4200
```

**Dev Services:**
Quarkus automatically starts:
- PostgreSQL on port 5432
- Keycloak on port 9080

### Running Tests

**Backend:**
```powershell
cd backend
./mvnw test              # Unit tests
./mvnw verify            # Tests + quality checks
```

**Frontend:**
```powershell
cd frontend
npm test                 # Unit tests (Karma/Jasmine)
npm run test:e2e         # E2E tests (Playwright)
```

## Current Project Status

**Phase**: Beta / Pre-Production

**Completed Features:**
✅ Book/Author/Series management
✅ EPUB reader with Readium
✅ Unified search
✅ Reading progress tracking
✅ Demo data generation
✅ OIDC authentication
✅ Health checks and metrics

**Known Issues (Critical):**
⚠️ Advanced search broken (NG01050 form error) - See T-001
⚠️ Author pictures failing (HTTP 406) - See T-003
⚠️ Demo data production risk - See T-009

**Missing:**
❌ CI/CD pipeline (T-012)
❌ E2E test suite (T-015)
❌ HTTP caching (T-013)
❌ Production deployment documentation

See `audit_report.md` for complete assessment and `tasks.md` for prioritized backlog.

## Key Constraints for Development

### MUST Follow (Architectural Rules)

**Backend:**
- ✅ Use hexagonal architecture patterns
- ✅ Domain layer has no framework dependencies
- ✅ All database access through repository ports
- ✅ Business logic in domain/application layers only
- ✅ REST controllers only handle HTTP concerns

**Frontend:**
- ✅ Use standalone components only
- ✅ All form controls within FormGroup scope
- ✅ Lazy load routes for better performance
- ✅ Use signals for state management
- ✅ Follow Angular style guide

**General:**
- ✅ Write tests for all new features
- ✅ Follow coding standards in `.serena/memories/*_code_style.md`
- ✅ Update documentation when making changes
- ✅ Run quality checks before committing

### MUST NOT Do

**Backend:**
❌ Put business logic in REST controllers
❌ Access database directly from controllers
❌ Use framework-specific code in domain core
❌ Skip writing tests for business logic
❌ Hardcode configuration values

**Frontend:**
❌ Use NgModules (project uses standalone components)
❌ Use formControlName without parent FormGroup
❌ Put business logic in components (use services)
❌ Skip writing unit tests
❌ Ignore bundle size limits (500KB)

## Quick Reference Commands

### Backend
```powershell
./mvnw quarkus:dev           # Start dev mode
./mvnw test                  # Run tests
./mvnw verify                # Tests + quality checks
./mvnw clean install         # Build
./mvnw quarkus:dev -Ddebug   # Debug mode
```

### Frontend
```powershell
npm start                    # Dev server
npm test                     # Unit tests
npm run build                # Production build
npm run size                 # Check bundle size
npm run analyze              # Analyze bundle
```

## Getting Help

1. **Documentation**: Start with `llms.txt` → `docs/PROJECT_OVERVIEW.md`
2. **Architecture**: See `ARCHITECTURE.md` for patterns and diagrams
3. **Known Issues**: Check `audit_report.md` before reporting bugs
4. **Tasks**: See `tasks.md` for planned work and implementation guides
5. **Standards**: Read `.serena/memories/*_code_style.md` for coding conventions
6. **Workflow**: Follow `.serena/memories/*_task_completion.md` checklists

## Important Files to Know

- **`llms.txt`** - Entry point for understanding project
- **`docs/PROJECT_OVERVIEW.md`** - Most comprehensive documentation
- **`ARCHITECTURE.md`** - Technical architecture with diagrams
- **`audit_report.md`** - Current state assessment
- **`tasks.md`** - Detailed implementation guides
- **`CONTRIBUTING.md`** - Development workflow

## Business Context

**Target Users**: Individual book collectors, small libraries, readers who want control over their digital collections

**Value Proposition**: Self-hosted, web-based alternative to commercial ebook platforms with:
- Full ownership of book files and data
- No vendor lock-in
- Modern reading experience
- Powerful organization and search
- Cross-device reading progress sync

**Scope**: Personal/small library management (not a multi-tenant commercial platform)
