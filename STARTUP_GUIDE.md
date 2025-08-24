# Bookstore API - Comprehensive Startup Guide

## Prerequisites

### System Requirements
- **Java 21 LTS** - Required for compilation and runtime
- **Docker 20.10+** with Docker Compose - For containerized deployment and integration tests
- **Git** - For version control
- **curl** - For API testing (or any HTTP client)

### Verify Prerequisites
```bash
# Check Java version (must be 21)
java -version
javac -version

# Check Docker and Docker Compose
docker --version
docker compose --version

# Check Git
git --version

# Check curl
curl --version
```

## Quick Start (Recommended)

### Option 1: Full Containerized Deployment
This is the fastest way to get the complete system running:

```bash
# 1. Navigate to project directory
cd bookstore-api

# 2. Set required environment variables
export DB_USERNAME=bookstore
export DB_PASSWORD=bookstore123
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=admin123
export USER_USERNAME=user
export USER_PASSWORD=user123

# 3. Start all services (API + PostgreSQL)
docker compose up --build -d

# 4. Wait for services to be ready (30-60 seconds)
docker compose logs app --tail=20

# 5. Verify API is healthy
curl http://localhost:8080/actuator/health

# 6. Access Swagger UI with authentication
# Use the credentials from environment variables
open http://localhost:8080/swagger-ui.html
```

### Option 2: Local Development Setup
Run API on host with PostgreSQL in container:

```bash
# 1. Start only PostgreSQL database
docker compose -f docker-compose.dev.yml up -d postgres

# 2. Wait for database to be ready
docker compose -f docker-compose.dev.yml logs postgres --tail=10

# 3. Set required environment variables
export DB_USERNAME=bookstore
export DB_PASSWORD=bookstore123
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=admin123
export USER_USERNAME=user
export USER_PASSWORD=user123

# 4. Run application locally using convenience script
./scripts/run-local.sh

# Or manually:
./gradlew bootRun --args='--spring.profiles.active=local'
```

**Note:** All security credentials must be provided via environment variables. The application will fail to start if any required credentials are missing.

## Testing Strategy

### Important: Testing Prerequisites

#### For Host-Based Testing
Before running tests on the host version, ensure:

1. **Docker is running** - Integration tests use Testcontainers
2. **No conflicting services** on ports 5432 (PostgreSQL)
3. **Sufficient memory** - Testcontainers need ~2GB RAM

#### For Container-Based Testing
Before running containerized tests:

1. **Stop any running instances** to avoid port conflicts:
   ```bash
   docker compose down
   ```

2. **Clean Docker environment** if needed:
   ```bash
   docker system prune -f
   ```

### Unit Tests

Unit tests focus on business logic and don't require external dependencies.

**Run Unit Tests (Host Version):**
```bash
# Run only unit tests
./gradlew test --tests "*.unit.*"

# With coverage report
./gradlew test --tests "*.unit.*" jacocoTestReport
```

**Unit Test Coverage:**
- Service layer business logic (`BookServiceImplTest`)
- DTO mapping (`BookMapperTest`) 
- Validation rules
- Error handling scenarios

### Integration Tests

Integration tests require PostgreSQL database and test the full application stack.

#### Host Version Integration Tests

**Prerequisites:**
1. Docker must be running (for Testcontainers)
2. No manual PostgreSQL setup required (Testcontainers handles this)

**Run Integration Tests:**
```bash
# Run only integration tests (Testcontainers will start PostgreSQL automatically)
./gradlew test --tests "*.integration.*"

# Run all integration tests with detailed output
./gradlew test --tests "*.integration.*" --info
```

**What Integration Tests Cover:**
- Full HTTP request/response cycles (`BookControllerIntegrationTest`)
- Database transactions and rollback scenarios
- Security enforcement (`SecurityIntegrationTest`)
- RFC-7807 error response validation
- Authentication and authorization

#### Container Version Integration Tests

**Prerequisites:**
1. Ensure containers are running:
   ```bash
   docker compose up -d
   ```

2. Wait for API to be healthy:
   ```bash
   # Wait for startup (usually 30-60 seconds)
   curl --retry 10 --retry-delay 3 --retry-connrefused http://localhost:8080/actuator/health
   ```

**Run Container Integration Tests:**
```bash
# Run smoke tests (comprehensive end-to-end testing)
./scripts/smoke.sh

# Or run manual API tests
curl -u admin:admin123 -X GET "http://localhost:8080/api/books?page=0&size=5"
curl -u user:user123 -X GET "http://localhost:8080/api/books"
```

### Complete Test Suite

**Run All Tests (Host Version):**
```bash
# Run complete test suite
./gradlew test

# Expected output: All unit and integration tests pass
# Integration tests will automatically start/stop PostgreSQL containers
```

**Run All Tests (Container Version):**
```bash
# 1. Start containers
docker compose up -d

# 2. Wait for readiness
curl --retry 10 --retry-delay 3 --retry-connrefused http://localhost:8080/actuator/health

# 3. Run smoke tests
./scripts/smoke.sh

# 4. Optional: Run additional API validation
./scripts/validate-api.sh  # If available
```

## Environment Configuration

### Development Profiles

The application supports multiple Spring profiles:

1. **default** - Production configuration (external PostgreSQL required)
2. **local** - Local development (PostgreSQL on localhost:5432)
3. **test** - Test configuration (used by Testcontainers)
4. **demo** - Includes sample data for demonstration

### Environment Variables

**Database Configuration:**
```bash
export DB_URL="jdbc:postgresql://localhost:5432/bookstore"
export DB_USERNAME="bookstore"
export DB_PASSWORD="bookstore123"
export DB_MAX_POOL_SIZE="30"
export DB_MIN_IDLE="10"
```

**Security Configuration:**
```bash
export ADMIN_USERNAME="admin"
export ADMIN_PASSWORD="admin123"
export USER_USERNAME="user"
export USER_PASSWORD="user123"
```

**Application Configuration:**
```bash
export SERVER_PORT="8080"
export LOG_LEVEL="INFO"
export SWAGGER_ENABLED="true"
```

## Database Setup

### Automatic Setup (Recommended)
The application uses Liquibase for automatic database schema management:
- Schema creation and updates are handled automatically
- Demo data is loaded with `demo` profile
- No manual database setup required

### Manual Database Setup (If Needed)
```bash
# Connect to PostgreSQL
docker exec -it bookstore-postgres psql -U postgres

# Create database and user
CREATE DATABASE bookstore;
CREATE USER bookstore WITH ENCRYPTED PASSWORD 'bookstore123';
GRANT ALL PRIVILEGES ON DATABASE bookstore TO bookstore;
```

## Troubleshooting

### Common Issues

#### Port Conflicts
```bash
# Check what's running on port 8080
lsof -i :8080

# Check what's running on port 5432
lsof -i :5432

# Stop conflicting services
docker compose down
```

#### Docker Issues
```bash
# Clean Docker environment
docker system prune -f

# Remove all containers and volumes
docker compose down -v
docker system prune -af --volumes
```

#### Test Failures
```bash
# If integration tests fail, check Docker
docker ps
docker compose logs postgres
docker compose logs app

# Run tests with more verbose output
./gradlew test --info --stacktrace
```

#### Memory Issues
```bash
# Increase Docker memory limits (Docker Desktop)
# Go to Settings > Resources > Memory > Increase to 4GB+

# Check available memory
docker stats
```

### Health Checks

**API Health:**
```bash
curl http://localhost:8080/actuator/health
```

**Database Connectivity:**
```bash
curl http://localhost:8080/actuator/health/db
```

**Readiness Probe:**
```bash
curl http://localhost:8080/actuator/health/readiness
```

**Liveness Probe:**
```bash
curl http://localhost:8080/actuator/health/liveness
```

## API Usage Examples

### Authentication
```bash
# Admin credentials (full CRUD access)
ADMIN_AUTH="-u admin:admin123"

# User credentials (read-only access)  
USER_AUTH="-u user:user123"
```

### Basic Operations
```bash
# List all books (paginated)
curl $USER_AUTH "http://localhost:8080/api/books?page=0&size=10"

# Search books by title
curl $USER_AUTH "http://localhost:8080/api/books?title=clean"

# Search books by author
curl $USER_AUTH "http://localhost:8080/api/books?author=martin"

# Get specific book
curl $USER_AUTH "http://localhost:8080/api/books/{book-id}"

# Create new book (Admin only)
curl $ADMIN_AUTH -X POST "http://localhost:8080/api/books" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Book",
    "price": 29.99,
    "publishedYear": 2024,
    "isbn": "978-1234567890",
    "authors": [{"name": "Author Name"}],
    "genres": [{"name": "Fiction"}]
  }'
```

## Monitoring and Observability

### Available Endpoints
- `/actuator/health` - Overall health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information
- `/swagger-ui.html` - Interactive API documentation (requires auth)

### Log Monitoring
```bash
# View application logs (container)
docker compose logs app --tail=50 -f

# View database logs
docker compose logs postgres --tail=20 -f

# View all service logs
docker compose logs -f
```

## Development Workflow

### Making Changes
```bash
# 1. Make code changes
# 2. Run unit tests
./gradlew test --tests "*.unit.*"

# 3. Run integration tests  
./gradlew test --tests "*.integration.*"

# 4. Test containerized version
docker compose up --build -d
./scripts/smoke.sh

# 5. Clean up
docker compose down
```

### Test-Driven Development
```bash
# Run tests in watch mode (if supported)
./gradlew test --continuous

# Run specific test class
./gradlew test --tests "BookServiceImplTest"

# Run tests with coverage
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## Performance Considerations

### JVM Optimization
The application includes production-ready JVM settings:
- Container-aware memory allocation
- Virtual threads enabled for high I/O throughput
- G1 garbage collector with 200ms max pause
- Heap dump on OutOfMemoryError

### Database Optimization
- HikariCP connection pooling (30 max, 10 min idle)
- Strategic indexes on search fields
- Functional unique indexes for case-insensitive searches
- Connection leak detection enabled

### Monitoring Performance
```bash
# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check database connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections

# Check HTTP request metrics
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## Production Deployment Checklist

- [ ] Environment variables configured
- [ ] Database credentials secured
- [ ] Swagger UI disabled (`SWAGGER_ENABLED=false`)
- [ ] Log level set to INFO/WARN
- [ ] Health checks configured
- [ ] Resource limits defined
- [ ] Backup strategy implemented
- [ ] Monitoring alerts configured

## Getting Help

### Log Analysis
```bash
# Application startup logs
docker compose logs app | grep "Started BookstoreApiApplication"

# Error logs
docker compose logs app | grep ERROR

# Database connection logs
docker compose logs app | grep HikariPool
```

### Test Debugging
```bash
# Run single test with full output
./gradlew test --tests "BookControllerIntegrationTest.createBook_AsAdmin_ShouldReturn201" --info

# Check test reports
open build/reports/tests/test/index.html
```

The system is designed to be production-ready with comprehensive testing, monitoring, and operational tooling meeting enterprise standards.