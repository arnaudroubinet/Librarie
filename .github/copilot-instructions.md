# Copilot Coding Agent Onboarding Guide

(<= 2 pages)

## Golden Rules (ALWAYS follow)
1. Build fast: run `mvn -DskipTests clean package` early/often; avoid unnecessary full test cycles while coding.
2. Test before commit: always finish with `mvn test` (and frontend `npm run build` if frontend changed) plus a healthcheck smoke curl.
3. Trust this doc first: only search the repo or explore broadly if information here proves incomplete/incorrect. Thrown an alert if something seem to not match.
4. Query `context7` first (MCP) when available for extra context/metadata before filesystem searches.
5. Minimize new dependencies: justify each new library (prefer Quarkus extensions already present); remove unused imports/config.


## 1. Repository Summary
Librarie is a dual-module workspace:
- Backend: Quarkus 3.x (Java 21, Maven) REST API. Currently a simple greeting + health endpoints with environment profile config (dev/prod). Includes OpenAPI, Health, OIDC, Flyway, Quartz, Micrometer, OpenTelemetry, Hibernate ORM (present but no entities yet), Keycloak Dev Services (pulled automatically in tests due to OIDC), and Testcontainers usage via Quarkus Dev Services.
- Frontend: Angular 20 (TypeScript) scaffold. Minimal routing; placeholder template.
Purpose: Scaffold for a future library management system.

## 2. Tech & Layout
Root files: `pom.xml`, `start-dev.sh`, `README.md`, `docs/`, `frontend/`, `src/` (backend), `target/` build output.
Key paths:
- Backend source: `src/main/java/...`
- Backend tests: `src/test/java/...`
- Backend config: `src/main/resources/application*.properties`
- Frontend app: `frontend/src/`
- Angular config: `frontend/angular.json`, `frontend/package.json`
- Script to launch both dev servers: `./start-dev.sh`
- Documentation: `docs/` currently includes `feature-map.md`, `migration-plan.md`, `open-questions.md` – read these to understand the vision, upcoming workstreams, and open questions before adding major features.
No GitHub Actions workflows yet (`.github/workflows` absent).

## 3. Runtimes & Required Versions (ALWAYS honor)
- Java: 21 (project now sets `maven.compiler.release=21`). Use Temurin/OpenJDK 21.
- Maven: 3.8+ recommended (Surefire 3.5.3 used).
- Node: 18+ (Angular CLI 20 works under Node 18 or 20). Prefer LTS (Node 20) if available.
- npm: 9+.
- Docker: Needed for Quarkus Dev Services (Keycloak test container) during `mvn test`. Ensure Docker daemon is available or disable OIDC / Dev Services if adding fast unit tests.

## 4. Build / Run / Test Cheat Sheet
ALWAYS run `mvn -q -DskipTests clean package` before committing backend changes to ensure compilation & packaging succeed.

Backend:
1. Clean & compile (skip tests): `mvn -DskipTests clean package`
2. Full tests: `mvn test` (pulls Keycloak image first time; adds ~60–120s). Expect warnings: Agroal (no datasource), Hibernate (no entities). Harmless unless adding persistence.
3. Dev mode: `mvn quarkus:dev` (hot reload, port 8080). Greeting endpoint: `GET http://localhost:8080/hello`.
4. Profiles: dev (default), prod (set with `-Dquarkus.profile=prod`). In prod profile OpenAPI + Swagger UI disabled.
5. Native build (if ever needed): `mvn -Dnative -DskipTests package` (GraalVM/mandrel required; not currently configured beyond profile skeleton).

Frontend:
1. Install deps (ALWAYS after cloning or when `package.json` changes): `cd frontend && npm ci`.
2. Dev server: `npm start` (Angular live reload on port 4200).
3. Build: `npm run build` output in `frontend/dist/`.
4. Unit tests (headless): ensure Chrome/Chromium available. Command: `npm test` (Karma). Provide `--watch=false --browsers=ChromeHeadless` for CI style.

Combined dev startup: `./start-dev.sh` (spawns Quarkus then Angular). Script assumes working directory root, existing `frontend` dependencies installed. ALWAYS run `npm ci` once before first script invocation.

## 5. Common Pitfalls & Mitigations
- Long test startup: First `mvn test` triggers Docker pulls for Keycloak + Ryuk (~230MB). Subsequent runs faster. Do NOT assume a hang; observe logs.
- Missing Docker: Tests may fail/time out when Dev Services cannot start Keycloak. If Docker unavailable, you can temporarily disable OIDC tests by removing OIDC extension or setting `-Dquarkus.oidc.enabled=false` (not yet in config) but prefer keeping default.
- Java version mismatch: If environment supplies Java <21 build may fail or use unsupported features; ensure Java 21 is active (`java -version`).
- Frontend tests may need system dependencies (Chrome). In minimal containers install `chromium-browser` or adjust Karma config.
- OpenTelemetry warnings (connection refused to localhost:4317) are benign unless you configure an OTLP collector. Ignore for now.

## 6. Configuration Overview
Files:
- `application.properties`: base config (port 8080, name, version, dev profile hint).
- `application-dev.properties`: enables OpenAPI & Swagger UI, debug logging, sets OIDC placeholders.
- `application-prod.properties`: disables OpenAPI/Swagger, info logging.
Tests rely on config profile overrides to assert OpenAPI flags in dev vs prod.

## 7. Test Suite Summary
Tests (`src/test/java/...`):
- `GreetingResourceTest`: Asserts `/hello` returns "Hello from Quarkus REST".
- `HealthEndpointTest`: Asserts `/q/health` status UP.
- Profile tests (`DevProfileConfigurationTest`, `ProductionProfileConfigurationTest` + nested classes) validate OpenAPI/Swagger enablement toggles between profiles.
No frontend tests defined beyond Angular scaffold (Karma default harness auto-generated).

## 8. Adding Backend Features (Guidance)
Where to put code:
- New REST resources: `src/main/java/org/roubinet/librarie/` (create packages as domain grows).
- Entities: add JPA @Entity classes then configure datasource (add JDBC driver dependency and `quarkus.datasource.*` properties). Remove current Hibernate warning.
ALWAYS add corresponding test under parallel package in `src/test/java/...` using `@QuarkusTest`.

## 9. Dependency & Plugin Notes
- Quarkus BOM manages versions; add extensions via `<dependency>` on `io.quarkus:quarkus-<name>`.
- Surefire/Failsafe already configured. Integration tests currently skipped (`skipITs=true`) unless native profile used.
- If adding Flyway migrations place under `src/main/resources/db/migration`.

## 10. Quality / Validation Steps Before Commit
ALWAYS perform:
1. Backend compile: `mvn -DskipTests clean package`
2. Backend tests (if modifying code or config): `mvn test`
3. Frontend (if touched): `cd frontend && npm ci && npm run build`
4. Optional quick manual smoke: start dev (`mvn quarkus:dev`) then `curl localhost:8080/hello` expecting plain text.
Document any new environment variables in README + here.

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
Backend dev: `mvn quarkus:dev`
Run tests: `mvn test`
Package: `mvn -DskipTests clean package`
Frontend dev: `cd frontend && npm ci && npm start`
All dev (after initial npm ci): `./start-dev.sh`
Smoke test: `curl -s localhost:8080/hello | grep 'Hello from Quarkus REST'`

---
Adhere strictly to the "ALWAYS" cautions above to minimize failed PR feedback. Keep modifications minimal, coherent, and accompanied by tests.
