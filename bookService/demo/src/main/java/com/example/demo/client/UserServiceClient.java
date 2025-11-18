package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.Map;

/**
 * UserServiceClient - Declarative REST Client using OpenFeign
 * 
 * 🎯 What is Feign?
 * Feign is a declarative REST client that makes HTTP calls look like Java method calls.
 * You just declare the interface - Spring generates the implementation automatically!
 * 
 * Before Feign (WebClient):
 * ──────────────────────────────────────────────────────────────
 * Map<String, Boolean> response = webClient
 *     .get()
 *     .uri("/api/users/{id}/can-borrow", userId)
 *     .retrieve()
 *     .bodyToMono(Map.class)
 *     .block();
 * return response.get("canBorrow");
 * ──────────────────────────────────────────────────────────────
 * 
 * With Feign:
 * ──────────────────────────────────────────────────────────────
 * Map<String, Boolean> response = userServiceClient.canUserBorrow(userId);
 * return response.get("canBorrow");
 * ──────────────────────────────────────────────────────────────
 * 
 * 📚 Learning: How Feign Works
 * 
 * 1. @FeignClient - Tells Spring to create HTTP client
 *    - name = Logical name for this client
 *    - url = Base URL of the target service
 * 
 * 2. Interface Methods - Map to HTTP endpoints
 *    - @GetMapping = HTTP GET request
 *    - @PutMapping = HTTP PUT request
 *    - @PathVariable = URL path parameter {id}
 * 
 * 3. Spring Auto-Implementation
 *    - At startup, Spring scans @FeignClient interfaces
 *    - Creates proxy implementation that makes HTTP calls
 *    - Handles serialization/deserialization automatically
 * 
 * SOLID Principles Applied:
 * 
 * 1. Single Responsibility (S): ⭐
 *    - ONLY defines User Service API contract
 *    - NO implementation details
 *    - NO business logic
 * 
 * 2. Interface Segregation (I): ⭐
 *    - Clean interface with only needed methods
 *    - Easy to mock for testing
 * 
 * 3. Dependency Inversion (D): ⭐
 *    - Code depends on interface, not implementation
 *    - Spring provides implementation at runtime
 * 
 * Microservices Communication Pattern:
 * 
 * ┌───────────────────────────────────────────────┐
 * │           Book Service (8080)                 │
 * │                                               │
 * │  BookController → BookService                 │
 * │                      ↓                        │
 * │         UserServiceClient (Feign) ────────────┼───► User Service (8081)
 * │                      ↓                        │         ↓
 * │           Spring-generated proxy              │     UserController
 * │                      ↓                        │         ↓
 * │                  HTTP GET/PUT  ◄──────────────┼─────UserService
 * │                      ↓                        │         ↓
 * │                 BookService                   │     UserRepository
 * │                      ↓                        │         ↓
 * │                 BookRepository                │      Database
 * └───────────────────────────────────────────────┘
 * 
 * Benefits of Feign:
 * 
 * ✅ Less Boilerplate - No manual HTTP client code
 * ✅ Type Safe - Compile-time checking
 * ✅ Easy Testing - Just mock the interface
 * ✅ Readable - Methods look like regular Java calls
 * ✅ Maintainable - Changes are simple and clear
 * ✅ Spring Integration - Works seamlessly with Spring Boot
/**
 * @FeignClient - Spring Cloud annotation
 * name = Service name in Eureka (matches spring.application.name in User Service)
 *
 * 🎯 Service Discovery Integration:
 * Instead of hardcoded URL (http://localhost:8081), we use service name.
 * Eureka automatically resolves "user-service" to actual instance(s).
 *
 * Benefits:
 * ✅ Dynamic discovery - No hardcoded URLs
 * ✅ Load balancing - Automatically distributes load across instances
 * ✅ Resilience - Automatically routes to healthy instances
 * ✅ Scalability - Add/remove instances without code changes
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    /**
     * Check if user can borrow more books
     * 
     * Feign Magic ✨:
     * - Spring sees @GetMapping annotation
     * - Generates HTTP GET request: GET http://localhost:8081/api/users/{id}/can-borrow
     * - Replaces {id} with userId parameter value
     * - Automatically deserializes JSON response to Map
     * 
     * API Call: GET http://localhost:8081/api/users/{userId}/can-borrow
     * 
     * Flow:
     * 1. BookService calls this method: canUserBorrow(123L)
     * 2. Feign proxy intercepts the call
     * 3. Makes HTTP GET: http://localhost:8081/api/users/123/can-borrow
     * 4. User Service responds: {"canBorrow": true}
     * 5. Feign deserializes JSON to Map
     * 6. Returns Map to BookService
     * 
     * @param userId User ID (automatically inserted into URL path)
     * @return Map containing {"canBorrow": true/false}
     * 
     * Example Response:
     * {
     *   "canBorrow": true
     * }
     */
    @GetMapping("/api/users/{id}/can-borrow")
    Map<String, Boolean> canUserBorrow(@PathVariable("id") Long userId);

    /**
     * Notify User Service that user borrowed a book
     * 
     * Feign generates: PUT http://localhost:8081/api/users/{id}/borrow
     * 
     * @param userId User ID
     * @return Updated user data
     */
    @PutMapping("/api/users/{id}/borrow")
    Map<String, Object> notifyBookBorrowed(@PathVariable("id") Long userId);

    /**
     * Notify User Service that user returned a book
     * 
     * Feign generates: PUT http://localhost:8081/api/users/{id}/return
     * 
     * @param userId User ID
     * @return Updated user data
     */
    @PutMapping("/api/users/{id}/return")
    Map<String, Object> notifyBookReturned(@PathVariable("id") Long userId);

    /**
     * Get user details
     * 
     * Feign generates: GET http://localhost:8081/api/users/{id}
     * 
     * @param userId User ID
     * @return User details
     */
    @GetMapping("/api/users/{id}")
    Map<String, Object> getUserDetails(@PathVariable("id") Long userId);


}
