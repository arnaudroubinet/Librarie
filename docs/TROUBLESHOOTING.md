# Troubleshooting Guide

This guide helps you diagnose and resolve common issues when developing, building, or running the Librarie application.

---

## 🔍 Quick Diagnostics

**Before troubleshooting**, verify your environment:

```bash
# Check versions
java --version        # Should be 21+
node --version        # Should be 18+
docker --version      # Required for Dev Services
mvn --version         # Should be 3.9+ (or use ./mvnw)
```

**Check if services are running:**

```bash
# Backend should be on port 8080
curl http://localhost:8080/q/health

# Frontend should be on port 4200
curl http://localhost:4200

# Check Docker containers (if using Dev Services)
docker ps
```

---

## 🔧 Backend Issues

### Backend Won't Start

#### ❌ Error: "JAVA_HOME not set" or "Java version too old"

**Symptom**: Maven or Quarkus refuses to start with Java version errors.

**Solution**:
```bash
# Check current Java version
java --version

# Set JAVA_HOME (Windows PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Set JAVA_HOME (Linux/macOS)
export JAVA_HOME=/path/to/jdk-21

# Verify
echo $env:JAVA_HOME   # PowerShell
echo $JAVA_HOME       # Bash/Zsh
```

**Prevention**: Use [SDKMAN](https://sdkman.io/) (Linux/macOS) or [jEnv](https://www.jenv.be/) to manage Java versions.

---

#### ❌ Error: "Port 8080 is already in use"

**Symptom**: Backend fails to start with `Address already in use` exception.

**Solution**:
```bash
# Find process using port 8080 (Windows)
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Find and kill process (Linux/macOS)
lsof -i :8080
kill -9 <PID>

# Alternative: Run backend on different port
./mvnw quarkus:dev -Dquarkus.http.port=8081
```

---

#### ❌ Error: "Docker is not running" or "TestContainers failed to start"

**Symptom**: Dev Services fail to provision PostgreSQL or Keycloak containers.

**Solution**:
```bash
# 1. Start Docker Desktop (Windows/macOS) or Docker daemon (Linux)
# 2. Verify Docker is running
docker ps

# 3. Pull required images manually (optional)
docker pull postgres:17
docker pull quay.io/keycloak/keycloak:26.3

# 4. Restart backend
./mvnw clean quarkus:dev
```

**Workaround (if Docker unavailable)**: Use external PostgreSQL and disable Dev Services:
```properties
# In application-dev.properties
quarkus.datasource.devservices.enabled=false
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/librarie
quarkus.datasource.username=postgres
quarkus.datasource.password=postgres
```

---

#### ❌ Error: "Table 'book' doesn't exist" or migration errors

**Symptom**: SQL errors at startup about missing tables.

**Solution**:
```bash
# 1. Check Flyway migration status
./mvnw flyway:info

# 2. Drop and recreate database (DEV ONLY - destroys data!)
docker exec -it <postgres-container-id> psql -U postgres -c "DROP DATABASE librarie;"
docker exec -it <postgres-container-id> psql -U postgres -c "CREATE DATABASE librarie;"

# 3. Restart backend (migrations will run)
./mvnw clean quarkus:dev

# 4. If migrations are broken, repair and migrate
./mvnw flyway:repair
./mvnw flyway:migrate
```

---

#### ❌ Error: "OutOfMemoryError: Java heap space"

**Symptom**: Backend crashes during startup or large file uploads.

**Solution**:
```bash
# Increase heap size
export MAVEN_OPTS="-Xmx2g"  # Linux/macOS
$env:MAVEN_OPTS="-Xmx2g"    # Windows PowerShell

# Run with increased memory
./mvnw quarkus:dev -Dquarkus.jvm.heap.initial-size=512m -Dquarkus.jvm.heap.max-size=2g
```

---

#### ❌ Error: "Failed to load demo data" or duplicate key violations

**Symptom**: CSV import fails with constraint violations.

**Solution**:
```bash
# 1. Check CSV files exist
ls backend/data/*.csv

# 2. Verify CSV format (UTF-8, no BOM, valid UUIDs)
file backend/data/books.csv

# 3. Check for duplicate IDs in CSV
# (IDs must be unique UUIDs)

# 4. Disable demo data if not needed
# In application-dev.properties:
librarie.demo.enabled=false
```

**Reference**: See [DEMO_DATA_IDEMPOTENCY.md](../../backend/DEMO_DATA_IDEMPOTENCY.md) for demo data behavior.

---

### Backend Build Issues

#### ❌ Error: "Tests fail with connection timeout"

**Symptom**: Integration tests fail to connect to TestContainers.

**Solution**:
```bash
# 1. Enable TestContainers reuse (speeds up tests)
# Create ~/.testcontainers.properties:
echo "testcontainers.reuse.enable=true" > ~/.testcontainers.properties

# 2. Increase timeout in tests (if needed)
# In test class:
@QuarkusTestResource(value = PostgresResource.class, restrictToAnnotatedClass = true)

# 3. Skip tests temporarily (not recommended)
./mvnw clean package -DskipTests
```

---

#### ❌ Error: "ArchUnit architecture test failures"

**Symptom**: Tests fail with messages like "Classes in 'domain' should not depend on 'infrastructure'".

**Solution**: Fix the code to respect hexagonal architecture boundaries.

**Example violation**:
```java
// ❌ BAD: Domain depends on infrastructure
package org.rlh.domain.book;
import org.rlh.infrastructure.persistence.BookRepository;  // VIOLATION

// ✅ GOOD: Domain depends on port interface
package org.rlh.domain.book;
import org.rlh.application.ports.out.BookRepositoryPort;  // OK
```

**Allowed dependencies**:
- `domain` → (nothing)
- `application` → `domain`
- `infrastructure` → `domain`, `application`

**Reference**: See [ARCHITECTURE.md](../../ARCHITECTURE.md#hexagonal-architecture) for layer rules.

---

## 🌐 Frontend Issues

### Frontend Won't Start

#### ❌ Error: "Cannot find module" or dependency errors

**Symptom**: npm install fails or Angular CLI reports missing modules.

**Solution**:
```bash
# 1. Clear npm cache and node_modules
rm -rf node_modules package-lock.json
npm cache clean --force

# 2. Reinstall dependencies
npm install

# 3. If still failing, check Node version
node --version  # Must be 18+

# 4. Use specific npm version
npm install -g npm@10
```

---

#### ❌ Error: "Port 4200 is already in use"

**Symptom**: `ng serve` fails with EADDRINUSE error.

**Solution**:
```bash
# Find and kill process (Windows)
netstat -ano | findstr :4200
taskkill /PID <PID> /F

# Find and kill process (Linux/macOS)
lsof -i :4200
kill -9 <PID>

# Alternative: Run on different port
ng serve --port 4201
```

---

#### ❌ Error: "Proxy error: Could not proxy request /api/books"

**Symptom**: Frontend can't reach backend API.

**Solution**:
```bash
# 1. Verify backend is running
curl http://localhost:8080/q/health

# 2. Check proxy configuration (frontend/proxy.conf.json)
cat frontend/proxy.conf.json
# Should have:
# {
#   "/api": {
#     "target": "http://localhost:8080",
#     "secure": false
#   }
# }

# 3. Restart frontend with proxy
npm start  # Uses proxy.conf.json by default

# 4. If still failing, bypass proxy temporarily
# Update environment.ts to point directly to localhost:8080
```

---

#### ❌ Error: TypeScript compilation errors

**Symptom**: `ng build` or `ng serve` fails with type errors.

**Solution**:
```bash
# 1. Check TypeScript version
npx tsc --version  # Should be ~5.8

# 2. Verify tsconfig.json is correct
cat tsconfig.json

# 3. Clean build cache
rm -rf .angular dist

# 4. Rebuild
npm run build

# 5. If error persists, enable detailed logging
ng build --verbose
```

---

### Frontend Runtime Issues

#### ❌ Error: "NullInjectorError: No provider for HttpClient"

**Symptom**: Console error when trying to make HTTP requests.

**Solution**: Ensure `provideHttpClient()` is in `app.config.ts`:

```typescript
// ✅ GOOD: app.config.ts
import { provideHttpClient } from '@angular/common/http';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(),  // Required!
    // ... other providers
  ]
};
```

---

#### ❌ Error: "CORS error when calling backend API"

**Symptom**: Browser console shows `Access-Control-Allow-Origin` errors.

**Solution**:
```bash
# 1. Check backend CORS configuration (application.properties)
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:4200

# 2. Restart backend
./mvnw quarkus:dev

# 3. Verify frontend URL matches allowed origins
```

---

#### ❌ Error: "401 Unauthorized" when accessing protected resources

**Symptom**: API calls fail with 401 even when logged in.

**Solution**:
```javascript
// 1. Check if token is being sent
// In browser DevTools Network tab, inspect request headers
// Should see: Authorization: Bearer <token>

// 2. Verify token is not expired
// Decode JWT at https://jwt.io

// 3. Check AuthService is attaching token
// In frontend/src/app/services/auth.service.ts
```

---

## 🗄️ Database Issues

### Connection Problems

#### ❌ Error: "Connection refused to localhost:5432"

**Symptom**: Can't connect to PostgreSQL.

**Solution**:
```bash
# Dev mode: Ensure Docker is running (for Dev Services)
docker ps | grep postgres

# Production: Check PostgreSQL service
# Windows:
sc query postgresql-x64-17

# Linux:
sudo systemctl status postgresql

# Test connection manually
psql -h localhost -U postgres -d librarie
```

---

### Data Issues

#### ❌ Problem: Demo data not loading

**Symptom**: Empty database after startup with `librarie.demo.enabled=true`.

**Solution**:
```bash
# 1. Check CSV files exist and are valid
ls -lh backend/data/*.csv

# 2. Check application logs for CSV parsing errors
# Look for: "Failed to load demo data" or SQL exceptions

# 3. Verify demo.enabled property
grep "demo.enabled" backend/src/main/resources/application.properties

# 4. Manually trigger demo data load (if idempotent)
# Restart backend with clean database
```

---

#### ❌ Problem: "Duplicate key value violates unique constraint"

**Symptom**: Errors when inserting data.

**Solution**:
```sql
-- 1. Check for duplicate IDs
SELECT id, COUNT(*)
FROM book
GROUP BY id
HAVING COUNT(*) > 1;

-- 2. If duplicates exist, clean up manually
DELETE FROM book WHERE id = '<duplicate-uuid>' AND <condition>;

-- 3. Check CSV files for duplicate IDs
grep -o '"[0-9a-f-]*"' backend/data/books.csv | sort | uniq -d
```

---

## 🔐 Authentication Issues

### Keycloak Problems

#### ❌ Error: "Unable to connect to Keycloak"

**Symptom**: OIDC authentication fails.

**Solution**:
```bash
# Dev mode: Check if Keycloak container is running
docker ps | grep keycloak

# Access Keycloak admin console
# http://localhost:8180/admin (DevServices default)
# Username: admin
# Password: admin

# Check realm and client configuration
# Should have: librarie realm with librarie-frontend client
```

---

#### ❌ Error: "Invalid redirect URI"

**Symptom**: OAuth flow fails with redirect error.

**Solution**:
```bash
# 1. Check frontend redirect URI in Keycloak client settings
# Should include: http://localhost:4200/*

# 2. Verify frontend OIDC configuration
# In environment.ts:
oidc: {
  issuer: 'http://localhost:8180/realms/librarie',
  redirectUri: window.location.origin,
  clientId: 'librarie-frontend'
}
```

---

## 🚀 Build & Deployment Issues

### Production Build Failures

#### ❌ Error: "Native build failed" (GraalVM)

**Symptom**: `mvnw package -Pnative` fails.

**Solution**:
```bash
# 1. Ensure GraalVM is installed
java --version  # Should say "GraalVM"

# 2. Install native-image tool
gu install native-image

# 3. Increase memory for native build
export MAVEN_OPTS="-Xmx8g"

# 4. Build with verbose output
./mvnw package -Pnative -Dquarkus.native.additional-build-args="--verbose"
```

---

#### ❌ Error: Frontend production build too large

**Symptom**: `dist/` bundle is huge (>5MB).

**Solution**:
```bash
# 1. Build with production optimizations
ng build --configuration production

# 2. Analyze bundle size
npx webpack-bundle-analyzer dist/stats.json

# 3. Enable source map for debugging (if needed)
ng build --configuration production --source-map

# 4. Check for accidental dev dependencies in production
npm prune --production
```

---

## 🧪 Testing Issues

### Test Failures

#### ❌ Error: "Karma cannot start Chrome"

**Symptom**: Frontend tests fail with browser launch errors.

**Solution**:
```bash
# 1. Use headless Chrome
ng test --browsers=ChromeHeadless --watch=false

# 2. Install Chrome/Chromium if missing
# Windows: Download from google.com/chrome
# Linux: sudo apt install chromium-browser

# 3. Set Chrome path (if non-standard location)
export CHROME_BIN=/usr/bin/chromium-browser
```

---

#### ❌ Error: "Backend tests fail with 'Database not ready'"

**Symptom**: Integration tests timeout waiting for database.

**Solution**:
```bash
# 1. Enable TestContainers reuse
echo "testcontainers.reuse.enable=true" > ~/.testcontainers.properties

# 2. Increase startup timeout in tests
@QuarkusTestResource(initArgs = @ResourceArg(name = "POSTGRES_INIT_TIMEOUT", value = "60"))

# 3. Check Docker has enough resources
# Docker Desktop → Settings → Resources → Memory: 4GB+
```

---

## 📊 Performance Issues

### Slow Startup

#### Problem: Backend takes >30 seconds to start

**Solution**:
```bash
# 1. Enable continuous testing (keeps Quarkus running)
./mvnw quarkus:dev

# 2. Use TestContainers reuse
echo "testcontainers.reuse.enable=true" > ~/.testcontainers.properties

# 3. Profile startup time
./mvnw quarkus:dev -Dquarkus.log.category."io.quarkus".level=DEBUG

# 4. Check if antivirus is scanning target/ directory
# Exclude project directory from real-time scanning
```

---

### Slow API Responses

#### Problem: API calls take >2 seconds

**Solution**:
```bash
# 1. Check database query performance
# Enable SQL logging:
quarkus.hibernate-orm.log.sql=true

# 2. Look for N+1 query problems
# (Fetching related entities in loops)

# 3. Add database indexes
# See backend/src/main/resources/db/migration/

# 4. Enable HTTP caching
# See backend/docs/HTTP_CACHING.md
```

---

## 🆘 Still Stuck?

### Get More Information

```bash
# Backend logs (verbose)
./mvnw quarkus:dev -Dquarkus.log.level=DEBUG

# Frontend logs (verbose)
ng serve --verbose

# Check application health
curl http://localhost:8080/q/health

# Check metrics
curl http://localhost:8080/q/metrics
```

### Report an Issue

If none of the above helps:

1. Check existing issues: [GitHub Issues](../../issues)
2. Collect diagnostics:
   ```bash
   # Save environment info
   java --version > diagnostics.txt
   node --version >> diagnostics.txt
   docker --version >> diagnostics.txt
   mvn --version >> diagnostics.txt
   
   # Save logs
   ./mvnw quarkus:dev > backend.log 2>&1
   ng serve > frontend.log 2>&1
   ```
3. Open a new issue with:
   - Problem description
   - Steps to reproduce
   - Environment info (from diagnostics.txt)
   - Relevant logs

---

## 📚 Related Documentation

- [PROJECT_OVERVIEW.md](./PROJECT_OVERVIEW.md) - Full project documentation
- [ARCHITECTURE.md](../ARCHITECTURE.md) - Architecture details
- [CONTRIBUTING.md](../CONTRIBUTING.md) - Development workflow
- [GLOSSARY.md](./GLOSSARY.md) - Terminology reference

---

> **Tip**: Most issues are resolved by ensuring Docker is running, versions are correct, and dependencies are freshly installed. Start there first! 🎯
