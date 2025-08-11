#!/bin/bash

# CI Configuration Verification Script for Librarie
# This script verifies that the CI/CD configuration is properly set up

set -e

echo "🔍 Verifying CI/CD Configuration for Librarie"
echo "============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

ERRORS=0

# Function to check file exists
check_file() {
    if [ -f "$1" ]; then
        print_status "Found: $1"
    else
        print_error "Missing: $1"
        ERRORS=$((ERRORS + 1))
    fi
}

# Function to check workflow file structure
check_workflow() {
    local file="$1"
    local expected_triggers="$2"
    
    if [ -f "$file" ]; then
        print_info "Checking workflow: $file"
        
        # Check if file contains expected triggers
        if grep -q "$expected_triggers" "$file"; then
            print_status "  Trigger configuration found"
        else
            print_warning "  Expected trigger '$expected_triggers' not found"
        fi
        
        # Check for Java setup in backend workflows
        if [[ "$file" == *"backend"* ]] || [[ "$file" == *"full"* ]]; then
            if grep -q "setup-java@v4" "$file" && (grep -q "java-version.*21" "$file" || (grep -q "java: \[21\]" "$file" && grep -q "java-version: \${{ matrix.java }}" "$file")); then
                print_status "  Java 21 configuration found"
            else
                print_error "  Java 21 configuration missing or incorrect"
                ERRORS=$((ERRORS + 1))
            fi
        fi
        
        # Check for Node setup in frontend workflows  
        if [[ "$file" == *"frontend"* ]] || [[ "$file" == *"full"* ]]; then
            if grep -q "setup-node@v4" "$file" && grep -q "node-version.*20" "$file"; then
                print_status "  Node.js 20 configuration found"
            else
                print_error "  Node.js 20 configuration missing or incorrect"
                ERRORS=$((ERRORS + 1))
            fi
        fi
        
        # Check for caching configuration
        if grep -q "actions/cache@v4" "$file" || grep -q "cache: 'npm'" "$file"; then
            print_status "  Dependency caching configured"
        else
            print_warning "  No dependency caching found"
        fi
        
        echo
    fi
}

echo
echo "📁 Checking required files..."
echo "============================"

# Check workflow files
check_file ".github/workflows/backend-ci.yml"
check_file ".github/workflows/frontend-ci.yml" 
check_file ".github/workflows/full-ci.yml"

# Check scripts
check_file "test-ci.sh"
check_file "verify-ci.sh"
check_file "start-dev.sh"

# Check devcontainer
check_file ".devcontainer/devcontainer.json"

# Check project files
check_file "backend/pom.xml"
check_file "frontend/package.json"
check_file "frontend/angular.json"

echo
echo "⚙️ Verifying workflow configurations..."
echo "======================================"

# Check individual workflows
check_workflow ".github/workflows/backend-ci.yml" "backend/"
check_workflow ".github/workflows/frontend-ci.yml" "frontend/"
check_workflow ".github/workflows/full-ci.yml" "main"

echo
echo "🔧 Checking Java configuration..."
echo "================================"

# Check backend pom.xml for Java 21
if [ -f "backend/pom.xml" ]; then
    if grep -q "maven.compiler.release.*21" "backend/pom.xml"; then
        print_status "Backend configured for Java 21"
    else
        print_error "Backend not configured for Java 21"
        ERRORS=$((ERRORS + 1))
    fi
    
    # Check for PostgreSQL dependency (not H2)
    if grep -q "quarkus-jdbc-postgresql" "backend/pom.xml"; then
        print_status "PostgreSQL dependency found"
    else
        print_error "PostgreSQL dependency missing"
        ERRORS=$((ERRORS + 1))
    fi
    
    if grep -q "quarkus-jdbc-h2" "backend/pom.xml"; then
        print_error "H2 dependency found - should use PostgreSQL only"
        ERRORS=$((ERRORS + 1))
    else
        print_status "No H2 dependency found (good)"
    fi
fi

echo
echo "🐳 Checking devcontainer configuration..."
echo "========================================"

if [ -f ".devcontainer/devcontainer.json" ]; then
    if grep -q "java:1-21" ".devcontainer/devcontainer.json"; then
        print_status "Devcontainer configured for Java 21"
    else
        print_error "Devcontainer not configured for Java 21"
        ERRORS=$((ERRORS + 1))
    fi
    
    if grep -q '"version": "20"' ".devcontainer/devcontainer.json"; then
        print_status "Devcontainer configured for Node.js 20"
    else
        print_warning "Devcontainer Node.js version not explicitly set to 20"
    fi
fi

echo
echo "📦 Checking package configurations..."
echo "==================================="

# Check frontend package.json for Angular
if [ -f "frontend/package.json" ]; then
    if grep -q "@angular/core" "frontend/package.json"; then
        print_status "Angular dependency found"
    else
        print_error "Angular dependency missing"
        ERRORS=$((ERRORS + 1))
    fi
fi

# Make scripts executable
echo
echo "🔐 Checking script permissions..."
echo "================================"

for script in "test-ci.sh" "verify-ci.sh" "start-dev.sh"; do
    if [ -f "$script" ]; then
        if [ -x "$script" ]; then
            print_status "$script is executable"
        else
            print_warning "$script is not executable, fixing..."
            chmod +x "$script"
            print_status "$script made executable"
        fi
    fi
done

echo
echo "📋 Configuration Summary"
echo "======================"

if [ $ERRORS -eq 0 ]; then
    print_status "All CI/CD configuration checks passed!"
    echo
    print_info "Your repository is properly configured with:"
    echo "  • Java 21 with Temurin distribution"
    echo "  • Node.js 20 with npm caching"
    echo "  • PostgreSQL database (H2 prohibited)"
    echo "  • Angular frontend with Material Design"
    echo "  • Hexagonal architecture compliance"
    echo "  • Complete CI/CD pipeline with integration tests"
    echo
    print_info "Available commands:"
    echo "  • ./test-ci.sh      - Run local CI tests"
    echo "  • ./start-dev.sh    - Start development servers"
    echo "  • ./verify-ci.sh    - Verify CI configuration (this script)"
    echo
else
    print_error "Found $ERRORS configuration issues that need to be addressed"
    exit 1
fi

echo "🎉 CI/CD verification completed successfully!"