# Project Guidelines — Library Management Microservices

This document captures project-specific knowledge to help advanced contributors build, test, and extend this repository efficiently. It focuses on the two Spring Boot microservices in this mono-repo and their integration.

Repository root: /Users/raj/IdeaProjects/libraryManagementMicroservice

Services:
- Book Service: bookService/demo (Spring Boot 3.4.x, Java 17, Spring Cloud OpenFeign 2024.0.x)
- User Service: userService (Spring Boot 3.2.x, Java 17)


## 1) Build and Configuration

General
- Java: 17 (enforced per-module via maven property <java.version>17</java.version>).
- Build tool: Maven (wrapper provided for Book Service: mvnw/mvnw.cmd; User Service expects mvn).
- Independent modules: build/run each service from its module directory. There is no root aggregator pom.

Book Service (bookService/demo)
- Ports and service identity
  - server.port=8080
  - spring.application.name=book-service
- Feign: Enabled via @EnableFeignClients in DemoApplication. Uses Spring Cloud BOM 2024.0.0.
- Databases
  - MySQL intended for normal runs (see application.properties). Defaults point to local MySQL:
    - spring.datasource.url=jdbc:mysql://localhost:3306/library_db?createDatabaseIfNotExist=true
    - spring.datasource.username=root
    - spring.datasource.password=
    - spring.jpa.hibernate.ddl-auto=update
  - H2 dependency is present for dev/testing with scope runtime; switch to H2 via profile or by overriding properties if needed.
- Actuator: spring-boot-starter-actuator included. Health endpoints can be used for smoke checks once exposed (default base path /actuator).
- Build commands
  - Clean and test: mvn -q -DskipTests=false clean test
  - Package: mvn -q -DskipTests package
  - Run: mvn spring-boot:run

User Service (userService)
- Ports and service identity
  - Default port expected: 8081 (check controller/README for endpoints). application.properties in this module sets server specifics if needed.
- Web stack: spring-boot-starter-web plus an additional webflux dependency used primarily for client side; controllers are MVC.
- Databases: MySQL runtime, H2 runtime available for tests.
- Build commands
  - Clean and test: mvn -q -DskipTests=false clean test
  - Package: mvn -q -DskipTests package
  - Run: mvn spring-boot:run

Environment and Profiles
- Default properties are geared for local development with MySQL running on localhost.
- If you do not have MySQL, either:
  - Provide a dockerized MySQL (e.g., docker run -p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=library_db mysql:8), or
  - Override datasource to H2 for local runs by adding an application-local.properties and starting with --spring.profiles.active=local, or by exporting env vars for spring.datasource.*.
- Logging is tuned for SQL visibility in Book Service:
  - logging.level.org.hibernate.SQL=DEBUG
  - logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
  Use these only in dev; reduce levels in production.

Inter-service communication
- Book Service depends on User Service via OpenFeign (see com.example.demo.client.UserServiceClient). Ensure User Service is up on 8081 when exercising endpoints that call the user service.


## 2) Testing

Test stack
- JUnit 5 via spring-boot-starter-test in both modules. The Book Service includes a contextLoads smoke test out of the box.

Per-module test execution
- From bookService/demo: mvn -q test
- From userService: mvn -q test

Running a single test class
- mvn -q -Dtest=com.example.demo.DemoApplicationTests test

Adding new tests (guidelines)
- Naming: Use *Test or *Tests suffix; package under src/test/java mirroring main package structure.
- Spring tests: Prefer @SpringBootTest for end-to-end context checks, and @WebMvcTest/@DataJpaTest for focused slices to improve speed.
- External calls: When testing Book Service logic that calls User Service, mock Feign clients using @MockBean for the interface (e.g., UserServiceClient) to avoid network calls in unit tests.
- Database: For repository tests, prefer @DataJpaTest with embedded database (H2). If switching to H2, ensure the correct dialect and ddl settings, or use @AutoConfigureTestDatabase.

Verified example (creation and execution)
- A simple JUnit 5 test was created temporarily in bookService/demo to demonstrate the process and executed successfully with mvn test, then removed to keep the repository clean. To reproduce locally, you can create a file like:

  src/test/java/com/example/demo/MathSmokeTest.java
  
  package com.example.demo;
  
  import org.junit.jupiter.api.Test;
  import static org.junit.jupiter.api.Assertions.*;
  
  class MathSmokeTest {
      @Test
      void addsNumbers() {
          assertEquals(4, 2 + 2);
      }
  }

- Run it: mvn -q test
- Scope run: mvn -q -Dtest=com.example.demo.MathSmokeTest test

Surefire/failsafe notes
- Only surefire is used (unit tests). There are no integration-test phases configured. Keep new unit tests under src/test/java and avoid naming patterns like *IT if you do not intend to run them in surefire.

CI considerations
- The project currently has no CI configuration in-repo. If adding CI, run tests per-module; there is no parent aggregator pom. For GitHub Actions, set working-directory to each module and cache ~/.m2.


## 3) Additional Development Notes

Code style and conventions
- Java 17, Lombok used for DTOs and entities; avoid business logic inside Lombok-annotated data holders.
- DTO vs Entity: Keep API DTOs in com.example.demo.dto and com.library.user.model.UserDto separate from entities to maintain validation and external contracts.
- Exceptions: Use custom exceptions (ResourceNotFoundException) and centralized handling via GlobalExceptionHandler.

Logging and diagnostics
- Prefer structured logs. For Feign-related issues, enable DEBUG for feign and web clients as needed:
  - logging.level.org.springframework.web=INFO or DEBUG
  - logging.level.feign=DEBUG
- For DB troubleshooting, keep Hibernate SQL logs enabled temporarily; revert to INFO+ in production.

Local integration
- Start order for manual testing:
  1) userService on 8081
  2) bookService on 8080
- Verify with curl:
  - curl http://localhost:8081/api/users/health
  - curl http://localhost:8080/api/books/health

Dependency and version alignment
- Book Service aligns with Spring Cloud 2024.0.0 for OpenFeign. If upgrading Spring Boot in User Service to 3.4.x, review Cloud BOM compatibility before bumping versions.

Common pitfalls
- MySQL not available: either run a container or switch to H2 for local testing. Failing to provide DB may cause contextLoads tests to fail if JPA tries to initialize against MySQL; use @DataJpaTest or property overrides to decouple tests from MySQL.
- Feign client bean not created: Ensure @EnableFeignClients is present and interfaces are under component scan (com.example.demo or specified basePackages).

Release packaging
- Each service builds its own fat jar under target/; run with java -jar target/<artifact>.jar. Ensure the corresponding service it depends on is running when performing end-to-end checks.
