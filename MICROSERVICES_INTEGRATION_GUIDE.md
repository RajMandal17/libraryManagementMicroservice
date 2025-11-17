# 🔗 Microservices Integration Guide

## 🎯 Goal

Connect Book Service (Port 8080) with User Service (Port 8081) so they can communicate.

---

## 📐 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Client                              │
│                    (Postman/Browser)                        │
└────────────┬────────────────────────┬───────────────────────┘
             │                        │
    ┌────────▼──────────┐    ┌────────▼──────────┐
    │  Book Service     │    │  User Service     │
    │  Port: 8080       │    │  Port: 8081       │
    │  Database: library_db  │  Database: user_db│
    └────────┬──────────┘    └────────┬──────────┘
             │                        │
             │   REST API Calls       │
             │  (WebClient/RestTemplate)
             │◄──────────────────────►│
             │                        │
    When user borrows book:          │
    1. Book Service receives request │
    2. Book Service calls User Service ────►
    3. User Service checks eligibility
    4. User Service returns YES/NO ◄────────
    5. Book Service updates book count
```

---

## 🛠️ Implementation Options

### Option 1: **RestTemplate** (Traditional, Simple)
- ✅ Easy to understand
- ✅ Synchronous (blocking)
- ❌ Deprecated in newer Spring Boot versions

### Option 2: **WebClient** (Modern, Recommended) ✅
- ✅ Non-blocking, reactive
- ✅ Better performance
- ✅ Recommended by Spring
- ✅ We'll use this!

---

## 📝 Step-by-Step Implementation

### **Step 1: Add WebClient Configuration to Book Service**

**File**: `src/main/java/com/example/demo/config/WebClientConfig.java`

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClientConfig - Configuration for inter-service communication
 * 
 * Creates WebClient bean to call User Service from Book Service
 */
@Configuration
public class WebClientConfig {

    /**
     * Create WebClient for User Service
     * 
     * Base URL: http://localhost:8081
     */
    @Bean
    public WebClient userServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081")  // User Service URL
                .build();
    }
}
```

---

### **Step 2: Create UserServiceClient (Book Service)**

This class handles all communication with User Service.

**File**: `src/main/java/com/example/demo/client/UserServiceClient.java`

```java
package com.example.demo.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * UserServiceClient - Handles communication with User Service
 * 
 * SOLID Principle: Single Responsibility
 * - ONLY handles User Service API calls
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient userServiceWebClient;

    /**
     * Check if user can borrow more books
     * 
     * Calls: GET http://localhost:8081/api/users/{userId}/can-borrow
     * 
     * @param userId User ID
     * @return true if user can borrow, false otherwise
     */
    public boolean canUserBorrowBooks(Long userId) {
        log.info("Checking if user {} can borrow books", userId);

        try {
            // Call User Service API
            Map<String, Boolean> response = userServiceWebClient
                    .get()
                    .uri("/api/users/{id}/can-borrow", userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();  // Blocking call (waits for response)

            boolean canBorrow = response != null && 
                    Boolean.TRUE.equals(response.get("canBorrow"));

            log.info("User {} borrow eligibility: {}", userId, canBorrow);
            return canBorrow;

        } catch (Exception e) {
            log.error("Error checking user eligibility: {}", e.getMessage());
            // If User Service is down, deny borrowing for safety
            return false;
        }
    }

    /**
     * Notify User Service that user borrowed a book
     * 
     * Calls: PUT http://localhost:8081/api/users/{userId}/borrow
     * 
     * @param userId User ID
     */
    public void notifyBookBorrowed(Long userId) {
        log.info("Notifying User Service: User {} borrowed a book", userId);

        try {
            userServiceWebClient
                    .put()
                    .uri("/api/users/{id}/borrow", userId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("User Service notified successfully");

        } catch (Exception e) {
            log.error("Error notifying User Service: {}", e.getMessage());
            // Log error but don't fail the borrow operation
            // Consider implementing compensation logic here
        }
    }

    /**
     * Notify User Service that user returned a book
     * 
     * Calls: PUT http://localhost:8081/api/users/{userId}/return
     * 
     * @param userId User ID
     */
    public void notifyBookReturned(Long userId) {
        log.info("Notifying User Service: User {} returned a book", userId);

        try {
            userServiceWebClient
                    .put()
                    .uri("/api/users/{id}/return", userId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("User Service notified successfully");

        } catch (Exception e) {
            log.error("Error notifying User Service: {}", e.getMessage());
        }
    }

    /**
     * Get user details from User Service
     * 
     * Calls: GET http://localhost:8081/api/users/{userId}
     * 
     * @param userId User ID
     * @return User object (as Map)
     */
    public Map<String, Object> getUserDetails(Long userId) {
        log.info("Fetching user details for user {}", userId);

        try {
            Map<String, Object> user = userServiceWebClient
                    .get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("User details fetched: {}", user);
            return user;

        } catch (Exception e) {
            log.error("Error fetching user details: {}", e.getMessage());
            return null;
        }
    }
}
```

---

### **Step 3: Update BookService Interface**

Add new methods that require user validation.

```java
package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.entity.BookDto;

import java.util.List;
import java.util.Optional;

public interface BookService {

    Book createBook(BookDto book);
    List<Book> getAllBooks();
    Optional<Book> getBookById(Long id);
    Book getBookByIsbn(String isbn);
    
    // ✅ New methods with user validation
    Book borrowBook(String isbn, Long userId);  // Added userId parameter
    Book returnBook(String isbn, Long userId);  // Added userId parameter
    
    List<Book> availableBooks();
    void deleteBook(Long id);
    Book updateBook(String isbn, BookDto bookDto);
    List<Book> getAllBookByAuthor(String author);
}
```

---

### **Step 4: Update BookServiceImpl with User Validation**

```java
package com.example.demo.serviceImpl;

import com.example.demo.client.UserServiceClient;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookDto;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BookRepository;
import com.example.demo.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserServiceClient userServiceClient;  // ✅ Inject UserServiceClient

    // ... other methods remain same ...

    @Override
    public Book borrowBook(String isbn, Long userId) {
        log.info("Processing borrow request for ISBN: {} by user: {}", isbn, userId);

        // Step 1: Check if user can borrow books (call User Service)
        boolean canBorrow = userServiceClient.canUserBorrowBooks(userId);
        
        if (!canBorrow) {
            log.error("User {} is not eligible to borrow books", userId);
            throw new IllegalStateException(
                "User is not eligible to borrow books. " +
                "Possible reasons: membership expired, max limit reached, or account suspended"
            );
        }

        // Step 2: Get book and check availability
        Book book = getBookByIsbn(isbn);

        if (book.getAvailableCopies() <= 0) {
            log.error("Book {} is out of stock", book.getTitle());
            throw new IllegalStateException("Book is currently out of stock");
        }

        // Step 3: Update book availability
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        Book updatedBook = bookRepository.save(book);

        // Step 4: Notify User Service (update user's borrowed count)
        userServiceClient.notifyBookBorrowed(userId);

        log.info("Book borrowed successfully: {}. User: {}. Remaining copies: {}",
                updatedBook.getTitle(), userId, updatedBook.getAvailableCopies());

        return updatedBook;
    }

    @Override
    public Book returnBook(String isbn, Long userId) {
        log.info("Processing return request for ISBN: {} by user: {}", isbn, userId);

        // Step 1: Get book
        Book book = getBookByIsbn(isbn);

        // Step 2: Check if all copies already returned
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            log.error("All copies of {} already returned", book.getTitle());
            throw new IllegalStateException("All copies are already returned");
        }

        // Step 3: Update book availability
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        Book updatedBook = bookRepository.save(book);

        // Step 4: Notify User Service (update user's borrowed count)
        userServiceClient.notifyBookReturned(userId);

        log.info("Book returned successfully: {}. User: {}. Available copies: {}",
                updatedBook.getTitle(), userId, updatedBook.getAvailableCopies());

        return updatedBook;
    }

    // ... rest of methods remain same ...
}
```

---

### **Step 5: Update BookController**

Update controller to accept userId parameter.

```java
/**
 * Borrow a book (with user validation)
 * PUT /api/books/{isbn}/borrow?userId={userId}
 */
@PutMapping("/{isbn}/borrow")
public ResponseEntity<Book> borrowBook(
        @PathVariable String isbn,
        @RequestParam Long userId) {  // ✅ Added userId as request parameter
    log.info("PUT /api/books/{}/borrow - User {} borrowing book", isbn, userId);
    Book book = bookService.borrowBook(isbn, userId);
    return ResponseEntity.ok(book);
}

/**
 * Return a book (with user validation)
 * PUT /api/books/{isbn}/return?userId={userId}
 */
@PutMapping("/{isbn}/return")
public ResponseEntity<Book> returnBook(
        @PathVariable String isbn,
        @RequestParam Long userId) {  // ✅ Added userId as request parameter
    log.info("PUT /api/books/{}/return - User {} returning book", isbn, userId);
    Book book = bookService.returnBook(isbn, userId);
    return ResponseEntity.ok(book);
}
```

---

## 🧪 Testing the Integration

### **Step 1: Start Both Services**

**Terminal 1** - Start User Service:
```bash
cd /home/devel-rajkumar/java/userService
mvn spring-boot:run
```

**Terminal 2** - Start Book Service:
```bash
cd /home/devel-rajkumar/java/springBootPracticeAssignment?/demo
mvn spring-boot:run
```

---

### **Step 2: Create a User (User Service)**

```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "membershipType": "REGULAR"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "membershipType": "REGULAR",
  "membershipStatus": "ACTIVE",
  "borrowedBooksCount": 0,
  "maxBooksAllowed": 5
}
```

---

### **Step 3: Create a Book (Book Service)**

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "978-0134685991",
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "totalCopies": 10,
    "availableCopies": 10
  }'
```

---

### **Step 4: Borrow Book (Integration Test)**

```bash
curl -X PUT "http://localhost:8080/api/books/978-0134685991/borrow?userId=1"
```

**What happens:**
1. Book Service receives request
2. Book Service calls User Service: `GET /api/users/1/can-borrow`
3. User Service checks eligibility → returns `{"canBorrow": true}`
4. Book Service updates book: `availableCopies: 10 → 9`
5. Book Service calls User Service: `PUT /api/users/1/borrow`
6. User Service updates user: `borrowedBooksCount: 0 → 1`

**Response:**
```json
{
  "id": 1,
  "isbn": "978-0134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "totalCopies": 10,
  "availableCopies": 9
}
```

---

### **Step 5: Verify User Updated (User Service)**

```bash
curl http://localhost:8081/api/users/1
```

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "borrowedBooksCount": 1,  ← Updated!
  "maxBooksAllowed": 5
}
```

---

### **Step 6: Return Book**

```bash
curl -X PUT "http://localhost:8080/api/books/978-0134685991/return?userId=1"
```

**What happens:**
1. Book: `availableCopies: 9 → 10`
2. User: `borrowedBooksCount: 1 → 0`

---

## 🔥 Advanced: Handling Service Failures

### **Problem: What if User Service is down?**

```java
public boolean canUserBorrowBooks(Long userId) {
    try {
        // Call User Service
        return userServiceWebClient.get()...block();
    } catch (Exception e) {
        log.error("User Service unavailable: {}", e.getMessage());
        
        // ✅ Fail-safe: Deny borrowing if service is down
        return false;
        
        // OR: Implement Circuit Breaker pattern (Resilience4j)
        // OR: Return cached data
        // OR: Allow borrowing with manual verification later
    }
}
```

---

## 📊 Communication Patterns Comparison

| Pattern | When to Use | Example |
|---------|-------------|---------|
| **Synchronous (WebClient.block())** | Simple operations, immediate response needed | Checking user eligibility |
| **Asynchronous (WebClient.subscribe())** | Non-blocking, fire-and-forget | Sending notifications |
| **Message Queue (RabbitMQ/Kafka)** | Decoupled, eventual consistency | Order processing, analytics |
| **API Gateway** | Single entry point for clients | Kong, Spring Cloud Gateway |

---

## 🚀 Next Steps

1. ✅ **Basic Integration** - Completed
2. 🔄 **Error Handling** - Add retry logic
3. 📊 **Monitoring** - Add distributed tracing (Spring Cloud Sleuth)
4. 🔒 **Security** - Add JWT authentication
5. 🐳 **Containerization** - Dockerize both services
6. ☁️ **Deployment** - Deploy to Kubernetes/AWS

---

## 📖 Summary

You now have two microservices that communicate via REST APIs:

- ✅ **User Service** (8081) - Manages users and memberships
- ✅ **Book Service** (8080) - Manages books and validates with User Service
- ✅ **WebClient** - Modern, non-blocking communication
- ✅ **SOLID Principles** - Maintainable, testable code

**Key Takeaways:**
- Microservices communicate via REST APIs
- Each service has its own database (database per service pattern)
- Services should handle failures gracefully
- Use WebClient for modern Spring Boot applications
