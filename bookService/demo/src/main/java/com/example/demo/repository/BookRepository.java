package com.example.demo.repository;

import com.example.demo.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BookRepository - Data Access Layer for Book entity
 * 
 * SOLID Principles Applied:
 * 
 * 1. Single Responsibility (S):
 *    - ONLY handles database operations
 *    - No business logic (that's in BookService)
 * 
 * 2. Dependency Inversion (D):
 *    - BookService depends on this INTERFACE, not implementation
 *    - Spring Data JPA provides implementation at runtime
 * 
 * 3. Open/Closed (O):
 *    - Can add new query methods without modifying existing ones
 * 
 * JpaRepository provides:
 * - save(entity) - Insert/Update
 * - findById(id) - Find by ID
 * - findAll() - Get all records
 * - deleteById(id) - Delete by ID
 * - count() - Count records
 * - existsById(id) - Check if exists
 * 
 * Custom Query Methods:
 * Spring Data JPA automatically implements methods based on method name!
 * 
 * Naming Convention:
 * - findBy + FieldName + (Optional: OrderBy + FieldName)
 * 
 * Examples:
 * - findByIsbn() → SELECT * FROM books WHERE isbn = ?
 * - findByAuthor() → SELECT * FROM books WHERE author = ?
 * - findByAvailableCopiesGreaterThan() → SELECT * FROM books WHERE available_copies > ?
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find book by ISBN
     * 
     * Query: SELECT * FROM books WHERE isbn = ?
     * 
     * @param isbn Book ISBN
     * @return Optional<Book> (empty if not found)
     * 
     * Why Optional?
     * - Avoids null pointer exceptions
     * - Forces explicit handling of "not found" case
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Find all books by author
     * 
     * Query: SELECT * FROM books WHERE author = ?
     * 
     * @param author Author name
     * @return List of books (empty list if none found)
     */
    List<Book> findByAuthor(String author);

    /**
     * Find all books with available copies > 0
     * 
     * Query: SELECT * FROM books WHERE available_copies > 0
     * 
     * @return List of available books
     */
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    List<Book> findAvailableBooks();

    /**
     * Alternative using method name (same as above)
     * Spring Data JPA auto-generates query
     */
    List<Book> findByAvailableCopiesGreaterThan(Integer copies);

    /**
     * Check if book exists by ISBN
     * 
     * Query: SELECT COUNT(*) > 0 FROM books WHERE isbn = ?
     * 
     * @param isbn Book ISBN
     * @return true if exists, false otherwise
     */
    boolean existsByIsbn(String isbn);

    /**
     * Find books by title containing keyword (case-insensitive)
     * 
     * Query: SELECT * FROM books WHERE LOWER(title) LIKE LOWER(CONCAT('%', ?, '%'))
     * 
     * @param keyword Search keyword
     * @return List of matching books
     * 
     * Example: keyword = "java"
     * Matches: "Effective Java", "Java Programming", "JAVA 101"
     */
    List<Book> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Count available books
     * 
     * Query: SELECT COUNT(*) FROM books WHERE available_copies > 0
     * 
     * @return Number of available books
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.availableCopies > 0")
    long countAvailableBooks();
}
