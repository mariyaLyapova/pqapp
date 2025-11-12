#!/bin/bash

# PromptQuest JSON Import Script
# This script starts the application and imports questions from a JSON file

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
JSON_FILE=""
PORT=8081
AUTO_START=false
CLEAR_DB=true

# Function to display help
show_help() {
    echo -e "${BLUE}PromptQuest JSON Import Script${NC}"
    echo
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "Options:"
    echo "  -f, --file FILE          JSON file to import (required)"
    echo "  -p, --port PORT          Server port (default: 8081)"
    echo "  -s, --start              Start application automatically"
    echo "  --no-clear              Don't clear existing database"
    echo "  -h, --help              Show this help message"
    echo
    echo "Examples:"
    echo "  $0 -f questions.json"
    echo "  $0 -f input/promptquest-questions-test.json -s"
    echo "  $0 -f questions.json -p 8080 --no-clear"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -f|--file)
            JSON_FILE="$2"
            shift 2
            ;;
        -p|--port)
            PORT="$2"
            shift 2
            ;;
        -s|--start)
            AUTO_START=true
            shift
            ;;
        --no-clear)
            CLEAR_DB=false
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Validate JSON file argument
if [[ -z "$JSON_FILE" ]]; then
    echo -e "${RED}Error: JSON file is required${NC}"
    show_help
    exit 1
fi

# Check if JSON file exists
if [[ ! -f "$JSON_FILE" ]]; then
    echo -e "${RED}Error: File '$JSON_FILE' not found${NC}"
    exit 1
fi

# Get absolute path of JSON file
JSON_FILE_ABS=$(realpath "$JSON_FILE")

echo -e "${BLUE}=== PromptQuest JSON Import Script ===${NC}"
echo -e "JSON File: ${GREEN}$JSON_FILE_ABS${NC}"
echo -e "Port: ${GREEN}$PORT${NC}"
echo -e "Clear DB: ${GREEN}$CLEAR_DB${NC}"
echo

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven (mvn) is not installed or not in PATH${NC}"
    exit 1
fi

# Check if we're in the right directory
if [[ ! -f "pom.xml" ]]; then
    echo -e "${RED}Error: pom.xml not found. Please run this script from the project root directory${NC}"
    exit 1
fi

# Create input directory if it doesn't exist
mkdir -p input

# Copy JSON file to input directory
INPUT_FILE="input/$(basename "$JSON_FILE")"
echo -e "${YELLOW}Copying JSON file to input directory...${NC}"
cp "$JSON_FILE_ABS" "$INPUT_FILE"

# Set application properties for the import
export SPRING_PROFILES_ACTIVE=import
export PROMPTQUEST_AUTO_INITIALIZE=true
export PROMPTQUEST_JSON_FILE_PATH="file:$INPUT_FILE"
export PROMPTQUEST_CLEAR_ON_STARTUP="$CLEAR_DB"
export SERVER_PORT="$PORT"

echo -e "${YELLOW}Starting Spring Boot application...${NC}"

if [[ "$AUTO_START" == true ]]; then
    # Start application in background
    echo -e "${BLUE}Starting application automatically in background...${NC}"
    nohup mvn spring-boot:run > import.log 2>&1 &
    APP_PID=$!

    echo -e "${GREEN}Application started with PID: $APP_PID${NC}"
    echo -e "${YELLOW}Waiting for application to start...${NC}"

    # Wait for application to be ready
    for i in {1..30}; do
        if curl -s "http://localhost:$PORT/actuator/health" >/dev/null 2>&1 || curl -s "http://localhost:$PORT" >/dev/null 2>&1; then
            echo -e "${GREEN}✓ Application is ready!${NC}"
            break
        fi
        if [[ $i -eq 30 ]]; then
            echo -e "${RED}✗ Application failed to start within 30 seconds${NC}"
            kill $APP_PID 2>/dev/null || true
            exit 1
        fi
        sleep 1
        echo -n "."
    done

    echo
    echo -e "${GREEN}=== Import Summary ===${NC}"
    echo -e "Application URL: ${BLUE}http://localhost:$PORT${NC}"
    echo -e "Quiz URL: ${BLUE}http://localhost:$PORT${NC}"
    echo -e "Log file: ${YELLOW}import.log${NC}"
    echo -e "Process ID: ${YELLOW}$APP_PID${NC}"
    echo
    echo -e "${BLUE}To stop the application, run:${NC} kill $APP_PID"
    echo -e "${BLUE}To view logs, run:${NC} tail -f import.log"

else
    # Start application interactively
    echo -e "${BLUE}Starting application interactively...${NC}"
    echo -e "${YELLOW}Press Ctrl+C to stop the application${NC}"
    echo

    mvn spring-boot:run
fi

echo -e "${GREEN}Script completed successfully!${NC}"