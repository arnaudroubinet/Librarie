# Backend Project Overview

## Purpose
Librarie Backend is a Quarkus-based REST API for managing a digital library system. It handles:
- Book, author, and series management
- EPUB/PDF book file processing with Readium integration
- Reading progress tracking
- Unified search across books, authors, and series
- Demo data generation for development
- File storage and asset serving

## Tech Stack
- **Framework**: Quarkus 3.25.2
- **Language**: Java 21
- **Database**: PostgreSQL 16 with Flyway migrations
- **Architecture**: Hexagonal Architecture (Ports & Adapters)
- **Build Tool**: Maven 3.9+
- **Testing**: JUnit 5, Mockito, ArchUnit, REST Assured
- **Monitoring**: OpenTelemetry, Micrometer
- **Security**: OIDC (optional), input sanitization
- **API Documentation**: OpenAPI/Swagger

## Project Structure
```
backend/
├── pom.xml                          # Maven configuration
├── src/main/
│   ├── java/org/motpassants/
│   │   ├── domain/                  # Core business logic (pure Java)
│   │   │   ├── core/model/          # Domain entities
│   │   │   └── port/                # Interfaces (ports)
│   │   │       ├── in/              # Use case interfaces
│   │   │       └── out/             # Repository interfaces
│   │   ├── application/service/     # Use case implementations
│   │   └── infrastructure/          # Adapters
│   │       ├── adapter/in/rest/     # REST controllers
│   │       ├── adapter/out/         # Repository implementations
│   │       ├── config/              # Configuration
│   │       ├── media/               # Image processing
│   │       ├── readium/             # EPUB processing
│   │       └── security/            # Security adapters
│   └── resources/
│       ├── db/migration/            # Flyway SQL migrations
│       └── application.properties   # Quarkus configuration
├── assets/                          # Storage for books, covers, images
└── target/                          # Maven build output
```

## Key Architectural Principles
1. **Hexagonal Architecture**: Clear separation between domain, application, and infrastructure layers
2. **Dependency Inversion**: Core business logic has no dependencies on frameworks
3. **Port & Adapters**: All external interactions go through defined interfaces
4. **ArchUnit Validation**: Architecture rules are automatically tested
5. **Domain-Driven Design**: Rich domain models with business logic

## Configuration
- **Demo Mode**: `librarie.demo.enabled=true` - Generates sample data for development
- **Storage**: `librarie.storage.base-dir=assets` - Base directory for book files and images
- **File Size**: `librarie.storage.max-file-size=104857600` (100 MB)
- **CORS**: Enabled for `http://localhost:4200` (Angular frontend)
- **Database**: PostgreSQL with automatic Flyway migrations at startup
