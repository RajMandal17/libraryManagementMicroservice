# Eureka Server - Service Discovery

## Overview
Eureka Server is a service registry that enables microservices to find and communicate with each other dynamically. It's part of Netflix OSS and integrated with Spring Cloud.

## Purpose
- **Service Registration**: All microservices register themselves with Eureka
- **Service Discovery**: Services can discover other services by name
- **Load Balancing**: Automatically distributes load across service instances
- **Health Monitoring**: Tracks the health status of registered services

## Configuration
- **Port**: 8761 (default Eureka port)
- **Dashboard**: http://localhost:8761
- **Self-Registration**: Disabled (server doesn't register itself)

## Running the Server

```bash
cd eureka-server
mvn spring-boot:run
```

Or run the JAR:
```bash
mvn clean package
java -jar target/eureka-server-0.0.1-SNAPSHOT.jar
```

## Accessing the Dashboard
Once started, access the Eureka Dashboard at:
```
http://localhost:8761
```

The dashboard shows:
- All registered services
- Number of instances per service
- Health status
- Service metadata

## Registered Services
After starting all services, you should see:
1. **BOOK-SERVICE** (Port 8080)
2. **USER-SERVICE** (Port 8081)

## Key Features
1. **Self-Preservation Mode**: Protects against network partition issues
2. **Heartbeat Mechanism**: Services send heartbeats every 30 seconds
3. **Eviction**: Services not sending heartbeats are removed after 90 seconds
4. **Registry Replication**: Supports multiple Eureka servers (not configured here)

## Properties Explained
```properties
eureka.client.register-with-eureka=false
# Server doesn't register itself as a client

eureka.client.fetch-registry=false
# Server doesn't fetch registry (it IS the registry)

eureka.server.enable-self-preservation=true
# Protects against false positives in service removal
```

## Integration with Other Services
Both Book Service and User Service are configured as Eureka clients and will:
1. Register themselves on startup
2. Send heartbeats every 30 seconds
3. Discover each other through Eureka
4. Use service names instead of hardcoded URLs

