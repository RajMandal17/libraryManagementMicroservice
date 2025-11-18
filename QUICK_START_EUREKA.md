# Quick Start Guide - Library Management Microservices with Eureka

## Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL Server running on localhost:3306
- Ports 8080, 8081, and 8761 available

## Quick Start - Automated Setup

### Option 1: Use the Automated Script (Recommended)
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice
./start-all-services-with-eureka.sh
```

This script will:
1. Build all three services
2. Check port availability
3. Start Eureka Server (8761)
4. Start User Service (8081)
5. Start Book Service (8080)
6. Verify registration with Eureka

**Press Ctrl+C to stop all services**

## Manual Setup

### Step 1: Start Eureka Server
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice/eureka-server
mvn spring-boot:run
```
**Wait for**: "Started EurekaServerApplication"  
**Access Dashboard**: http://localhost:8761

### Step 2: Start User Service
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice/userService
mvn spring-boot:run
```
**Wait for**: "Started UserServiceApplication"  
**Verify**: Check Eureka dashboard - USER-SERVICE should appear

### Step 3: Start Book Service
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice/bookService/demo
mvn spring-boot:run
```
**Wait for**: "Started DemoApplication"  
**Verify**: Check Eureka dashboard - BOOK-SERVICE should appear

## Verify Setup

### 1. Check Eureka Dashboard
Open http://localhost:8761

You should see:
```
Instances currently registered with Eureka:
┌─────────────────┬──────────────┬────────┬───────────┐
│ Application     │ AMIs         │ Zone   │ Status    │
├─────────────────┼──────────────┼────────┼───────────┤
│ BOOK-SERVICE    │ n/a (1)      │ (1)    │ UP        │
│ USER-SERVICE    │ n/a (1)      │ (1)    │ UP        │
└─────────────────┴──────────────┴────────┴───────────┘
```

### 2. Test User Service
```bash
# Get all users
curl http://localhost:8081/api/users

# Create a user
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "membershipDate": "2024-01-01"
  }'
```

### 3. Test Book Service
```bash
# Get all books
curl http://localhost:8080/api/books

# Add a book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Great Gatsby",
    "author": "F. Scott Fitzgerald",
    "isbn": "978-0743273565",
    "availableCopies": 5
  }'
```

### 4. Test Inter-Service Communication (via Eureka)
```bash
# Borrow a book (Book Service calls User Service via Eureka)
curl -X POST http://localhost:8080/api/books/1/borrow/1

# Return a book
curl -X POST http://localhost:8080/api/books/1/return/1
```

**Expected Behavior**:
- Book Service discovers User Service through Eureka (not hardcoded URL)
- Validates user can borrow (max 5 books)
- Updates book availability
- Updates user's borrowed book count

## Service URLs

| Service        | Port | URL                                    | Purpose                  |
|----------------|------|----------------------------------------|--------------------------|
| Eureka Server  | 8761 | http://localhost:8761                  | Service Registry         |
| Book Service   | 8080 | http://localhost:8080/api/books        | Book Management          |
| User Service   | 8081 | http://localhost:8081/api/users        | User Management          |

## Architecture Highlights

### Service Discovery Flow
```
1. Services Start → Register with Eureka (8761)
2. Book Service needs User Service
3. Book Service asks Eureka: "Where is user-service?"
4. Eureka responds: "localhost:8081"
5. Book Service makes HTTP call to User Service
6. No hardcoded URLs! ✨
```

### Before Eureka
```java
@FeignClient(name = "user-service", url = "http://localhost:8081")
// ❌ Hardcoded URL - needs code change to scale
```

### After Eureka
```java
@FeignClient(name = "user-service")
// ✅ Dynamic discovery - can run multiple instances
```

## Key Features

### 1. Service Discovery
- ✅ Dynamic service registration
- ✅ Automatic service lookup
- ✅ No hardcoded URLs
- ✅ Load balancing ready

### 2. Health Monitoring
- ✅ Heartbeat mechanism (every 30 seconds)
- ✅ Automatic service removal if unhealthy
- ✅ Dashboard visualization
- ✅ Self-preservation mode

### 3. Microservice Communication
- ✅ Feign Client with service names
- ✅ Automatic retry on failure
- ✅ Circuit breaker ready
- ✅ Load balancing across instances

### 4. Scalability
- ✅ Run multiple instances of any service
- ✅ Automatic load distribution
- ✅ Zero configuration changes
- ✅ Horizontal scaling ready

## Troubleshooting

### Services not showing in Eureka
**Problem**: Service starts but doesn't appear in dashboard

**Solution**:
1. Check Eureka Server is running first
2. Wait 30 seconds for registration
3. Verify `eureka.client.service-url.defaultZone` in application.properties
4. Check service logs for registration errors

### Feign Client error: UnknownHostException
**Problem**: `java.net.UnknownHostException: user-service`

**Solution**:
1. Verify USER-SERVICE is UP in Eureka dashboard
2. Check service name matches: `spring.application.name=user-service`
3. Restart Book Service to refresh service registry
4. Verify `eureka.client.fetch-registry=true`

### Port already in use
**Problem**: `Address already in use: 8080`

**Solution**:
```bash
# Find process using the port
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Database connection error
**Problem**: `Connection refused to MySQL`

**Solution**:
1. Start MySQL Server
2. Create database: `CREATE DATABASE library_db;`
3. Check credentials in `application.properties`
4. Update password if needed

## Testing the Complete Flow

### Scenario: User Borrows a Book

```bash
# Step 1: Create a user
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Smith",
    "email": "alice@example.com",
    "membershipDate": "2024-01-15"
  }'
# Response: { "id": 1, "name": "Alice Smith", ... }

# Step 2: Add a book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "1984",
    "author": "George Orwell",
    "isbn": "978-0451524935",
    "availableCopies": 3
  }'
# Response: { "id": 1, "title": "1984", ... }

# Step 3: Borrow the book (Inter-service communication via Eureka!)
curl -X POST http://localhost:8080/api/books/1/borrow/1
# Book Service → Eureka → User Service (dynamic discovery!)
# Response: Book borrowed successfully!

# Step 4: Verify changes
curl http://localhost:8080/api/books/1
# availableCopies should be 2

curl http://localhost:8081/api/users/1
# borrowedBooks should be 1

# Step 5: Return the book
curl -X POST http://localhost:8080/api/books/1/return/1
# Response: Book returned successfully!
```

## Advanced: Running Multiple Instances

### Run Multiple User Service Instances
```bash
# Terminal 1 - Instance on 8081
cd userService
mvn spring-boot:run

# Terminal 2 - Instance on 8082
cd userService
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082

# Terminal 3 - Instance on 8083
cd userService
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8083
```

**Result**: Eureka shows 3 instances, Feign Client automatically load balances!

## Next Steps

1. **Add API Gateway**: Use Spring Cloud Gateway for routing
2. **Add Circuit Breaker**: Use Resilience4j for fault tolerance
3. **Add Distributed Tracing**: Use Zipkin for request tracing
4. **Add Config Server**: Centralize configuration
5. **Add Authentication**: Use Spring Security with OAuth2

## Documentation

- **Complete Setup**: [EUREKA_SETUP_GUIDE.md](./EUREKA_SETUP_GUIDE.md)
- **Architecture**: [ARCHITECTURE_DIAGRAMS.md](./ARCHITECTURE_DIAGRAMS.md)
- **Feign Client**: [FEIGN_CLIENT_GUIDE.md](./FEIGN_CLIENT_GUIDE.md)
- **Testing**: [TEST_RESULTS.md](./TEST_RESULTS.md)

## Summary

✅ **Eureka Server**: Service registry running on port 8761  
✅ **Book Service**: Registered as `book-service` on port 8080  
✅ **User Service**: Registered as `user-service` on port 8081  
✅ **Service Discovery**: Dynamic URL resolution via Eureka  
✅ **Load Balancing**: Ready for multiple instances  
✅ **Health Monitoring**: Automatic heartbeat and failover  

**You now have a production-ready microservices architecture!** 🎉

