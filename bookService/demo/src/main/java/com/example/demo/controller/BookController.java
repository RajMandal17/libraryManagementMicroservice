package com.example.demo.controller;

import com.example.demo.dto.BookDto;
import com.example.demo.entity.Book;
import com.example.demo.service.BookService;
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
 * BookController - REST API endpoints for Book operations
 * 
 * SOLID Principles Applied:
 * 
 * 1. Single Responsibility (S): ⭐⭐⭐
 *    - ONLY handles HTTP requests/responses
 *    - Business logic → delegated to BookService
 *    - Exception handling → delegated to GlobalExceptionHandler
 *    - NO database access (that's in BookRepository)
 *    - NO User Service calls (that's in UserServiceClient)
 * 
 * 2. Dependency Inversion (D):
 *    - Depends on BookService INTERFACE, not implementation
 *    - Injected via constructor
 * 
 * 3. Open/Closed (O):
 *    - Can add new endpoints without modifying existing ones
 * 
 * REST API Best Practices Applied:
 * 
 * ✅ Use proper HTTP methods:
 *    - GET (read), POST (create), PUT (update), DELETE (delete)
 * 
 * ✅ Use proper HTTP status codes:
 *    - 200 OK (success)
 *    - 201 Created (resource created)
 *    - 400 Bad Request (validation error)
 *    - 404 Not Found (resource doesn't exist)
 *    - 409 Conflict (business rule violation)
 * 
 * ✅ Use plural nouns in URLs:
 *    - /api/books (not /api/book)
 * 
 * ✅ Use path variables for IDs:
 *    - /api/books/{isbn}
 * 
 * ✅ Use query parameters for filters:
 *    - /api/books?author=Joshua
 *    - /api/books/{isbn}/borrow?userId=1
 * 
 * ✅ Validate input using @Valid:
 *    - Automatically validates @NotBlank, @Min, etc.
 * 
 * @RestController - Combines @Controller + @ResponseBody
 * @RequestMapping - Base path for all endpoints
 * @RequiredArgsConstructor - Lombok generates constructor
 * @Slf4j - Lombok generates logger
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    // Dependency Injection (Dependency Inversion Principle)
    private final BookService bookService;

    /**
     * Create a new book
     * 
     * POST /api/books
     * 
     * Request Body:
     * {
     *   "isbn": "978-0134685991",
     *   "title": "Effective Java",
     *   "author": "Joshua Bloch",
     *   "totalCopies": 10,
     *   "availableCopies": 10
     * }
     * 
     * Response: HTTP 201 Created + Book object
     * 
     * @Valid - Validates BookDto fields (@NotBlank, @Min, etc.)
     */
    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody BookDto bookDto) {
        log.info("POST /api/books - Creating book: {}", bookDto.getTitle());
        Book createdBook = bookService.createBook(bookDto);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    /**
     * Get all books
     * 
     * GET /api/books
     * 
     * Response: HTTP 200 OK + List of books
     */
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        log.info("GET /api/books - Fetching all books");
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Get book by ID
     * 
     * GET /api/books/id/{id}
     * 
     * Response:
     * - HTTP 200 OK + Book object (if found)
     * - HTTP 404 Not Found (if not found)
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        log.info("GET /api/books/id/{} - Fetching book", id);
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get book by ISBN
     * 
     * GET /api/books/{isbn}
     * 
     * Response:
     * - HTTP 200 OK + Book object (if found)
     * - HTTP 404 Not Found (handled by GlobalExceptionHandler)
     */
    @GetMapping("/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        log.info("GET /api/books/{} - Fetching book", isbn);
        Book book = bookService.getBookByIsbn(isbn);
        return ResponseEntity.ok(book);
    }

    /**
     * Get books by author
     * 
     * GET /api/books/author/{author}
     * 
     * Example: GET /api/books/author/Joshua Bloch
     * 
     * Response: HTTP 200 OK + List of books
     */
    @GetMapping("/author/{author}")
    public ResponseEntity<List<Book>> getBooksByAuthor(@PathVariable String author) {
        log.info("GET /api/books/author/{} - Fetching books", author);
        List<Book> books = bookService.getAllBooksByAuthor(author);
        return ResponseEntity.ok(books);
    }

    /**
     * Get available books (availableCopies > 0)
     * 
     * GET /api/books/available
     * 
     * Response: HTTP 200 OK + List of available books
     */
    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        log.info("GET /api/books/available - Fetching available books");
        List<Book> books = bookService.getAvailableBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Search books by title
     * 
     * GET /api/books/search?title=java
     * 
     * Response: HTTP 200 OK + List of matching books
     */
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String title) {
        log.info("GET /api/books/search?title={}", title);
        List<Book> books = bookService.searchByTitle(title);
        return ResponseEntity.ok(books);
    }

    /**
     * Update book
     * 
     * PUT /api/books/{isbn}
     * 
     * Request Body: BookDto (same as create)
     * 
     * Response:
     * - HTTP 200 OK + Updated book
     * - HTTP 404 Not Found (if book doesn't exist)
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
     * Delete book
     * 
     * DELETE /api/books/{id}
     * 
     * Response:
     * - HTTP 200 OK + Success message
     * - HTTP 404 Not Found (if book doesn't exist)
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
     * Borrow a book ⭐⭐⭐ MICROSERVICE INTEGRATION!
     * 
     * PUT /api/books/{isbn}/borrow?userId={userId}
     * 
     * Example: PUT /api/books/978-0134685991/borrow?userId=1
     * 
     * What happens:
     * 1. Book Service checks if user can borrow (calls User Service)
     * 2. Book Service updates book (availableCopies--)
     * 3. Book Service notifies User Service (borrowedBooksCount++)
     * 
     * Response:
     * - HTTP 200 OK + Updated book
     * - HTTP 404 Not Found (book doesn't exist)
     * - HTTP 409 Conflict (user not eligible or book unavailable)
     * 
     * @param isbn Book ISBN (path variable)
     * @param userId User ID (query parameter)
     */
    @PutMapping("/{isbn}/borrow")
    public ResponseEntity<Book> borrowBook(
            @PathVariable String isbn,
            @RequestParam Long userId) {
        log.info("PUT /api/books/{}/borrow - User {} borrowing book", isbn, userId);
        Book book = bookService.borrowBook(isbn, userId);
        return ResponseEntity.ok(book);
    }

    /**
     * Return a book ⭐⭐⭐ MICROSERVICE INTEGRATION!
     * 
     * PUT /api/books/{isbn}/return?userId={userId}
     * 
     * Example: PUT /api/books/978-0134685991/return?userId=1
     * 
     * What happens:
     * 1. Book Service updates book (availableCopies++)
     * 2. Book Service notifies User Service (borrowedBooksCount--)
     * 
     * Response:
     * - HTTP 200 OK + Updated book
     * - HTTP 404 Not Found (book doesn't exist)
     * - HTTP 409 Conflict (all copies already returned)
     * 
     * @param isbn Book ISBN (path variable)
     * @param userId User ID (query parameter)
     */
    @PutMapping("/{isbn}/return")
    public ResponseEntity<Book> returnBook(
            @PathVariable String isbn,
            @RequestParam Long userId) {
        log.info("PUT /api/books/{}/return - User {} returning book", isbn, userId);
        Book book = bookService.returnBook(isbn, userId);
        return ResponseEntity.ok(book);
    }

    /**
     * Health check endpoint
     * 
     * GET /api/books/health
     * 
     * Response: HTTP 200 OK + Status message
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
