# CI/CD Workflows

[![CI Pipeline](https://github.com/arnaudroubinet/Librarie/actions/workflows/ci.yml/badge.svg)](https://github.com/arnaudroubinet/Librarie/actions/workflows/ci.yml)
[![CodeQL](https://github.com/arnaudroubinet/Librarie/actions/workflows/codeql.yml/badge.svg)](https://github.com/arnaudroubinet/Librarie/actions/workflows/codeql.yml)
[![Backend CI](https://github.com/arnaudroubinet/Librarie/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/arnaudroubinet/Librarie/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/arnaudroubinet/Librarie/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/arnaudroubinet/Librarie/actions/workflows/frontend-ci.yml)

This repository contains GitHub Actions workflows for continuous integration and deployment.

## Quick Links
- 📖 [Contributing Guidelines](../../CONTRIBUTING.md)
- 🔒 [Branch Protection Setup](./BRANCH_PROTECTION.md)
- 🔍 [Actions Dashboard](https://github.com/arnaudroubinet/Librarie/actions)

## Workflows

### Main CI Pipeline (`ci.yml`) ⭐
**Primary workflow for pull requests and pushes to main/develop branches.**

**Triggers:**
- Pull requests to `main` or `develop` branches
- Direct pushes to `main` or `develop` branches

**Features:**
- **Smart Change Detection**: Only runs jobs for changed components using path filters
- **Parallel Execution**: Backend, frontend, and security scans run concurrently
- **Unified Status Check**: Single gate that verifies all jobs passed
- **Optimized Performance**: Skips unnecessary builds using concurrency controls

**Jobs:**
1. **detect-changes**: Determines which parts of the codebase changed
2. **backend-ci**: Builds and tests Java backend (conditional)
3. **frontend-ci**: Builds and tests Angular frontend (conditional)
4. **dependency-scan**: Scans for vulnerable dependencies with Trivy
5. **ci-status**: Final verification that all jobs succeeded

### CodeQL Security Analysis (`codeql.yml`) 🔒
**Automated security vulnerability scanning.**

**Triggers:**
- Pull requests to `main` or `develop`
- Pushes to `main` or `develop`
- Weekly schedule (Mondays at 00:00 UTC)

**Languages Analyzed:**
- Java (backend)
- JavaScript/TypeScript (frontend)

**Features:**
- Security vulnerability detection
- Code quality analysis
- Results uploaded to GitHub Security tab
- Uses `security-and-quality` query suite

### Backend CI (`backend-ci.yml`)
**Dedicated workflow for backend-specific changes.**

- **Triggers**: Changes to `backend/**` or this workflow file (ignoring `main` branch)
- **Java Version**: 21 (Temurin distribution)
- **Features**:
  - Maven dependency caching
  - Quarkus build cache optimization
  - Checkstyle linting (non-blocking)
  - Runs tests with `mvn clean test`
  - Code coverage with JaCoCo
  - Packages application with `mvn package -DskipTests`
  - Uploads test results and coverage reports as artifacts

### Frontend CI (`frontend-ci.yml`)
**Dedicated workflow for frontend-specific changes.**

- **Triggers**: Changes to `frontend/**` or this workflow file (ignoring `main` branch)
- **Node Version**: 20
- **Features**:
  - npm dependency caching
  - Code formatting checks with Prettier
  - Runs unit tests in headless Chrome with coverage
  - Builds production Angular application
  - Uploads test results and build artifacts

## Security Scanning

### Dependency Scanning (Trivy)
The main CI workflow includes Trivy vulnerability scanning that:
- Scans all project dependencies
- Detects CRITICAL and HIGH severity vulnerabilities
- Uploads results to GitHub Security tab (SARIF format)
- Results available as downloadable artifacts

### CodeQL Analysis
Static code analysis for security vulnerabilities:
- Analyzes both Java and JavaScript/TypeScript code
- Runs on PRs, pushes, and weekly schedule
- Detects common security issues and code quality problems
- Integrates with GitHub Security tab


## Artifacts & Reports

All workflows upload artifacts with 7-day retention:

| Workflow | Artifact Name | Contents |
|----------|---------------|----------|
| Backend CI | `backend-test-results-java-21` | Surefire test reports |
| Backend CI | `backend-coverage-java-21` | JaCoCo coverage reports |
| Frontend CI | `frontend-test-results-node-20` | Karma/Jasmine coverage reports |
| Frontend CI | `frontend-build-node-20` | Production build files |
| Main CI | `trivy-security-scan` | Vulnerability scan results (SARIF) |

## Required Status Checks for PRs

To enable branch protection, configure these required status checks:
1. **Backend - Build & Test** (from `ci.yml`)
2. **Frontend - Build & Test** (from `ci.yml`)
3. **Security - Dependency Scan** (from `ci.yml`)
4. **CI Status Check** (from `ci.yml`)
5. **Analyze Code (java)** (from `codeql.yml`)
6. **Analyze Code (javascript)** (from `codeql.yml`)

See [Branch Protection Setup](./BRANCH_PROTECTION.md) for detailed configuration steps.


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