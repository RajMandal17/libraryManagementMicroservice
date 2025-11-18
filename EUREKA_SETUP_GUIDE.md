# Eureka Service Discovery - Complete Setup Guide

## Overview
This guide covers the complete integration of Netflix Eureka Server for service discovery in the Library Management Microservice System.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Eureka Server (8761)                     │
│                    Service Registry                          │
│                                                             │
│  Dashboard: http://localhost:8761                          │
│                                                             │
│  Registered Services:                                       │
│  • BOOK-SERVICE (localhost:8080)                           │
│  • USER-SERVICE (localhost:8081)                           │
└─────────────────────────────────────────────────────────────┘
                            ▲
                            │
                ┌───────────┴───────────┐
                │                       │
        Register & Heartbeat      Register & Heartbeat
                │                       │
                │                       │
┌───────────────▼─────────┐    ┌──────▼──────────────────┐
│   Book Service (8080)   │    │  User Service (8081)    │
│                         │    │                         │
│  • Registers with       │    │  • Registers with       │
│    Eureka               │    │    Eureka               │
│  • Discovers User       │    │  • Provides user        │
│    Service via Eureka   │    │    management APIs      │
│  • Uses Feign Client    │◄───┤                         │
│    with service name    │    │                         │
│                         │    │                         │
│  Feign: user-service    │    │  Spring Application:    │
│  (no hardcoded URL!)    │    │  user-service           │
└─────────────────────────┘    └─────────────────────────┘
```

## Services Configuration

### 1. Eureka Server (Port 8761)
**Purpose**: Central service registry
- All microservices register here
- Provides service discovery
- Offers web dashboard for monitoring

**Key Dependencies**:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

**Configuration** (`application.properties`):
```properties
spring.application.name=eureka-server
server.port=8761

# Don't register itself as a client
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false

# Self-preservation mode
eureka.server.enable-self-preservation=true
```

**Main Class**:
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### 2. Book Service (Port 8080)
**Purpose**: Manages books and borrowing transactions
**Service Name**: `book-service`

**Key Dependencies**:
```xml
<!-- Eureka Client -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<!-- Feign Client for inter-service communication -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**Configuration** (`application.properties`):
```properties
spring.application.name=book-service
server.port=8080

# Eureka Client Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true

# Instance Configuration
eureka.instance.prefer-ip-address=true
eureka.instance.hostname=localhost
```

**Main Class**:
```java
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

**Feign Client** (Service Discovery Integration):
```java
// Before: Hardcoded URL
@FeignClient(name = "user-service", url = "http://localhost:8081")

// After: Service Discovery
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/users/{id}/can-borrow")
    Map<String, Boolean> canUserBorrow(@PathVariable Long id);
    
    @PutMapping("/api/users/{id}/borrowed-books")
    void updateBorrowedBooksCount(@PathVariable Long id, @RequestBody Map<String, Integer> request);
}
```

### 3. User Service (Port 8081)
**Purpose**: Manages library users/members
**Service Name**: `user-service`

**Key Dependencies**:
```xml
<!-- Eureka Client -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<!-- Spring Cloud Dependency Management -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-dependencies</artifactId>
    <version>2023.0.0</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

**Configuration** (`application.properties`):
```properties
spring.application.name=user-service
server.port=8081

# Eureka Client Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true

# Instance Configuration
eureka.instance.prefer-ip-address=true
eureka.instance.hostname=localhost
```

**Main Class**:
```java
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

## Starting the Services

### Order of Startup
1. **Eureka Server** (must start first)
2. **User Service** (can start in any order)
3. **Book Service** (can start in any order)

### Commands

#### Terminal 1 - Eureka Server
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice/eureka-server
mvn spring-boot:run
```
Wait for: `Started EurekaServerApplication in X seconds`
Access: http://localhost:8761

#### Terminal 2 - User Service
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice/userService
mvn spring-boot:run
```
Wait for: `Started UserServiceApplication in X seconds`
Verify: Check Eureka dashboard - should see USER-SERVICE registered

#### Terminal 3 - Book Service
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice/bookService/demo
mvn spring-boot:run
```
Wait for: `Started DemoApplication in X seconds`
Verify: Check Eureka dashboard - should see BOOK-SERVICE registered

## Verification Steps

### 1. Check Eureka Dashboard
Open http://localhost:8761 in browser

**Expected Output**:
```
Instances currently registered with Eureka:
┌─────────────────┬──────────────┬────────┬───────────┐
│ Application     │ AMIs         │ Zone   │ Status    │
├─────────────────┼──────────────┼────────┼───────────┤
│ BOOK-SERVICE    │ n/a (1)      │ (1)    │ UP        │
│ USER-SERVICE    │ n/a (1)      │ (1)    │ UP        │
└─────────────────┴──────────────┴────────┴───────────┘
```

### 2. Check Service Logs
**Book Service** should show:
```
DiscoveryClient_BOOK-SERVICE - registration status: 204 (UP)
Fetched registry successfully from Eureka server
```

**User Service** should show:
```
DiscoveryClient_USER-SERVICE - registration status: 204 (UP)
Fetched registry successfully from Eureka server
```

### 3. Test Inter-Service Communication
```bash
# Test Book Service calling User Service via Eureka
curl -X POST http://localhost:8080/api/books/1/borrow/1
```

**Success Indicators**:
- Book Service logs show Feign client call to USER-SERVICE
- No hardcoded URL in the logs
- Service name resolution via Eureka
- Successful response

## How Service Discovery Works

### Registration Process
1. **Service Starts**: Book/User service starts up
2. **Registers with Eureka**: Sends registration request to http://localhost:8761/eureka/
3. **Heartbeat**: Sends heartbeat every 30 seconds (default)
4. **Stay Alive**: Eureka keeps service in registry if heartbeats continue

### Discovery Process
1. **Feign Call**: Book Service needs to call User Service
2. **Lookup**: Feign asks Eureka "Where is user-service?"
3. **Response**: Eureka returns: "localhost:8081"
4. **Connect**: Feign makes HTTP call to localhost:8081
5. **Cache**: Service locations cached locally for performance

### Service Health
- **UP**: Service is healthy and accepting requests
- **DOWN**: Service stopped or not responding to heartbeats
- **OUT_OF_SERVICE**: Service manually marked as unavailable

## Benefits of Service Discovery

### Before Eureka
```java
// Hardcoded URLs - problems:
@FeignClient(name = "user-service", url = "http://localhost:8081")

❌ Can't change ports without code change
❌ Can't run multiple instances
❌ Can't handle service failures
❌ Manual configuration for each environment
```

### After Eureka
```java
// Service name only - benefits:
@FeignClient(name = "user-service")

✅ Dynamic port assignment
✅ Multiple instances with load balancing
✅ Automatic failover to healthy instances
✅ Same code for dev/test/prod
✅ Auto-discovery of new instances
```

## Troubleshooting

### Services Not Showing in Eureka
**Problem**: Service starts but doesn't appear in Eureka dashboard

**Checks**:
1. Ensure Eureka Server is running first
2. Verify `eureka.client.service-url.defaultZone` is correct
3. Check service logs for registration errors
4. Confirm `@EnableDiscoveryClient` annotation present
5. Wait 30 seconds for registration to propagate

### Feign Client Can't Find Service
**Problem**: `java.net.UnknownHostException: user-service`

**Solution**:
1. Verify service name matches exactly: `spring.application.name=user-service`
2. Check service is UP in Eureka dashboard
3. Ensure `eureka.client.fetch-registry=true` in calling service
4. Restart Book Service to refresh registry cache

### Service Shows as DOWN
**Problem**: Service registered but status is DOWN

**Solution**:
1. Check if service is actually running
2. Verify network connectivity to Eureka
3. Look for firewall blocking heartbeat
4. Check for port conflicts

## Best Practices

### 1. Service Naming
- Use lowercase with hyphens: `book-service`, `user-service`
- Match `spring.application.name` exactly in Feign client
- Keep names descriptive and consistent

### 2. Health Checks
Add Spring Boot Actuator for better health monitoring:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 3. Multiple Instances
Run multiple instances on different ports:
```bash
# Instance 1
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080

# Instance 2
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

Eureka automatically load balances between them!

### 4. Production Considerations
- Run multiple Eureka servers for high availability
- Configure proper timeouts and retry policies
- Enable security (Spring Security + Eureka)
- Monitor with Spring Boot Admin or similar tools

## Configuration Reference

### Essential Eureka Client Properties
```properties
# Service name (MUST match in Feign clients)
spring.application.name=book-service

# Eureka Server URL
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Enable registration
eureka.client.register-with-eureka=true

# Enable discovery (fetch other services)
eureka.client.fetch-registry=true

# Use IP instead of hostname
eureka.instance.prefer-ip-address=true

# Instance hostname
eureka.instance.hostname=localhost
```

### Advanced Configuration
```properties
# Heartbeat interval (default: 30s)
eureka.instance.lease-renewal-interval-in-seconds=30

# Eviction timeout (default: 90s)
eureka.instance.lease-expiration-duration-in-seconds=90

# Registry fetch interval (default: 30s)
eureka.client.registry-fetch-interval-seconds=30

# Initial instance info replication interval
eureka.client.initial-instance-info-replication-interval-seconds=40
```

## Summary

### What We Achieved
✅ Created Eureka Server for service registry
✅ Registered Book Service with Eureka
✅ Registered User Service with Eureka
✅ Updated Feign Client to use service discovery
✅ Removed hardcoded URLs from configuration
✅ Enabled dynamic service discovery

### Key Files Modified
1. **eureka-server/** - New Eureka Server project
2. **bookService/demo/pom.xml** - Added Eureka Client dependency
3. **bookService/demo/src/main/resources/application.properties** - Added Eureka config
4. **bookService/demo/src/main/java/.../DemoApplication.java** - Added @EnableDiscoveryClient
5. **bookService/demo/src/main/java/.../client/UserServiceClient.java** - Removed hardcoded URL
6. **userService/pom.xml** - Added Eureka Client and Spring Cloud dependencies
7. **userService/src/main/resources/application.properties** - Added Eureka config
8. **userService/src/main/java/.../UserServiceApplication.java** - Added @EnableDiscoveryClient

### Service Ports
- **Eureka Server**: 8761
- **Book Service**: 8080
- **User Service**: 8081

### Next Steps
1. Start all three services in order
2. Verify registration in Eureka dashboard
3. Test inter-service communication
4. Explore load balancing with multiple instances
5. Consider adding API Gateway (Spring Cloud Gateway)

