#!/bin/bash

# Bookstore API - Local Development Runner
# This script starts the application for local development

set -e

echo "Starting Bookstore API in local development mode..."

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "ERROR: Docker is not running. Please start Docker and try again."
    exit 1
fi

# Start PostgreSQL in Docker
echo "Starting PostgreSQL database..."
docker compose -f docker-compose.dev.yml up -d postgres

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
timeout=60
counter=0
while ! docker exec bookstore-postgres-dev pg_isready -U bookstore -d bookstore >/dev/null 2>&1; do
    if [ $counter -ge $timeout ]; then
        echo "ERROR: PostgreSQL failed to start within $timeout seconds"
        exit 1
    fi
    echo "   Waiting for PostgreSQL... ($counter/$timeout)"
    sleep 2
    counter=$((counter + 2))
done

echo "PostgreSQL is ready!"

# Run the application locally
echo "Starting Spring Boot application locally..."
echo "Application will be available at: http://localhost:8080"
echo "API Documentation: http://localhost:8080/swagger-ui.html"
echo "Default credentials: admin / admin123"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

./gradlew bootRun --args='--spring.profiles.active=local,basic'