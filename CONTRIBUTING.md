# Contributing to Librarie

Thank you for your interest in contributing to Librarie! This document provides guidelines and information about our development process.

## Table of Contents
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [CI/CD Pipeline](#cicd-pipeline)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Pull Request Process](#pull-request-process)

## Getting Started

### Prerequisites
- **Backend**: Java 21, Maven 3.9+
- **Frontend**: Node.js 20+, npm 10+
- Git

### Local Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/arnaudroubinet/Librarie.git
   cd Librarie
   ```

2. **Backend setup**
   ```bash
   cd backend
   mvn clean install
   ```

3. **Frontend setup**
   ```bash
   cd frontend
   npm ci
   ```

## Development Workflow

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Follow the coding standards (see below)
   - Write tests for new functionality
   - Update documentation as needed

3. **Test locally**
   ```bash
   # Backend
   cd backend
   mvn clean verify
   
   # Frontend
   cd frontend
   npm run test
   npm run build
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: descriptive commit message"
   ```

5. **Push and create a Pull Request**
   ```bash
   git push origin feature/your-feature-name
   ```

## CI/CD Pipeline

Our project uses GitHub Actions for continuous integration and deployment. All pull requests must pass CI checks before merging.

### Workflows

#### Main CI Pipeline (`ci.yml`)
Runs on every pull request and push to main/develop branches.

**Features:**
- **Change Detection**: Only runs jobs for changed components
- **Backend CI**: Builds and tests Java backend with Maven
- **Frontend CI**: Builds and tests Angular frontend with npm
- **Security Scanning**: Scans dependencies for vulnerabilities using Trivy
- **Status Check**: Final gate that requires all jobs to pass

**Triggered by:**
- Pull requests targeting `main` or `develop`
- Direct pushes to `main` or `develop`

#### CodeQL Analysis (`codeql.yml`)
Performs static code analysis for security vulnerabilities.

**Features:**
- Analyzes Java and JavaScript/TypeScript code
- Detects security vulnerabilities and code quality issues
- Runs on PRs, pushes, and weekly schedule

**Languages analyzed:**
- Java (backend)
- JavaScript/TypeScript (frontend)

#### Backend CI (`backend-ci.yml`)
Dedicated workflow for backend changes.

**Runs:**
- Maven tests with coverage
- Package application
- Upload test results and coverage reports

#### Frontend CI (`frontend-ci.yml`)
Dedicated workflow for frontend changes.

**Runs:**
- Code formatting checks (Prettier)
- Unit tests with Karma/Jasmine
- Build production application
- Upload test results and build artifacts

### Required Status Checks

The following checks must pass before a PR can be merged:

1. **Backend CI**: All backend tests and build must succeed
2. **Frontend CI**: All frontend tests and build must succeed  
3. **Security Scan**: No critical or high severity vulnerabilities
4. **CodeQL Analysis**: No security issues detected
5. **CI Status Check**: Final verification that all jobs passed

### Artifacts

CI workflows upload the following artifacts (retained for 7 days):

- **Backend**:
  - Test results (`surefire-reports/`)
  - Code coverage reports (`jacoco/`)
  
- **Frontend**:
  - Test results with coverage
  - Production build artifacts

- **Security**:
  - Trivy vulnerability scan results (SARIF format)

### Local CI Testing

You can run the same checks locally before pushing:

```bash
# Backend
cd backend
mvn clean verify

# Frontend  
cd frontend
npm ci
npx prettier --check "src/**/*.{ts,html,scss,css}"
npm run test -- --watch=false --browsers=ChromeHeadless --code-coverage
npm run build
```

## Coding Standards

### Backend (Java/Quarkus)
- Follow Java coding conventions
- Use meaningful variable and method names
- Write Javadoc for public APIs
- Keep methods focused and concise
- Write unit tests for all business logic

### Frontend (Angular/TypeScript)
- Follow Angular style guide
- Use TypeScript strict mode
- Format code with Prettier
- Use semantic HTML and proper accessibility attributes
- Write unit tests for components and services

### Git Commit Messages
Follow conventional commits format:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `test:` Adding or updating tests
- `refactor:` Code refactoring
- `chore:` Maintenance tasks

Example: `feat: add book search functionality`

## Testing

### Backend Testing
```bash
cd backend

# Run unit tests
mvn test

# Run tests with coverage
mvn verify

# Run specific test
mvn test -Dtest=YourTestClass
```

### Frontend Testing
```bash
cd frontend

# Run tests once
npm run test -- --watch=false --browsers=ChromeHeadless

# Run tests in watch mode
npm run test

# Run tests with coverage
npm run test -- --watch=false --browsers=ChromeHeadless --code-coverage
```

## Pull Request Process

1. **Create PR**
   - Use a descriptive title
   - Fill out the PR template
   - Link related issues

2. **CI Checks**
   - All CI workflows must pass
   - Address any failing tests or security issues
   - Ensure code coverage doesn't decrease significantly

3. **Code Review**
   - Request reviews from maintainers
   - Address review comments
   - Update PR as needed

4. **Merge Requirements**
   - ✅ All CI status checks passing
   - ✅ At least one approved review
   - ✅ No merge conflicts
   - ✅ Branch is up to date with target branch

5. **Merge Strategy**
   - Squash and merge for feature branches
   - Keep commit history clean

## Security

### Reporting Vulnerabilities
If you discover a security vulnerability, please email the maintainers directly instead of opening a public issue.

### Dependency Updates
- Dependencies are scanned automatically by Trivy
- Security updates should be prioritized
- Test thoroughly after updating dependencies

## Questions?

If you have questions about contributing, please:
- Check existing documentation
- Search closed issues
- Open a new issue with your question

Thank you for contributing to Librarie! 🎉
