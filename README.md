# 🚀 Library Management Microservices

## Complete Learning Guide for SOLID Principles & Microservices Architecture

---

## 📖 START HERE

**Welcome!** This project contains everything you need to learn microservices architecture with SOLID principles.

### **📚 Read the documentation in this order:**

1. **[QUICK_START_EUREKA.md](./QUICK_START_EUREKA.md)** - Start services with Eureka ⭐ **NEW!**
2. **[INDEX.md](./INDEX.md)** - Complete navigation guide
3. **[PROJECT_SUMMARY.md](./PROJECT_SUMMARY.md)** - What we built and how to run it
4. **[EUREKA_SETUP_GUIDE.md](./EUREKA_SETUP_GUIDE.md)** - Service Discovery setup ⭐ **NEW!**
5. **[SOLID_PRINCIPLES_GUIDE.md](./SOLID_PRINCIPLES_GUIDE.md)** - Learn SOLID with examples
6. **[BOOK_SERVICE_IMPROVEMENTS.md](./BOOK_SERVICE_IMPROVEMENTS.md)** - Refactoring guide
7. **[MICROSERVICES_INTEGRATION_GUIDE.md](./MICROSERVICES_INTEGRATION_GUIDE.md)** - Connect services
8. **[FEIGN_CLIENT_GUIDE.md](./FEIGN_CLIENT_GUIDE.md)** - Declarative REST clients
9. **[QUICK_REFERENCE.md](./QUICK_REFERENCE.md)** - Cheat sheet (keep handy!)
10. **[ARCHITECTURE_DIAGRAMS.md](./ARCHITECTURE_DIAGRAMS.md)** - Visual guide

---

## 🎯 What You'll Build

Three microservices with service discovery:

```
        ┌─────────────────────────┐
        │   Eureka Server         │
        │   Port: 8761            │
        │   Service Registry      │
        └───────────┬─────────────┘
                    │
        ┌───────────┴────────────┐
        │                        │
┌───────▼─────────┐      ┌──────▼──────────┐
│  Book Service   │      │  User Service   │
│  Port: 8080     │◄────►│  Port: 8081     │
│  library_db     │ Feign│  library_db     │
└─────────────────┘      └─────────────────┘
```

**Features:**
- ✅ User registration & membership management
- ✅ Book inventory management
- ✅ Borrow/Return with validation
- ✅ **Service Discovery with Netflix Eureka**
- ✅ **Dynamic inter-service communication**
- ✅ **Load balancing ready**
- ✅ SOLID principles applied throughout
- ✅ Production-ready code structure

---

## ⚡ Quick Start

### **Option 1: Automated Startup (Recommended)**
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice
./start-all-services-with-eureka.sh
```
This starts all services in the correct order. Press Ctrl+C to stop all.

### **Option 2: Manual Startup**

### **1. Start Eureka Server** (Terminal 1)
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice/eureka-server
mvn spring-boot:run
```
**Access Dashboard**: http://localhost:8761

### **2. Start User Service** (Terminal 2)
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice/userService
mvn spring-boot:run
```

### **3. Start Book Service** (Terminal 3)
```bash
cd /Users/raj/IdeaProjects/libraryManagementMicroservice/bookService/demo
mvn spring-boot:run
```

### **4. Verify**
- **Eureka Dashboard**: http://localhost:8761 - Should show BOOK-SERVICE and USER-SERVICE as UP
- **Book Service**: http://localhost:8080/api/books
- **User Service**: http://localhost:8081/api/users

✅ See **[QUICK_START_EUREKA.md](./QUICK_START_EUREKA.md)** for detailed testing guide!

---

## 📁 Project Structure

```
/home/devel-rajkumar/java/
│
├── 📚 Documentation (Start with INDEX.md)
│   ├── INDEX.md                           ← Navigation guide
│   ├── PROJECT_SUMMARY.md                 ← Overview
│   ├── SOLID_PRINCIPLES_GUIDE.md          ← Learn SOLID
│   ├── BOOK_SERVICE_IMPROVEMENTS.md       ← Refactoring
│   ├── MICROSERVICES_INTEGRATION_GUIDE.md ← Integration
│   ├── QUICK_REFERENCE.md                 ← Cheat sheet
│   └── ARCHITECTURE_DIAGRAMS.md           ← Visuals
│
├── 🆕 User Service (Port 8081)
│   └── userService/
│       ├── src/main/java/com/library/user/
│       │   ├── UserServiceApplication.java
│       │   ├── model/
│       │   ├── repository/
│       │   ├── service/
│       │   ├── serviceImpl/
│       │   ├── controller/
│       │   └── exception/
│       └── pom.xml
│
└── 📖 Book Service (Port 8080)
    └── springBootPracticeAssignment?/demo/
        ├── src/main/java/com/example/demo/
        └── pom.xml
```

---

## 🎓 Learning Paths

### **Path 1: Beginner** (~2 hours)
Learn basics and run services
- Read documentation
- Start services
- Test APIs
- Understand architecture

### **Path 2: Intermediate** (~3 hours)
Apply SOLID principles
- Refactor Book Service
- Connect services
- Test integration
- Add error handling

### **Path 3: Advanced** (~4 hours)
Production-ready code
- Add WebClient
- Implement full flow
- Write tests
- Add monitoring

**👉 See [INDEX.md](./INDEX.md) for detailed paths**

---

## 🎯 Learning Outcomes

### **You Will Master:**
- ✅ Spring Boot microservices
- ✅ SOLID design principles
- ✅ RESTful API design
- ✅ Inter-service communication
- ✅ JPA/Hibernate
- ✅ Exception handling
- ✅ Dependency injection
- ✅ Layered architecture

### **You Will Build:**
- ✅ 2 complete microservices
- ✅ RESTful APIs (20+ endpoints)
- ✅ Database schemas (MySQL)
- ✅ Inter-service integration
- ✅ Production-ready code

---

## 📊 Technologies Used

| Technology | Purpose | Version |
|------------|---------|---------|
| Spring Boot | Framework | 3.2.0 |
| JPA/Hibernate | Database ORM | Latest |
| MySQL | Database | 8.0+ |
| Maven | Build Tool | Latest |
| Lombok | Reduce Boilerplate | Latest |
| WebClient | HTTP Client | Latest |

---

## 🧪 Testing Example

### **Scenario: User borrows a book**

```bash
# 1. Create user
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@test.com","phone":"1234567890","membershipType":"STUDENT"}'

# Response: User ID = 1

# 2. Create book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"isbn":"978-123","title":"Java Guide","author":"Expert","totalCopies":5,"availableCopies":5}'

# 3. Borrow book (microservices communicate!)
curl -X PUT "http://localhost:8080/api/books/978-123/borrow?userId=1"

# ✅ Book Service checks with User Service
# ✅ User Service validates eligibility
# ✅ Book count decreases
# ✅ User's borrowed count increases
```

---

## 🏆 What Makes This Special

### **Professional Quality**
- ✅ SOLID principles applied correctly
- ✅ Clean code organization
- ✅ Comprehensive documentation
- ✅ Production-ready patterns
- ✅ Real-world architecture

### **Learning Focused**
- ✅ Step-by-step guides
- ✅ Code comments explaining WHY
- ✅ Multiple learning paths
- ✅ Visual diagrams
- ✅ Troubleshooting tips

### **Portfolio Ready**
- ✅ Demonstrate microservices knowledge
- ✅ Show SOLID principles understanding
- ✅ Live demo capability
- ✅ Well-documented
- ✅ Extensible foundation

---

## 📞 Need Help?

### **Quick Fixes**
👉 See [QUICK_REFERENCE.md](./QUICK_REFERENCE.md)

### **Understanding Concepts**
👉 See [SOLID_PRINCIPLES_GUIDE.md](./SOLID_PRINCIPLES_GUIDE.md)

### **Implementation Details**
👉 See [MICROSERVICES_INTEGRATION_GUIDE.md](./MICROSERVICES_INTEGRATION_GUIDE.md)

### **Visual Explanations**
👉 See [ARCHITECTURE_DIAGRAMS.md](./ARCHITECTURE_DIAGRAMS.md)

---

## 🚀 Next Steps

After mastering this:

1. **Testing** - Add JUnit tests
2. **Security** - Add Spring Security + JWT
3. **Containerization** - Docker & Kubernetes
4. **Cloud** - Deploy to AWS/Azure/GCP
5. **Monitoring** - Add Prometheus & Grafana
6. **CI/CD** - GitHub Actions pipeline

---

## 📈 Progress Checklist

- [ ] Read INDEX.md
- [ ] Both services running
- [ ] Tested all APIs
- [ ] Understand SOLID principles
- [ ] Completed integration test
- [ ] Refactored Book Service
- [ ] Added WebClient
- [ ] Written tests (bonus)

---

## 🎉 Congratulations!

You're about to build a **production-grade microservices architecture**!

**Start with**: [INDEX.md](./INDEX.md)

**Time Required**: 2-4 hours
**Difficulty**: Beginner to Advanced
**Outcome**: Portfolio-ready project

---

## 📜 License

Educational Use - Feel free to learn, modify, and share!

---

**Built with ❤️ for learning**  
**Happy Coding! 🚀**

---

## 📞 Quick Links

- 📖 [Complete Documentation Index](./INDEX.md)
- 🏁 [Project Summary](./PROJECT_SUMMARY.md)
- 🎓 [SOLID Principles Guide](./SOLID_PRINCIPLES_GUIDE.md)
- 🔗 [Microservices Integration](./MICROSERVICES_INTEGRATION_GUIDE.md)
- ⚡ [Quick Reference](./QUICK_REFERENCE.md)
- 📐 [Architecture Diagrams](./ARCHITECTURE_DIAGRAMS.md)

**👉 Start with INDEX.md for the complete learning journey!**
# libraryManagementMicroservice
