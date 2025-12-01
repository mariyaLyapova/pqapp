#!/bin/bash

# PromptQuest Docker Runner Script
# This script provides easy commands to manage the PromptQuest Docker application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project name
PROJECT_NAME="promptquest"

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}  PromptQuest Docker Manager${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
}

# Function to create necessary directories
create_directories() {
    print_status "Creating necessary directories..."
    mkdir -p db input logs
    print_status "Directories created successfully."
}

# Function to build the application
build() {
    print_status "Building PromptQuest Docker image..."
    docker-compose build
    print_status "Build completed successfully."
}

# Function to start the application
start() {
    print_status "Starting PromptQuest application..."
    create_directories
    docker-compose up -d
    print_status "Application started successfully."
    print_status "Access the quiz at: http://localhost:8081/index.html"
    print_status "Access the admin panel at: http://localhost:8081/admin.html"
}


# Function to stop the application
stop() {
    print_status "Stopping PromptQuest application..."
    docker-compose down
    print_status "Application stopped successfully."
}

# Function to restart the application
restart() {
    print_status "Restarting PromptQuest application..."
    docker-compose restart
    print_status "Application restarted successfully."
}

# Function to view logs
logs() {
    print_status "Showing application logs (press Ctrl+C to exit)..."
    docker-compose logs -f
}

# Function to show application status
status() {
    print_status "Application status:"
    docker-compose ps
    echo
    print_status "Container health:"
    docker-compose exec promptquest curl -f http://localhost:8081/actuator/health 2>/dev/null || print_warning "Health check failed"
}

# Function to clean up (remove containers, volumes, and images)
clean() {
    print_warning "This will remove all containers, networks, and the local image."
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_status "Cleaning up PromptQuest Docker resources..."
        docker-compose down -v --rmi local
        print_status "Cleanup completed."
    else
        print_status "Cleanup cancelled."
    fi
}

# Function to enter the container shell
shell() {
    print_status "Entering PromptQuest container shell..."
    docker-compose exec promptquest sh
}

# Function to backup database
backup() {
    print_status "Creating database backup..."
    timestamp=$(date +%Y%m%d_%H%M%S)
    docker-compose exec promptquest cp /app/db/promptquest.db /app/db/backup_${timestamp}.db
    print_status "Database backed up to: db/backup_${timestamp}.db"
}

# Function to show help
show_help() {
    print_header
    echo "Usage: $0 [COMMAND]"
    echo
    echo "Commands:"
    echo "  build       Build the Docker image"
    echo "  start       Start the application"
    echo "  stop        Stop the application"
    echo "  restart     Restart the application"
    echo "  logs        View application logs"
    echo "  status      Show application status and health"
    echo "  shell       Enter the container shell"
    echo "  backup      Backup the database"
    echo "  clean       Remove all containers and images"
    echo "  help        Show this help message"
    echo
    echo "Examples:"
    echo "  $0 start       # Start the application on port 8081"
    echo "  $0 logs        # View real-time logs"
    echo "  $0 status      # Check if application is healthy"
}

# Main script logic
main() {
    check_docker

    case "${1:-help}" in
        build)
            build
            ;;
        start)
            start
            ;;
        stop)
            stop
            ;;
        restart)
            restart
            ;;
        logs)
            logs
            ;;
        status)
            status
            ;;
        shell)
            shell
            ;;
        backup)
            backup
            ;;
        clean)
            clean
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown command: $1"
            echo
            show_help
            exit 1
            ;;
    esac
}

# Run the main function with all arguments
main "$@"