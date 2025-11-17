package com.example.demo.serviceImpl;

import com.example.demo.client.UserServiceClient;
import com.example.demo.dto.BookDto;
import com.example.demo.entity.Book;
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
 * BookServiceImpl - Implementation of BookService interface
 * 
 * SOLID Principles Applied:
 * 
 * 1. Single Responsibility (S): ⭐⭐⭐
 *    - ONLY handles BOOK BUSINESS LOGIC
 *    - Database access → delegated to BookRepository
 *    - HTTP handling → delegated to BookController
 *    - User Service calls → delegated to UserServiceClient
 * 
 * 2. Open/Closed (O):
 *    - Open for extension (can create CachedBookServiceImpl)
 *    - Closed for modification (implements interface contract)
 * 
 * 3. Liskov Substitution (L):
 *    - Can be replaced by any other BookService implementation
 *    - Maintains same behavior contract
 * 
 * 4. Interface Segregation (I):
 *    - Depends only on interfaces it needs (BookRepository, UserServiceClient)
 * 
 * 5. Dependency Inversion (D):
 *    - Depends on interfaces (BookRepository, BookService)
 *    - Injected via constructor (not 'new' keyword)
 * 
 * Layer Architecture:
 * 
 * Controller (HTTP)
 *     ↓
 * Service (Business Logic) ← YOU ARE HERE
 *     ↓                 ↓
 * Repository        UserServiceClient
 *     ↓                 ↓
 * Database         User Service API
 * 
 * @Service - Marks as Spring service component
 * @RequiredArgsConstructor - Lombok generates constructor
 * @Slf4j - Lombok generates logger
 * @Transactional - Database transactions (rollback on error)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookServiceImpl implements BookService {

    // Dependencies injected by Spring via constructor
    private final BookRepository bookRepository;
    private final UserServiceClient userServiceClient;  // ⭐ For microservice communication

    /**
     * Create a new book
     * 
     * Flow:
     * 1. Validate ISBN is unique
     * 2. Validate availableCopies <= totalCopies
     * 3. Convert DTO → Entity
     * 4. Save to database
     */
    @Override
    public Book createBook(BookDto bookDto) {
        log.info("📚 Creating book: {}", bookDto.getTitle());

        // Business Rule: ISBN must be unique
        if (bookRepository.existsByIsbn(bookDto.getIsbn())) {
            log.error("❌ Book with ISBN {} already exists", bookDto.getIsbn());
            throw new IllegalArgumentException("Book with ISBN " + bookDto.getIsbn() + " already exists");
        }

        // Business Rule: availableCopies <= totalCopies
        if (bookDto.getAvailableCopies() > bookDto.getTotalCopies()) {
            log.error("❌ Available copies ({}) cannot exceed total copies ({})", 
                    bookDto.getAvailableCopies(), bookDto.getTotalCopies());
            throw new IllegalArgumentException("Available copies cannot exceed total copies");
        }

        // Convert DTO → Entity
        Book book = new Book();
        book.setIsbn(bookDto.getIsbn());
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setTotalCopies(bookDto.getTotalCopies());
        book.setAvailableCopies(bookDto.getAvailableCopies());

        // Save to database
        Book savedBook = bookRepository.save(book);
        log.info("✅ Book created successfully: ID={}, ISBN={}", savedBook.getId(), savedBook.getIsbn());
        
        return savedBook;
    }

    /**
     * Get all books
     */
    @Override
    public List<Book> getAllBooks() {
        log.info("📚 Fetching all books");
        List<Book> books = bookRepository.findAll();
        log.info("✅ Found {} books", books.size());
        return books;
    }

    /**
     * Get book by ID
     */
    @Override
    public Optional<Book> getBookById(Long id) {
        log.info("🔍 Fetching book by ID: {}", id);
        return bookRepository.findById(id);
    }

    /**
     * Get book by ISBN (throws exception if not found)
     */
    @Override
    public Book getBookByIsbn(String isbn) {
        log.info("🔍 Fetching book by ISBN: {}", isbn);
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> {
                    log.error("❌ Book not found with ISBN: {}", isbn);
                    return new ResourceNotFoundException("Book not found with ISBN: " + isbn);
                });
    }

    /**
     * Get books by author
     */
    @Override
    public List<Book> getAllBooksByAuthor(String author) {
        log.info("🔍 Fetching books by author: {}", author);
        List<Book> books = bookRepository.findByAuthor(author);
        log.info("✅ Found {} books by {}", books.size(), author);
        return books;
    }

    /**
     * Get available books (availableCopies > 0)
     */
    @Override
    public List<Book> getAvailableBooks() {
        log.info("📚 Fetching available books");
        List<Book> books = bookRepository.findAvailableBooks();
        log.info("✅ Found {} available books", books.size());
        return books;
    }

    /**
     * Update book
     */
    @Override
    public Book updateBook(String isbn, BookDto bookDto) {
        log.info("📝 Updating book: {}", isbn);

        // Find existing book
        Book book = getBookByIsbn(isbn);

        // Business Rule: availableCopies <= totalCopies
        if (bookDto.getAvailableCopies() > bookDto.getTotalCopies()) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies");
        }

        // Update fields
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setTotalCopies(bookDto.getTotalCopies());
        book.setAvailableCopies(bookDto.getAvailableCopies());

        Book updatedBook = bookRepository.save(book);
        log.info("✅ Book updated successfully: {}", isbn);
        
        return updatedBook;
    }

    /**
     * Delete book
     */
    @Override
    public void deleteBook(Long id) {
        log.info("🗑️ Deleting book: {}", id);

        // Check if book exists
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        bookRepository.deleteById(id);
        log.info("✅ Book deleted successfully: {}", book.getTitle());
    }

    /**
     * Borrow a book ⭐⭐⭐ MICROSERVICE INTEGRATION!
     * 
     * This method demonstrates inter-service communication!
     * 
     * Flow:
     * 1. Validate user eligibility (call User Service)
     * 2. Check book availability
     * 3. Update book (availableCopies--)
     * 4. Notify User Service (borrowedBooksCount++)
     * 
     * Business Rules:
     * - User must be eligible (active membership, under borrow limit)
     * - Book must have available copies
     * - Both services must be updated (distributed transaction)
     */
    @Override
    public Book borrowBook(String isbn, Long userId) {
        log.info("📖 Processing borrow request: ISBN={}, UserID={}", isbn, userId);

        // Step 1: Validate user eligibility via User Service ⭐
        log.info("Step 1: Checking user eligibility...");
        
        try {
            // Feign automatically makes: GET http://localhost:8081/api/users/{userId}/can-borrow
            java.util.Map<String, Boolean> response = userServiceClient.canUserBorrow(userId);
            boolean canBorrow = response != null && Boolean.TRUE.equals(response.get("canBorrow"));
            
            if (!canBorrow) {
                log.error("❌ User {} is not eligible to borrow books", userId);
                throw new IllegalStateException(
                    "User is not eligible to borrow books. " +
                    "Possible reasons: membership expired, max limit reached, or account suspended"
                );
            }
            log.info("✅ User {} is eligible to borrow", userId);
        } catch (feign.FeignException e) {
            log.error("❌ Error calling User Service: {}", e.getMessage());
            throw new IllegalStateException("Unable to verify user eligibility. User Service may be down.", e);
        }

        // Step 2: Get book and check availability
        log.info("Step 2: Checking book availability...");
        Book book = getBookByIsbn(isbn);

        if (book.getAvailableCopies() <= 0) {
            log.error("❌ Book '{}' is out of stock", book.getTitle());
            throw new IllegalStateException("Book is currently out of stock");
        }
        log.info("✅ Book '{}' has {} copies available", book.getTitle(), book.getAvailableCopies());

        // Step 3: Update book availability
        log.info("Step 3: Updating book availability...");
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        Book updatedBook = bookRepository.save(book);
        log.info("✅ Book updated: {} → {} copies available", 
                book.getAvailableCopies() + 1, updatedBook.getAvailableCopies());

        // Step 4: Notify User Service ⭐
        log.info("Step 4: Notifying User Service...");
        userServiceClient.notifyBookBorrowed(userId);
        log.info("✅ User Service notified");

        log.info("🎉 Book borrowed successfully: '{}' by user {}", updatedBook.getTitle(), userId);
        return updatedBook;
    }

    /**
     * Return a book ⭐⭐⭐ MICROSERVICE INTEGRATION!
     * 
     * Flow:
     * 1. Validate book exists
     * 2. Check if return is valid (not all copies already returned)
     * 3. Update book (availableCopies++)
     * 4. Notify User Service (borrowedBooksCount--)
     */
    @Override
    public Book returnBook(String isbn, Long userId) {
        log.info("📕 Processing return request: ISBN={}, UserID={}", isbn, userId);

        // Step 1: Get book
        log.info("Step 1: Fetching book...");
        Book book = getBookByIsbn(isbn);
        log.info("✅ Book found: '{}'", book.getTitle());

        // Step 2: Validate return is possible
        log.info("Step 2: Validating return...");
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            log.error("❌ All copies of '{}' are already returned", book.getTitle());
            throw new IllegalStateException("All copies are already returned");
        }
        log.info("✅ Return is valid ({}/{} copies returned)", 
                book.getAvailableCopies(), book.getTotalCopies());

        // Step 3: Update book availability
        log.info("Step 3: Updating book availability...");
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        Book updatedBook = bookRepository.save(book);
        log.info("✅ Book updated: {} → {} copies available", 
                book.getAvailableCopies() - 1, updatedBook.getAvailableCopies());

        // Step 4: Notify User Service ⭐
        log.info("Step 4: Notifying User Service...");
        userServiceClient.notifyBookReturned(userId);
        log.info("✅ User Service notified");

        log.info("🎉 Book returned successfully: '{}' by user {}", updatedBook.getTitle(), userId);
        return updatedBook;
    }

    /**
     * Search books by title
     */
    @Override
    public List<Book> searchByTitle(String keyword) {
        log.info("🔍 Searching books with keyword: {}", keyword);
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase(keyword);
        log.info("✅ Found {} books matching '{}'", books.size(), keyword);
        return books;
    }
}
