# Backend: What to Do When a Task is Completed

## Before Committing
1. **Run all tests**: `mvn test`
2. **Run code quality checks**: `mvn verify` (includes Checkstyle, Jacoco)
3. **Build the application**: `mvn clean install`
4. **Verify architecture rules**: Tests should include ArchUnit validations
5. **Check for compilation warnings**: Address any warnings

## Testing Requirements
### Unit Tests
- Write unit tests for all new business logic (domain and application layers)
- Achieve reasonable test coverage (aim for >80% on business logic)
- Use Mockito to mock dependencies
- Follow Given-When-Then structure

### Architecture Tests
- ArchUnit tests automatically validate hexagonal architecture rules
- Ensure no violations of dependency rules
- Tests are in: `src/test/java/org/motpassants/architecture/`

### Integration Tests
- Write integration tests for REST endpoints if adding new controllers
- Use `@QuarkusTest` annotation
- Use RestAssured for API testing

## Code Quality Checks
### Checkstyle
- Automatically runs during `mvn validate` phase
- Configuration: `google_checks.xml`
- Fix any violations before committing
- Command: `mvn checkstyle:check`

### Jacoco Coverage
- Coverage report generated in: `target/site/jacoco/index.html`
- Review coverage for new code
- Command: `mvn jacoco:report`

## Build Verification
### Local Build
```powershell
cd backend
mvn clean install
```

### Run in Dev Mode
```powershell
cd backend
mvnw quarkus:dev
```
- Verify application starts without errors
- Test API endpoints manually or with Swagger UI: `http://localhost:8080/q/swagger-ui`

## Documentation Updates
### When to Update Documentation
- **New API endpoints**: Update OpenAPI annotations
- **New domain concepts**: Document in class Javadoc
- **Configuration changes**: Update README.md or ARCHITECTURE.md
- **Architectural decisions**: Add ADR in ARCHITECTURE.md

### Javadoc
- Ensure all public classes and interfaces have Javadoc
- Document method parameters and return values
- Explain business rules and constraints

## Database Migrations
### If You Modified the Database Schema
1. **Create Flyway migration**: Add new SQL file in `src/main/resources/db/migration/`
2. **Naming**: `V<version>__<description>.sql` (e.g., `V1.2__add_tags_table.sql`)
3. **Test migration**: Run application and verify migration succeeds
4. **Verify rollback strategy**: Document how to revert if needed

### Migration Best Practices
- **Idempotent**: Migrations should be safe to run multiple times
- **Backward compatible**: Consider impact on running instances
- **Test with data**: Verify migration works with existing data
- **Separate schema and data**: Keep DDL and DML migrations separate when possible

## Security Verification
### If You Added File Upload or User Input
- **Input sanitization**: Use `InputSanitizationService`
- **File validation**: Use `SecureFileProcessingPort`
- **SQL injection**: Use parameterized queries only
- **Path traversal**: Validate and sanitize file paths

### Security Checklist
- [ ] All user inputs are sanitized
- [ ] File uploads are validated (type, size, content)
- [ ] No sensitive data in logs
- [ ] Authentication/authorization applied where needed
- [ ] SQL queries are parameterized
- [ ] File paths are validated

## Performance Considerations
### If You Added Resource-Intensive Operations
- **Database queries**: Check query performance and indexing
- **File operations**: Consider async processing for large files
- **Memory usage**: Profile for memory leaks with large datasets
- **Caching**: Consider caching for frequently accessed data

## Commit Checklist
Before committing code:
- [ ] All tests pass (`mvn test`)
- [ ] Code quality checks pass (`mvn verify`)
- [ ] Application builds successfully (`mvn clean install`)
- [ ] Application runs in dev mode without errors
- [ ] New code has appropriate test coverage
- [ ] Javadoc is complete for public APIs
- [ ] No unnecessary commented-out code
- [ ] No debug statements or console logs
- [ ] Git commit message follows conventions (see CONTRIBUTING.md)
- [ ] Database migrations are tested (if applicable)
- [ ] Security considerations are addressed (if applicable)

## Commands Summary
```powershell
# Windows PowerShell commands for backend

# Run tests
cd backend
./mvnw test

# Run all quality checks (tests + checkstyle + jacoco)
./mvnw verify

# Build application
./mvnw clean install

# Run in development mode (hot reload)
./mvnw quarkus:dev

# Run specific test class
./mvnw test -Dtest=BookServiceTest

# Generate coverage report
./mvnw jacoco:report
# View report: target/site/jacoco/index.html

# Check code style
./mvnw checkstyle:check

# Package application for production
./mvnw package
```

## CI/CD Considerations
The project uses GitHub Actions for CI/CD. Before pushing:
1. **Local verification**: Run all checks locally to catch issues early
2. **Branch protection**: Main branch requires passing CI checks
3. **Review artifacts**: CI uploads test results and coverage reports
4. **Security scanning**: Trivy scans for vulnerabilities

## Troubleshooting
### Common Issues After Changes
1. **Tests fail**: Check for breaking changes in interfaces or contracts
2. **Architecture tests fail**: Verify package structure follows hexagonal architecture
3. **Build fails**: Check for compilation errors, missing dependencies
4. **Checkstyle violations**: Review Google style guide, fix formatting
5. **Migration fails**: Check SQL syntax, verify on clean database

### Getting Help
- Check ARCHITECTURE.md for architecture patterns
- Review existing code for examples
- Check Quarkus documentation: https://quarkus.io/guides/
- Run `./mvnw quarkus:dev` and check logs for detailed errors
