package com.library.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * User Service Application - Microservice for managing library members
 * 
 * This service runs on port 8081
 * Book Service runs on port 8080
 * Eureka Server runs on port 8761
 *
 * @EnableDiscoveryClient - Enables service registration with Eureka Server
 */
@SpringBootApplication
@EnableDiscoveryClient  // ⭐ Enable Eureka service discovery
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
