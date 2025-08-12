# Copilot Coding Agent Onboarding Guide

## Golden Rules (ALWAYS follow)
1. Always validate the code before commit
2. Always checking security rules before commit
3. Always apply instructions before commit
4. Query `context7` for extra context/metadata before filesystem searches.
5. Minimize new dependencies: justify each new library (prefer Quarkus extensions already present); remove unused imports/config.

## Repository Summary
Librarie is a dual-module workspace:
- Backend is a Quarkus 3 server
- Frontend is an Angular 20 (TypeScript) SPA.

## Pull requests

Before commit always do a `mvn clean install` into the /backend directory, build must be OK and don't have any error.
Before asking a review always do a `mvn clean install` into the /backend directory, build must be OK and don't have any error. Add any warning in a commentary.

Before asking a review, you must show the proof the backend run with a http://localhost:8080/q/dev-ui/extensions screen capture and a http://localhost:4200/books screenshot.
If you work on a frontend feature, take a screenshot of this page.

Commit message must match
{Feature type} - {content of the commit} - java {mvn build time} - angular {npm build time}
{feature type} is "feature" for new feature, "improvement" for an improvement on a existing feature or "bugfix" for a bug fix.

## Tech & Layout
Root files: `docs/`, `frontend/`, `backend/`.
Key paths:
- Backend source: `backend/src/main/java/...`
- Backend tests: `backend/src/test/java/...`
- Backend config: `backend/src/main/resources/application*.properties`
- Backend Maven: `backend/pom.xml`
- Frontend app: `frontend/src/`
- Angular config: `frontend/angular.json`, `frontend/package.json`
- Documentation: `docs/` read these to understand the vision, upcoming workstreams, and open questions before adding major features.
- CI/CD workflows: `.github/workflows` contains backend-ci.yml, frontend-ci.yml, full-ci.yml.

## Common Pitfalls & Mitigations
- Long test startup: First `mvn test` triggers Docker pulls for Keycloak + Ryuk (~230MB). Subsequent runs faster. Do NOT assume a hang; observe logs.
- Missing Docker: Tests may fail/time out when Dev Services cannot start Keycloak. If Docker unavailable, you can temporarily disable OIDC tests by removing OIDC extension or setting `-Dquarkus.oidc.enabled=false` (not yet in config) but prefer keeping default.
- Java version mismatch: If environment supplies Java <21, build may fail or use unsupported features; ensure Java 21 is active (`java -version`).
- Frontend tests may need system dependencies (Chrome). In minimal containers install `chromium-browser` or adjust Karma config.
- OpenTelemetry warnings (connection refused to localhost:4317) are benign unless you configure an OTLP collector. Ignore for now.

## Before Commit
Document any new environment variables in README + here.

## When To Search
Trust this document first and .github/instructions/** after. ONLY perform repo-wide searches if:
- A file/path mentioned here is missing or renamed.
- A build/test command fails unexpectedly after following preconditions (installs, Docker running).
- You need to locate newly added code not yet integrated into this guide.

If an MCP (Model Context Protocol) server named `context7` is available, ALWAYS query it first for additional context, metadata or documentation before performing costly filesystem searches.
