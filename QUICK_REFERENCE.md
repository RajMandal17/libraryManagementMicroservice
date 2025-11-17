# 🎯 Quick Reference Card - Microservices with SOLID

## 🏃 Quick Start Commands

### Start Services
```bash
# Terminal 1 - User Service (Port 8081)
cd /home/devel-rajkumar/java/userService
mvn spring-boot:run

# Terminal 2 - Book Service (Port 8080)
cd /home/devel-rajkumar/java/springBootPracticeAssignment?/demo
mvn spring-boot:run
```

### Health Checks
```bash
curl http://localhost:8081/api/users/health
curl http://localhost:8080/api/books/health
```

---

## 📋 Common Operations

### Create User
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John","email":"john@test.com","phone":"1234567890","membershipType":"REGULAR"}'
```

### Create Book
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"isbn":"123-456","title":"Test Book","author":"Author","totalCopies":5,"availableCopies":5}'
```

### Borrow Book (Microservices in Action!)
```bash
curl -X PUT "http://localhost:8080/api/books/123-456/borrow?userId=1"
```

### Return Book
```bash
curl -X PUT "http://localhost:8080/api/books/123-456/return?userId=1"
```

---

## 🎓 SOLID Principles Cheat Sheet

| Principle | Remember | Example |
|-----------|----------|---------|
| **S** - Single Responsibility | One class = One job | Controller only handles HTTP |
| **O** - Open/Closed | Extend, don't modify | Use interfaces |
| **L** - Liskov Substitution | Swap implementations | Any UserService works |
| **I** - Interface Segregation | Small, focused interfaces | UserService ≠ BookService |
| **D** - Dependency Inversion | Depend on abstractions | Inject interfaces, not classes |

---

## 🏗️ Architecture Layers

```
Controller  → HTTP requests/responses
   ↓
Service     → Business logic
   ↓
Repository  → Database operations
   ↓
Database    → Data storage
```

---

## 🔥 Common Issues & Solutions

### Issue: "Port already in use"
```bash
# Find process using port
sudo lsof -i :8080
sudo lsof -i :8081

# Kill process
kill -9 <PID>
```

### Issue: "Connection refused to User Service"
- Check User Service is running on port 8081
- Check URL in WebClient config: `http://localhost:8081`

### Issue: "Book borrowed count not updating"
- Check logs in both services
- Verify WebClient is configured in Book Service
- Check UserServiceClient is injected in BookServiceImpl

---

## 📊 Database Tables

### User Service Database: `user_db`
```sql
users table:
- id (Long, Primary Key)
- name (String)
- email (String, Unique)
- phone (String)
- membership_type (ENUM)
- membership_status (ENUM)
- joined_date (DateTime)
- expiry_date (DateTime)
- borrowed_books_count (Integer)
- max_books_allowed (Integer)
```

### Book Service Database: `library_db`
```sql
books table:
- id (Long, Primary Key)
- isbn (String, Unique)
- title (String)
- author (String)
- total_copies (Integer)
- available_copies (Integer)
```

---

## 🧪 Testing Workflow

1. ✅ Start MySQL
2. ✅ Start User Service (8081)
3. ✅ Start Book Service (8080)
4. ✅ Create a user
5. ✅ Create a book
6. ✅ Borrow book (tests integration)
7. ✅ Verify user's borrowed count increased
8. ✅ Return book
9. ✅ Verify counts restored

---

## 📁 Important Files Locations

### User Service
```
/home/devel-rajkumar/java/userService/
├── src/main/java/com/library/user/
│   ├── UserServiceApplication.java
│   ├── controller/UserController.java
│   ├── service/UserService.java
│   ├── serviceImpl/UserServiceImpl.java
│   └── ...
└── pom.xml
```

### Book Service
```
/home/devel-rajkumar/java/springBootPracticeAssignment?/demo/
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java
│   ├── controller/bookController.java
│   ├── service/BookService.java
│   └── ...
└── pom.xml
```

---

## 🔗 Inter-Service Communication

### Book Service → User Service Flow

```java
// 1. In Book Service
@Service
public class BookServiceImpl {
    private final UserServiceClient userServiceClient;
    
    public Book borrowBook(String isbn, Long userId) {
        // 2. Check with User Service
        boolean canBorrow = userServiceClient.canUserBorrowBooks(userId);
        
        if (!canBorrow) {
            throw new IllegalStateException("User cannot borrow");
        }
        
        // 3. Update book
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        
        // 4. Notify User Service
        userServiceClient.notifyBookBorrowed(userId);
        
        return book;
    }
}
```

---

## 🎯 Learning Path

### Completed ✅
- Spring Boot basics
- REST API design
- SOLID principles
- Microservices architecture
- Inter-service communication

### Next Steps 🚀
1. **Testing** - JUnit, Mockito
2. **Security** - JWT, Spring Security
3. **Containerization** - Docker
4. **Orchestration** - Kubernetes
5. **Cloud** - AWS/Azure/GCP

---

## 📖 Documentation Files

1. `SOLID_PRINCIPLES_GUIDE.md` - Learn SOLID with examples
2. `BOOK_SERVICE_IMPROVEMENTS.md` - How to refactor existing code
3. `MICROSERVICES_INTEGRATION_GUIDE.md` - Connect services
4. `PROJECT_SUMMARY.md` - Complete overview
5. `QUICK_REFERENCE.md` - This file!

---

## 💡 Pro Tips

1. **Always check logs** - They tell you what's happening
2. **Test endpoints individually** - Before integration
3. **Use Postman** - Easier than curl for complex requests
4. **Read error messages** - They usually tell you what's wrong
5. **Start simple** - Get basic flow working, then add features

---

## 🆘 Get Help

### Check Logs
```bash
# User Service logs in Terminal 1
# Book Service logs in Terminal 2
# Look for ERROR or WARN messages
```

### Common Log Messages
- `ResourceNotFoundException` → Entity not found in database
- `IllegalArgumentException` → Business rule violation
- `Connection refused` → Target service not running
- `Port already in use` → Service already running

---

## 🎉 Success Indicators

You know it's working when:
- ✅ Both services start without errors
- ✅ Health checks return 200 OK
- ✅ Can create users and books
- ✅ Borrowing a book updates both services
- ✅ Returning a book restores counts
- ✅ Logs show inter-service calls

---

## 📞 Quick Contacts

| Service | Port | Base URL | Database |
|---------|------|----------|----------|
| User Service | 8081 | http://localhost:8081/api/users | user_db |
| Book Service | 8080 | http://localhost:8080/api/books | library_db |

---

**Print this page and keep it handy while coding!** 📌
