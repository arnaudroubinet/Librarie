# Copilot Coding Agent Onboarding Guide

(<= 2 pages)

## Golden Rules (ALWAYS follow)
1. Build fast: run `mvn -DskipTests clean package` early/often; avoid unnecessary full test cycles while coding.
2. Test before commit: always finish with `mvn test` (and frontend `npm run build` if frontend changed) plus a healthcheck smoke curl.
3. Trust this doc first: only search the repo or explore broadly if information here proves incomplete/incorrect. Thrown an alert if something seem to not match.
4. Query `context7` first (MCP) when available for extra context/metadata before filesystem searches.
5. Minimize new dependencies: justify each new library (prefer Quarkus extensions already present); remove unused imports/config.
6. **NEVER use H2 database**: Always use PostgreSQL for all environments. H2 is prohibited.
7. **Frontend Style Guide Compliance**: ALWAYS ensure the `/frontend` directory and Angular project follows the [Angular Style Guide](https://angular.dev/style-guide) including naming conventions, file organization, and code structure.
8. **Backend Architecture Compliance**: ALWAYS ensure the `/backend` follows [hexagonal architecture principles](https://scalastic.io/en/hexagonal-architecture-domain) with proper separation of domain, application, and infrastructure layers.


## 1. Repository Summary
Librarie is a dual-module workspace:
- Backend: Quarkus 3.x (Java 21, Maven) REST API. Currently a simple greeting + health endpoints with environment profile config (dev/prod). Includes OpenAPI, Health, OIDC, Flyway, Quartz, Micrometer, OpenTelemetry, Hibernate ORM (present but no entities yet), Keycloak Dev Services (pulled automatically in tests due to OIDC), and Testcontainers usage via Quarkus Dev Services.
- Frontend: Angular 20 (TypeScript) scaffold. Minimal routing; placeholder template.
Purpose: Scaffold for a future library management system.

## 2. Tech & Layout
Root files: `start-dev.sh`, `README.md`, `docs/`, `frontend/`, `backend/` (backend).
Key paths:
- Backend source: `backend/src/main/java/...`
- Backend tests: `backend/src/test/java/...`
- Backend config: `backend/src/main/resources/application*.properties`
- Backend Maven: `backend/pom.xml`
- Frontend app: `frontend/src/`
- Angular config: `frontend/angular.json`, `frontend/package.json`
- Script to launch both dev servers: `./start-dev.sh`
- Documentation: `docs/` currently includes `feature-map.md`, `migration-plan.md`, `open-questions.md` – read these to understand the vision, upcoming workstreams, and open questions before adding major features.
- CI/CD workflows: `.github/workflows` contains backend-ci.yml, frontend-ci.yml, full-ci.yml.

## 2.1. Frontend Architecture Guidelines (Angular Style Guide)
The `/frontend` directory MUST follow [Angular Style Guide](https://angular.dev/style-guide):

**File Naming & Organization:**
- Use kebab-case for file names: `book-list.component.ts`, `user-profile.service.ts`
- Append type suffix: `.component.ts`, `.service.ts`, `.model.ts`, `.pipe.ts`
- Place feature files in feature folders: `/components`, `/services`, `/models`, `/pipes`

**Component Structure:**
- Use standalone components (Angular 17+ pattern)
- Keep template and styles inline for small components, separate files for larger ones
- Use OnPush change detection strategy when appropriate
- Implement lifecycle interfaces explicitly (`OnInit`, `OnDestroy`)

**Service and Dependency Injection:**
- Use providedIn: 'root' for singleton services
- Inject dependencies through constructor
- Use proper typing with TypeScript

**Code Organization:**
- Export symbols from barrel files (index.ts) when needed
- Group related functionality in feature modules
- Keep shared components in `/shared` directory if created

## 2.2. Backend Architecture Guidelines (Hexagonal Architecture)
The `/backend` directory MUST follow [hexagonal architecture principles](https://scalastic.io/en/hexagonal-architecture-domain):

**Domain Layer** (`/domain`):
- Contains business entities and domain logic
- No dependencies on infrastructure or application layers
- Pure business rules and domain models

**Application Layer** (`/application`):
- **Ports In** (`/port/in`): Primary ports defining use cases (interfaces that incoming adapters will implement)
- **Ports Out** (`/port/out`): Secondary ports for outgoing dependencies (interfaces for repositories, external services)
- **Services** (`/service`): Application services implementing use cases

**Infrastructure Layer** (`/infrastructure`):
- **Adapters In** (`/adapter/in`): Controllers, REST endpoints, event listeners
- **Adapters Out** (`/adapter/out`): Repository implementations, external service clients
- Configuration and framework-specific code

**Key Principles:**
- Dependencies point inward: Infrastructure → Application → Domain
- Domain layer has no outward dependencies
- Use dependency inversion through interfaces (ports)
- Keep business logic in domain/application layers

## 3. Runtimes & Required Versions (ALWAYS honor)
- Java: 21 (project now sets `maven.compiler.release=21`). Use Temurin/OpenJDK 21.
- Maven: 3.8+ recommended (Surefire 3.5.3 used).
- Node: 18+ (Angular CLI 20 works under Node 18 or 20). Prefer LTS (Node 20) if available.
- npm: 9+.
- Docker: Needed for Quarkus Dev Services (Keycloak test container) during `mvn test`. Ensure Docker daemon is available or disable OIDC / Dev Services if adding fast unit tests.

**Version Installation Requirements:**
If the development environment does not have the correct JDK or Node.js versions, you MUST install them using SDK:
- For Java 21: `sdk install java 21.0.1-tem` (using SDKMAN)
- For Node.js 20: `sdk install node 20.10.0` (using SDKMAN)
- Always maintain Java 21 compatibility - do not downgrade to accommodate older environments.

## 4. Build / Run / Test Cheat Sheet
ALWAYS run `cd backend && mvn -q -DskipTests clean package` before committing backend changes to ensure compilation & packaging succeed.

Backend:
1. Clean & compile (skip tests): `cd backend && mvn -DskipTests clean package`
2. Full tests: `cd backend && mvn test` (pulls Keycloak image first time; adds ~60–120s). Expect warnings: Agroal (no datasource), Hibernate (no entities). Harmless unless adding persistence.
3. Dev mode: `cd backend && mvn quarkus:dev` (hot reload, port 8080). Greeting endpoint: `GET http://localhost:8080/hello`.
4. Profiles: dev (default), prod (set with `-Dquarkus.profile=prod`). In prod profile OpenAPI + Swagger UI disabled.
5. Native build (if ever needed): `cd backend && mvn -Dnative -DskipTests package` (GraalVM/mandrel required; not currently configured beyond profile skeleton).

Frontend:
1. Install deps (ALWAYS after cloning or when `package.json` changes): `cd frontend && npm ci`.
2. Dev server: `npm start` (Angular live reload on port 4200).
3. Build: `npm run build` output in `frontend/dist/`.
4. Unit tests (headless): ensure Chrome/Chromium available. Command: `npm test` (Karma). Provide `--watch=false --browsers=ChromeHeadless` for CI style.

Combined dev startup: `./start-dev.sh` (spawns Quarkus then Angular). Script assumes working directory root, existing `frontend` dependencies installed. ALWAYS run `npm ci` once before first script invocation.

## 5. Common Pitfalls & Mitigations
- Long test startup: First `mvn test` triggers Docker pulls for Keycloak + Ryuk (~230MB). Subsequent runs faster. Do NOT assume a hang; observe logs.
- Missing Docker: Tests may fail/time out when Dev Services cannot start Keycloak. If Docker unavailable, you can temporarily disable OIDC tests by removing OIDC extension or setting `-Dquarkus.oidc.enabled=false` (not yet in config) but prefer keeping default.
- Java version mismatch: If environment supplies Java <21, build may fail or use unsupported features; ensure Java 21 is active (`java -version`).
- Frontend tests may need system dependencies (Chrome). In minimal containers install `chromium-browser` or adjust Karma config.
- OpenTelemetry warnings (connection refused to localhost:4317) are benign unless you configure an OTLP collector. Ignore for now.

## 6. Configuration Overview
Files:
- `application.properties`: base config (port 8080, name, version, PostgreSQL database configuration).
- `application-dev.properties`: enables OpenAPI & Swagger UI, debug logging, sets OIDC placeholders.
- `application-prod.properties`: disables OpenAPI/Swagger, info logging.

**Database Configuration - CRITICAL:**
- ALWAYS use PostgreSQL (`quarkus-jdbc-postgresql` dependency).
- NEVER use H2 database (`quarkus-jdbc-h2`) - it is prohibited in all environments.
- Database configured with PostgreSQL connection defaults in `application.properties`.

Tests rely on config profile overrides to assert OpenAPI flags in dev vs prod.

## 7. Test Suite Summary
Tests (`src/test/java/...`):
- `GreetingResourceTest`: Asserts `/hello` returns "Hello from Quarkus REST".
- `HealthEndpointTest`: Asserts `/q/health` status UP.
- Profile tests (`DevProfileConfigurationTest`, `ProductionProfileConfigurationTest` + nested classes) validate OpenAPI/Swagger enablement toggles between profiles.
No frontend tests defined beyond Angular scaffold (Karma default harness auto-generated).

## 8. Adding Backend Features (Guidance)
Where to put code:
- New REST resources: `backend/src/main/java/org/roubinet/librarie/` (create packages as domain grows).
- Entities: add JPA @Entity classes then configure datasource (add JDBC driver dependency and `quarkus.datasource.*` properties). Remove current Hibernate warning.
ALWAYS add corresponding test under parallel package in `backend/src/test/java/...` using `@QuarkusTest`.

## 9. Dependency & Plugin Notes
- Quarkus BOM manages versions; add extensions via `<dependency>` on `io.quarkus:quarkus-<name>`.
- Surefire/Failsafe already configured. Integration tests currently skipped (`skipITs=true`) unless native profile used.
- If adding Flyway migrations place under `backend/src/main/resources/db/migration`.

## 10. Quality / Validation Steps Before Commit
ALWAYS perform:
1. Backend compile: `cd backend && mvn -DskipTests clean package`
2. Backend tests (if modifying code or config): `cd backend && mvn test`
3. Frontend (if touched): `cd frontend && npm ci && npm run build`
4. Optional quick manual smoke: start dev (`cd backend && mvn quarkus:dev`) then `curl localhost:8080/hello` expecting plain text.
5. **Style Guide Validation**: Before EVERY commit, verify:
   - **Frontend**: Angular Style Guide compliance (file naming, component structure, service patterns)
   - **Backend**: Hexagonal architecture compliance (proper layer separation, dependency direction)
6. **Architecture Review**: Ensure new code follows established patterns:
   - Frontend components use standalone pattern and proper lifecycle management
   - Backend maintains clear domain/application/infrastructure boundaries
   - All new services have corresponding interfaces (ports) when crossing layer boundaries

Document any new environment variables in README + here.

**Each review submission MUST include verification that both Angular Style Guide and Hexagonal Architecture principles are followed.**

## 11. Conventions
- Package base: `org.roubinet.librarie`.
- Use RESTEasy Reactive annotations (Jakarta). Return simple DTOs or primitives.
- Keep responses small and deterministic for tests.

## 12. When To Search
Trust this document first. ONLY perform repo-wide searches if:
- A file/path mentioned here is missing or renamed.
- A build/test command fails unexpectedly after following preconditions (installs, Docker running).
- You need to locate newly added code not yet integrated into this guide.

If an MCP (Model Context Protocol) server named `context7` is available, ALWAYS query it first for additional context, metadata or documentation before performing costly filesystem searches. The configuration now lives in `.vscode/settings.json` under `mcp.servers.context7`.

## 13. Quick Reference
Backend dev: `cd backend && mvn quarkus:dev`
Run tests: `cd backend && mvn test`
Package: `cd backend && mvn -DskipTests clean package`
Frontend dev: `cd frontend && npm ci && npm start`
All dev (after initial npm ci): `./start-dev.sh`
Smoke test: `curl -s localhost:8080/hello | grep 'Hello from Quarkus REST'`

---
Adhere strictly to the "ALWAYS" cautions above to minimize failed PR feedback. Keep modifications minimal, coherent, and accompanied by tests.
