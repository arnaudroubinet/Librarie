# Backend Suggested Commands

## Development Commands

### Start Development Server
```powershell
cd c:\dev\gitRepository\Librarie\backend
./mvnw quarkus:dev
```
- Starts Quarkus in dev mode with hot reload
- API available at: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/q/swagger-ui`
- Dev UI: `http://localhost:8080/q/dev`

### Run Tests
```powershell
# All tests
cd c:\dev\gitRepository\Librarie\backend
./mvnw test

# Specific test class
./mvnw test -Dtest=BookServiceTest

# Specific test method
./mvnw test -Dtest=BookServiceTest#shouldReturnBook_whenIdExists

# Architecture tests only
./mvnw test -Dtest=HexagonalArchitectureTest
```

### Build and Package
```powershell
# Clean build
cd c:\dev\gitRepository\Librarie\backend
./mvnw clean install

# Quick build (skip tests)
./mvnw clean install -DskipTests

# Package for production
./mvnw package

# Run packaged application
java -jar target/quarkus-app/quarkus-run.jar
```

### Code Quality
```powershell
# Run all quality checks (tests + checkstyle + jacoco)
cd c:\dev\gitRepository\Librarie\backend
./mvnw verify

# Run Checkstyle only
./mvnw checkstyle:check

# Generate Jacoco coverage report
./mvnw jacoco:report
# View report in browser: target/site/jacoco/index.html
```

### Database Operations
```powershell
# Run Flyway migrations manually
cd c:\dev\gitRepository\Librarie\backend
./mvnw flyway:migrate

# Get migration info
./mvnw flyway:info

# Clean database (CAREFUL: deletes all data)
./mvnw flyway:clean
```

### Dependency Management
```powershell
# List dependencies
cd c:\dev\gitRepository\Librarie\backend
./mvnw dependency:tree

# Check for updates
./mvnw versions:display-dependency-updates

# Check for plugin updates
./mvnw versions:display-plugin-updates
```

## VS Code Tasks
The following tasks are configured in `.vscode/tasks.json`:

### Backend: mvn clean install (mandatory)
```powershell
# Press Ctrl+Shift+B or run task: "Backend: mvn clean install (mandatory)"
```

### Backend: mvn test
```powershell
# Run task: "Backend: mvn test"
```

## Useful Endpoints (when running)
- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Health Check**: http://localhost:8080/q/health
- **Metrics**: http://localhost:8080/q/metrics
- **OpenAPI Spec**: http://localhost:8080/q/openapi
- **Dev UI**: http://localhost:8080/q/dev (dev mode only)

## Git Commands (PowerShell)
```powershell
# Check status
git status

# Create feature branch
git checkout -b feature/your-feature-name

# Stage changes
git add .

# Commit with conventional commit message
git commit -m "feat: add book search functionality"

# Push to remote
git push origin feature/your-feature-name

# Pull latest changes
git pull origin main

# View commit history
git log --oneline --graph --all
```

## Windows-Specific Commands

### File Operations
```powershell
# List files
Get-ChildItem -Path . -Recurse

# Find files by pattern
Get-ChildItem -Path . -Filter "*.java" -Recurse

# Search in files
Select-String -Path "src/**/*.java" -Pattern "BookService"

# View file content
Get-Content -Path pom.xml

# Create directory
New-Item -ItemType Directory -Path "src/main/resources/db/migration"
```

### Process Management
```powershell
# Find process by port
Get-NetTCPConnection -LocalPort 8080 | Select-Object -Property OwningProcess
Get-Process -Id <ProcessId>

# Kill process
Stop-Process -Id <ProcessId> -Force

# Kill process by name
Stop-Process -Name "java" -Force
```

### Environment Variables
```powershell
# View environment variable
$env:JAVA_HOME

# Set environment variable (current session)
$env:MAVEN_OPTS = "-Xmx1024m"

# View all environment variables
Get-ChildItem Env:
```

## Debugging

### Enable Debug Logging
```powershell
# In application.properties or command line
./mvnw quarkus:dev -Dquarkus.log.level=DEBUG

# For specific package
./mvnw quarkus:dev -Dquarkus.log.category."org.motpassants".level=DEBUG
```

### Remote Debugging
```powershell
# Quarkus dev mode already enables debugging on port 5005
# Connect debugger to: localhost:5005
```

### View Logs
```powershell
# Real-time log viewing (if logging to file)
Get-Content -Path logs/application.log -Wait -Tail 50
```

## Performance Analysis

### Generate Thread Dump
```powershell
# Get Java process ID
jps -l

# Generate thread dump
jstack <pid> > threaddump.txt
```

### Generate Heap Dump
```powershell
# Get Java process ID
jps -l

# Generate heap dump
jmap -dump:format=b,file=heapdump.hprof <pid>
```

## Docker (if using Docker for PostgreSQL)
```powershell
# Start PostgreSQL container
docker run -d --name librarie-postgres `
  -e POSTGRES_DB=librarie `
  -e POSTGRES_USER=librarie `
  -e POSTGRES_PASSWORD=librarie `
  -p 5432:5432 `
  postgres:16

# Stop PostgreSQL
docker stop librarie-postgres

# Remove PostgreSQL container
docker rm librarie-postgres

# View logs
docker logs librarie-postgres -f

# Connect to PostgreSQL
docker exec -it librarie-postgres psql -U librarie -d librarie
```

## Quick Reference

### Most Common Commands
```powershell
cd c:\dev\gitRepository\Librarie\backend

# Development
./mvnw quarkus:dev              # Start dev server

# Testing
./mvnw test                     # Run tests
./mvnw verify                   # Run all quality checks

# Building
./mvnw clean install            # Full build with tests
./mvnw package                  # Package application

# Quality
./mvnw checkstyle:check         # Check code style
./mvnw jacoco:report            # Generate coverage report
```

### Environment Setup
```powershell
# Verify Java version
java -version                   # Should be 21+

# Verify Maven version
./mvnw -version                 # Should be 3.9+

# Check PostgreSQL connection
# Update application.properties with your database credentials
```

## Troubleshooting Commands

### Clear Maven Cache
```powershell
# Remove local repository (CAREFUL: re-downloads all dependencies)
Remove-Item -Path "$env:USERPROFILE\.m2\repository" -Recurse -Force
```

### Clean Quarkus Build
```powershell
cd c:\dev\gitRepository\Librarie\backend
./mvnw clean
Remove-Item -Path "target" -Recurse -Force
```

### Reset Database (Dev Mode)
```powershell
# Stop application
# Drop and recreate database in PostgreSQL
# Restart application - Flyway will recreate schema
```
