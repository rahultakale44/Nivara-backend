# CampusCare API Performance Baseline

## Overview

This directory contains a JMeter test plan to establish a reproducible performance baseline for the CampusCare REST API. This is designed for local development and technical interview discussion purposes only.

## Test Scope

### APIs Tested

1. **POST /api/auth/login** - User authentication
2. **POST /api/complaints** - Create new complaint (STUDENT role required)
3. **GET /api/complaints/my** - Retrieve user's complaints (STUDENT role required)

### Load Configuration

- **Concurrent Users:** 10
- **Ramp-up Period:** 10 seconds (1 user per second)
- **Loop Count:** 20 iterations per user
- **Total Requests:** ~600 (10 users × 20 iterations × 3 requests)

This is intentionally a small, reproducible test suitable for local development environments.

## Prerequisites

### 1. Apache JMeter

Download and install Apache JMeter 5.6.3 or later:
- https://jmeter.apache.org/download_jmeter.cgi
- Extract and add `bin/` directory to your PATH

### 2. Backend Application

Ensure the CampusCare backend is running locally:

```bash
# Start the backend (from campuscare-backend directory)
./mvnw spring-boot:run
```

The backend should be accessible at: `http://localhost:8080`

### 3. Test User Account

Create a test user with STUDENT role before running the performance test:

**Option A: Using the API**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Performance Test User",
    "email": "perftest@campuscare.test",
    "password": "TestPassword123"
  }'
```

**Option B: Manually via database**

Insert a test user directly into your MySQL database.

**Default Test Credentials (configured in JMeter):**
- Email: `perftest@campuscare.test`
- Password: `TestPassword123`

**IMPORTANT:** These credentials are for testing only. Never use real user credentials.

## Running the Test

### GUI Mode (For Development/Validation)

```bash
# From the performance directory
jmeter -t CampusCare-API-Baseline.jmx
```

Or use JMeter GUI to open `CampusCare-API-Baseline.jmx`

### Command-Line Mode (For Actual Benchmark)

```bash
# From the performance directory
jmeter -n -t CampusCare-API-Baseline.jmx -l results.jtl -e -o report
```

Parameters:
- `-n` : Non-GUI mode
- `-t` : Test plan file
- `-l` : Results file (JTL format)
- `-e` : Generate HTML report
- `-o` : Output directory for HTML report

## Customizing Variables

You can override variables from the command line:

```bash
jmeter -n -t CampusCare-API-Baseline.jmx \
  -JBASE_URL=http://localhost:8080 \
  -JTEST_EMAIL=your_test_user@example.com \
  -JTEST_PASSWORD=YourTestPassword \
  -l results.jtl
```

## Measurements Collected

The test plan captures:

1. **Number of Samples** - Total requests executed
2. **Average Response Time** - Mean time in milliseconds
3. **Minimum Response Time** - Fastest response
4. **Maximum Response Time** - Slowest response
5. **Throughput** - Requests per second
6. **Error %** - Percentage of failed requests
7. **Response Codes** - HTTP status codes distribution

## Understanding Results

### Summary Report

After the test completes, view the "Summary Report" listener in JMeter GUI, or review the generated HTML report.

**Expected Behavior:**
- Login requests should succeed (HTTP 200) and return JWT token
- Complaint creation should succeed (HTTP 200) with authenticated requests
- Get complaints should succeed (HTTP 200) with authenticated requests
- Error rate should be 0% for a healthy system

**Typical Response Times (Local Development):**
- These will vary based on your hardware, database performance, and system load
- Baseline metrics are for comparison, not absolute performance claims
- Use these results to detect regressions or compare optimization efforts

### Interpreting Metrics

- **Response Time:** Includes network latency, processing time, and database queries
- **Throughput:** Actual requests/sec achieved by the test
- **Error %:** Should be 0% for a properly configured system

## Authentication Flow

The test plan implements JWT authentication:

1. **Login Request** sends credentials to `/api/auth/login`
2. **JSON Extractor** captures the JWT token from the `token` field in the response
3. **Subsequent Requests** include `Authorization: Bearer <token>` header
4. Each thread (virtual user) performs its own login to obtain a unique token

## Limitations

- **Local Development Only:** This test is designed for local MySQL/H2 database
- **Small Load:** 10 concurrent users is not representative of production load
- **No Think Time:** Real users pause between actions; this test does not
- **Single Scenario:** Only tests complaint creation flow, not all endpoints
- **No Data Cleanup:** Test creates complaints in the database

## Observations

**Potential Performance Considerations:**

1. Database queries are executed synchronously
2. No caching layer present
3. BCrypt password hashing is CPU-intensive (by design for security)
4. Each complaint creation triggers database write operations
5. No connection pooling optimization applied

**Note:** These are observations, not criticisms. This is a portfolio project demonstrating clean architecture, not production-optimized infrastructure.

## Safety

- Do NOT run this against production environments
- Do NOT increase load beyond 50 concurrent users without reviewing database capacity
- Do NOT run extended duration tests (>5 minutes) without monitoring
- Test data will accumulate in the database

## Files

- `CampusCare-API-Baseline.jmx` - JMeter test plan
- `README.md` - This documentation
- `results.jtl` - Generated test results (after execution)
- `report/` - Generated HTML report directory (after execution)

## Troubleshooting

### Connection Refused

- Ensure backend is running on port 8080
- Check firewall settings

### Authentication Errors (401)

- Verify test user exists in database
- Check credentials match in JMeter variables
- Ensure user has STUDENT role

### Validation Errors (400)

- Check request payloads match API validation rules
- Complaint title: 3-200 characters
- Complaint description: 10-2000 characters

### Database Errors (500)

- Ensure MySQL is running
- Check database credentials in backend configuration
- Verify database schema is initialized

## Next Steps

After establishing the baseline:
1. Document current metrics for comparison
2. Identify performance bottlenecks (if needed)
3. Test optimization impacts by re-running this baseline
4. Discuss trade-offs in technical interviews

---

**Created for:** CampusCare Portfolio Project  
**Purpose:** Performance baseline and interview discussion  
**Environment:** Local development only
