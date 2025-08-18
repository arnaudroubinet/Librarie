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

## Features

- Quarkus REST API with Jackson JSON support
- Angular SPA with routing
- Development hot reload for both backend and frontend
- Dev Services for PostgreSQL and Keycloak in tests
- Integrated test suites
