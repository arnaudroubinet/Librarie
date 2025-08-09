#!/bin/bash

# Start both Quarkus backend and Angular frontend in development mode

echo "Starting Librarie development servers..."

# Start Quarkus backend in background
echo "Starting Quarkus backend on port 8080..."
mvn quarkus:dev &
QUARKUS_PID=$!

# Wait a moment for Quarkus to start
sleep 5

# Start Angular frontend in background
echo "Starting Angular frontend on port 4200..."
cd frontend && npm start &
ANGULAR_PID=$!

echo "Both servers are starting..."
echo "Quarkus backend: http://localhost:8080"
echo "Angular frontend: http://localhost:4200"
echo ""
echo "Press Ctrl+C to stop both servers"

# Function to cleanup processes on script exit
cleanup() {
    echo "Stopping servers..."
    kill $QUARKUS_PID 2>/dev/null
    kill $ANGULAR_PID 2>/dev/null
    exit
}

# Set trap to cleanup on script exit
trap cleanup SIGINT SIGTERM

# Wait for both processes
wait