package com.library.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Routes Configuration
 *
 * Configures routes programmatically for better control and documentation.
 * This provides an alternative to application.properties configuration.
 *
 * Route Components:
 * 1. ID - Unique identifier for the route
 * 2. URI - Destination (lb:// means load-balanced via Eureka)
 * 3. Predicates - Conditions to match requests
 * 4. Filters - Modifications to requests/responses
 *
 * Learning: Spring Cloud Gateway Features
 *
 * Load Balancing (lb://):
 *    - Automatically distributes load across instances
 *    - Uses Eureka to discover available instances
 *    - Built-in ribbon/spring-cloud-loadbalancer integration
 *
 * Path Predicates:
 *    - Match based on request path
 *    - Support patterns like /service/**
 *
 * Filters:
 *    - StripPrefix: Remove path segments before forwarding
 *    - AddRequestHeader: Add headers to requests
 *    - AddResponseHeader: Add headers to responses
 *    - CircuitBreaker: Handle failures gracefully
 *    - RateLimiter: Control request rates
 *
 * Example Flow:
 * Request: http://localhost:8080/book-service/api/books
 * Step 1: Gateway receives request
 * Step 2: Matches route: /book-service/**
 * Step 3: Applies StripPrefix(1): removes /book-service
 * Step 4: Asks Eureka: "Where is BOOK-SERVICE?"
 * Step 5: Eureka responds: "localhost:8081"
 * Step 6: Forwards to: http://localhost:8081/api/books
 * Step 7: Returns response to client
 */
@Configuration
public class GatewayConfig {

    /**
     * Define routes programmatically
     *
     * This bean is optional - routes can be configured in application.properties
     * However, Java configuration provides:
     * - Type safety
     * - Better IDE support
     * - Easier to add complex logic
     * - Easier to test
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route 1: Book Service
                .route("book-service-route", r -> r
                        .path("/book-service/**")
                        .filters(f -> f
                                .stripPrefix(1)  // Remove /book-service from path
                                .addResponseHeader("X-Gateway", "API-Gateway")  // Add custom header
                                .retry(3))  // Retry failed requests up to 3 times
                        .uri("lb://BOOK-SERVICE"))  // Load-balanced to BOOK-SERVICE

                // Route 2: User Service
                .route("user-service-route", r -> r
                        .path("/user-service/**")
                        .filters(f -> f
                                .stripPrefix(1)  // Remove /user-service from path
                                .addResponseHeader("X-Gateway", "API-Gateway")
                                .retry(3))
                        .uri("lb://USER-SERVICE"))  // Load-balanced to USER-SERVICE

                // Route 3: Eureka Dashboard
                .route("eureka-route", r -> r
                        .path("/eureka/**")
                        .uri("http://localhost:8761"))  // Direct to Eureka (no load balancing needed)

                .build();
    }
}

