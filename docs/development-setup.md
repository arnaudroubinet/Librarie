# Development Setup Guide

This guide will help you set up your development environment for the Librarie project with optimal performance.

## Prerequisites

- Docker Desktop installed and running
- Java 21 or later
- Maven (included via Maven wrapper)
- Node.js 18+ and npm (for frontend development)

## Quick Start

1. Clone the repository
2. Enable TestContainers reuse (see below)
3. Run the development script

## Enable TestContainers Reuse

**Important**: This configuration reduces backend startup time from ~28-31 seconds to **less than 5 seconds** for subsequent starts.

### What is TestContainers Reuse?

When running the backend in development mode, Quarkus automatically starts required services (PostgreSQL database and Keycloak) using Docker containers via TestContainers. By default, these containers are destroyed and recreated on each application restart, causing significant startup delays.

Enabling container reuse keeps these containers running between application restarts, dramatically improving development productivity.

### Configuration Steps

#### Step 1: Create the TestContainers Configuration File

The TestContainers configuration file must be created in your home directory:

**Linux/Mac:**
```bash
~/.testcontainers.properties
```

**Windows:**
```
C:\Users\<YourUsername>\.testcontainers.properties
```

#### Step 2: Add the Configuration

Create the file with the following content:

```properties
# Enable container reuse for faster development
testcontainers.reuse.enable=true

# Optional: Customize other TestContainers settings
# testcontainers.docker.socket.override=/var/run/docker.sock
```

You can also copy the template file from this repository:

**Linux/Mac:**
```bash
cp docs/testcontainers.properties.template ~/.testcontainers.properties
```

**Windows (PowerShell):**
```powershell
Copy-Item docs\testcontainers.properties.template $env:USERPROFILE\.testcontainers.properties
```

**Windows (Command Prompt):**
```cmd
copy docs\testcontainers.properties.template %USERPROFILE%\.testcontainers.properties
```

#### Step 3: Restart Docker Desktop (Optional but Recommended)

After creating the configuration file, restart Docker Desktop to ensure the settings are picked up.

### Verifying the Setup

After enabling container reuse, start the backend:

```bash
cd backend
./mvnw quarkus:dev
```

**First Start:** ~28-31 seconds (containers are created)
**Subsequent Starts:** < 5 seconds (containers are reused)

You should see log messages indicating that containers are being reused:
```
INFO  [org.testcontainers...] Reusing container with ID: ...
```

### Manual Container Cleanup

Reused containers persist across restarts. To clean them up manually:

**View all containers (including stopped):**
```bash
docker ps -a
```

**Remove specific containers:**
```bash
docker rm -f <container-id>
```

**Remove all stopped containers:**
```bash
docker container prune
```

**Remove all TestContainers (includes ryuk, postgres, keycloak):**
```bash
docker ps -a | grep -E "testcontainers|postgres|keycloak" | awk '{print $1}' | xargs docker rm -f
```

## Running the Application

### Backend Only

```bash
cd backend
./mvnw quarkus:dev
```

The backend will be available at:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/q/swagger-ui
- Health Check: http://localhost:8080/q/health

### Frontend Only

```bash
cd frontend
npm install
npm start
```

The frontend will be available at http://localhost:4200

### Full Stack (Backend + Frontend)

Use the development script in the root directory:

**Windows (PowerShell):**
```powershell
.\dev.ps1
```

This script will:
1. Check if Docker is running and start it if needed
2. Start the backend in development mode
3. Start the frontend development server
4. Stream logs from both services with color-coded prefixes

To use separate Windows Terminal tabs instead:
```powershell
.\dev.ps1 -UseWindowsTerminal
```

**Linux/Mac:**

Create a similar script or run both services in separate terminals.

## Development Tips

### Hot Reload

- **Backend**: Quarkus provides automatic live reload when code changes are saved
- **Frontend**: Angular CLI provides hot module replacement (HMR)

### Database Access

When running in dev mode, you can access the PostgreSQL database directly:

**Connection Details (from logs):**
- Host: localhost
- Port: (check logs for dynamically assigned port)
- Database: quarkus
- Username: quarkus
- Password: quarkus

Or use the Quarkus Dev UI at http://localhost:8080/q/dev

### Debugging

#### Backend Debugging

Start Quarkus in debug mode:
```bash
./mvnw quarkus:dev -Ddebug=5005
```

Then attach your IDE debugger to port 5005.

#### Frontend Debugging

Use browser developer tools or your IDE's built-in debugger for Angular applications.

## Troubleshooting

### Containers Not Reusing

1. Verify the `.testcontainers.properties` file exists in your home directory
2. Ensure `testcontainers.reuse.enable=true` is set
3. Restart Docker Desktop
4. Clean up old containers: `docker container prune`

### Port Conflicts

If you see port conflict errors:
- Check for other services using ports 8080, 4200, 5432, or 8180
- Stop conflicting services or change ports in configuration files

### Docker Not Running

The `dev.ps1` script will attempt to start Docker Desktop on Windows. On Linux/Mac, ensure Docker daemon is running:

```bash
# Check Docker status
docker info

# Start Docker (Linux)
sudo systemctl start docker
```

### Slow Initial Startup

The first startup will always be slower as containers need to be pulled and started. Subsequent startups with container reuse should be < 5 seconds.

If initial startup is taking longer than expected:
- Check your internet connection (for pulling images)
- Ensure Docker has sufficient resources allocated (4GB+ RAM recommended)
- Check Docker Desktop settings for resource limits

## Additional Resources

- [Quarkus Dev Services](https://quarkus.io/guides/dev-services)
- [TestContainers Documentation](https://testcontainers.com/)
- [Quarkus Development Mode](https://quarkus.io/guides/maven-tooling#dev-mode)

## Platform-Specific Notes

### Windows

- Use PowerShell for the best experience with the `dev.ps1` script
- Windows Terminal provides a better development experience with split panes
- Ensure Windows Subsystem for Linux (WSL 2) is configured for Docker Desktop

### macOS

- Docker Desktop for Mac is required
- Ensure Docker Desktop has sufficient resources allocated in Preferences
- Use Homebrew for installing prerequisites: `brew install openjdk@21 node`

### Linux

- Docker Engine is sufficient (Docker Desktop is optional)
- Ensure your user is in the `docker` group: `sudo usermod -aG docker $USER`
- Log out and back in for group changes to take effect
