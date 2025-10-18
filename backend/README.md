# Librarie Backend

Quarkus-based backend for the Librarie book management application.

## Configuration

### Demo Data

The application includes a demo data feature that populates the database with sample books, authors, and series for development and testing purposes.

#### Production Safety

**Demo data is disabled by default** to prevent accidental activation in production environments. The application includes a runtime safeguard that will throw an exception if demo mode is enabled in the production profile.

#### Profile-based Configuration

Demo data can be controlled via the `librarie.demo.enabled` configuration property:

- **Production (`prod` profile)**: Demo data is disabled by default and **cannot be enabled**. Any attempt to enable demo data in production will result in a `IllegalStateException` being thrown at startup.
  
- **Development (`dev` profile)**: Demo data is enabled by default via `application-dev.properties`. The application will populate the database with sample data on first startup if the database is empty.

- **Test (`test` profile)**: Demo data is enabled for integration tests to verify functionality.

#### Configuration Files

- `application.properties` - Base configuration with demo **disabled** by default
- `application-dev.properties` - Development profile with demo **enabled**
- `application-prod.properties` - Production profile (demo cannot be enabled)

#### Demo Data Settings

When enabled, demo data can be configured with the following properties:

```properties
# Enable/disable demo data (disabled by default for safety)
librarie.demo.enabled=false

# Number of demo entities to create
librarie.demo.book-count=100
librarie.demo.author-count=50
librarie.demo.series-count=20
```

#### Running with Different Profiles

```bash
# Development mode (demo enabled)
mvn quarkus:dev

# Production mode (demo disabled and protected)
mvn quarkus:dev -Dquarkus.profile=prod

# Test mode (demo enabled for testing)
mvn test
```

#### Production Safeguard

The `DemoDataService` includes a runtime check that prevents demo data from being populated in production:

1. If demo is disabled, the service returns early without any action
2. If demo is enabled, the service checks the active profile
3. If the profile is `prod` (case-insensitive), an `IllegalStateException` is thrown with a clear security message
4. Only non-production profiles can populate demo data

This safeguard ensures that even if configuration is accidentally misconfigured, demo data will never run in production.

## Development

### Building the Application

```bash
mvn clean compile
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=DemoDataServiceTest
```

### Running the Application

```bash
# Development mode with live reload
mvn quarkus:dev

# With specific profile
mvn quarkus:dev -Dquarkus.profile=dev
```

## License

See the main project README for license information.
