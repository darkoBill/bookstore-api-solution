#!/usr/bin/env pwsh
# Bookstore API - Local Development Runner (PowerShell)
# This script starts the application for local development on Windows

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host "Starting Bookstore API in local development mode..."

# Check if Docker is running
try {
    docker info | Out-Null
} catch {
    Write-Error "ERROR: Docker is not running. Please start Docker and try again."
    exit 1
}

Write-Host "Starting PostgreSQL database..."
docker compose -f docker-compose.dev.yml up -d postgres | Out-Null

Write-Host "Waiting for PostgreSQL to be ready..."
$timeout = 60
$counter = 0
while (-not (docker exec bookstore-postgres-dev pg_isready -U bookstore -d bookstore > $null 2>&1)) {
    if ($counter -ge $timeout) {
        Write-Error "ERROR: PostgreSQL failed to start within $timeout seconds"
        exit 1
    }
    Write-Host "   Waiting for PostgreSQL... ($counter/$timeout)"
    Start-Sleep -Seconds 2
    $counter += 2
}

Write-Host "PostgreSQL is ready!"

Write-Host "Starting Spring Boot application locally..."
Write-Host "Application will be available at: http://localhost:8080"
Write-Host "API Documentation: http://localhost:8080/swagger-ui.html"
Write-Host "Default credentials: admin / admin123"
Write-Host ""
Write-Host "Press Ctrl+C to stop the application"
Write-Host ""

./gradlew.bat bootRun --args='--spring.profiles.active=local'