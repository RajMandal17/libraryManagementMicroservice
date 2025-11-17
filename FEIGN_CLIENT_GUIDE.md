# 🎯 Spring Cloud OpenFeign - Complete Learning Guide

## What is Feign?

**Feign** is a **declarative REST client** that makes writing HTTP clients as easy as writing Java interfaces. Instead of manually constructing HTTP requests, you simply define an interface with annotations, and Feign generates the implementation automatically!

---

## 📚 Why Use Feign Over WebClient?

### Before Feign (WebClient) ❌
```java
@Component
@RequiredArgsConstructor
public class UserServiceClient {
    private final WebClient webClient;
    
    public Map<String, Boolean> canUserBorrow(Long userId) {
        try {
            Map<String, Boolean> response = webClient
                .get()
                .uri("/api/users/{id}/can-borrow", userId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            return response;
        } catch (WebClientResponseException e) {
            // Manual error handling
            throw new RuntimeException(e);
        }
    }
    
    public Map<String, Object> notifyBookBorrowed(Long userId) {
        try {
            Map<String, Object> response = webClient
                .put()
                .uri("/api/users/{id}/borrow", userId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            return response;
        } catch (WebClientResponseException e) {
            // Manual error handling
            throw new RuntimeException(e);
        }
    }
}
```

**Problems:**
- ❌ Too much boilerplate code
- ❌ Manual error handling for each method
- ❌ Repetitive code for similar HTTP calls
- ❌ Need to configure WebClient separately
- ❌ Complex to test

### After Feign (Declarative) ✅
```java
@FeignClient(name = "user-service", url = "http://localhost:8081")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}/can-borrow")
    Map<String, Boolean> canUserBorrow(@PathVariable("id") Long userId);
    
    @PutMapping("/api/users/{id}/borrow")
    Map<String, Object> notifyBookBorrowed(@PathVariable("id") Long userId);
}
```

**Benefits:**
- ✅ Clean, concise code
- ✅ Auto-generated implementation
- ✅ Centralized error handling
- ✅ Easy to test (just mock the interface)
- ✅ Type-safe
- ✅ Spring Boot integration

---

## 🔧 How to Implement Feign

### Step 1: Add Dependencies

Add to `pom.xml`:

```xml
<properties>
    <java.version>17</java.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
</properties>

<dependencies>
    <!-- Spring Cloud OpenFeign -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
</dependencies>

<!-- Add Spring Cloud dependency management -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Step 2: Enable Feign Clients

Add `@EnableFeignClients` to your main application class:

```java
@SpringBootApplication
@EnableFeignClients  // ⭐ Enable Feign client scanning
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### Step 3: Create Feign Client Interface

```java
package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(
    name = "user-service",              // Logical name (for logging/metrics)
    url = "http://localhost:8081"       // Target service URL
)
public interface UserServiceClient {
    
    // HTTP GET
    @GetMapping("/api/users/{id}/can-borrow")
    Map<String, Boolean> canUserBorrow(@PathVariable("id") Long userId);
    
    // HTTP PUT
    @PutMapping("/api/users/{id}/borrow")
    Map<String, Object> notifyBookBorrowed(@PathVariable("id") Long userId);
    
    // HTTP POST with request body
    @PostMapping("/api/users/{id}/return")
    Map<String, Object> notifyBookReturned(@PathVariable("id") Long userId);
    
    // HTTP GET
    @GetMapping("/api/users/{id}")
    Map<String, Object> getUserDetails(@PathVariable("id") Long userId);
}
```

### Step 4: Use the Feign Client

Inject and use it like any Spring bean:

```java
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    
    private final UserServiceClient userServiceClient;  // ⭐ Injected by Spring
    
    @Override
    public Book borrowBook(String isbn, Long userId) {
        // Feign handles the HTTP call automatically!
        Map<String, Boolean> response = userServiceClient.canUserBorrow(userId);
        boolean canBorrow = response.get("canBorrow");
        
        if (!canBorrow) {
            throw new IllegalStateException("User cannot borrow books");
        }
        
        // ... rest of business logic
        
        // Notify User Service
        userServiceClient.notifyBookBorrowed(userId);
        
        return book;
    }
}
```

---

## 🎓 Learning: How Feign Works Internally

```
┌─────────────────────────────────────────────────────────────┐
│                  Application Startup                        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  1. @EnableFeignClients scans for @FeignClient interfaces   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  2. Spring creates PROXY implementation of the interface    │
│                                                             │
│     UserServiceClient (interface)                           │
│             ↓                                               │
│     UserServiceClient$$Proxy (generated by Spring)          │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  3. Proxy is registered as Spring Bean                      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Runtime Execution                         │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Your code calls:                                           │
│  userServiceClient.canUserBorrow(123L);                     │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Proxy intercepts the call                                  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Proxy reads annotations:                                   │
│  - @GetMapping("/api/users/{id}/can-borrow")               │
│  - @PathVariable("id")                                      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Constructs HTTP request:                                   │
│  GET http://localhost:8081/api/users/123/can-borrow         │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Sends HTTP request to User Service                         │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Receives JSON response:                                    │
│  {"canBorrow": true}                                        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Deserializes JSON → Map<String, Boolean>                   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Returns result to your code                                │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 Feign Annotations Explained

### @FeignClient

```java
@FeignClient(
    name = "user-service",           // Required: Logical name
    url = "http://localhost:8081",   // Required: Base URL
    configuration = FeignConfig.class // Optional: Custom configuration
)
```

**Attributes:**
- `name` - Service identifier (used in logs, metrics)
- `url` - Base URL of the target service
- `configuration` - Custom Feign configuration (timeouts, encoders, decoders)

### HTTP Method Annotations

| Annotation | HTTP Method | Example |
|------------|-------------|---------|
| `@GetMapping` | GET | `@GetMapping("/api/users/{id}")` |
| `@PostMapping` | POST | `@PostMapping("/api/users")` |
| `@PutMapping` | PUT | `@PutMapping("/api/users/{id}")` |
| `@PatchMapping` | PATCH | `@PatchMapping("/api/users/{id}")` |
| `@DeleteMapping` | DELETE | `@DeleteMapping("/api/users/{id}")` |

### Parameter Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@PathVariable` | URL path parameter | `@PathVariable("id") Long userId` |
| `@RequestParam` | Query parameter | `@RequestParam("page") int page` |
| `@RequestBody` | Request body | `@RequestBody UserDto user` |
| `@RequestHeader` | HTTP header | `@RequestHeader("Authorization") String token` |

---

## 🛠️ Advanced Feign Features

### 1. Custom Configuration

```java
@Configuration
public class FeignConfig {
    
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;  // Log all request/response details
    }
    
    @Bean
    public Request.Options options() {
        // Custom timeouts
        return new Request.Options(
            5000,  // Connect timeout (5 seconds)
            10000  // Read timeout (10 seconds)
        );
    }
}
```

### 2. Error Handling with ErrorDecoder

```java
public class CustomErrorDecoder implements ErrorDecoder {
    
    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 404:
                return new ResourceNotFoundException("Resource not found");
            case 403:
                return new UnauthorizedException("Access denied");
            case 500:
                return new ServiceUnavailableException("Service error");
            default:
                return new Exception("Generic error");
        }
    }
}
```

### 3. Fallback (Circuit Breaker Pattern)

```java
@FeignClient(
    name = "user-service",
    url = "http://localhost:8081",
    fallback = UserServiceClientFallback.class  // Fallback implementation
)
public interface UserServiceClient {
    @GetMapping("/api/users/{id}")
    Map<String, Object> getUserDetails(@PathVariable("id") Long userId);
}

@Component
public class UserServiceClientFallback implements UserServiceClient {
    @Override
    public Map<String, Object> getUserDetails(Long userId) {
        // Return default/cached data when service is down
        return Map.of("id", userId, "name", "Unknown", "status", "unavailable");
    }
}
```

### 4. Request Interceptors

```java
@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate template) {
        // Add authentication header to all requests
        template.header("Authorization", "Bearer " + getToken());
        
        // Add correlation ID
        template.header("X-Correlation-ID", UUID.randomUUID().toString());
    }
}
```

---

## 🎯 Best Practices

### 1. Use Interfaces, Not Classes
```java
// ✅ Good - Interface
@FeignClient(name = "user-service")
public interface UserServiceClient { }

// ❌ Bad - Class
@FeignClient(name = "user-service")
public class UserServiceClient { }
```

### 2. Separate DTOs for Requests/Responses
```java
// ✅ Good - Type-safe DTOs
@PostMapping("/api/users")
UserResponse createUser(@RequestBody CreateUserRequest request);

// ❌ Bad - Generic Map
@PostMapping("/api/users")
Map<String, Object> createUser(@RequestBody Map<String, Object> data);
```

### 3. Use Meaningful Client Names
```java
// ✅ Good - Descriptive
@FeignClient(name = "user-management-service")

// ❌ Bad - Generic
@FeignClient(name = "service1")
```

### 4. Configure Timeouts
```java
// Always configure timeouts to prevent hanging
@Bean
public Request.Options options() {
    return new Request.Options(5000, 10000);
}
```

### 5. Enable Logging in Development
```yaml
# application.yml
logging:
  level:
    com.example.demo.client.UserServiceClient: DEBUG

feign:
  client:
    config:
      default:
        loggerLevel: FULL
```

---

## 🧪 Testing Feign Clients

### Unit Test with Mockito
```java
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    
    @Mock
    private UserServiceClient userServiceClient;
    
    @Mock
    private BookRepository bookRepository;
    
    @InjectMocks
    private BookServiceImpl bookService;
    
    @Test
    void borrowBook_Success() {
        // Arrange
        Long userId = 1L;
        String isbn = "978-0134685991";
        
        // Mock Feign client response
        when(userServiceClient.canUserBorrow(userId))
            .thenReturn(Map.of("canBorrow", true));
        
        when(bookRepository.findByIsbn(isbn))
            .thenReturn(Optional.of(createTestBook()));
        
        // Act
        Book result = bookService.borrowBook(isbn, userId);
        
        // Assert
        assertNotNull(result);
        verify(userServiceClient).canUserBorrow(userId);
        verify(userServiceClient).notifyBookBorrowed(userId);
    }
}
```

### Integration Test with WireMock
```java
@SpringBootTest
@AutoConfigureWireMock(port = 8081)
class UserServiceClientIntegrationTest {
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Test
    void canUserBorrow_ReturnsTrue() {
        // Setup mock HTTP server
        stubFor(get(urlEqualTo("/api/users/1/can-borrow"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"canBorrow\":true}")));
        
        // Test
        Map<String, Boolean> response = userServiceClient.canUserBorrow(1L);
        
        // Verify
        assertTrue(response.get("canBorrow"));
    }
}
```

---

## 📊 Comparison: Feign vs Other HTTP Clients

| Feature | Feign | RestTemplate | WebClient |
|---------|-------|--------------|-----------|
| **Declarative** | ✅ Yes | ❌ No | ❌ No |
| **Boilerplate** | ✅ Minimal | ❌ High | ⚠️ Medium |
| **Type Safety** | ✅ Strong | ⚠️ Weak | ⚠️ Medium |
| **Non-Blocking** | ❌ No | ❌ No | ✅ Yes |
| **Testing** | ✅ Easy | ⚠️ Medium | ⚠️ Medium |
| **Spring Boot 3** | ✅ Supported | ⚠️ Deprecated | ✅ Recommended |
| **Learning Curve** | ✅ Easy | ✅ Easy | ⚠️ Medium |

**Verdict:**
- **Feign** - Best for synchronous microservice calls (most common)
- **WebClient** - Use for reactive/non-blocking applications
- **RestTemplate** - Deprecated, avoid in new projects

---

## 🚀 Summary

### What We Learned:

1. **Feign is a declarative REST client** - Write interfaces, get HTTP clients
2. **Reduces boilerplate** - No manual HTTP request construction
3. **Type-safe** - Compile-time checking of API calls
4. **Easy to test** - Just mock the interface
5. **Spring Boot integration** - Works seamlessly with dependency injection
6. **Production-ready** - Supports timeouts, retries, fallbacks, logging

### Key Takeaways:

✅ Feign makes microservice communication simple and maintainable  
✅ Use `@FeignClient` on interfaces to auto-generate HTTP clients  
✅ Add `@EnableFeignClients` to enable scanning  
✅ Spring handles all HTTP details automatically  
✅ Perfect for synchronous service-to-service calls  

---

## 📚 Further Reading

- [Spring Cloud OpenFeign Documentation](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/)
- [Feign GitHub Repository](https://github.com/OpenFeign/feign)
- [Microservices Design Patterns](https://microservices.io/patterns/index.html)

---

**Happy Learning! 🎉**
