#!/bin/bash

# CI test script to run both backend and frontend builds locally
# This simulates what the GitHub Actions workflows will do

set -e

echo "=== Running local CI tests ==="

# Check prerequisites
echo "Checking Java version..."
java -version

echo "Checking Node version..."
node -v

echo "Checking npm version..."
npm -v

echo ""
echo "=== Building Backend ==="
echo "Running Maven clean test..."
mvn clean test

echo "Running Maven package..."
mvn package -DskipTests

echo ""
echo "=== Building Frontend ==="
cd frontend

echo "Installing dependencies..."
npm ci

echo "Running tests..."
npm run test -- --watch=false --browsers=ChromeHeadless --code-coverage

echo "Building application..."
npm run build

cd ..

echo ""
echo "=== CI tests completed successfully ==="