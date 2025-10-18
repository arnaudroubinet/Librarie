# Librarie

A modern library management application built with Quarkus (backend) and Angular (frontend).

## Features

- Book management with metadata and cover images
- Author and series tracking
- Advanced search and filtering capabilities
- Demo data population for testing
- RESTful API with OpenAPI documentation
- Health checks and monitoring

## Quick Start

For detailed setup instructions, see the [Development Setup Guide](docs/development-setup.md).

### Prerequisites

- Docker Desktop (running)
- Java 21+
- Node.js 18+
- Maven (included via wrapper)

### Enable Fast Development Mode

**Important**: Configure TestContainers reuse to improve backend startup performance.

PostgreSQL container startup: **~0.4s** (with reuse) vs **~2s** (without reuse)

Copy the TestContainers template to your home directory:

**Linux/Mac:**
```bash
cp docs/testcontainers.properties.template ~/.testcontainers.properties
```

**Windows (PowerShell):**
```powershell
Copy-Item docs\testcontainers.properties.template $env:USERPROFILE\.testcontainers.properties
```

See the [Development Setup Guide](docs/development-setup.md) for complete instructions.

### Running the Application

**Full Stack (Windows):**
```powershell
.\dev.ps1
```

**Backend Only:**
```bash
cd backend
./mvnw quarkus:dev
```

**Frontend Only:**
```bash
cd frontend
npm install
npm start
```

## Project Structure

```
.
├── backend/          # Quarkus backend application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/        # Application code
│   │   │   └── resources/   # Configuration files
│   │   └── test/            # Test files
│   └── pom.xml
├── frontend/         # Angular frontend application
│   ├── src/
│   └── package.json
├── docs/             # Documentation
│   ├── development-setup.md
│   └── testcontainers.properties.template
├── scripts/          # Utility scripts
└── dev.ps1          # Development script (Windows)
```

## API Documentation

When running in development mode, access the Swagger UI at:
- http://localhost:8080/q/swagger-ui

## Development URLs

- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/q/health
- **Dev UI**: http://localhost:8080/q/dev

## Documentation

- [Development Setup Guide](docs/development-setup.md) - Complete setup instructions with TestContainers configuration

## Technology Stack

### Backend
- Quarkus 3.25.2
- Java 21
- PostgreSQL
- Flyway (database migrations)
- Keycloak (authentication)
- OpenTelemetry (observability)
- Micrometer (metrics)

### Frontend
- Angular (latest)
- TypeScript
- RxJS

## Architecture

The backend follows hexagonal architecture (ports and adapters) with Domain-Driven Design principles:

- **Domain Layer**: Core business logic and entities
- **Application Layer**: Use cases and application services
- **Infrastructure Layer**: Adapters for external systems (REST, database, etc.)

## Testing

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

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests
5. Submit a pull request

## License

[License information to be added]

## Support

For issues and questions, please use the GitHub issue tracker.
