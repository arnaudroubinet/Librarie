## 6. Very Detailed Step-by-Step Refactor Plan (Agent Handoff)

### 6.1. Move Business Logic to Services
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

### 6.2. Use Records and Sealed Classes
1. Review all domain models and DTOs:
	- Identify which are immutable and can be converted to `record` types
	- Identify hierarchies (e.g., error/result types) that can use `sealed` classes/interfaces
2. For each candidate:
	- Refactor the class to a `record` or `sealed` class/interface
	- Update constructors, builders, and serialization logic as needed
	- Update usages throughout the codebase
3. Ensure all tests pass and serialization (JSON, DB) works as expected

### 6.3. Pattern Matching
1. Search for all `instanceof` and `switch` statements in the codebase
2. For each occurrence:
	- Refactor to use Java 21 pattern matching (e.g., `instanceof` with variable binding, switch expressions)
	- Ensure logic remains correct and readable
3. Add/Update tests to cover pattern-matched branches

### 6.4. Enforce Package Boundaries
1. Review the current package structure for domain, application, adapter, and infrastructure layers
2. For each misplaced class:
	- Move it to the correct package
	- Update imports and usages
3. Add ArchUnit (or similar) tests to enforce boundaries
4. Make classes package-private where possible

### 6.5. Add Integration Tests with Testcontainers
1. Add Testcontainers dependencies to `pom.xml` (PostgreSQL, Keycloak, etc.)
2. For each adapter (REST, persistence):
	- Write integration tests that spin up containers for required services
	- Ensure tests are isolated, reproducible, and run in CI
3. Document how to run integration tests locally and in CI

### 6.6. Increase Unit Test Coverage
1. Run code coverage tool (e.g., JaCoCo) to identify untested code
2. For each uncovered domain/application class:
	- Write unit tests for all business rules, edge cases, and error conditions
	- Ensure tests are meaningful and not just coverage for coverage's sake
3. Track coverage improvements and set a target threshold

### 6.7. Clean Up Dependencies
1. Run `mvn dependency:analyze` to find unused dependencies
2. For each unused or redundant dependency:
	- Remove it from `pom.xml`
	- Prefer Quarkus extensions over third-party libraries
3. Test the build to ensure nothing is broken

### 6.8. Document Environment Variables
1. Review `application.properties` and code for all environment variable usages
2. For each variable:
	- Document its name, purpose, default value, and required/optional status in the README and `/docs`
3. Update documentation whenever variables are added or changed
## 5. Detailed Refactor Plans

### 5.1. Move Business Logic to Services
- Identify all business logic currently in REST controllers (e.g., validation, entity manipulation, complex queries).
- Create application/service classes in the application layer to encapsulate business logic.
- Refactor controllers to delegate to these services, keeping them thin and focused on HTTP concerns.
- Ensure services are injected using CDI (`@Inject`).
- Write unit tests for new service classes.

### 5.2. Use Records and Sealed Classes
- Review all domain models and DTOs for immutability opportunities.
- Refactor simple, immutable data carriers to Java 21 `record` types.
- For domain hierarchies (e.g., error types, result types), use `sealed` classes and interfaces.
- Update mapping and serialization logic to support records and sealed classes.

### 5.3. Pattern Matching
- Refactor `instanceof` checks and switch statements to use Java 21 pattern matching where possible.
- Review error handling, DTO mapping, and domain logic for pattern matching opportunities.
- Ensure code remains readable and maintainable after refactor.

### 5.4. Enforce Package Boundaries
- Audit the codebase for violations of hexagonal architecture boundaries (domain, application, adapter, infrastructure).
- Move misplaced classes to their correct layers.
- Use package-private visibility where possible to restrict access.
- Add ArchUnit or similar architecture tests to enforce boundaries.

### 5.5. Add Integration Tests with Testcontainers
- Add Testcontainers dependency to the Maven build for integration testing (PostgreSQL, Keycloak, etc.).
- Write integration tests for adapters (REST, persistence) using Testcontainers.
- Ensure tests are isolated, reproducible, and run in CI.

### 5.6. Increase Unit Test Coverage
- Identify untested domain and application logic.
- Write unit tests for all business rules, edge cases, and error conditions.
- Use code coverage tools (e.g., JaCoCo) to track progress.

### 5.7. Clean Up Dependencies
- Review `pom.xml` for unused or redundant dependencies.
- Remove any libraries not used in the codebase.
- Prefer Quarkus extensions over third-party libraries where possible.
- Run `mvn dependency:analyze` to identify unused dependencies.

### 5.8. Document Environment Variables
- List all environment variables used in `application.properties` and code.
- Document each variable's purpose, default value, and required/optional status in the README and `/docs`.
- Update documentation whenever new variables are added or changed.

# Java/Quarkus 3 Backend Refactor Plan (Project-Specific)

## 1. Concrete Observations from Code Review
- The backend uses Quarkus 3.25.2 and Java 21, with a structure inspired by hexagonal/clean architecture.
- REST controllers (`BookController`, `AuthorController`, `SeriesController`, etc.) are large and contain business logic, manual DTO mapping, and ad-hoc error handling.
- Domain models are mutable POJOs, not Java 21 records or sealed classes.
- No use of MapStruct or automated mapping; all mapping is manual.
- No centralized exception handling (no global exception mapper/handler found); exceptions are caught and handled ad-hoc in controllers.
- No RBAC or endpoint-level security annotations (e.g., `@RolesAllowed`) in controllers; OIDC is configured but not enforced at endpoint level.
- DTOs are present and used at API boundaries, but mapping is verbose and repetitive.
- No use of modern Java features (records, sealed classes, pattern matching) in domain or DTO layers.
- No testcontainers or advanced integration testing setup found.
- Application and infrastructure layers are separated, but business logic leaks into controllers.

## 2. Refactor Recommendations (Prioritized)

### 2.1. Security & Exception Handling
- [ ] **Implement RBAC**: Add `@RolesAllowed` or equivalent security annotations to all REST endpoints. Ensure endpoints are protected according to business requirements.
- [ ] **Centralize Exception Handling**: Create a global exception mapper/handler (e.g., using `@Provider` and `ExceptionMapper` in Quarkus) to handle business and system exceptions consistently. Remove ad-hoc try/catch blocks from controllers.
- [ ] **Define BizException/SysException**: Introduce custom exception types for business and system errors, and handle them in the global exception handler.

### 2.2. Controller & Service Layer
- [ ] **Move Business Logic to Services**: Refactor controllers to delegate all business logic to application/domain services. Controllers should only handle HTTP request/response and validation.
- [ ] **Reduce Controller Size**: Split large controllers into smaller, focused classes if needed.

### 2.3. DTO Mapping
- [ ] **Automate DTO Mapping**: Introduce MapStruct or a similar library to automate mapping between domain models and DTOs. Remove manual mapping code from controllers.

### 2.4. Java 21 Modernization
- [ ] **Use Records and Sealed Classes**: Refactor immutable domain models and DTOs to use Java 21 records. Use sealed classes for domain hierarchies where appropriate.
- [ ] **Pattern Matching**: Apply pattern matching in switch statements and instanceof checks where possible.

### 2.5. Hexagonal Architecture Enforcement
- [ ] **Enforce Package Boundaries**: Ensure strict separation between domain, application, and adapter layers. Move any business logic out of adapters/controllers.
- [ ] **Rename Controllers to Adapters**: For clarity, rename `*Controller` classes to `*Adapter` if following hexagonal conventions.

### 2.6. Testing Improvements
- [ ] **Add Integration Tests with Testcontainers**: Use Quarkus testcontainers for integration testing of adapters and persistence.
- [ ] **Increase Unit Test Coverage**: Ensure all domain logic is covered by unit tests.

### 2.7. Build & CI
- [ ] **Clean Up Dependencies**: Remove unused dependencies from `pom.xml`.
- [ ] **Document Environment Variables**: Ensure all required environment variables are documented in the README.

## 3. Summary of Key Issues
- No RBAC or endpoint-level security.
- No centralized/global exception handling.
- Manual, repetitive DTO mapping.
- No use of modern Java 21 features in domain or DTOs.
- Business logic present in controllers.

## 4. Next Steps
1. Prioritize security and exception handling improvements.
2. Refactor controllers to move business logic to services and automate DTO mapping.
3. Modernize codebase with Java 21 features.
4. Enforce hexagonal architecture boundaries.
5. Improve testing and CI setup.
- Review each point above in the codebase.
- Prioritize by impact and effort.
- Propose concrete refactor tasks for each area.

---
*This plan is a starting point. Each item should be validated against the actual codebase before execution.*
