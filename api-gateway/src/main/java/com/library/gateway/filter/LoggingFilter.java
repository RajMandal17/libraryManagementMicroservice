package com.library.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global Logging Filter
 *
 * Logs all requests passing through the gateway.
 * This is a Global Filter - applies to ALL routes automatically.
 *
 * Purpose:
 * - Request/Response logging
 * - Performance monitoring
 * - Debugging
 * - Audit trail
 *
 * Learning: Gateway Filters
 *
 * Types of Filters:
 * 1. Global Filters - Apply to all routes (like this one)
 * 2. Route-specific Filters - Apply to specific routes
 *
 * Filter Order:
 * - Implements Ordered interface
 * - Lower number = higher priority
 * - Executes before routing to backend service
 *
 * Reactive Programming:
 * - Uses Reactor (Mono/Flux)
 * - Non-blocking I/O
 * - Better performance under load
 *
 * Flow:
 * 1. Request arrives
 * 2. Filter captures request details
 * 3. Logs request information
 * 4. Passes to next filter/route
 * 5. Backend processes request
 * 6. Response returns
 * 7. Logs response information
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    /**
     * Filter logic executed for every request
     *
     * @param exchange - Contains request and response
     * @param chain - Chain of filters
     * @return Mono<Void> - Reactive completion signal
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Capture request start time
        long startTime = System.currentTimeMillis();

        // Extract request details
        String requestPath = exchange.getRequest().getPath().value();
        String requestMethod = exchange.getRequest().getMethod().toString();
        String clientIp = exchange.getRequest().getRemoteAddress() != null
            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
            : "unknown";

        // Log incoming request
        log.info("==> Incoming Request: {} {} from IP: {}",
                requestMethod, requestPath, clientIp);

        // Continue filter chain and handle response
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Calculate request processing time
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Extract response details
            int statusCode = exchange.getResponse().getStatusCode() != null
                ? exchange.getResponse().getStatusCode().value()
                : 0;

            // Log response
            log.info("<== Response: {} {} - Status: {} - Duration: {}ms",
                    requestMethod, requestPath, statusCode, duration);
        }));
    }

    /**
     * Define filter order
     * Lower values have higher priority
     *
     * @return -1 to execute early in the filter chain
     */
    @Override
    public int getOrder() {
        return -1;  // Execute before other filters
    }
}

