# Plan for Agent: Java/Quarkus 3 Backend Refactor

This file provides a very detailed, step-by-step plan for each refactor area, suitable for handoff to another agent. Each step includes concrete actions, review points, and testing requirements to ensure a smooth and thorough refactor process.

## 1. Move Business Logic to Services
1. For each REST controller (BookController, AuthorController, SeriesController, etc.), identify all code that:
   - Performs validation (e.g., required fields, format checks)
   - Contains business rules (e.g., entity state changes, calculations)
   - Handles demo data or other non-HTTP concerns
2. For each identified logic block:
   - Move the logic to the appropriate ApplicationService (e.g., BookService, AuthorService)
   - If no suitable service exists, create a new one in `application/service/`
   - Ensure all business logic is unit tested in the service layer
3. Refactor controller methods to:
   - Only extract HTTP parameters and delegate to the service
   - Return the result from the service as a DTO/response
   - Remove all try/catch except for mapping service exceptions to HTTP responses
4. Update or add unit tests for the refactored services and controllers

## 2. Use Records and Sealed Classes
1. Review all domain models and DTOs:
   - Identify which are immutable and can be converted to `record` types
   - Identify hierarchies (e.g., error/result types) that can use `sealed` classes/interfaces
2. For each candidate:
   - Refactor the class to a `record` or `sealed` class/interface
   - Update constructors, builders, and serialization logic as needed
   - Update usages throughout the codebase
3. Ensure all tests pass and serialization (JSON, DB) works as expected

## 3. Pattern Matching
1. Search for all `instanceof` and `switch` statements in the codebase
2. For each occurrence:
   - Refactor to use Java 21 pattern matching (e.g., `instanceof` with variable binding, switch expressions)
   - Ensure logic remains correct and readable
3. Add/Update tests to cover pattern-matched branches

## 4. Enforce Package Boundaries
1. Review the current package structure for domain, application, adapter, and infrastructure layers
2. For each misplaced class:
   - Move it to the correct package
   - Update imports and usages
3. Add ArchUnit (or similar) tests to enforce boundaries
4. Make classes package-private where possible

## 5. Add Integration Tests with Testcontainers
1. Add Testcontainers dependencies to `pom.xml` (PostgreSQL, Keycloak, etc.)
2. For each adapter (REST, persistence):
   - Write integration tests that spin up containers for required services
   - Ensure tests are isolated, reproducible, and run in CI
3. Document how to run integration tests locally and in CI

## 6. Increase Unit Test Coverage
1. Run code coverage tool (e.g., JaCoCo) to identify untested code
2. For each uncovered domain/application class:
   - Write unit tests for all business rules, edge cases, and error conditions
   - Ensure tests are meaningful and not just coverage for coverage's sake
3. Track coverage improvements and set a target threshold

## 7. Clean Up Dependencies
1. Run `mvn dependency:analyze` to find unused dependencies
2. For each unused or redundant dependency:
   - Remove it from `pom.xml`
   - Prefer Quarkus extensions over third-party libraries
3. Test the build to ensure nothing is broken

## 8. Document Environment Variables
1. Review `application.properties` and code for all environment variable usages
2. For each variable:
   - Document its name, purpose, default value, and required/optional status in the README and `/docs`
3. Update documentation whenever variables are added or changed
