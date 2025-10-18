# Librarie - Digital Library Management System

A modern, full-stack library management system for managing and reading digital books (EPUB, PDF, and more).

## Features

- 📚 **Book Management**: Organize books, authors, and series
- 🔍 **Unified Search**: Search across books, authors, and series
- 📖 **eBook Reader**: Built-in EPUB reader powered by Readium
- 📊 **Reading Progress**: Track reading position across devices
- 🎨 **Modern UI**: Responsive design with Angular Material
- 🚀 **Fast & Lightweight**: Quarkus backend with sub-second startup
- 🔐 **Security**: OIDC authentication support (optional)
- 🌍 **Multi-language**: Support for multiple languages (BCP 47 standard)

## Architecture

For detailed architecture documentation including C4 diagrams, hexagonal architecture patterns, and architectural decision records (ADRs), see:

**📐 [ARCHITECTURE.md](./ARCHITECTURE.md)**

### Quick Overview

- **Backend**: Quarkus 3.25 (Java 21) with hexagonal architecture
- **Frontend**: Angular 20 (TypeScript) with standalone components
- **Database**: PostgreSQL 16 with Flyway migrations
- **API**: RESTful JSON API
- **eBook Reader**: Readium Web Reader

## Getting Started

### Prerequisites

- Java 21 or later
- Node.js 18 or later
- PostgreSQL 16 or later
- Maven 3.9+ (or use included wrapper)

### Backend Setup

```bash
cd backend

# Run in development mode (with hot reload)
./mvnw quarkus:dev

# The API will be available at http://localhost:8080
# Swagger UI: http://localhost:8080/q/swagger-ui
```

### Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm start

# The application will be available at http://localhost:4200
```

### Configuration

Backend configuration is in `backend/src/main/resources/application.properties`:

```properties
# Demo mode (generates sample data)
librarie.demo.enabled=true

# Storage settings
librarie.storage.base-dir=assets
librarie.storage.max-file-size=104857600

# Database (configure for your environment)
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/librarie
```

## Development

### Running Tests

**Backend:**
```bash
cd backend
./mvnw test
```

**Frontend:**
```bash
cd frontend
npm test
```

### Architecture Validation

The backend includes ArchUnit tests that automatically validate the hexagonal architecture:

```bash
cd backend
./mvnw test -Dtest=HexagonalArchitectureTest
```

### Building for Production

**Backend:**
```bash
cd backend
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

**Frontend:**
```bash
cd frontend
npm run build
# Output in dist/ directory
```

## Project Structure

```
librarie/
├── backend/                 # Quarkus backend application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/motpassants/
│   │   │   │   ├── domain/          # Core business logic
│   │   │   │   ├── application/     # Use cases
│   │   │   │   └── infrastructure/  # Adapters
│   │   │   └── resources/
│   │   │       ├── db/migration/    # Flyway migrations
│   │   │       └── application.properties
│   │   └── test/
│   └── pom.xml
├── frontend/                # Angular frontend application
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/  # Feature components
│   │   │   ├── services/    # API services
│   │   │   └── models/      # TypeScript models
│   │   └── index.html
│   └── package.json
├── ARCHITECTURE.md          # Architecture documentation
└── README.md               # This file
```

## Documentation

- **[Architecture Documentation](./ARCHITECTURE.md)** - System architecture, C4 diagrams, and ADRs
- **[Frontend README](./frontend/README.md)** - Angular-specific documentation
- **[Backend API](http://localhost:8080/q/swagger-ui)** - Swagger UI (when running)

## Technology Stack

### Backend
- **Framework**: Quarkus 3.25
- **Language**: Java 21
- **Database**: PostgreSQL 16
- **Migrations**: Flyway
- **Testing**: JUnit 5, ArchUnit, Mockito
- **Monitoring**: OpenTelemetry, Micrometer
- **API Docs**: OpenAPI/Swagger

### Frontend
- **Framework**: Angular 20
- **Language**: TypeScript 5.8
- **UI Library**: Angular Material
- **eBook Reader**: Readium Navigator
- **Build**: Angular CLI
- **Testing**: Jasmine, Karma, Playwright

## Contributing

1. Follow the hexagonal architecture patterns (see ARCHITECTURE.md)
2. Run tests before committing
3. Update documentation for significant changes
4. Follow existing code style and conventions

## License

[Add your license here]

## Support

For questions or issues, please [open an issue](../../issues) on GitHub.
