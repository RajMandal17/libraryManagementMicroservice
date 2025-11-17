package com.example.demo.service;

import com.example.demo.dto.BookDto;
import com.example.demo.entity.Book;

import java.util.List;
import java.util.Optional;

/**
 * BookService - Business logic interface for Book operations
 * 
 * SOLID Principles Applied:
 * 
 * 1. Dependency Inversion (D): ⭐
 *    - Controller depends on THIS INTERFACE, not implementation
 *    - Can swap implementations without changing controller
 *    - Easy to test (can create mock implementation)
 * 
 * 2. Interface Segregation (I):
 *    - Clients only see methods they need
 *    - Can create specialized interfaces if needed
 * 
 * 3. Open/Closed (O):
 *    - Can add new methods without modifying existing code
 *    - Can create new implementations for different behaviors
 * 
 * Why use interface?
 * 
 * ✅ Testability - Can mock in unit tests
 * ✅ Flexibility - Multiple implementations (cache, audit, etc.)
 * ✅ Loose Coupling - Controller doesn't know about implementation details
 * ✅ Contract - Defines what operations are available
 * 
 * Implementation:
 * - BookServiceImpl (main implementation)
 * - CachedBookServiceImpl (with caching)
 * - AuditedBookServiceImpl (with audit logging)
 */
public interface BookService {

    /**
     * Create a new book
     * 
     * Business Rules:
     * - ISBN must be unique
     * - availableCopies <= totalCopies
     * - All fields validated
     * 
     * @param bookDto Book data
     * @return Created book with generated ID
     * @throws IllegalArgumentException if ISBN already exists
     */
    Book createBook(BookDto bookDto);

    /**
     * Get all books (including unavailable)
     * 
     * @return List of all books
     */
    List<Book> getAllBooks();

    /**
     * Get book by ID
     * 
     * @param id Book ID
     * @return Optional<Book> (empty if not found)
     */
    Optional<Book> getBookById(Long id);

    /**
     * Get book by ISBN
     * 
     * @param isbn Book ISBN
     * @return Book
     * @throws ResourceNotFoundException if book not found
     */
    Book getBookByIsbn(String isbn);

    /**
     * Get all books by author
     * 
     * @param author Author name
     * @return List of books by author
     */
    List<Book> getAllBooksByAuthor(String author);

    /**
     * Get all available books (availableCopies > 0)
     * 
     * @return List of available books
     */
    List<Book> getAvailableBooks();

    /**
     * Update book details
     * 
     * Business Rules:
     * - Cannot change ISBN (unique identifier)
     * - availableCopies <= totalCopies
     * 
     * @param isbn Book ISBN
     * @param bookDto Updated book data
     * @return Updated book
     * @throws ResourceNotFoundException if book not found
     */
    Book updateBook(String isbn, BookDto bookDto);

    /**
     * Delete book
     * 
     * Business Rule:
     * - Should check if any copies are currently borrowed
     *   (Not implemented in basic version)
     * 
     * @param id Book ID
     * @throws ResourceNotFoundException if book not found
     */
    void deleteBook(Long id);

    /**
     * Borrow a book (with user validation) ⭐
     * 
     * THIS IS THE KEY INTEGRATION METHOD!
     * 
     * Flow:
     * 1. Call User Service to check if user can borrow
     * 2. Validate book availability
     * 3. Decrement availableCopies
     * 4. Notify User Service to increment user's borrowed count
     * 
     * Business Rules:
     * - User must be eligible (checked via User Service)
     * - Book must have available copies
     * - Updates both services atomically (or with compensation)
     * 
     * @param isbn Book ISBN
     * @param userId User ID
     * @return Updated book
     * @throws ResourceNotFoundException if book not found
     * @throws IllegalStateException if user not eligible or book unavailable
     */
    Book borrowBook(String isbn, Long userId);

    /**
     * Return a book (with user validation) ⭐
     * 
     * THIS IS THE KEY INTEGRATION METHOD!
     * 
     * Flow:
     * 1. Validate book exists
     * 2. Increment availableCopies
     * 3. Notify User Service to decrement user's borrowed count
     * 
     * Business Rules:
     * - Cannot return more copies than total copies
     * - Updates both services
     * 
     * @param isbn Book ISBN
     * @param userId User ID
     * @return Updated book
     * @throws ResourceNotFoundException if book not found
     * @throws IllegalStateException if all copies already returned
     */
    Book returnBook(String isbn, Long userId);

    /**
     * Search books by title (case-insensitive)
     * 
     * @param keyword Search keyword
     * @return List of matching books
     */
    List<Book> searchByTitle(String keyword);
}
