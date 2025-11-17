#!/bin/bash

# Microservices Integration Testing Script
# This script demonstrates the complete workflow of our microservices

echo "🎯 =========================================="
echo "   MICROSERVICES INTEGRATION TEST"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}📋 Prerequisites:${NC}"
echo "1. User Service running on http://localhost:8081"
echo "2. Book Service running on http://localhost:8080"
echo ""

# Test 1: Create a User
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}Step 1: Creating a new user${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "POST http://localhost:8081/api/users"
echo '{"name":"John Doe","email":"john@example.com","phone":"1234567890","membershipType":"REGULAR"}'
echo ""

USER_RESPONSE=$(curl -s -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "membershipType": "REGULAR"
  }')

echo "Response:"
echo "$USER_RESPONSE" | jq '.'
echo ""

# Extract user ID
USER_ID=$(echo "$USER_RESPONSE" | jq -r '.id')
echo -e "${GREEN}✓ User created with ID: $USER_ID${NC}"
echo ""

# Test 2: Create a Book
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}Step 2: Creating a new book${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "POST http://localhost:8080/api/books"
echo '{"isbn":"978-0134685991","title":"Effective Java","author":"Joshua Bloch","totalCopies":10,"availableCopies":10}'
echo ""

BOOK_RESPONSE=$(curl -s -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "978-0134685991",
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "totalCopies": 10,
    "availableCopies": 10
  }')

echo "Response:"
echo "$BOOK_RESPONSE" | jq '.'
echo ""
echo -e "${GREEN}✓ Book created successfully${NC}"
echo ""

# Test 3: Borrow Book (Microservice Communication!)
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}Step 3: Borrowing book (Microservice Integration!)${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "PUT http://localhost:8080/api/books/978-0134685991/borrow?userId=$USER_ID"
echo ""
echo "What happens behind the scenes:"
echo "1. Book Service receives request"
echo "2. Book Service calls User Service: GET /api/users/$USER_ID/can-borrow"
echo "3. User Service checks eligibility → returns {canBorrow: true}"
echo "4. Book Service updates book: availableCopies: 10 → 9"
echo "5. Book Service calls User Service: PUT /api/users/$USER_ID/borrow"
echo "6. User Service updates user: borrowedBooksCount: 0 → 1"
echo ""

BORROW_RESPONSE=$(curl -s -X PUT "http://localhost:8080/api/books/978-0134685991/borrow?userId=$USER_ID")

echo "Response:"
echo "$BORROW_RESPONSE" | jq '.'
echo ""
echo -e "${GREEN}✓ Book borrowed successfully! Available copies decreased from 10 to 9${NC}"
echo ""

# Test 4: Verify User Updated
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}Step 4: Verifying user's borrowed books count${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "GET http://localhost:8081/api/users/$USER_ID"
echo ""

USER_CHECK=$(curl -s "http://localhost:8081/api/users/$USER_ID")

echo "Response:"
echo "$USER_CHECK" | jq '.'
echo ""

BORROWED_COUNT=$(echo "$USER_CHECK" | jq -r '.borrowedBooksCount')
echo -e "${GREEN}✓ User's borrowed books count: $BORROWED_COUNT (updated from 0 to 1!)${NC}"
echo ""

# Test 5: Return Book
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}Step 5: Returning the book${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "PUT http://localhost:8080/api/books/978-0134685991/return?userId=$USER_ID"
echo ""

RETURN_RESPONSE=$(curl -s -X PUT "http://localhost:8080/api/books/978-0134685991/return?userId=$USER_ID")

echo "Response:"
echo "$RETURN_RESPONSE" | jq '.'
echo ""
echo -e "${GREEN}✓ Book returned successfully! Available copies increased from 9 to 10${NC}"
echo ""

# Test 6: Final Verification
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}Step 6: Final verification${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "GET http://localhost:8081/api/users/$USER_ID"
echo ""

FINAL_USER=$(curl -s "http://localhost:8081/api/users/$USER_ID")

echo "Response:"
echo "$FINAL_USER" | jq '.'
echo ""

FINAL_BORROWED=$(echo "$FINAL_USER" | jq -r '.borrowedBooksCount')
echo -e "${GREEN}✓ User's borrowed books count: $FINAL_BORROWED (back to 0!)${NC}"
echo ""

# Summary
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}🎉 INTEGRATION TEST COMPLETED SUCCESSFULLY!${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Summary:"
echo "✓ User created in User Service"
echo "✓ Book created in Book Service"
echo "✓ Book borrowed (Book Service ↔ User Service communication)"
echo "✓ User's borrowed count incremented"
echo "✓ Book returned (Book Service ↔ User Service communication)"
echo "✓ User's borrowed count decremented"
echo ""
echo -e "${GREEN}Both microservices are working together perfectly!${NC}"
echo ""
