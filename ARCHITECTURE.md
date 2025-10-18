# Librarie Architecture Documentation

## Technology Stack

### Backend
- **Framework**: Quarkus 3.25.2
- **Language**: Java 21
- **Database**: PostgreSQL with Flyway migrations
- **Security**: OIDC authentication
- **Observability**: Micrometer metrics, OpenTelemetry tracing, SmallRye Health
- **API**: REST with Jackson serialization, SmallRye OpenAPI (Swagger UI)

## Architectural Decisions

### Dependency Management

#### Quartz Scheduler Removal (2025-10-18)

**Decision**: Removed `quarkus-quartz` dependency from the project.

**Rationale**:
- Comprehensive codebase search revealed no usage of scheduled tasks:
  - No `@Scheduled` annotations found
  - No Quartz Job implementations found
  - No QuartzScheduler references found
- Application logs confirmed: "No scheduled business methods found - Quartz scheduler will not be started"
- Removing unused dependencies:
  - Reduces application size and startup time
  - Minimizes security attack surface
  - Simplifies maintenance and dependency management
  - Follows the principle of keeping dependencies minimal

**Verification**:
- Build: ✅ Successful (mvn clean compile)
- Tests: ✅ All 150 tests passing
- Application: ✅ Starts and functions normally
- Installed features: Confirmed `quartz` and `scheduler` no longer present

**Impact**:
- No functional impact as the dependency was not being used
- Reduced dependency footprint
- Faster build and startup times

**Future Considerations**:
If scheduled tasks are needed in the future, the `quarkus-quartz` dependency can be re-added to `backend/pom.xml`:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-quartz</artifactId>
</dependency>
```

Then scheduled tasks can be implemented using the `@Scheduled` annotation as documented in the [Quarkus Scheduler Guide](https://quarkus.io/guides/scheduler).

## Project Structure

The backend follows a hexagonal architecture pattern (ports and adapters), with clear separation of concerns between domain logic, application services, and infrastructure adapters.
