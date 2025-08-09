# Librarie
A Quarkus and Angular library management system

## Project Structure

This workspace contains:
- **Backend**: Quarkus REST API (Java 21, Maven)
- **Frontend**: Angular SPA (TypeScript, npm)

## Prerequisites

- Java 21 (Temurin/OpenJDK) or later
- Maven 3.6+
- Node.js 18+ and npm

## Development

### Quick Start
Run both development servers with a single command:
```bash
./start-dev.sh
```

This will start:
- Quarkus backend on http://localhost:8080
- Angular frontend on http://localhost:4200

### Manual Start

#### Backend (Quarkus)
```bash
mvn quarkus:dev
```
The backend will be available at http://localhost:8080

Test the API:
```bash
curl http://localhost:8080/hello
```

#### Frontend (Angular)
```bash
cd frontend
npm start
```
The frontend will be available at http://localhost:4200

### Testing

#### Backend Tests
```bash
mvn test
```

#### Frontend Tests
```bash
cd frontend
npm test
```

## API Endpoints

- `GET /hello` - Simple greeting endpoint

## Features

- Quarkus REST API with Jackson JSON support
- Angular SPA with routing
- Development hot reload for both backend and frontend
- Integrated test suites
