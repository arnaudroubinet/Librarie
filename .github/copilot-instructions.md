# Copilot Coding Agent Instructions

ALWAYS follow these instructions and only fallback to additional search and context gathering if the information in these instructions is incomplete or found to be in error.

## Golden Rules (ALWAYS follow - NON-NEGOTIABLE)
1. **MANDATORY COMPILATION**: Always execute both backend AND frontend compilations before completing ANY task - NO EXCEPTIONS
2. **MANDATORY TEST VALIDATION**: Always run both backend AND frontend tests before completing ANY task - NO EXCEPTIONS
3. **FAILURE HANDLING**: If ANY compilation or test fails, MUST fix the issues, rerun tests, and retry compilation until ALL processes complete successfully
4. Always validate the code before commit
5. Always check security rules before commit
6. Always apply these instructions before commit
7. Query `context7` for extra context/metadata before filesystem searches
8. Minimize new dependencies: justify each new library (prefer Quarkus extensions already present); remove unused imports/config

## Repository Summary
Librarie is a dual-module workspace:
- **Backend**: Quarkus 3.25.2 REST API (Java 21, Maven) in `backend/`
- **Frontend**: Angular 20 SPA (TypeScript, npm) in `frontend/`

## MANDATORY COMPILATION AND TEST WORKFLOW

**CRITICAL REQUIREMENT**: Before completing ANY task, you MUST execute this complete workflow and ensure ALL steps pass:

### Step 1: Backend Compilation (MANDATORY)
```bash
cd /home/runner/work/Librarie/Librarie/backend
mvn clean install  # MUST succeed - TIMEOUT: 30 minutes
```
**Expected**: BUILD SUCCESS, ~69 seconds (after initial dependencies)
**On Failure**: Fix compilation errors, retry until success

### Step 2: Backend Tests (MANDATORY)  
```bash
cd /home/runner/work/Librarie/Librarie/backend
mvn test  # MUST succeed - TIMEOUT: 90 minutes first run, 10 minutes subsequent
```
**Expected**: 150 tests pass, ~57 seconds (after Docker images downloaded)
**On Failure**: Fix failing tests, retry until ALL tests pass

### Step 3: Frontend Compilation (MANDATORY)
```bash
cd /home/runner/work/Librarie/Librarie/frontend
npm run build  # MUST succeed - TIMEOUT: 10 minutes
```
**Expected**: Bundle generation complete, ~9 seconds
**On Failure**: Fix compilation errors, retry until success

### Step 4: Frontend Tests (MANDATORY)
```bash
cd /home/runner/work/Librarie/Librarie/frontend  
npm test -- --watch=false --browsers=ChromeHeadless  # MUST succeed - TIMEOUT: 10 minutes
```
**Expected**: 2 tests pass, ~9 seconds
**On Failure**: Fix failing tests, retry until ALL tests pass

**NO TASK IS COMPLETE UNTIL ALL 4 STEPS PASS SUCCESSFULLY**

## Prerequisites and Environment
### Required Software
- **Java 21** (Temurin/OpenJDK) - REQUIRED. Check with `java -version`
- **Maven 3.6+** - included via `./mvnw` wrapper
- **Node.js 20+** and **npm 10+** - Check with `node -v && npm -v`
- **Docker** - REQUIRED for backend tests and DevServices. Check with `docker --version && docker info`

### Critical Docker Requirement
**Docker MUST be running** before starting backend development or tests. Backend uses Quarkus DevServices which automatically starts:
- PostgreSQL database container
- Keycloak OIDC authentication container

## Build and Test Timing (NEVER CANCEL)

### Backend Build Times
- **Initial build**: `mvn clean install` takes ~2.5 minutes (includes dependency downloads)
- **Subsequent builds**: `mvn compile` takes ~30 seconds
- **Tests**: `mvn test` takes ~56 seconds with 150 tests
  - **NEVER CANCEL** - First test run downloads Docker images (PostgreSQL, Keycloak, Ryuk) ~230MB
  - Subsequent test runs are faster (~30 seconds)
  - Set timeout to **90+ minutes** for first test run, **10+ minutes** for subsequent runs

### Frontend Build Times  
- **Dependencies**: `npm install` takes ~15 seconds
- **Build**: `npm run build` takes ~10 seconds
- **Tests**: `npm test` takes ~6 seconds with 2 tests
- **NEVER CANCEL** - Set timeout to **10+ minutes** for all frontend operations

### Development Server Startup
- **Backend**: `./mvnw quarkus:dev` takes ~2-3 minutes on first start (includes DevServices startup)
- **Frontend**: `npm start` takes ~30 seconds
- **NEVER CANCEL** - Set timeout to **10+ minutes** for dev server startup

## Working Effectively

### Bootstrap and Build Process
```bash
# 1. Verify prerequisites
java -version  # Must be Java 21
node -v        # Must be Node 20+
npm -v         # Must be npm 10+
docker --version && docker info  # Docker must be running

# 2. Backend build (NEVER CANCEL - takes 2.5 minutes)
cd backend
mvn clean install  # TIMEOUT: 30 minutes

# 3. Frontend build (NEVER CANCEL - stable dependencies available)
cd ../frontend
# NOTE: @readium packages now have stable 2.0.0 releases (August 2025)
# Only @readium/css remains in beta (2.0.0-beta.18)
npm install     # TIMEOUT: 10 minutes
npm run build   # TIMEOUT: 10 minutes
```

### Running the Applications
```bash
# Backend Dev Server (NEVER CANCEL - takes 2-3 minutes first time)
cd backend
./mvnw quarkus:dev  # TIMEOUT: 15 minutes

# Frontend Dev Server (in separate terminal)
cd frontend  
npm start           # TIMEOUT: 5 minutes

# URLs when running:
# Backend Dev UI: http://localhost:8080/q/dev-ui/extensions
# Frontend App: http://localhost:4200/books
# API Documentation: http://localhost:8080/q/swagger-ui
# Health Check: http://localhost:8080/q/health
```

### Testing
```bash
# Backend Tests (NEVER CANCEL - 56 seconds, 150 tests)
cd backend
mvn test  # TIMEOUT: 90 minutes (first run), 10 minutes (subsequent)

# Frontend Tests (NEVER CANCEL - 6 seconds, 2 tests)  
cd frontend
npm test -- --watch=false --browsers=ChromeHeadless  # TIMEOUT: 10 minutes
```

## MANDATORY Validation Scenarios (BLOCKING REQUIREMENTS)

**CRITICAL**: These validations are MANDATORY and BLOCKING. NO task can be considered complete until ALL these scenarios pass successfully. If ANY step fails, you MUST fix the issue and retry until ALL validations pass.

After making ANY changes, you MUST execute and PASS all these validation scenarios:

### 1. MANDATORY Build Validation (MUST PASS)
**REQUIREMENT**: Both backend and frontend MUST compile successfully. Any failure MUST be fixed.

```bash
# Backend build MUST succeed - RETRY until success
cd backend && mvn clean install  # TIMEOUT: 30 minutes

# Frontend build MUST succeed - RETRY until success
cd frontend && npm run build     # TIMEOUT: 10 minutes
```

**FAILURE HANDLING**: If either build fails:
1. Analyze the compilation errors
2. Fix ALL compilation issues
3. Re-run the failed build command
4. Repeat until BOTH builds succeed

### 2. MANDATORY Test Validation (MUST PASS)
**REQUIREMENT**: Both backend and frontend tests MUST pass completely. Any test failure MUST be fixed.

```bash
# Backend tests MUST pass - RETRY until success
cd backend && mvn test  # TIMEOUT: 90 minutes first run, 10 minutes subsequent

# Frontend tests MUST pass - RETRY until success  
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless  # TIMEOUT: 10 minutes
```

**FAILURE HANDLING**: If any tests fail:
1. Analyze the test failures
2. Fix ALL test issues (code fixes, not test modifications unless tests are incorrect)
3. Re-run the failed test command
4. Repeat until ALL tests pass

### 3. Application Startup Validation
- Start backend: `./mvnw quarkus:dev` (TIMEOUT: 15 minutes)
- Start frontend: `npm start` (TIMEOUT: 5 minutes)
- **MANDATORY**: Take screenshot of http://localhost:8080/q/dev-ui/extensions
- **MANDATORY**: Take screenshot of http://localhost:4200/books

### 4. API Endpoint Validation
```bash
# Test core endpoints are responding
curl -s http://localhost:8080/q/health | head -5
curl -s http://localhost:8080/v1/books | jq '.content | length'  # Should return book count
curl -s http://localhost:8080/v1/authors | jq '.content | length'  # Should return author count
```

### 5. User Workflow Validation
Navigate through the frontend application:
- Browse books at http://localhost:4200/books (should show book covers)
- Test navigation to Authors, Series, Search pages
- Verify all navigation links work correctly

## Common Issues and Solutions

### Frontend Dependency Status
**Current Status** (as of August 2025): Most @readium packages now have stable 2.0.0 releases
- ✅ @readium/navigator: 2.0.0 (stable)
- ✅ @readium/shared: 2.0.0 (stable) 
- ⚠️ @readium/css: 2.0.0-beta.18 (still in beta)
- ✅ @readium/navigator-html-injectables: 2.0.0 (stable)

**package.json configuration**:
```json
"@readium/navigator": "^2.0.0",
"@readium/shared": "^2.0.0",
"@readium/css": "^2.0.0-beta.18",
"@readium/navigator-html-injectables": "^2.0.0"
```

### Docker Issues
- **Missing Docker**: Tests fail with DevServices errors. Install Docker Desktop and ensure it's running.
- **First test run**: Downloads ~230MB of containers (PostgreSQL, Keycloak, Ryuk). Be patient.
- **Container reuse warning**: Benign warning about container reuse. Can be ignored.

### Performance Warnings (Ignore These)
- OpenTelemetry connection refused to localhost:4317 - expected, no OTLP collector configured
- Mockito self-attaching warnings - expected in test environment
- Java agent loading warnings - expected behavior

## Tech & Layout
```
Root structure:
├── docs/               # Project documentation and vision
├── backend/            # Quarkus REST API
│   ├── src/main/java/  # Application source code
│   ├── src/test/java/  # Test source code  
│   ├── src/main/resources/application*.properties  # Configuration
│   └── pom.xml         # Maven configuration
├── frontend/           # Angular SPA
│   ├── src/            # Application source code
│   ├── angular.json    # Angular configuration
│   └── package.json    # npm dependencies and scripts
├── scripts/            # Utility scripts (cover repair, data extraction)
└── .github/workflows/  # CI/CD pipeline definitions
```

## API Endpoints (Validated Working)
- Health Check: `GET /q/health`
- Books: `GET /v1/books` (returns paginated book list)
- Authors: `GET /v1/authors` (returns paginated author list)
- Dev UI: `GET /q/dev-ui/extensions` (Quarkus development interface)
- OpenAPI: `GET /q/openapi` (API specification)

## Pull Request Requirements (BLOCKING REQUIREMENTS)

**CRITICAL**: ALL requirements below are MANDATORY and BLOCKING. NO pull request can be submitted until these requirements are met.

### Before Every Commit (MANDATORY - NO EXCEPTIONS)
These validations MUST be executed and MUST pass before any commit:

1. **MANDATORY Backend Build**: `cd backend && mvn clean install` (TIMEOUT: 30 minutes)
   - MUST succeed with zero compilation errors
   - If fails: Fix errors, retry until success
   
2. **MANDATORY Backend Tests**: `cd backend && mvn test` (TIMEOUT: 90 minutes first run)
   - ALL tests MUST pass (currently 150 tests)
   - If fails: Fix failing tests, retry until ALL pass
   
3. **MANDATORY Frontend Build**: `cd frontend && npm run build` (TIMEOUT: 10 minutes)
   - MUST succeed with zero compilation errors
   - If fails: Fix errors, retry until success
   
4. **MANDATORY Frontend Tests**: `cd frontend && npm test -- --watch=false --browsers=ChromeHeadless` (TIMEOUT: 10 minutes)
   - ALL tests MUST pass (currently 2 tests)
   - If fails: Fix failing tests, retry until ALL pass

**FAILURE ESCALATION**: If you cannot resolve compilation or test failures:
1. Document the specific error messages
2. Analyze the root cause
3. Implement targeted fixes (minimal changes)
4. Re-run validation
5. Repeat until success - DO NOT proceed with incomplete validation

### Before Requesting Review  
1. **Proof of backend**: Screenshot of http://localhost:8080/q/dev-ui/extensions
2. **Proof of frontend**: Screenshot of http://localhost:4200/books  
3. **Build timing**: Include actual build times in commit message
4. **Warning documentation**: Add any build warnings as PR comments

### Commit Message Format (MANDATORY)
```
{Feature type} - {content of the commit} - java {mvn build time} - angular {npm build time} - ALL_VALIDATIONS_PASSED
```
Where {feature type} is: `feature`, `improvement`, or `bugfix`

**REQUIRED**: The commit message MUST include "ALL_VALIDATIONS_PASSED" to confirm that ALL mandatory compilation and test validations have been executed and passed successfully.

## Environment Variables and Configuration
See `docs/environment-variables.md` for complete reference. Key production variables:
- `DB_USERNAME`, `DB_PASSWORD`, `DB_URL` (PostgreSQL connection)
- `OIDC_AUTH_SERVER_URL`, `OIDC_CLIENT_ID`, `OIDC_CLIENT_SECRET` (Authentication)
- `LIBRARIE_STORAGE_BASE_DIR` (File storage location)

## When to Search Beyond These Instructions
ONLY perform repository-wide searches if:
- A file/path mentioned here is missing or renamed
- A build/test command fails unexpectedly after following all prerequisites
- You need to locate newly added code not yet integrated into this guide

Always query `context7` MCP server first for additional context before performing filesystem searches.
