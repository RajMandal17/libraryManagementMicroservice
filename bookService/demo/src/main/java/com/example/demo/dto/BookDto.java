package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BookDto - Data Transfer Object for Book
 * 
 * Purpose: Transfer data between layers (Controller ↔ Service)
 * 
 * SOLID Principles Applied:
 * 
 * 1. Single Responsibility (S):
 *    - ONLY transfers data (no business logic)
 *    - Separates external API structure from internal entity
 * 
 * 2. Interface Segregation (I):
 *    - Clients (API users) only see fields they need
 *    - Hides internal fields like ID, timestamps
 * 
 * Why separate DTO from Entity?
 * 
 * ✅ Security - Don't expose internal fields (createdAt, updatedAt)
 * ✅ Flexibility - API structure can differ from database structure
 * ✅ Validation - Different validation rules for create vs update
 * ✅ Versioning - Can maintain multiple API versions
 * 
 * Example Usage:
 * 
 * Client sends POST /api/books:
 * {
 *   "isbn": "978-0134685991",
 *   "title": "Effective Java",
 *   "author": "Joshua Bloch",
 *   "totalCopies": 10,
 *   "availableCopies": 10
 * }
 * 
 * Controller receives BookDto → Service converts to Book entity → Saves to DB
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {

    /**
     * ISBN - Unique book identifier
     * 
     * Validation:
     * - Cannot be blank
     * - Must be unique (checked in service layer)
     */
    @NotBlank(message = "ISBN is required")
    private String isbn;

    /**
     * Book title
     * 
     * Validation:
     * - Cannot be blank
     */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * Author name
     * 
     * Validation:
     * - Cannot be blank
     */
    @NotBlank(message = "Author is required")
    private String author;

    /**
     * Total number of copies
     * 
     * Validation:
     * - Must be at least 0
     */
    @Min(value = 0, message = "Total copies must be at least 0")
    private Integer totalCopies;

    /**
     * Available copies
     * 
     * Validation:
     * - Must be at least 0
     * - Must be <= totalCopies (checked in service layer)
     */
    @Min(value = 0, message = "Available copies must be at least 0")
    private Integer availableCopies;
}
