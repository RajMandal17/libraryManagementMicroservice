# API Gateway - Library Management System

## Overview
Spring Cloud Gateway provides a single entry point for all microservices in the Library Management System. It routes requests to appropriate services, provides load balancing, and handles cross-cutting concerns.

## Purpose
- **Single Entry Point**: All client requests go through one gateway
- **Service Discovery**: Automatic routing via Eureka
- **Load Balancing**: Distributes load across service instances
- **Cross-Cutting Concerns**: Logging, security, rate limiting (centralized)

## Architecture

```
                    Client Application
                          │
                          ▼
                 ┌─────────────────┐
                 │  API Gateway    │  Port: 8080
                 │  (Entry Point)  │
                 └────────┬────────┘
                          │
              ┌───────────┼───────────┐
              │           │           │
       /book-service  /user-service  /eureka
              │           │           │
              ▼           ▼           ▼
        ┌─────────┐ ┌──────────┐ ┌─────────┐
        │  Book   │ │   User   │ │ Eureka  │
        │ Service │ │ Service  │ │ Server  │
        │  8081   │ │  8082    │ │  8761   │
        └─────────┘ └──────────┘ └─────────┘
```

## Configuration

### Port
- **Gateway Port**: 8080
- **Eureka**: 8761
- **Book Service**: Changed to 8081 (was 8080)
- **User Service**: Changed to 8082 (was 8081)

### Routes

| Route Pattern | Target Service | Example |
|---------------|----------------|---------|
| `/book-service/**` | BOOK-SERVICE (via Eureka) | `http://localhost:8080/book-service/api/books` |
| `/user-service/**` | USER-SERVICE (via Eureka) | `http://localhost:8080/user-service/api/users` |
| `/eureka/**` | Eureka Server | `http://localhost:8080/eureka/web` |

## How It Works

### Request Flow
```
1. Client sends request to Gateway
   → http://localhost:8080/book-service/api/books

2. Gateway matches route pattern
   → /book-service/** matched

3. Gateway applies filters
   → StripPrefix(1): removes /book-service
   → Adds response header: X-Gateway

4. Gateway queries Eureka
   → "Where is BOOK-SERVICE?"
   → Eureka: "localhost:8081"

5. Gateway forwards request
   → http://localhost:8081/api/books

6. Backend service processes

7. Gateway returns response to client
```

### Load Balancing
```
Multiple Instances:
┌──────────────┐
│ Book Service │ → Instance 1 (8081)
│ Book Service │ → Instance 2 (8083)
│ Book Service │ → Instance 3 (8084)
└──────────────┘

Gateway automatically distributes requests across all instances!
```

## Features

### 1. Service Discovery Integration
- Automatically discovers services from Eureka
- No hardcoded service URLs
- Dynamic routing based on service availability

### 2. Load Balancing
- Uses `lb://` scheme for load-balanced routing
- Distributes requests across multiple instances
- Built-in health checking

### 3. Path Rewriting
- `StripPrefix` filter removes path prefix
- Example: `/book-service/api/books` → `/api/books`

### 4. Global Filters
- **LoggingFilter**: Logs all requests/responses
- Captures request method, path, client IP
- Measures response time
- Easy to add authentication, rate limiting, etc.

### 5. Retry Mechanism
- Automatically retries failed requests
- Configured for 3 retries per route
- Improves resilience

### 6. CORS Support
- Global CORS configuration
- Allows cross-origin requests
- Configurable per route if needed

## Running the Gateway

### Prerequisites
1. Eureka Server must be running (port 8761)
2. Book Service must be registered with Eureka
3. User Service must be registered with Eureka

### Start Gateway
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice/api-gateway
mvn spring-boot:run
```

### Verify
```bash
# Check gateway health
curl http://localhost:8080/actuator/health

# List all routes
curl http://localhost:8080/actuator/gateway/routes

# Check Eureka dashboard through gateway
open http://localhost:8080/eureka/web
```

## Usage Examples

### Before Gateway (Direct Service Calls)
```bash
# Direct to Book Service
curl http://localhost:8081/api/books

# Direct to User Service  
curl http://localhost:8082/api/users
```

### After Gateway (Through API Gateway)
```bash
# Via Gateway to Book Service
curl http://localhost:8080/book-service/api/books

# Via Gateway to User Service
curl http://localhost:8080/user-service/api/users
```

### Complete Workflow
```bash
# 1. Create user via gateway
curl -X POST http://localhost:8080/user-service/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@test.com","membershipDate":"2024-01-01"}'

# 2. Add book via gateway
curl -X POST http://localhost:8080/book-service/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"1984","author":"Orwell","isbn":"123","availableCopies":3}'

# 3. Borrow book via gateway
curl -X POST http://localhost:8080/book-service/api/books/1/borrow/1
```

## Benefits

### For Clients
✅ **Single URL** - Only need to know gateway address  
✅ **Simplified** - No need to track individual service URLs  
✅ **Consistent** - Same base URL for all services  

### For Operations
✅ **Centralized Monitoring** - All traffic goes through one point  
✅ **Easy Security** - Apply authentication once at gateway  
✅ **Rate Limiting** - Control traffic at gateway level  
✅ **Logging** - Comprehensive request/response logging  

### For Scaling
✅ **Load Balancing** - Automatic across instances  
✅ **Service Discovery** - Services can move/scale freely  
✅ **Health Checking** - Only routes to healthy instances  

## Configuration Details

### application.properties
```properties
# Gateway runs on 8080
server.port=8080

# Register with Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Routes defined in properties (alternative to Java config)
spring.cloud.gateway.routes[0].id=book-service
spring.cloud.gateway.routes[0].uri=lb://BOOK-SERVICE
spring.cloud.gateway.routes[0].predicates[0]=Path=/book-service/**
spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1
```

### GatewayConfig.java (Alternative)
```java
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("book-service-route", r -> r
            .path("/book-service/**")
            .filters(f -> f.stripPrefix(1).retry(3))
            .uri("lb://BOOK-SERVICE"))
        .build();
}
```

## Actuator Endpoints

| Endpoint | Purpose | URL |
|----------|---------|-----|
| Health | Gateway health status | `http://localhost:8080/actuator/health` |
| Routes | List all configured routes | `http://localhost:8080/actuator/gateway/routes` |
| Filters | List all global filters | `http://localhost:8080/actuator/gateway/globalfilters` |

## Troubleshooting

### Gateway can't find service
**Problem**: 503 Service Unavailable

**Solution**:
1. Check service is registered in Eureka: http://localhost:8761
2. Verify service name matches route URI
3. Check Eureka client is enabled in gateway
4. Wait 30 seconds for service registration

### Route not matching
**Problem**: 404 Not Found

**Solution**:
1. Check route pattern in configuration
2. Verify path includes service prefix: `/book-service/`
3. List all routes: `curl http://localhost:8080/actuator/gateway/routes`
4. Check logs for route matching details

### CORS errors
**Problem**: Cross-origin request blocked

**Solution**:
1. Check global CORS configuration
2. Verify allowed origins includes your client
3. Ensure preflight OPTIONS requests work

## Next Steps

### Immediate
1. Start gateway and test routes
2. Verify load balancing with multiple instances
3. Check logging output
4. Test via Postman/curl

### Future Enhancements
1. **Circuit Breaker** - Add Resilience4j for fault tolerance
2. **Rate Limiting** - Add Redis-based rate limiter
3. **Authentication** - Add OAuth2/JWT validation
4. **Request/Response Transformation** - Modify headers/body
5. **API Documentation** - Add Swagger/OpenAPI aggregation
6. **Metrics** - Add Prometheus/Grafana integration
7. **Distributed Tracing** - Add Zipkin/Sleuth

## Key Files

```
api-gateway/
├── pom.xml                                  # Dependencies
├── src/main/java/com/library/gateway/
│   ├── ApiGatewayApplication.java          # Main class
│   ├── config/
│   │   └── GatewayConfig.java              # Route configuration
│   └── filter/
│       └── LoggingFilter.java              # Global logging filter
└── src/main/resources/
    └── application.properties               # Configuration
```

## Summary

The API Gateway provides:
- ✅ Single entry point (localhost:8080)
- ✅ Automatic service discovery
- ✅ Load balancing across instances
- ✅ Request/response logging
- ✅ Retry mechanism for resilience
- ✅ CORS support
- ✅ Easy to extend with filters

**All client applications should now connect to the gateway (8080) instead of individual services!**

