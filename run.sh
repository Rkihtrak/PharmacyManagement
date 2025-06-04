#!/bin/bash

# Default values
RUN_TESTS=false
RUN_SONAR=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        --test|-t)
        RUN_TESTS=true
        shift
        ;;
        --sonar|-s)
        RUN_SONAR=true
        shift
        ;;
        *)
        echo "Unknown option: $1"
        echo "Usage: ./run.sh [--test|-t] [--sonar|-s]"
        exit 1
        ;;
    esac
done

# Run tests if flag is set
if [ "$RUN_TESTS" = true ]; then
    echo "Running tests in Docker container..."
    
    # Create a volume for Maven cache if it doesn't exist
    docker volume create --name maven-repo || true

    # Clean up any existing test containers
    docker ps -a --filter "ancestor=surecost-test" -q | xargs docker rm -f 2>/dev/null || true
    
    # Build a test image and run tests
    docker build \
        --target builder \
        --build-arg MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository" \
        -t surecost-test . || { echo "Failed to build test image"; exit 1; }

    # Run tests in the container with a unique name
    TEST_CONTAINER_NAME="surecost-test-$(date +%s)"
    docker run --name $TEST_CONTAINER_NAME \
        --rm \
        -v maven-repo:/root/.m2 \
        surecost-test \
        ./mvnw verify
    
    # Store the test result
    TEST_RESULT=$?

    # Clean up the container if it still exists (in case --rm failed)
    docker rm -f $TEST_CONTAINER_NAME 2>/dev/null || true
    
    # Check if tests passed
    if [ $TEST_RESULT -ne 0 ]; then
        echo "Tests failed! Aborting deployment."
        exit 1
    fi
    echo "Tests passed successfully!"
fi

# Build and run the application
echo "Building and running the application..."
docker-compose down --remove-orphans
docker-compose build
docker-compose up -d --remove-orphans

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print messages
print_message() {
    echo -e "${GREEN}[SureCost]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[Warning]${NC} $1"
}

print_error() {
    echo -e "${RED}[Error]${NC} $1"
}

# Function to check if Docker is running
check_docker_running() {
    if ! docker info >/dev/null 2>&1; then
        if [ "$(uname)" = "Darwin" ]; then
            # On macOS, try to start Docker Desktop if it exists
            if [ -d "/Applications/Docker.app" ]; then
                print_warning "Docker is not running. Attempting to start Docker Desktop..."
                open -a Docker
                # Wait for Docker to start (max 30 seconds)
                for i in {1..30}; do
                    if docker info >/dev/null 2>&1; then
                        print_message "✓ Docker is now running"
                        return 0
                    fi
                    sleep 1
                done
                print_error "Docker Desktop failed to start. Please start it manually."
                return 1
            fi
        fi
        return 1
    fi
    return 0
}

# Function to check Java version
# check_java() {
#     if ! command -v java >/dev/null 2>&1; then
#         print_error "Java is not installed"
#         return 1
#     fi
    
#     version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F. '{print $1}')
#     if [ "$version" -lt 17 ]; then
#         print_error "Java version must be 17 or higher (found version $version)"
#         return 1
#     fi
#     print_message "✓ Java 17+ is installed (version $version)"
#     return 0
# }

# Check minimum required dependencies
print_message "Checking minimum required dependencies..."

# Check Java
# if ! check_java; then
#     print_error "Please install Java 17 or higher to continue"
#     exit 1
# fi

# Check Docker
if ! check_docker_running; then
    print_error "Please ensure Docker is installed and running to continue"
    exit 1
fi
print_message "✓ Docker is running"

# Check docker-compose
if ! docker-compose version >/dev/null 2>&1; then
    print_error "Docker Compose is not available. Please install Docker Compose to continue"
    exit 1
fi
print_message "✓ Docker Compose is available"

# Create necessary directories
print_message "Setting up project directories..."
mkdir -p data/mongodb
mkdir -p logs

# Set correct permissions for MongoDB directory
chmod 777 data/mongodb

# Build the project with Maven
print_message "Building the project..."
if [ "$RUN_TESTS" = true ]; then
    print_message "Skipping local build as tests will be run in Docker"
else
    if [ -f "./mvnw" ]; then
        chmod +x ./mvnw
        ./mvnw clean package -DskipTests
    else
        print_error "Maven wrapper not found. Please ensure you're in the correct directory."
        exit 1
    fi
fi

# Wait for services to be ready
print_message "Waiting for services to start..."
sleep 10

# Check if services are running
print_message "Checking service status..."
docker-compose ps

# Print access instructions
echo ""
print_message "Services are running!"
echo "Access the API through NGINX load balancer at http://localhost:80"
echo "MongoDB is available at localhost:27017"
echo ""
echo "Useful commands:"
echo "  ./run.sh --test       # Run tests only"
echo "  ./run.sh --sonar      # Run tests with SonarQube analysis"
echo "  docker-compose logs   # View service logs"
echo "  docker-compose down   # Stop all services"


