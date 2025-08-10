# CI/CD Workflows

This repository contains GitHub Actions workflows for continuous integration and deployment.

## Workflows

### Backend CI (`backend-ci.yml`)
- **Triggers**: Changes to `src/`, `pom.xml`, or `backend/` directory
- **Java Version**: 21 (Temurin distribution)
- **Features**:
  - Maven dependency caching
  - Runs tests with `mvn clean test`
  - Packages application with `mvn package -DskipTests`
  - Uploads test results and coverage reports as artifacts

### Frontend CI (`frontend-ci.yml`)
- **Triggers**: Changes to `frontend/` directory
- **Node Version**: 20
- **Features**:
  - npm dependency caching
  - Code formatting checks with Prettier
  - Runs tests in headless Chrome with coverage
  - Builds production Angular application
  - Uploads test results and build artifacts


## Local Testing

Use the `test-ci.sh` script to run the same tests locally:

```bash
./test-ci.sh
```

## Cache Strategy

- **Maven**: Caches `~/.m2/repository` based on `pom.xml` hash
- **npm**: Uses GitHub Actions' built-in npm caching based on `package-lock.json`

## Best Practices Implemented

### Maven/Java
- Uses official `actions/setup-java@v4` with Temurin distribution
- Proper cache key based on `pom.xml` changes
- Separates test and package steps for better failure isolation
- Uploads test reports for debugging failed builds

### npm/Node.js
- Uses official `actions/setup-node@v4` with built-in caching
- Runs tests in headless Chrome for CI environment
- Enables code coverage collection
- Proper working directory handling for monorepo structure

### General
- Path-based triggers to avoid unnecessary builds
- Artifact retention policies (7 days)
- Conditional steps with proper `if` conditions
- Matrix strategy support for potential multi-version testing