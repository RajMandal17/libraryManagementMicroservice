#!/bin/bash

# ============================================================================
# Library Management Microservices - Complete Startup Script
# ============================================================================
# This script starts all three services in the correct order:
# 1. Eureka Server (Service Registry)
# 2. User Service (Registers with Eureka)
# 3. Book Service (Registers with Eureka)
# ============================================================================

# Color codes for better output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Base directory
BASE_DIR="/Users/raj/IdeaProjects/libraryManagementMicroservice"

echo -e "${BLUE}============================================================================${NC}"
echo -e "${BLUE}  Library Management Microservices - Startup with Eureka${NC}"
echo -e "${BLUE}============================================================================${NC}"
echo ""

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to wait for a service to be ready
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=60
    local attempt=0

    echo -e "${YELLOW}Waiting for ${service_name} to be ready...${NC}"

    while [ $attempt -lt $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ ${service_name} is ready!${NC}"
            return 0
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done

    echo -e "${RED}✗ ${service_name} failed to start within expected time${NC}"
    return 1
}

# Clean up function
cleanup() {
    echo ""
    echo -e "${YELLOW}Cleaning up...${NC}"

    # Kill all Java processes started by this script
    if [ ! -z "$EUREKA_PID" ]; then
        kill $EUREKA_PID 2>/dev/null
        echo -e "${GREEN}✓ Stopped Eureka Server${NC}"
    fi

    if [ ! -z "$USER_SERVICE_PID" ]; then
        kill $USER_SERVICE_PID 2>/dev/null
        echo -e "${GREEN}✓ Stopped User Service${NC}"
    fi

    if [ ! -z "$BOOK_SERVICE_PID" ]; then
        kill $BOOK_SERVICE_PID 2>/dev/null
        echo -e "${GREEN}✓ Stopped Book Service${NC}"
    fi

    exit 0
}

# Set up cleanup trap
trap cleanup SIGINT SIGTERM

# ============================================================================
# Step 1: Build All Services
# ============================================================================
echo -e "${BLUE}Step 1: Building all services...${NC}"
echo ""

# Build Eureka Server
echo -e "${YELLOW}Building Eureka Server...${NC}"
cd "$BASE_DIR/eureka-server"
mvn clean package -DskipTests > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Eureka Server built successfully${NC}"
else
    echo -e "${RED}✗ Failed to build Eureka Server${NC}"
    exit 1
fi
echo ""

# Build User Service
echo -e "${YELLOW}Building User Service...${NC}"
cd "$BASE_DIR/userService"
mvn clean package -DskipTests > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ User Service built successfully${NC}"
else
    echo -e "${RED}✗ Failed to build User Service${NC}"
    exit 1
fi
echo ""

# Build Book Service
echo -e "${YELLOW}Building Book Service...${NC}"
cd "$BASE_DIR/bookService/demo"
mvn clean package -DskipTests > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Book Service built successfully${NC}"
else
    echo -e "${RED}✗ Failed to build Book Service${NC}"
    exit 1
fi
echo ""

# ============================================================================
# Step 2: Check if ports are available
# ============================================================================
echo -e "${BLUE}Step 2: Checking port availability...${NC}"
echo ""

if check_port 8761; then
    echo -e "${RED}✗ Port 8761 (Eureka Server) is already in use${NC}"
    echo "  Please stop the process using this port and try again"
    exit 1
fi

if check_port 8080; then
    echo -e "${RED}✗ Port 8080 (Book Service) is already in use${NC}"
    echo "  Please stop the process using this port and try again"
    exit 1
fi

if check_port 8081; then
    echo -e "${RED}✗ Port 8081 (User Service) is already in use${NC}"
    echo "  Please stop the process using this port and try again"
    exit 1
fi

echo -e "${GREEN}✓ All ports are available${NC}"
echo ""

# ============================================================================
# Step 3: Start Eureka Server (Port 8761)
# ============================================================================
echo -e "${BLUE}Step 3: Starting Eureka Server...${NC}"
echo ""

cd "$BASE_DIR/eureka-server"
java -jar target/eureka-server-0.0.1-SNAPSHOT.jar > eureka-server.log 2>&1 &
EUREKA_PID=$!

echo -e "${YELLOW}Eureka Server PID: $EUREKA_PID${NC}"

# Wait for Eureka Server to be ready
wait_for_service "Eureka Server" "http://localhost:8761"

echo ""
echo -e "${GREEN}✓ Eureka Server Dashboard: ${BLUE}http://localhost:8761${NC}"
echo ""
sleep 5

# ============================================================================
# Step 4: Start User Service (Port 8081)
# ============================================================================
echo -e "${BLUE}Step 4: Starting User Service...${NC}"
echo ""

cd "$BASE_DIR/userService"
java -jar target/user-service-0.0.1-SNAPSHOT.jar > user-service.log 2>&1 &
USER_SERVICE_PID=$!

echo -e "${YELLOW}User Service PID: $USER_SERVICE_PID${NC}"

# Wait for User Service to be ready
wait_for_service "User Service" "http://localhost:8081/api/users"

echo ""
sleep 5

# ============================================================================
# Step 5: Start Book Service (Port 8080)
# ============================================================================
echo -e "${BLUE}Step 5: Starting Book Service...${NC}"
echo ""

cd "$BASE_DIR/bookService/demo"
java -jar target/demo-0.0.1-SNAPSHOT.jar > book-service.log 2>&1 &
BOOK_SERVICE_PID=$!

echo -e "${YELLOW}Book Service PID: $BOOK_SERVICE_PID${NC}"

# Wait for Book Service to be ready
wait_for_service "Book Service" "http://localhost:8080/api/books"

echo ""

# ============================================================================
# Step 6: Verify Eureka Registration
# ============================================================================
echo -e "${BLUE}Step 6: Verifying service registration...${NC}"
echo ""
sleep 10  # Wait for services to register

echo -e "${GREEN}All services started successfully!${NC}"
echo ""
echo -e "${BLUE}============================================================================${NC}"
echo -e "${BLUE}  Service Status${NC}"
echo -e "${BLUE}============================================================================${NC}"
echo ""
echo -e "${GREEN}✓ Eureka Server${NC}   : http://localhost:8761"
echo -e "${GREEN}✓ Book Service${NC}    : http://localhost:8080/api/books"
echo -e "${GREEN}✓ User Service${NC}    : http://localhost:8081/api/users"
echo ""
echo -e "${BLUE}============================================================================${NC}"
echo -e "${BLUE}  Process IDs${NC}"
echo -e "${BLUE}============================================================================${NC}"
echo ""
echo -e "Eureka Server PID : ${YELLOW}$EUREKA_PID${NC}"
echo -e "User Service PID  : ${YELLOW}$USER_SERVICE_PID${NC}"
echo -e "Book Service PID  : ${YELLOW}$BOOK_SERVICE_PID${NC}"
echo ""
echo -e "${BLUE}============================================================================${NC}"
echo -e "${BLUE}  Log Files${NC}"
echo -e "${BLUE}============================================================================${NC}"
echo ""
echo -e "Eureka Server : ${YELLOW}$BASE_DIR/eureka-server/eureka-server.log${NC}"
echo -e "User Service  : ${YELLOW}$BASE_DIR/userService/user-service.log${NC}"
echo -e "Book Service  : ${YELLOW}$BASE_DIR/bookService/demo/book-service.log${NC}"
echo ""
echo -e "${BLUE}============================================================================${NC}"
echo -e "${YELLOW}Press Ctrl+C to stop all services${NC}"
echo -e "${BLUE}============================================================================${NC}"
echo ""

# Keep script running
wait

