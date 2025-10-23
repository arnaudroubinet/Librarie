# Backend Coding Style and Conventions

## Java Coding Standards
- **Style Guide**: Google Java Style Guide (enforced by Checkstyle)
- **Java Version**: Java 21 with modern language features
- **Encoding**: UTF-8 for all source files

## Naming Conventions
### Classes and Interfaces
- **Domain Models**: Simple names (e.g., `Book`, `Author`, `Series`)
- **Use Case Interfaces**: `<Entity>UseCase` (e.g., `BookUseCase`, `AuthorUseCase`)
- **Services**: `<Entity>Service` (e.g., `BookService`, `AuthorService`)
- **Repository Ports**: `<Entity>RepositoryPort` or `<Entity>Repository`
- **REST Controllers**: `<Entity>Controller` (e.g., `BookController`)
- **DTOs**: `<Entity>RequestDto`, `<Entity>ResponseDto`, `<Entity>DetailsDto`, `<Entity>ListItemDto`
- **Adapters**: `<Purpose>Adapter` (e.g., `BookRepositoryAdapter`, `JbossLoggingAdapter`)

### Methods
- **Use Case Methods**: Verb-based describing business action (e.g., `createBook`, `findAllAuthors`, `deleteById`)
- **Query Methods**: Start with `find`, `get`, or `search` (e.g., `findBookById`, `getAllAuthors`)
- **Boolean Methods**: Start with `is`, `has`, `can` (e.g., `isEnabled`, `hasBooks`)

### Variables
- **camelCase** for all variables and method parameters
- **Descriptive names** that clearly indicate purpose
- **Avoid abbreviations** unless universally understood

### Constants
- **UPPER_SNAKE_CASE** for all constants
- Define in appropriate classes or configuration

## Package Organization
### Hexagonal Architecture Layers
1. **Domain Layer** (`org.motpassants.domain`)
   - `core.model`: Domain entities (pure Java, no framework dependencies)
   - `port.in`: Inbound ports (use case interfaces)
   - `port.out`: Outbound ports (repository interfaces, external service interfaces)

2. **Application Layer** (`org.motpassants.application`)
   - `service`: Use case implementations (orchestrates domain and calls ports)

3. **Infrastructure Layer** (`org.motpassants.infrastructure`)
   - `adapter.in.rest`: REST controllers and DTOs
   - `adapter.out.persistence`: JPA/JDBC repository implementations
   - `adapter.out.config`: Configuration adapters
   - `adapter.out.logging`: Logging adapters
   - `adapter.out.security`: Security adapters
   - `config`: Quarkus configuration classes
   - `media`: Image processing services
   - `readium`: EPUB/Readium integration
   - `security`: Security services

## Dependency Rules (Enforced by ArchUnit)
1. **Domain core** → No dependencies outside Java standard library
2. **Domain ports** → Only domain core
3. **Application services** → Domain ports + domain core
4. **Infrastructure** → Can access all layers (implements ports)
5. **No cyclic dependencies** between packages
6. **REST controllers** only in `infrastructure.adapter.in.rest`
7. **JPA annotations** only in infrastructure layer

## Code Documentation
### Javadoc
- **All public classes and interfaces**: Must have class-level Javadoc
- **Public methods in use case interfaces**: Describe purpose and behavior
- **Complex algorithms**: Add inline comments explaining logic
- **Domain models**: Document business rules and invariants

### Comments
- **Use sparingly**: Code should be self-documenting through clear naming
- **Explain "why", not "what"**: Code shows what; comments explain reasoning
- **Update comments**: Keep comments in sync with code changes

## Error Handling
- Use **checked exceptions** for recoverable errors
- Use **runtime exceptions** for programming errors
- Define custom exceptions in domain layer when appropriate
- Log errors with appropriate context
- Return `Optional<T>` for methods that may not find results

## Testing Conventions
### Test Classes
- **Unit tests**: `<ClassName>Test` (e.g., `BookServiceTest`)
- **Architecture tests**: `<Topic>ArchitectureTest` (e.g., `HexagonalArchitectureTest`)
- **Integration tests**: Use `@QuarkusTest` annotation

### Test Methods
- **Naming**: `should<ExpectedBehavior>_when<Condition>` (e.g., `shouldReturnBook_whenIdExists`)
- Use **BDD style**: Given-When-Then structure in test body
- **Mock dependencies**: Use Mockito for unit tests
- **Test edge cases**: Null values, empty collections, boundary conditions

### Test Coverage
- Jacoco configured with minimum 0% coverage (aspirational target higher)
- Focus on testing **business logic** in domain and application layers
- Infrastructure can have lower coverage (tested via integration tests)

## Code Quality Tools
### Checkstyle
- **Configuration**: `google_checks.xml`
- **Enforcement**: Runs on `mvn validate`
- **Violations**: Warnings (not blocking)
- **Override sparingly**: Only when Google style conflicts with domain requirements

### Jacoco
- **Coverage reporting**: Generates reports in `target/site/jacoco/`
- **Minimum coverage**: Currently 0% (configure higher for production)

## Best Practices
### Immutability
- Use **records** for DTOs and value objects
- Make domain entities immutable where possible
- Use `final` for fields that shouldn't change

### Null Safety
- Prefer `Optional<T>` over null returns
- Use `Objects.requireNonNull()` for parameter validation
- Consider `@NonNull` annotations from validation frameworks

### Collection Handling
- Return **empty collections** instead of null
- Use **immutable collections** (`List.of()`, `Set.of()`) where appropriate
- Consider streaming APIs for collection operations

### Dependency Injection
- Use **constructor injection** (required by Quarkus)
- Mark services with `@ApplicationScoped`
- Use interfaces for dependencies (ports)

### Configuration
- Externalize configuration in `application.properties`
- Use type-safe config with `@ConfigMapping`
- Support multiple profiles: dev, test, prod

### Logging
- Use **structured logging** with appropriate levels
- Include **context** (traceId, userId, etc.) via MDC
- Log at appropriate levels:
  - **ERROR**: Application errors requiring attention
  - **WARN**: Recoverable issues
  - **INFO**: Important business events
  - **DEBUG**: Detailed diagnostic information
  - **TRACE**: Very detailed diagnostic information

### Security
- **Sanitize all user inputs** using `InputSanitizationService`
- **Validate file uploads** using `SecureFileProcessingService`
- **Use parameterized queries** to prevent SQL injection
- **Apply principle of least privilege** for database access

## Modern Java Features (Java 21)
- **Records**: For DTOs and value objects
- **Pattern matching**: For type checks
- **Text blocks**: For multi-line strings (SQL, JSON)
- **Sealed classes**: For restricted type hierarchies
- **Virtual threads**: For improved concurrency (Quarkus support)
