#!/usr/bin/env bash
# Development environment setup script for Librarie project
# This script configures TestContainers reuse for faster development

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
TESTCONTAINERS_FILE="$HOME/.testcontainers.properties"
TEMPLATE_FILE="$REPO_ROOT/docs/testcontainers.properties.template"

echo "=================================="
echo "Librarie Development Setup"
echo "=================================="
echo ""

# Check if Docker is running
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed or not in PATH"
    echo "   Please install Docker Desktop and try again"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo "❌ Docker is not running"
    echo "   Please start Docker Desktop and try again"
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Check if TestContainers config already exists
if [ -f "$TESTCONTAINERS_FILE" ]; then
    echo "⚠️  TestContainers configuration already exists at:"
    echo "   $TESTCONTAINERS_FILE"
    echo ""
    read -p "Do you want to overwrite it? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Setup cancelled. Existing configuration preserved."
        exit 0
    fi
fi

# Copy template file
if [ ! -f "$TEMPLATE_FILE" ]; then
    echo "❌ Template file not found at:"
    echo "   $TEMPLATE_FILE"
    exit 1
fi

cp "$TEMPLATE_FILE" "$TESTCONTAINERS_FILE"
echo "✅ TestContainers configuration created at:"
echo "   $TESTCONTAINERS_FILE"
echo ""

# Display the configuration
echo "Configuration content:"
echo "---"
cat "$TESTCONTAINERS_FILE"
echo "---"
echo ""

echo "✅ Setup complete!"
echo ""
echo "Next steps:"
echo "1. Start the backend: cd backend && ./mvnw quarkus:dev"
echo "2. PostgreSQL containers will be reused between restarts"
echo "3. Check the logs for 'Reusing container' messages"
echo ""
echo "Performance improvements:"
echo "- PostgreSQL startup: ~0.4s (with reuse) vs ~2s (without reuse)"
echo "- Overall startup: ~27s (vs ~30s without reuse)"
echo ""
echo "For more information, see: docs/development-setup.md"
