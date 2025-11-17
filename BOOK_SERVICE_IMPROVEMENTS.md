# 🔧 Book Service SOLID Improvements

## ❌ Issues Found in Current Book Service

### 1. **Controller Issues** (`bookController.java`)

```java
@Autowired
private BookServiceImpe bookServiceImpe;  // ❌ Depends on concrete class
```

**Problems:**
- ❌ Violates Dependency Inversion Principle
- ❌ Controller depends on implementation, not interface
- ❌ Harder to test (cannot mock easily)

**Solution:**
```java
private final BookService bookService;  // ✅ Depends on interface

@Autowired
public BookController(BookService bookService) {
    this.bookService = bookService;
}
```

---

### 2. **Service Implementation Issues** (`BookServiceImpe.java`)

**Problem #1: Lombok Builder not used correctly**
```java
book.builder().availableCopies(book.getAvailableCopies() - 1);  // ❌ Doesn't work!
return bookRepository.save(book);
```

**Why this fails:**
- `builder()` creates a NEW builder, doesn't modify existing object
- Changes are lost!

**Solution:**
```java
book.setAvailableCopies(book.getAvailableCopies() - 1);  // ✅ Use setter
return bookRepository.save(book);
```

---

**Problem #2: Missing Exception Handling**
```java
Book book = bookRepository.findByIsbn(isbn);
// What if book is null? ❌
book.getAvailableCopies();  // NullPointerException!
```

**Solution:**
```java
Book book = bookRepository.findByIsbn(isbn);
if (book == null) {
    throw new ResourceNotFoundException("Book not found with ISBN: " + isbn);
}
```

---

**Problem #3: Inconsistent Exception Types**
```java
throw new RuntimeException("out side book");  // ❌ Generic exception
```

**Solution:**
```java
throw new IllegalStateException("All copies already returned");  // ✅ Specific exception
```

---

## ✅ IMPROVED Book Service Files

### 📄 Improved `BookController.java`

```java
package com.example.demo.controller;

import com.example.demo.entity.Book;
import com.example.demo.entity.BookDto;
import com.example.demo.service.BookService;  // ✅ Interface, not implementation
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BookController - REST API for Book operations
 * 
 * SOLID Principles Applied:
 * 1. Single Responsibility: Only handles HTTP requests/responses
 * 2. Dependency Inversion: Depends on BookService interface, not implementation
 */
@RestController
@RequestMapping("/api/books")  // ✅ Changed to plural "books"
@RequiredArgsConstructor  // ✅ Lombok generates constructor
@Slf4j
public class BookController {

    // ✅ Dependency Inversion: Depends on interface
    private final BookService bookService;

    /**
     * Create a new book
     * POST /api/books
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Book> createBook(@Valid @RequestBody BookDto bookDto) {
        log.info("POST /api/books - Creating book: {}", bookDto.getTitle());
        Book createdBook = bookService.createBook(bookDto);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    /**
     * Get all books
     * GET /api/books
     */
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        log.info("GET /api/books - Fetching all books");
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Get book by ID
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        log.info("GET /api/books/{} - Fetching book", id);
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return ResponseEntity.ok(book);
    }

    /**
     * Get book by ISBN
     * GET /api/books/isbn/{isbn}
     */
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        log.info("GET /api/books/isbn/{} - Fetching book", isbn);
        Book book = bookService.getBookByIsbn(isbn);
        return ResponseEntity.ok(book);
    }

    /**
     * Get books by author
     * GET /api/books/author/{author}
     */
    @GetMapping("/author/{author}")
    public ResponseEntity<List<Book>> getBooksByAuthor(@PathVariable String author) {
        log.info("GET /api/books/author/{} - Fetching books", author);
        List<Book> books = bookService.getAllBookByAuthor(author);
        return ResponseEntity.ok(books);
    }

    /**
     * Get available books
     * GET /api/books/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        log.info("GET /api/books/available - Fetching available books");
        List<Book> books = bookService.availableBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Borrow a book
     * PUT /api/books/{isbn}/borrow
     */
    @PutMapping("/{isbn}/borrow")
    public ResponseEntity<Book> borrowBook(@PathVariable String isbn) {
        log.info("PUT /api/books/{}/borrow - Borrowing book", isbn);
        Book book = bookService.borrowBook(isbn);
        return ResponseEntity.ok(book);
    }

    /**
     * Return a book
     * PUT /api/books/{isbn}/return
     */
    @PutMapping("/{isbn}/return")
    public ResponseEntity<Book> returnBook(@PathVariable String isbn) {
        log.info("PUT /api/books/{}/return - Returning book", isbn);
        Book book = bookService.returnBook(isbn);
        return ResponseEntity.ok(book);
    }

    /**
     * Update book details
     * PUT /api/books/{isbn}
     */
    @PutMapping("/{isbn}")
    public ResponseEntity<Book> updateBook(
            @PathVariable String isbn,
            @Valid @RequestBody BookDto bookDto) {
        log.info("PUT /api/books/{} - Updating book", isbn);
        Book updatedBook = bookService.updateBook(isbn, bookDto);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Delete a book
     * DELETE /api/books/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBook(@PathVariable Long id) {
        log.info("DELETE /api/books/{} - Deleting book", id);
        bookService.deleteBook(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Book deleted successfully");
        response.put("bookId", id.toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check
     * GET /api/books/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Book Service");
        response.put("port", "8080");
        return ResponseEntity.ok(response);
    }
}
```

---

### 📄 Improved `BookServiceImpl.java`

```java
package com.example.demo.serviceImpl;

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

/**
 * BookServiceImpl - Implementation of BookService
 * 
 * SOLID Principles Applied:
 * 1. Single Responsibility: Only handles book business logic
 * 2. Open/Closed: Can extend by creating new implementations
 * 3. Dependency Inversion: Depends on BookRepository interface
 */
@Service
@RequiredArgsConstructor  // ✅ Constructor injection
@Slf4j
@Transactional
public class BookServiceImpl implements BookService {

    // ✅ Dependency Injection via constructor
    private final BookRepository bookRepository;

    @Override
    public Book createBook(BookDto bookDto) {
        log.info("Creating book with ISBN: {}", bookDto.getIsbn());

        // Business Rule 1: ISBN must be unique
        if (bookRepository.existsByIsbn(bookDto.getIsbn())) {
            log.error("Book with ISBN {} already exists", bookDto.getIsbn());
            throw new IllegalArgumentException(
                "Book with ISBN " + bookDto.getIsbn() + " already exists"
            );
        }

        // Business Rule 2: Available copies cannot exceed total copies
        if (bookDto.getAvailableCopies() > bookDto.getTotalCopies()) {
            log.error("Available copies ({}) cannot exceed total copies ({})",
                    bookDto.getAvailableCopies(), bookDto.getTotalCopies());
            throw new IllegalArgumentException(
                "Available copies must not exceed total copies"
            );
        }

        // Build and save book
        Book book = Book.builder()
                .isbn(bookDto.getIsbn())
                .title(bookDto.getTitle())
                .author(bookDto.getAuthor())
                .totalCopies(bookDto.getTotalCopies())
                .availableCopies(bookDto.getAvailableCopies())
                .build();

        Book savedBook = bookRepository.save(book);
        log.info("Book created successfully: {}", savedBook.getTitle());

        return savedBook;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        log.info("Fetching all books");
        List<Book> books = bookRepository.findAll();
        log.info("Found {} books", books.size());
        return books;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> getBookById(Long id) {
        log.info("Fetching book with ID: {}", id);
        return bookRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Book getBookByIsbn(String isbn) {
        log.info("Fetching book with ISBN: {}", isbn);
        Book book = bookRepository.findByIsbn(isbn);
        
        // ✅ Proper null check
        if (book == null) {
            log.error("Book not found with ISBN: {}", isbn);
            throw new ResourceNotFoundException("Book not found with ISBN: " + isbn);
        }
        
        return book;
    }

    @Override
    public Book borrowBook(String isbn) {
        log.info("Processing borrow request for ISBN: {}", isbn);

        Book book = getBookByIsbn(isbn);  // ✅ Reuses method with null check

        // Business Rule: Must have available copies
        if (book.getAvailableCopies() <= 0) {
            log.error("Book {} is out of stock", book.getTitle());
            throw new IllegalStateException("Book is currently out of stock");
        }

        // ✅ Use setter, not builder
        book.setAvailableCopies(book.getAvailableCopies() - 1);

        Book updatedBook = bookRepository.save(book);
        log.info("Book borrowed: {}. Remaining copies: {}",
                updatedBook.getTitle(), updatedBook.getAvailableCopies());

        return updatedBook;
    }

    @Override
    public Book returnBook(String isbn) {
        log.info("Processing return request for ISBN: {}", isbn);

        Book book = getBookByIsbn(isbn);  // ✅ Reuses method with null check

        // Business Rule: Cannot return more than total copies
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            log.error("All copies of {} already returned", book.getTitle());
            throw new IllegalStateException("All copies are already returned");
        }

        // ✅ Use setter, not builder
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        Book updatedBook = bookRepository.save(book);
        log.info("Book returned: {}. Available copies: {}",
                updatedBook.getTitle(), updatedBook.getAvailableCopies());

        return updatedBook;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> availableBooks() {
        log.info("Fetching available books");
        List<Book> books = bookRepository.findByAvailableCopiesGreaterThan(0);
        log.info("Found {} available books", books.size());
        return books;
    }

    @Override
    public void deleteBook(Long id) {
        log.info("Deleting book with ID: {}", id);
        
        // ✅ Check if book exists before deleting
        Book book = getBookById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        
        bookRepository.delete(book);
        log.info("Book deleted: {}", book.getTitle());
    }

    @Override
    public Book updateBook(String isbn, BookDto bookDto) {
        log.info("Updating book with ISBN: {}", isbn);

        Book existingBook = getBookByIsbn(isbn);  // ✅ Reuses method with null check

        // Business Rule: Available copies cannot exceed total copies
        if (bookDto.getAvailableCopies() > bookDto.getTotalCopies()) {
            throw new IllegalArgumentException(
                "Available copies must not exceed total copies"
            );
        }

        // ✅ Use setters to update fields
        existingBook.setTitle(bookDto.getTitle());
        existingBook.setAuthor(bookDto.getAuthor());
        existingBook.setIsbn(bookDto.getIsbn());
        existingBook.setTotalCopies(bookDto.getTotalCopies());
        existingBook.setAvailableCopies(bookDto.getAvailableCopies());

        Book updatedBook = bookRepository.save(existingBook);
        log.info("Book updated: {}", updatedBook.getTitle());

        return updatedBook;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getAllBookByAuthor(String author) {
        log.info("Fetching books by author: {}", author);
        List<Book> books = bookRepository.findByAuthor(author);
        log.info("Found {} books by {}", books.size(), author);
        return books;
    }
}
```

---

## 📊 Before vs After Comparison

| Aspect | Before ❌ | After ✅ |
|--------|----------|---------|
| **Dependency Injection** | `@Autowired` on field | Constructor injection via `@RequiredArgsConstructor` |
| **Controller Dependency** | Depends on `BookServiceImpe` | Depends on `BookService` interface |
| **Null Handling** | No null checks | Proper exception throwing |
| **Builder Usage** | `book.builder()...` (doesn't work) | `book.setField()` (works correctly) |
| **Exception Types** | Generic `RuntimeException` | Specific exceptions (`ResourceNotFoundException`) |
| **Logging** | No logging | Comprehensive logging at INFO and ERROR levels |
| **Transaction Management** | No `@Transactional` | `@Transactional` with `readOnly` for queries |
| **Code Duplication** | Repeated null checks | Reusable methods |

---

## 🚀 How to Apply These Improvements

1. **Backup your current code**
2. **Apply Controller changes** - Replace dependency with interface
3. **Apply Service changes** - Fix builder usage and add null checks
4. **Test each endpoint** - Make sure everything still works
5. **Add logging statements** - Help with debugging

Would you like me to:
1. Create actual replacement files for your Book Service?
2. Show how to test these improvements?
3. Proceed with inter-service communication setup?
