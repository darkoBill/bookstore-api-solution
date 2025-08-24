#!/bin/bash

# Bookstore API - Stop Local Development Environment

set -e

echo "Stopping Bookstore API local development environment..."

# Stop PostgreSQL container
echo "Stopping PostgreSQL database..."
docker compose -f docker-compose.dev.yml down

echo "Local development environment stopped!"
echo "To start again, run: ./scripts/run-local.sh"