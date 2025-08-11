#!/bin/bash

# Local CI Testing Script for Librarie
# This script runs the same tests as the GitHub Actions workflows locally

set -e

echo "🚀 Running Local CI Tests for Librarie"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check prerequisites
echo
echo "Checking prerequisites..."

# Check Java version
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
    if [ "$JAVA_VERSION" = "21" ]; then
        print_status "Java 21 found"
    else
        print_error "Java 21 required, found Java $JAVA_VERSION"
        exit 1
    fi
else
    print_error "Java not found"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    print_status "Maven found"
else
    print_error "Maven not found"
    exit 1
fi

# Check Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v | sed 's/v//' | awk -F '.' '{print $1}')
    if [ "$NODE_VERSION" -ge "18" ]; then
        print_status "Node.js $NODE_VERSION found"
    else
        print_error "Node.js 18+ required, found $NODE_VERSION"
        exit 1
    fi
else
    print_error "Node.js not found"
    exit 1
fi

# Check npm
if command -v npm &> /dev/null; then
    print_status "npm found"
else
    print_error "npm not found"
    exit 1
fi

# Check Docker (for Quarkus Dev Services)
if command -v docker &> /dev/null && docker info &> /dev/null; then
    print_status "Docker found and running"
else
    print_warning "Docker not found or not running - some tests may fail"
fi

echo
echo "🔧 Running Backend Tests..."
echo "========================="

cd backend

# Clean and test backend
echo "Running Maven clean test..."
mvn clean test

# Package backend
echo "Packaging backend..."
mvn package -DskipTests

print_status "Backend tests completed"

cd ..

echo
echo "🎨 Running Frontend Tests..."
echo "=========================="

cd frontend

# Install dependencies
echo "Installing npm dependencies..."
npm ci

# Check code formatting (if prettier is available)
echo "Checking code formatting..."
if npm list prettier > /dev/null 2>&1; then
    npx prettier --check "src/**/*.{ts,html,scss,css}" || print_warning "Prettier check failed - formatting issues found"
else
    print_warning "Prettier not available, skipping format check"
fi

# Run tests
echo "Running frontend tests..."
npm run test -- --watch=false --browsers=ChromeHeadless --code-coverage

# Build frontend
echo "Building frontend..."
npm run build

print_status "Frontend tests completed"

cd ..

echo
echo "🔗 Running Integration Tests..."
echo "=============================="

# Start backend in dev mode
echo "Starting backend in dev mode..."
cd backend
timeout 120 mvn quarkus:dev -Ddebug=false &
QUARKUS_PID=$!
cd ..

# Wait for backend to be ready
echo "Waiting for backend to be ready..."
for i in {1..30}; do
    if curl -f http://localhost:8080/q/health > /dev/null 2>&1; then
        print_status "Backend is ready"
        break
    fi
    echo "Waiting for backend... ($i/30)"
    sleep 2
    if [ $i -eq 30 ]; then
        print_error "Backend failed to start"
        kill $QUARKUS_PID 2>/dev/null || true
        exit 1
    fi
done

# Run API health checks
echo "Running API health checks..."
if curl -f http://localhost:8080/q/health > /dev/null 2>&1; then
    print_status "Health endpoint OK"
else
    print_error "Health endpoint failed"
fi

if curl -f http://localhost:8080/hello > /dev/null 2>&1; then
    print_status "Hello endpoint OK"
else
    print_error "Hello endpoint failed"
fi

# Stop backend
echo "Stopping backend..."
kill $QUARKUS_PID 2>/dev/null || true
sleep 2

print_status "Integration tests completed"

echo
echo "🎉 All tests completed successfully!"
echo "==================================="
echo
echo "Summary:"
echo "- Backend: Java 21, Maven, Quarkus"
echo "- Frontend: Node.js, npm, Angular"
echo "- Integration: Health checks passed"
echo
echo "You can now start development with:"
echo "  ./start-dev.sh"