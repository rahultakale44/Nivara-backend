# CampusCare Backend

CampusCare is a full-stack campus complaint management system that allows students to submit and track complaints while administrators can manage complaint status and administrative functions. This backend provides a RESTful API with JWT authentication, role-based authorization, input validation, structured exception handling, and comprehensive automated testing.

This is a personal portfolio/interview project demonstrating clean architecture, security best practices, testing strategies, and CI/CD implementation.

---

## Key Features

### Authentication & Authorization
- **Student Registration** - Public registration endpoint (role automatically assigned as STUDENT)
- **JWT Authentication** - Secure token-based authentication with configurable secret
- **Role-Based Access Control** - STUDENT and ADMIN roles with endpoint-level authorization
- **Protected Endpoints** - JWT required for authenticated operations
- **Proper HTTP Status Handling** - 401 for unauthenticated, 403 for unauthorized

### Complaint Management
- **Create Complaints** - Students can submit complaints with title, description, category, and optional image
- **Track Complaints** - Students can view their own complaints
- **Admin Dashboard** - Administrators can view all complaints and statistics
- **Status Updates** - Admins can update complaint status with notes
- **Individual Complaint View** - Retrieve complaint details by ID

### File Handling
- **Image Upload** - Authenticated file upload with validation
- **File Size Validation** - 5MB maximum file size
- **Filename Sanitization** - Protection against directory traversal attacks
- **UUID-based Storage** - Secure file naming and organization

### Data Validation & Error Handling
- **Jakarta Bean Validation** - Request-level validation with meaningful error messages
- **Global Exception Handler** - Centralized exception handling with structured ErrorResponse
- **Custom Exceptions** - Domain-specific exceptions (ComplaintNotFoundException, InvalidCredentialsException, etc.)
- **Consistent API Responses** - Proper HTTP status codes (400, 401, 403, 404, 409, 500)

### Testing & Quality Assurance
- **83 Automated Tests** - Unit, controller, security, integration, and validation tests
- **H2 Test Database** - Isolated integration testing
- **100% Test Pass Rate** - All tests passing with 0 failures
- **MockMvc** - Controller testing with security context
- **Mockito** - Service and business logic testing

### CI/CD
- **GitHub Actions** - Automated test execution on push/PR
- **Maven Build** - Reproducible builds with Maven wrapper
- **Java 21** - Modern JDK with performance improvements

### Performance Testing
- **JMeter Test Plan** - Reproducible performance baseline
- **Authentication Flow** - JWT extraction and usage testing
- **Authenticated API Benchmarking** - Login, create complaint, get complaints
- **Configurable Load** - 10 users, 10s ramp-up, 20 iterations (~600 requests)

---

## Architecture

```
┌──────────────────┐
│  React Frontend  │
└────────┬─────────┘
         │ REST / JSON
         ▼
┌──────────────────┐
│  Spring Boot API │
└────────┬─────────┘
         │
    ┌────┴─────┬────────────┬────────────┐
    ▼          ▼            ▼            ▼
Security   Controllers  Validation   Exception
+ JWT                                 Handling
    │          │
    └──────────┼───────────┐
               ▼           ▼
           Services    Repositories
                           │
                  Spring Data JPA
                           │
                           ▼
                        MySQL
```

### Testing Architecture

```
JUnit + Mockito + MockMvc
         │
         ▼
Spring Boot Test Context
         │
         ▼
    H2 Test Database
```

### CI/CD Pipeline

```
Git Push / Pull Request
         │
         ▼
   GitHub Actions
         │
         ▼
   Java 21 + Maven
         │
         ▼
 ./mvnw clean test
         │
         ▼
   83 Automated Tests
         │
         ▼
   Build/Test Result
```

---

## Technology Stack

### Backend
- **Java 21** - Modern LTS JDK with performance improvements
- **Spring Boot 3.5.14** - Production-ready application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database abstraction with Hibernate
- **Spring Validation** - Jakarta Bean Validation
- **JWT (jjwt 0.12.5)** - Token-based authentication
- **Maven** - Dependency management and build automation
- **Lombok** - Boilerplate reduction
- **MySQL** - Production database

### Testing
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **MockMvc** - Spring MVC testing
- **Spring Boot Test** - Integration testing support
- **H2** - In-memory test database

### DevOps
- **Git/GitHub** - Version control
- **GitHub Actions** - Continuous Integration
- **Maven Wrapper** - Reproducible builds

### Performance Testing
- **Apache JMeter** - Load testing and performance baseline

---

## Security

### Authentication
- **JWT-Based Authentication** - Stateless token-based authentication
- **JWT Secret Externalization** - Secret configured via `JWT_SECRET` environment variable
- **Test-Only Secrets** - Separate secrets for automated testing (`application-test.properties`)
- **BCrypt Password Hashing** - Secure password storage
- **Token Expiration** - 1-hour JWT token lifetime

### Authorization
- **Role-Based Access Control** - STUDENT and ADMIN roles
- **Endpoint Protection** - Method-level security annotations
- **Public Registration Cannot Assign ADMIN** - Role field removed from RegisterRequest
- **Proper HTTP Status Codes** - 401 Unauthorized vs 403 Forbidden

### Input & File Security
- **Bean Validation** - Request-level validation with @Valid
- **File Size Limits** - 5MB maximum file upload
- **Filename Sanitization** - Directory traversal protection
- **Authenticated File Upload** - Upload endpoint requires authentication
- **Structured Validation Errors** - Clear error messages with field-level detail

### Security Considerations
This is a portfolio project demonstrating security fundamentals. Production deployments would require additional controls such as:
- Rate limiting and throttling
- Advanced password policies
- Account lockout mechanisms
- Security headers (CSP, HSTS)
- HTTPS enforcement
- Secret management systems (Vault, AWS Secrets Manager)
- Audit logging
- CORS policy refinement

---

## Testing Strategy

### Test Results
```
Tests run: 83
Failures: 0
Errors: 0
Skipped: 0
Success Rate: 100%
```

### Test Categories

#### Unit Tests (31 tests)
- **JwtService** (8 tests) - Token generation, extraction, validation
- **AuthService** (9 tests) - Registration, login, exception handling
- **ComplaintService** (14 tests) - CRUD operations, authorization logic

#### Controller Tests (31 tests)
- **AuthController** (4 tests) - Registration and login endpoints
- **ComplaintController** (12 tests) - Complaint API endpoints with security
- **FileUploadController** (6 tests) - File upload validation and security
- **ValidationTest** (9 tests) - Bean validation error handling

#### Security Tests (10 tests)
- **AdminRegistrationSecurityTest** (5 tests) - ADMIN role protection
- **JwtAuthenticationFilterTest** (5 tests) - JWT authentication flow

#### Integration Tests (10 tests)
- **AuthenticationIntegrationTest** (4 tests) - End-to-end authentication
- **ComplaintIntegrationTest** (6 tests) - End-to-end complaint management

#### Application Test (1 test)
- **CampuscareBackendApplicationTests** - Spring context loading

### Test Database
- **H2 In-Memory Database** - Isolated integration testing
- **Automatic Schema Creation** - JPA `create-drop` strategy
- **Test-Specific Configuration** - `application-test.properties`

### Running Tests
```bash
./mvnw clean test
```

---

## Engineering Problems & Solutions

| Problem | How It Was Discovered | Solution |
|---------|----------------------|----------|
| No automated test safety net | Initial project audit | Added 83 unit, controller, security, and integration tests with 100% pass rate |
| Public registration could assign ADMIN role | Security testing | Removed role field from RegisterRequest; role automatically assigned as STUDENT |
| File upload endpoint was unauthenticated | Security testing | Changed endpoint from `.permitAll()` to `.authenticated()` and added file validation |
| Generic runtime exceptions returned poor API responses | API testing | Created custom exceptions (ComplaintNotFoundException, InvalidCredentialsException, etc.) and GlobalExceptionHandler with structured ErrorResponse |
| Anonymous requests returned generic 403 status | Security configuration testing | Implemented UnauthorizedEntryPoint to return proper 401 status for unauthenticated requests |
| JWT secret was hardcoded in source code | Security/configuration review | Externalized JWT secret via `JWT_SECRET` environment variable; test-only secret in application-test.properties |
| No validation on user inputs | Input testing | Added Jakarta Bean Validation annotations to all DTOs with @Valid in controllers |
| Validation errors returned 500 status | Exception handling testing | Added @ControllerAdvice exception handler for MethodArgumentNotValidException returning 400 Bad Request |
| Regressions could reach repository unnoticed | Development workflow | Implemented GitHub Actions CI pipeline to run all 83 tests on every push/PR |
| No measurable API performance baseline | Performance review | Created reproducible JMeter test plan with login flow, JWT extraction, and authenticated complaint APIs |

---

## CI/CD

### GitHub Actions Workflow

```
Push / Pull Request
         ↓
   GitHub Actions
         ↓
Checkout Repository
         ↓
   Setup Java 21
         ↓
  Maven Wrapper (chmod)
         ↓
   ./mvnw clean test
         ↓
   Build/Test Result
```

### Workflow Configuration
- **Triggers:** Push or Pull Request to `main` or `master` branches
- **Runner:** ubuntu-latest
- **JDK:** Java 21 (Temurin distribution)
- **Build Tool:** Maven (wrapper)
- **Test Command:** `./mvnw clean test`
- **Maven Cache:** Enabled for faster builds

### Current CI Scope
- Backend test suite validation (83 tests)
- Build verification
- Test failure detection

### Not Included
- Automatic deployment
- Docker builds
- Cloud deployments
- Coverage reporting (tests run successfully but coverage not measured)

---

## Performance Testing

### JMeter Baseline Test Plan

**Test Configuration:**
- **Concurrent Users:** 10
- **Ramp-up Period:** 10 seconds (1 user/second)
- **Loop Count:** 20 iterations per user
- **Total Requests:** ~600 (10 × 20 × 3 endpoints)
- **Test Duration:** ~30-60 seconds (depends on response times)

**APIs Tested:**
1. **POST /api/auth/login** - Authentication with JWT extraction
2. **POST /api/complaints** - Create complaint (authenticated)
3. **GET /api/complaints/my** - Retrieve user complaints (authenticated)

**Metrics Collected:**
- Response times (avg, min, max, median, 90th/95th percentiles)
- Throughput (requests/second)
- Error percentage
- Data transfer rate (KB/sec)

**Execution Status:**
⚠️ **Test plan prepared and validated but NOT yet executed.** JMeter installation required for actual benchmark.

**Important Notes:**
- This is a local development baseline, NOT production capacity testing
- Small load (10 users) suitable for local MySQL database
- Designed for performance regression detection, not stress testing
- Test creates actual complaints in database (cleanup may be needed)

**Running the Benchmark:**
```bash
# From performance/ directory
jmeter -n -t CampusCare-API-Baseline.jmx -l results.jtl -e -o report
```

See `performance/README.md` and `performance/QUICK-START.md` for detailed instructions.

---

## API Overview

| Method | Endpoint | Purpose | Authentication | Role Required |
|--------|----------|---------|----------------|---------------|
| POST | /api/auth/register | Register new student | Public | None |
| POST | /api/auth/login | User login | Public | None |
| POST | /api/complaints | Create complaint | JWT | STUDENT |
| GET | /api/complaints/my | Get own complaints | JWT | STUDENT |
| GET | /api/complaints | Get all complaints | JWT | ADMIN |
| GET | /api/complaints/{id} | Get complaint by ID | JWT | Authenticated |
| PUT | /api/complaints/{id}/status | Update complaint status | JWT | ADMIN |
| POST | /api/upload | Upload image file | JWT | Authenticated |

### Request/Response Examples

**Register:**
```json
POST /api/auth/register
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
→ 200 OK: "User registered successfully"
```

**Login:**
```json
POST /api/auth/login
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
→ 200 OK: {"token": "eyJhbGc...", "role": "STUDENT"}
```

**Create Complaint:**
```json
POST /api/complaints
Authorization: Bearer <token>
{
  "title": "Broken AC in Library",
  "description": "The air conditioning unit in the library has been non-functional for 3 days",
  "category": "Infrastructure",
  "imageUrl": "http://localhost:8080/uploads/uuid_filename.jpg"
}
→ 200 OK: {complaint object}
```

**Validation Error Example:**
```json
POST /api/auth/register
{
  "fullName": "AB",
  "email": "invalid-email",
  "password": "123"
}
→ 400 Bad Request: {
  "field": "email",
  "message": "Email must be valid"
}
```

---

## Project Structure

```
campuscare-backend/
├── src/
│   ├── main/
│   │   ├── java/com/rahul/campuscare/
│   │   │   ├── CampuscareBackendApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ComplaintController.java
│   │   │   │   └── FileUploadController.java
│   │   │   ├── dto/
│   │   │   │   ├── AdminStatsResponse.java
│   │   │   │   ├── CreateComplaintRequest.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   └── UpdateComplaintStatusRequest.java
│   │   │   ├── entity/
│   │   │   │   ├── Complaint.java
│   │   │   │   ├── ComplaintStatus.java
│   │   │   │   ├── Role.java
│   │   │   │   └── User.java
│   │   │   ├── exception/
│   │   │   │   ├── ComplaintNotFoundException.java
│   │   │   │   ├── DuplicateEmailException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InvalidCredentialsException.java
│   │   │   │   ├── InvalidFileException.java
│   │   │   │   └── UserNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   ├── ComplaintRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── UnauthorizedEntryPoint.java
│   │   │   └── service/
│   │   │       ├── AuthService.java
│   │   │       ├── ComplaintService.java
│   │   │       └── JwtService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       ├── java/com/rahul/campuscare/
│       │   ├── CampuscareBackendApplicationTests.java
│       │   ├── controller/
│       │   │   ├── AuthControllerTest.java
│       │   │   ├── ComplaintControllerTest.java
│       │   │   ├── FileUploadControllerTest.java
│       │   │   └── ValidationTest.java
│       │   ├── integration/
│       │   │   ├── AuthenticationIntegrationTest.java
│       │   │   └── ComplaintIntegrationTest.java
│       │   ├── security/
│       │   │   ├── AdminRegistrationSecurityTest.java
│       │   │   └── JwtAuthenticationFilterTest.java
│       │   └── service/
│       │       ├── AuthServiceTest.java
│       │       ├── ComplaintServiceTest.java
│       │       └── JwtServiceTest.java
│       └── resources/
│           └── application-test.properties
├── performance/
│   ├── CampusCare-API-Baseline.jmx
│   ├── README.md
│   ├── QUICK-START.md
│   ├── setup-test-user.sh
│   └── setup-test-user.bat
├── .github/
│   └── workflows/
│       └── ci.yml
├── .env.example
├── pom.xml
├── mvnw
└── mvnw.cmd
```

---

## Setup Instructions

### Requirements
- **Java 21** - JDK 21 or higher
- **Maven** - Included via Maven Wrapper (./mvnw)
- **MySQL** - Local or remote MySQL instance
- **Apache JMeter** - Optional, only for performance testing

### Environment Variables

Create a `.env` file or set environment variables:

```bash
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/campuscare
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# JWT Configuration (REQUIRED)
JWT_SECRET=your-secure-jwt-secret-minimum-32-characters-for-production

# Optional: Server Port
PORT=8080
```

**Important:** 
- Generate a secure JWT secret: `openssl rand -base64 32`
- Never commit real secrets to Git
- See `.env.example` for reference

### Database Setup

```sql
CREATE DATABASE campuscare;
```

The application will automatically create tables on startup (JPA `ddl-auto=update`).

### Running the Application

**Run Tests:**
```bash
./mvnw clean test
```

**Run Application:**
```bash
# Ensure environment variables are set
export JWT_SECRET="your-secure-secret"
export DB_URL="jdbc:mysql://localhost:3306/campuscare"
export DB_USERNAME="your_username"
export DB_PASSWORD="your_password"

./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### Performance Testing

See `performance/README.md` for detailed JMeter setup and execution instructions.

**Quick Start:**
```bash
cd performance
./setup-test-user.sh  # Create test account
jmeter -t CampusCare-API-Baseline.jmx  # Run test
```

---

## Limitations

- **JMeter Baseline Not Yet Executed** - Test plan prepared but requires JMeter installation
- **Performance Testing Scope** - Local development baseline, not production capacity testing
- **Portfolio Project** - Demonstrates fundamentals; production systems require additional features
- **Single-Server Architecture** - No distributed architecture or microservices
- **No Automated Deployment** - CI validates tests but does not deploy
- **Production Security** - Additional controls needed for enterprise deployment (rate limiting, advanced monitoring, secret management systems)

---

## License

This is a personal portfolio project created for educational and interview purposes.

---

## Author

Rahul - Final Year Computer Science Student

**Project Purpose:** Portfolio/Interview Project demonstrating full-stack development, security best practices, testing strategies, and CI/CD implementation.
