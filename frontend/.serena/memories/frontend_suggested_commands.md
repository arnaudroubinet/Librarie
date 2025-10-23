# Frontend Suggested Commands

## Development Commands

### Start Development Server
```powershell
cd c:\dev\gitRepository\Librarie\frontend
npm start
```
- Starts Angular dev server with proxy to backend
- Application available at: `http://localhost:4200`
- Auto-reloads on file changes
- Proxies `/api` requests to `http://localhost:8080`

### Generate Code
```powershell
# Generate component
cd c:\dev\gitRepository\Librarie\frontend
ng generate component components/my-component --standalone

# Generate service
ng generate service services/my-service

# Generate directive
ng generate directive directives/my-directive --standalone

# Generate interface
ng generate interface models/my-model

# See all generate options
ng generate --help
```

## Testing Commands

### Unit Tests
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Run tests in watch mode (development)
npm test

# Run tests once (CI mode)
npm test -- --watch=false --browsers=ChromeHeadless

# Run tests with coverage
npm test -- --watch=false --browsers=ChromeHeadless --code-coverage

# Run specific test file
npm test -- --include='**/book-list.component.spec.ts' --watch=false

# Run tests with specific browser
npm test -- --browsers=Chrome
```

### E2E Tests
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Run Playwright tests
npx playwright test

# Run in UI mode (interactive)
npx playwright test --ui

# Run in headed mode (see browser)
npx playwright test --headed

# Run specific test file
npx playwright test tests/book-list.spec.ts

# Generate tests
npx playwright codegen http://localhost:4200
```

## Build Commands

### Development Build
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Build with development configuration
npm run build -- --configuration=development

# Watch mode (rebuilds on file changes)
npm run watch
```

### Production Build
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Build for production
npm run build

# Output directory: dist/frontend/browser/

# Build with stats (for analysis)
npm run build:stats
```

## Code Quality Commands

### Formatting
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Format all files
npx prettier --write "src/**/*.{ts,html,scss,css}"

# Check formatting (without changing files)
npx prettier --check "src/**/*.{ts,html,scss,css}"

# Format specific file
npx prettier --write src/app/components/book-list.component.ts

# Format specific directory
npx prettier --write "src/app/components/**/*.ts"
```

### Type Checking
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Check for TypeScript errors
npx tsc --noEmit

# Watch mode (continuous checking)
npx tsc --noEmit --watch
```

### Bundle Size
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Check bundle size (requires build first)
npm run build
npm run size

# Analyze bundle composition
npm run analyze

# Then upload dist/frontend/stats.json to:
# https://esbuild.github.io/analyze/
```

## Dependency Management

### Install Dependencies
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Install all dependencies (clean install)
npm ci

# Install specific package
npm install package-name

# Install dev dependency
npm install --save-dev package-name

# Install specific version
npm install package-name@1.2.3
```

### Update Dependencies
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Check for outdated packages
npm outdated

# Update all packages (respecting semver)
npm update

# Update Angular CLI
ng update @angular/cli

# Update Angular core and CLI
ng update @angular/core @angular/cli

# Update Angular Material
ng update @angular/material
```

### Audit Dependencies
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Audit for vulnerabilities
npm audit

# Fix vulnerabilities automatically
npm audit fix

# Force fix (may introduce breaking changes)
npm audit fix --force
```

## Angular CLI Commands

### Information
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Show Angular CLI version
ng version

# Show Angular CLI help
ng help

# Show help for specific command
ng generate --help
```

### Serve Options
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Serve with custom port
ng serve --port 4201

# Serve with open browser
ng serve --open

# Serve with specific host
ng serve --host 0.0.0.0

# Serve without proxy
ng serve --no-proxy
```

## VS Code Tasks
The following tasks are configured in `.vscode/tasks.json`:

### Angular dev serve with proxy
```powershell
# Press Ctrl+Shift+B or run task: "Angular dev serve with proxy"
# Starts dev server with proxy configuration
```

### Frontend: npm test headless
```powershell
# Run task: "Frontend: npm test headless"
# Runs tests in headless Chrome
```

### Frontend: npm run build
```powershell
# Run task: "Frontend: npm run build"
# Builds production version
```

## Git Commands (PowerShell)

### Branch Management
```powershell
# Check status
git status

# Create feature branch
git checkout -b feature/book-search

# Switch to branch
git checkout main

# List branches
git branch -a

# Delete local branch
git branch -d feature/old-feature

# Delete remote branch
git push origin --delete feature/old-feature
```

### Commit and Push
```powershell
# Stage all changes
git add .

# Stage specific files
git add src/app/components/book-list.component.ts

# Commit with message
git commit -m "feat: add advanced book search"

# Amend last commit
git commit --amend

# Push to remote
git push origin feature/book-search

# Force push (use with caution)
git push --force origin feature/book-search
```

### Pull and Sync
```powershell
# Pull latest changes
git pull origin main

# Rebase on main
git checkout feature/book-search
git rebase main

# Fetch all branches
git fetch --all

# View commit history
git log --oneline --graph --all
```

## Windows-Specific Commands

### File Operations
```powershell
# List files
Get-ChildItem -Path src -Recurse

# Find TypeScript files
Get-ChildItem -Path src -Filter "*.ts" -Recurse

# Search in files
Select-String -Path "src/**/*.ts" -Pattern "BookService"

# View file content
Get-Content -Path package.json

# Create directory
New-Item -ItemType Directory -Path "src/app/components"

# Copy files
Copy-Item -Path "src/app/old.component.ts" -Destination "src/app/new.component.ts"

# Delete files
Remove-Item -Path "src/app/old.component.ts"
```

### Process Management
```powershell
# Find Node process by port
Get-NetTCPConnection -LocalPort 4200 | Select-Object -Property OwningProcess
Get-Process -Id <ProcessId>

# Kill Node process
Stop-Process -Id <ProcessId> -Force

# Kill all Node processes (careful!)
Get-Process -Name "node" | Stop-Process -Force

# List running Node processes
Get-Process -Name "node"
```

### Environment Variables
```powershell
# View Node version
node --version

# View npm version
npm --version

# View environment variable
$env:NODE_ENV

# Set environment variable (current session)
$env:NODE_ENV = "production"

# View all environment variables
Get-ChildItem Env:
```

## Debugging

### Chrome DevTools
1. Start dev server: `npm start`
2. Open Chrome DevTools (F12)
3. Go to Sources tab
4. Set breakpoints in TypeScript files
5. Refresh page to hit breakpoints

### VS Code Debugging
1. Install "Debugger for Chrome" extension
2. Set breakpoints in VS Code
3. Press F5 to start debugging
4. VS Code launches Chrome and attaches debugger

### Angular DevTools
1. Install Angular DevTools extension in Chrome
2. Open Chrome DevTools
3. Find "Angular" tab
4. Inspect component tree and change detection

## Performance Analysis

### Lighthouse
```powershell
# Run Lighthouse in Chrome DevTools
# 1. Open DevTools (F12)
# 2. Go to Lighthouse tab
# 3. Generate report
# 4. Review performance, accessibility, SEO scores
```

### Bundle Analysis
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Generate stats
npm run build:stats

# Analyze at: https://esbuild.github.io/analyze/
# Upload: dist/frontend/stats.json
```

### Memory Profiling
```powershell
# Use Chrome DevTools Memory Profiler
# 1. Open DevTools (F12)
# 2. Go to Memory tab
# 3. Take heap snapshots
# 4. Compare snapshots to find leaks
```

## Useful URLs (when running)
- **Application**: http://localhost:4200
- **Backend API**: http://localhost:8080/api (proxied)
- **Backend Swagger**: http://localhost:8080/q/swagger-ui

## Quick Reference

### Most Common Commands
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Development
npm start                                    # Start dev server
npm test                                     # Run tests (watch mode)
npm test -- --watch=false --browsers=ChromeHeadless  # Run tests once

# Code Quality
npx prettier --write "src/**/*.{ts,html,scss,css}"  # Format code
npx tsc --noEmit                            # Check types

# Building
npm run build                               # Production build
npm run size                                # Check bundle size
npm run analyze                             # Analyze bundle

# Code Generation
ng generate component components/my-component --standalone
ng generate service services/my-service
```

### Environment Setup
```powershell
# Verify Node.js version
node --version                              # Should be 18+

# Verify npm version
npm --version                               # Should be 10+

# Install dependencies
cd c:\dev\gitRepository\Librarie\frontend
npm ci
```

## Troubleshooting Commands

### Clear Cache
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Remove node_modules and reinstall
Remove-Item -Path "node_modules" -Recurse -Force
npm ci

# Clear npm cache
npm cache clean --force

# Clear Angular cache
Remove-Item -Path ".angular" -Recurse -Force
```

### Reset Development Environment
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Full reset
Remove-Item -Path "node_modules" -Recurse -Force
Remove-Item -Path ".angular" -Recurse -Force
Remove-Item -Path "dist" -Recurse -Force
npm ci
npm start
```

### Fix Port Already in Use
```powershell
# Find process using port 4200
Get-NetTCPConnection -LocalPort 4200 | Select-Object -Property OwningProcess

# Kill the process
Stop-Process -Id <ProcessId> -Force

# Or use different port
ng serve --port 4201
```

## Testing Scenarios

### Before Pull Request
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Run complete validation
npx prettier --check "src/**/*.{ts,html,scss,css}"
npx tsc --noEmit
npm test -- --watch=false --browsers=ChromeHeadless --code-coverage
npm run build
npm run size
```

### Quick Validation
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Fast checks
npx tsc --noEmit
npm test -- --watch=false --browsers=ChromeHeadless
```
