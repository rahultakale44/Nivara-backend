# Nivara Backend - MIT ADT Campus Support Platform

**Enterprise Campus Infrastructure Management System - REST API**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9.16-C71A36?logo=apachemaven)](https://maven.apache.org/)

---

## About

**Nivara Backend** is the REST API server for MIT ADT University's campus infrastructure management platform. It provides secure JWT-based authentication, role-based access control, and comprehensive issue management capabilities.

**Repository:** https://github.com/rahultakale44/Nivara-backend

---

## Features

### 🔐 Authentication & Authorization
- JWT-based token authentication
- BCrypt password encryption
- Role-based access control (STUDENT, ADMIN)
- Secure session management
- Custom authentication entry points

### Issue Management
- Create, read, update, delete operations
- Status workflow (PENDING, IN_PROGRESS, RESOLVED, REJECTED)
- Priority levels (LOW, MEDIUM, HIGH, URGENT)
- Location-based tracking
- Image upload support
- Admin notes and feedback

### Location Management
- Multi-level location hierarchy (Building, Floor, Wing, Room)
- CRUD operations for locations
- Active/inactive status management
- Unique location validation
- Display name generation

### Security Features
- CORS configuration for frontend integration
- JWT token validation on every request
- Role-based endpoint protection
- Input validation and sanitization
- Secure exception handling

### Analytics & Reporting
- Issue statistics by status
- Category-wise distribution
- Time-based tracking
- Admin dashboard metrics

---

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming language |
| **Spring Boot** | 3.5.14 | Application framework |
| **Spring Security** | 6.4.3 | Authentication & authorization |
| **Spring Data JPA** | 3.4.3 | Database ORM |
| **Hibernate** | 6.6.5.Final | JPA implementation |
| **MySQL Connector** | Runtime (latest) | Database driver |
| **JWT (jjwt)** | 0.12.5 | Token generation & validation |
| **Lombok** | Provided by Spring Boot | Code generation |
| **Maven** | 3.9.16 | Build tool |
| **JUnit 5** | Provided by Spring Boot | Testing framework |
| **Mockito** | Provided by Spring Boot | Mocking framework |
| **H2 Database** | Test scope | In-memory testing |

### Version Notes
- **Spring Framework:** 6.2.3 (managed by Spring Boot 3.5.14)
- **Hibernate Validator:** 8.0.2.Final (managed by Spring Boot)
- **Jackson:** 2.18.3 (managed by Spring Boot)

---

## Project Structure

```
Nivara-backend/
├── src/
│   ├── main/
│   │   ├── java/com/rahul/campuscare/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java       # JWT & CORS configuration
│   │   │   │   └── WebConfig.java            # Web MVC configuration
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java       # Login/register endpoints
│   │   │   │   ├── ComplaintController.java  # Issue management
│   │   │   │   ├── LocationController.java   # Location CRUD
│   │   │   │   └── FileUploadController.java # Image upload
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── ComplaintResponse.java
│   │   │   │   ├── CreateComplaintRequest.java
│   │   │   │   ├── UpdateComplaintStatusRequest.java
│   │   │   │   ├── LocationResponse.java
│   │   │   │   ├── CreateLocationRequest.java
│   │   │   │   ├── UpdateLocationRequest.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java                 # User entity
│   │   │   │   ├── Complaint.java            # Issue entity
│   │   │   │   ├── Location.java             # Location entity
│   │   │   │   ├── Role.java                 # Role enum
│   │   │   │   ├── ComplaintStatus.java      # Status enum
│   │   │   │   └── Priority.java             # Priority enum
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InvalidCredentialsException.java
│   │   │   │   ├── DuplicateEmailException.java
│   │   │   │   ├── ComplaintNotFoundException.java
│   │   │   │   ├── LocationNotFoundException.java
│   │   │   │   ├── DuplicateLocationException.java
│   │   │   │   ├── InactiveLocationException.java
│   │   │   │   ├── InvalidFileException.java
│   │   │   │   └── UserNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ComplaintRepository.java
│   │   │   │   └── LocationRepository.java
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java  # JWT filter
│   │   │   │   └── UnauthorizedEntryPoint.java   # 401 handler
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── ComplaintService.java
│   │   │   │   ├── LocationService.java
│   │   │   │   └── JwtService.java
│   │   │   └── CampuscareBackendApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── static/
│   └── test/
│       └── java/com/rahul/campuscare/
│           ├── controller/
│           ├── service/
│           └── repository/
├── .mvn/wrapper/                    # Maven wrapper
├── uploads/                         # Image uploads directory
├── pom.xml                         # Maven configuration
├── mvnw                            # Maven wrapper script (Unix)
├── mvnw.cmd                        # Maven wrapper script (Windows)
├── run-backend.ps1                 # PowerShell startup script
└── README.md                       # This file
```

---

## Installation & Setup

### Prerequisites

- **Java Development Kit (JDK) 21** or higher
- **MySQL 8.0** or higher
- **Maven 3.9+** (or use included Maven wrapper)

### Database Setup

```sql
-- Create database
CREATE DATABASE campuscare_db;

-- Grant privileges (if needed)
GRANT ALL PRIVILEGES ON campuscare_db.* TO 'your_user'@'localhost';
FLUSH PRIVILEGES;
```

### Configuration

**Option 1: Using run-backend.ps1 (Windows PowerShell)**

Edit `run-backend.ps1` and update:

```powershell
$env:JWT_SECRET = "your-secure-jwt-secret-key-minimum-32-characters"
$env:DB_URL = "jdbc:mysql://localhost:3306/campuscare_db"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_password"
```

**Option 2: Using .env file**

Create `.env` file in the project root:

```env
JWT_SECRET=your-secure-jwt-secret-key-minimum-32-characters
DB_URL=jdbc:mysql://localhost:3306/campuscare_db
DB_USERNAME=root
DB_PASSWORD=your_password
PORT=8080
```

**Option 3: Using application.properties**

Create `src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campuscare_db
spring.datasource.username=root
spring.datasource.password=your_password
jwt.secret=your-secure-jwt-secret-key-minimum-32-characters
server.port=8080
```

### Running the Application

**Using Maven Wrapper (Recommended):**

```bash
# Windows
./mvnw.cmd spring-boot:run

# Unix/Linux/Mac
./mvnw spring-boot:run

# Or use the PowerShell script
./run-backend.ps1
```

**Using Maven (if installed):**

```bash
mvn spring-boot:run
```

**Building JAR:**

```bash
./mvnw clean package
java -jar target/campuscare-backend-0.0.1-SNAPSHOT.jar
```

The API will be available at: `http://localhost:8080`

---

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### Register Student
```http
POST /api/auth/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "securepassword"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "securepassword"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "STUDENT"
}
```

### Issue (Complaint) Endpoints

#### Create Issue (Student Only)
```http
POST /api/complaints
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Broken projector",
  "description": "Projector in room 301 not working",
  "category": "Classroom",
  "locationId": 5,
  "priority": "HIGH",
  "imageUrl": "uploads/image123.jpg"
}
```

#### Get My Issues (Student Only)
```http
GET /api/complaints/my
Authorization: Bearer <token>
```

#### Get All Issues (Admin Only)
```http
GET /api/complaints
Authorization: Bearer <token>
```

#### Update Issue Status (Admin Only)
```http
PUT /api/complaints/{id}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "IN_PROGRESS",
  "adminNote": "Technician assigned, will be fixed tomorrow"
}
```

### Location Endpoints

#### Get All Locations (Authenticated)
```http
GET /api/locations
Authorization: Bearer <token>
```

#### Create Location (Admin Only)
```http
POST /api/locations
Authorization: Bearer <token>
Content-Type: application/json

{
  "building": "Main Building",
  "floor": 3,
  "wing": "A",
  "roomNumber": "301"
}
```

#### Update Location (Admin Only)
```http
PUT /api/locations/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "building": "Main Building",
  "floor": 3,
  "wing": "A",
  "roomNumber": "301",
  "active": true
}
```

#### Delete Location (Admin Only)
```http
DELETE /api/locations/{id}
Authorization: Bearer <token>
```

### File Upload

#### Upload Image (Authenticated)
```http
POST /api/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <binary>

Response: "uploads/filename.jpg"
```

---

## 🧪 Testing

### Run All Tests

```bash
# Using Maven Wrapper
./mvnw test

# Using Maven
mvn test
```

### Test Coverage

- **Total Tests:** 107
- **Test Status:** ✅ All Passing
- **Coverage Areas:**
  - Controller layer tests
  - Service layer tests
  - Repository layer tests
  - Security configuration tests
  - Exception handling tests

### Test Database

Tests use H2 in-memory database for isolated testing.

---

## Security Configuration

### JWT Authentication

- **Algorithm:** HS256
- **Token Expiration:** 1 hour
- **Secret Key:** Configurable via environment variable

### CORS Configuration

Allowed origins (configured in `SecurityConfig.java`):
- `http://localhost:5173`
- `http://localhost:5174`
- Production frontend URLs

Allowed methods: GET, POST, PUT, DELETE, OPTIONS

### Role-Based Access

| Endpoint Pattern | Allowed Roles |
|------------------|---------------|
| `/api/auth/**` | Public |
| `/uploads/**` | Public |
| `/api/upload` | Authenticated |
| `/api/locations` (GET) | Authenticated |
| `/api/locations` (POST/PUT/DELETE) | ADMIN |
| `/api/complaints` (POST) | STUDENT |
| `/api/complaints/my` | STUDENT |
| `/api/complaints` (GET) | ADMIN |
| `/api/complaints/*/status` | ADMIN |

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('STUDENT', 'ADMIN') NOT NULL
);
```

### Locations Table
```sql
CREATE TABLE locations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  building VARCHAR(255) NOT NULL,
  floor INT NOT NULL,
  wing VARCHAR(255),
  room_number VARCHAR(255) NOT NULL,
  display_name VARCHAR(255),
  active BIT NOT NULL DEFAULT 1,
  UNIQUE KEY (building, floor, wing, room_number)
);
```

### Complaints Table
```sql
CREATE TABLE complaints (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  category VARCHAR(255) NOT NULL,
  status ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED'),
  priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
  image_url VARCHAR(500),
  admin_note TEXT,
  location_id BIGINT,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (location_id) REFERENCES locations(id)
);
```

---

## Deployment

### Build for Production

```bash
./mvnw clean package -DskipTests
```

JAR file will be created at: `target/campuscare-backend-0.0.1-SNAPSHOT.jar`

### Environment Variables (Production)

```env
JWT_SECRET=your-production-secret-key-minimum-32-characters
DB_URL=jdbc:mysql://production-host:3306/campuscare_db
DB_USERNAME=production_user
DB_PASSWORD=production_password
PORT=8080
```

### Docker Deployment (Optional)

```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/campuscare-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t nivara-backend .
docker run -p 8080:8080 \
  -e JWT_SECRET=your-secret \
  -e DB_URL=jdbc:mysql://host:3306/db \
  -e DB_USERNAME=user \
  -e DB_PASSWORD=pass \
  nivara-backend
```

---

## Project Statistics

- **Total Lines of Code:** 8,000+
- **Test Coverage:** 107 tests (100% passing)
- **API Endpoints:** 12+
- **Entities:** 3 (User, Complaint, Location)
- **DTOs:** 11
- **Custom Exceptions:** 9
- **Security Filters:** 2

---

## Development

### Code Style

- Java 21 features enabled
- Lombok for boilerplate reduction
- RESTful API design principles
- Layered architecture (Controller → Service → Repository)

### Logging

Default Spring Boot logging configuration.  
Logs available in console output.

### Hot Reload

Spring Boot DevTools enabled for automatic restart during development.

---

## Troubleshooting

### Common Issues

**1. Database Connection Failed**
```
Error: Access denied for user 'root'@'localhost'
```
Solution: Check database credentials in configuration

**2. JWT Secret Not Set**
```
Error: JWT_SECRET environment variable not set
```
Solution: Set JWT_SECRET in run-backend.ps1 or .env file

**3. Port Already in Use**
```
Error: Port 8080 is already in use
```
Solution: Stop the process using port 8080 or change PORT in configuration

**4. Tests Failing**
```
Error: Tests fail with database connection
```
Solution: Tests use H2, ensure H2 dependency is in pom.xml

---

## Related Repositories

- **Frontend:** [Nivara Frontend](https://github.com/rahultakale44/Nivara-frontend)

---

## License

This project is developed for educational purposes as part of academic coursework at MIT ADT University.

---

## Author

**Rahul Takale**  
Computer Science Student, MIT ADT University

---

## Acknowledgments

- MIT ADT University for project support
- Spring Boot community for excellent documentation
- Open source contributors for amazing libraries

---

<div align="center">

**Nivara Backend - Powering MIT ADT Campus Operations**

Made with ❤️ for MIT ADT University

</div>
