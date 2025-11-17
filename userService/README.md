# 📚 User Service - Library Management Microservice

## 🎯 Purpose
Manages library members (users) and their membership details.

## 🚀 Running the Service

### Prerequisites
- JDK 17+
- MySQL running on localhost:3306
- Maven

### Start Service
```bash
cd /home/devel-rajkumar/java/userService
mvn spring-boot:run
```

Service will start on **port 8081**

## 📋 Features
- User registration and management
- Membership types: STUDENT, REGULAR, PREMIUM
- Membership status tracking
- Book borrowing limits based on membership
- Integration with Book Service

## 🔗 API Endpoints (Coming Soon)
- `POST /api/users` - Register new user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `PUT /api/users/{id}/suspend` - Suspend membership
- `PUT /api/users/{id}/activate` - Activate membership

## 🗄️ Database
- Database: `user_db`
- Table: `users`
