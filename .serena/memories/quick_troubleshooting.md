# Quick Troubleshooting Reference

## Purpose
Fast access to the most common errors and their solutions. For detailed troubleshooting, see [TROUBLESHOOTING.md](../../docs/TROUBLESHOOTING.md).

## 🔥 Top 10 Most Common Issues

### 1. Backend Won't Start - "Port 8080 in use"
```powershell
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Alternative: Use different port
./mvnw quarkus:dev -Dquarkus.http.port=8081
```

### 2. Backend Won't Start - "Docker is not running"
```powershell
# 1. Start Docker Desktop
# 2. Verify
docker ps
# 3. Restart backend
./mvnw clean quarkus:dev
```

### 3. Backend Won't Start - "JAVA_HOME not set"
```powershell
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
# Verify
java --version  # Must be 21+
```

### 4. Frontend - "Cannot find module" after npm install
```powershell
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

### 5. Frontend - "Port 4200 is already in use"
```powershell
# Windows
netstat -ano | findstr :4200
taskkill /PID <PID> /F
```

### 6. Frontend - "Proxy error: Could not proxy request"
```powershell
# 1. Verify backend is running
curl http://localhost:8080/q/health

# 2. Check proxy.conf.json has:
# { "/api": { "target": "http://localhost:8080" } }

# 3. Restart frontend
npm start
```

### 7. Database - "Table 'book' doesn't exist"
```powershell
# Dev mode: Drop and recreate (destroys data!)
docker exec -it <postgres-container> psql -U postgres -c "DROP DATABASE librarie;"
docker exec -it <postgres-container> psql -U postgres -c "CREATE DATABASE librarie;"
./mvnw clean quarkus:dev
```

### 8. Tests - "TestContainers timeout"
```bash
# Enable reuse (speeds up tests)
echo "testcontainers.reuse.enable=true" > ~/.testcontainers.properties
```

### 9. Tests - "ArchUnit architecture test failures"
**Symptom**: "Classes in 'domain' should not depend on 'infrastructure'"

**Fix**: Respect layer boundaries:
- `domain` → (nothing)
- `application` → `domain`
- `infrastructure` → `domain`, `application`

### 10. Frontend - "NullInjectorError: No provider for HttpClient"
**Fix**: Ensure in `app.config.ts`:
```typescript
import { provideHttpClient } from '@angular/common/http';
export const appConfig: ApplicationConfig = {
  providers: [provideHttpClient(), ...]
};
```

## 🚨 Critical Checks Before Asking for Help

```powershell
# 1. Verify versions
java --version        # Must be 21+
node --version        # Must be 18+
docker --version      # Required for Dev Services
mvn --version         # Must be 3.9+

# 2. Check services
curl http://localhost:8080/q/health  # Backend
curl http://localhost:4200           # Frontend
docker ps                            # Containers

# 3. Check logs
./mvnw quarkus:dev   # Backend verbose
ng serve --verbose   # Frontend verbose
```

## 📊 Quick Diagnostic Commands

### Backend Health
```powershell
# Health check
curl http://localhost:8080/q/health

# Metrics
curl http://localhost:8080/q/metrics

# List all endpoints
curl http://localhost:8080/q/swagger-ui
```

### Database Connection
```powershell
# Find PostgreSQL container
docker ps | grep postgres

# Connect to database
docker exec -it <container-id> psql -U postgres -d librarie
```

### Frontend Build Issues
```bash
# Clear cache
rm -rf .angular dist

# Rebuild
npm run build

# Check bundle size
npm run size
```

## 🔍 Error Message Patterns

| Error Message Contains | Likely Cause | Quick Fix |
|------------------------|--------------|-----------|
| "Port ... in use" | Service already running | Kill process or use different port |
| "Docker not running" | Docker Desktop stopped | Start Docker |
| "Cannot find module" | npm dependencies corrupted | `rm -rf node_modules && npm install` |
| "JAVA_HOME" | Java not configured | Set `JAVA_HOME` env var |
| "TestContainers" | Docker issue or timeout | Check Docker, enable reuse |
| "Table ... doesn't exist" | Flyway migration failed | Drop DB and restart |
| "ArchUnit" | Architecture violation | Fix code to respect layers |
| "401 Unauthorized" | Token expired/missing | Re-login or check AuthService |
| "CORS error" | Backend CORS config | Check `quarkus.http.cors` properties |
| "NullInjectorError" | Missing provider | Add to `app.config.ts` providers |

## 📚 When to Consult Full Docs

- **Complex issues**: See [TROUBLESHOOTING.md](../../docs/TROUBLESHOOTING.md)
- **Architecture questions**: See [ARCHITECTURE.md](../../ARCHITECTURE.md)
- **Development workflow**: See [CONTRIBUTING.md](../../CONTRIBUTING.md)
- **Unknown terms**: See [GLOSSARY.md](../../docs/GLOSSARY.md)

## 💡 Performance Issues Quick Fixes

### Slow Backend Startup
```bash
# Enable TestContainers reuse
echo "testcontainers.reuse.enable=true" > ~/.testcontainers.properties

# Use continuous testing
./mvnw quarkus:dev  # Keeps Quarkus running
```

### Slow API Responses
```properties
# Enable SQL logging to find N+1 queries
quarkus.hibernate-orm.log.sql=true
```

### Large Frontend Bundle
```bash
# Analyze bundle
npm run analyze

# Check for dev dependencies in production
npm prune --production
```

## ⚙️ Configuration Quick Reference

### Backend Dev Mode
```properties
# application-dev.properties
quarkus.datasource.devservices.enabled=true
librarie.demo.enabled=true
quarkus.http.port=8080
```

### Backend Prod Mode
```properties
# application-prod.properties
quarkus.datasource.devservices.enabled=false
quarkus.datasource.jdbc.url=jdbc:postgresql://...
librarie.demo.enabled=false
```

### Frontend Proxy
```json
// proxy.conf.json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

---

> **Remember**: Most issues (80%) are resolved by ensuring Docker is running, versions are correct, and dependencies are freshly installed. Start there! 🎯
