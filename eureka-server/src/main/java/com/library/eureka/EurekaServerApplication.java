package com.library.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server - Service Discovery Server
 *
 * This is the central registry where all microservices register themselves.
 * Other services can discover and communicate with each other through this server.
 *
 * Runs on port 8761 by default
 * Dashboard available at: http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer  // Enable Eureka Server functionality
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

