# MotsPassants
A Quarkus and Angular library management system

## Project Structure

This workspace contains:
- **Backend**: Quarkus REST API (Java 21, Maven) in `backend/`
- **Frontend**: Angular SPA (TypeScript, npm) in `frontend/`

## Prerequisites

- Java 21 (Temurin/OpenJDK) or later
- Maven 3.6+
- Node.js 18+ and npm

## Development

### Quick Start (Windows)
Use the provided wrappers for a smooth experience:

```powershell
# Backend (runs on :8080 by default)
./backend/mvnw.cmd quarkus:dev

# Frontend (runs on :4200)
cd frontend
npm start
```

Once started:
- Backend Dev UI: http://localhost:8080/q/dev-ui/extensions
- Frontend app: http://localhost:4200/books

### Manual Start

#### Backend (Quarkus)
```bash
cd backend
./mvnw quarkus:dev
```
The backend Dev UI is at http://localhost:8080/q/dev-ui/extensions

#### Frontend (Angular)
```bash
cd frontend
npm start
```
The frontend will be available at http://localhost:4200

### Testing

#### Backend Tests
```bash
cd backend
mvn test
```

#### Frontend Tests
```bash
cd frontend
npm test
```

## API Endpoints

- See the OpenAPI docs at http://localhost:8080/q/swagger-ui

## Environment Variables

### Required for Production

| Variable | Purpose | Default | Required |
|----------|---------|---------|----------|
| `DB_USERNAME` | PostgreSQL database username | - | ✅ |
| `DB_PASSWORD` | PostgreSQL database password | - | ✅ |
| `DB_URL` | PostgreSQL JDBC connection URL | - | ✅ |
| `OIDC_AUTH_SERVER_URL` | OIDC authentication server URL | `https://auth.company.com/realms/prod-realm` | ✅ |
| `OIDC_CLIENT_ID` | OIDC client identifier | `librarie-prod` | ✅ |
| `OIDC_CLIENT_SECRET` | OIDC client secret | - | ✅ |

### Configuration Properties

The following properties can be overridden via environment variables or application.properties:

| Property | Purpose | Default | Type |
|----------|---------|---------|------|
| `librarie.storage.base-dir` | Base directory for file storage | `assets` | Directory path |
| `librarie.storage.max-file-size` | Maximum file upload size in bytes | `104857600` (100MB) | Number |
| `librarie.storage.allowed-book-extensions` | Allowed book file extensions | `pdf,epub,mobi,azw,azw3,fb2,txt,rtf,doc,docx` | Comma-separated |
| `librarie.storage.allowed-image-extensions` | Allowed image file extensions | `jpg,jpeg,png,gif,webp,bmp` | Comma-separated |
| `librarie.demo.enabled` | Enable demo data population | `true` | Boolean |
| `librarie.demo.book-count` | Number of demo books to create | `100` | Number |
| `librarie.demo.author-count` | Number of demo authors to create | `50` | Number |
| `librarie.demo.series-count` | Number of demo series to create | `20` | Number |
| `librarie.security.sanitization-enabled` | Enable input sanitization | `true` | Boolean |
| `librarie.security.file-validation-enabled` | Enable file type validation | `true` | Boolean |
| `librarie.security.max-request-size` | Maximum request size in bytes | `10485760` (10MB) | Number |

### Example Production Environment

```bash
# Database configuration
export DB_USERNAME=librarie_user
export DB_PASSWORD=secure_password
export DB_URL=jdbc:postgresql://db.company.com:5432/librarie

# OIDC authentication
export OIDC_AUTH_SERVER_URL=https://auth.company.com/realms/production
export OIDC_CLIENT_ID=librarie-app
export OIDC_CLIENT_SECRET=your-client-secret-here

# Optional: Override default storage location
export LIBRARIE_STORAGE_BASE_DIR=/var/lib/librarie/storage
```

## Features

- Quarkus REST API with Jackson JSON support
- Angular SPA with routing
- Development hot reload for both backend and frontend
- Dev Services for PostgreSQL and Keycloak in tests
- Integrated test suites
