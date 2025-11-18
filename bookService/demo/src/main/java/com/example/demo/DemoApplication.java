package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main Application Class
 * 
 * @SpringBootApplication - Combines @Configuration, @EnableAutoConfiguration, @ComponentScan
 * @EnableFeignClients - Enables Feign client scanning
 * @EnableDiscoveryClient - Enables service registration with Eureka Server
 *
 * 📚 Learning: What happens when application starts?
 * 
 * 1. Spring Boot scans all packages under com.example.demo
 * 2. Finds @FeignClient interface (UserServiceClient)
 * 3. Creates proxy implementation that makes HTTP calls
 * 4. Registers proxy as Spring bean
 * 5. Injects proxy into services that need it (BookServiceImpl)
 * 6. Registers this service with Eureka Server
 * 7. Fetches registry of other services from Eureka
 *
 * Magic ✨: You write interface, Spring generates HTTP client!
 * Magic ✨: Service auto-discovers other services via Eureka!
 */
@SpringBootApplication
@EnableFeignClients  // ⭐ Enable Feign client support
@EnableDiscoveryClient  // ⭐ Enable Eureka service discovery
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
