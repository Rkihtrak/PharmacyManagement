# SureCost API

A Spring Boot application for managing pharmacy items with MongoDB backend and NGINX load balancer.

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Maven (optional, wrapper included)

## Quick Start

1. Clone the repository:
```bash
git clone <repository-url>
cd surecost
```

2. Make the run script executable:
```bash
chmod +x run.sh
```

3. Start the application:
```bash
# Run without tests
./run.sh

# Run with tests and code analysis
./run.sh --test
```

The script will:
- Run tests and code analysis (if --test flag is used)
- Create necessary directories
- Build the application
- Start all Docker containers
- Set up MongoDB
- Configure NGINX load balancer

## Testing and Code Quality

The project includes comprehensive test coverage and code quality analysis:

- Minimum 80% code coverage requirement
- JaCoCo for code coverage analysis
- SonarQube for:
  - Code quality metrics
  - Vulnerability detection
  - Code complexity analysis
  - Maintainability metrics

To run tests and analysis:
```bash
./run.sh --test
```

This will:
1. Run all unit tests
2. Generate code coverage reports
3. Perform SonarQube analysis
4. Fail the build if:
   - Tests fail
   - Coverage is below 80%
   - Critical issues are found

Test reports are available in:
- JaCoCo: `target/site/jacoco/index.html`
- SonarQube: Available through SonarQube dashboard

## API Documentation

The API documentation is available through Swagger UI:
- Swagger UI: http://localhost/swagger-ui.html
- OpenAPI Specification: http://localhost/api-docs

You can:
- View all available endpoints
- Test the API directly from the browser
- See request/response schemas
- Download the OpenAPI specification

## Manual Setup

If you prefer to run commands manually:

1. Build the application:
```bash
./mvnw clean package -DskipTests
```

2. Start the services:
```bash
docker-compose up --build -d
```

## Project Structure

- `src/` - Source code
- `data/mongodb/` - MongoDB data directory
- `nginx.conf` - NGINX configuration
- `docker-compose.yml` - Docker services configuration
- `run.sh` - Initialization and run script

## API Endpoints

### Pharmacy Items

- `GET /api/pharmacy-items` - Get all items
- `POST /api/pharmacy-items` - Create single or multiple items
- `GET /api/pharmacy-items/{id}` - Get item by ID
- `PATCH /api/pharmacy-items/{id}` - Update specific fields of an item

### Search and Delete Operations

- `GET /api/pharmacy-items/search` - Search items with parameters:
  - `name` (case-insensitive partial match)
  - `manufacturer` (case-insensitive partial match)
  - `minPrice` and `maxPrice` for price range
  - `exactPrice` for exact price match

- `DELETE /api/pharmacy-items` - Delete items with parameters:
  - `manufacturer`
  - `name`
  - `minPrice` and `maxPrice` for price range

## Development

The application runs three instances behind an NGINX load balancer:
- API instances run on internal Docker network
- NGINX exposes port 80
- MongoDB runs on port 27017
- Swagger UI available at /swagger-ui.html

## Data Storage

MongoDB data is stored locally in the `data/mongodb` directory within the project.

## Useful Commands

- View logs: `docker-compose logs -f`
- Stop services: `docker-compose down`
- Rebuild and restart: `docker-compose up --build -d`
- Access MongoDB shell: `docker-compose exec mongodb mongo -u root -p example`

## Environment Variables

Default configuration:
- MongoDB host: mongodb
- MongoDB port: 27017
- MongoDB root username: root
- MongoDB root password: example

## Troubleshooting

1. If services don't start:
   - Check logs: `docker-compose logs`
   - Ensure ports 80 and 27017 are available
   - Verify Docker is running

2. If data persistence issues:
   - Check permissions on `data/mongodb` directory
   - Verify MongoDB container logs

## Security Notes

- Default MongoDB credentials should be changed in production
- API endpoints should be secured with authentication
- HTTPS should be configured for production use 