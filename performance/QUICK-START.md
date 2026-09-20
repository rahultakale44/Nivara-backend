# Quick Start Guide - JMeter Performance Testing

## Prerequisites Checklist

- [ ] Apache JMeter installed (download from https://jmeter.apache.org/download_jmeter.cgi)
- [ ] CampusCare backend running on http://localhost:8080
- [ ] Test user account created

## Step 1: Start the Backend

```bash
cd campuscare-backend
./mvnw spring-boot:run
```

Wait until you see: `Started CampuscareBackendApplication`

## Step 2: Create Test User

### Windows:
```bash
cd performance
setup-test-user.bat
```

### Linux/Mac:
```bash
cd performance
chmod +x setup-test-user.sh
./setup-test-user.sh
```

### Or manually via curl:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Performance Test User","email":"perftest@campuscare.test","password":"TestPassword123"}'
```

## Step 3: Run JMeter Test

### Option A: GUI Mode (Recommended for first run)

```bash
jmeter -t CampusCare-API-Baseline.jmx
```

Then click the green "Start" button (play icon) in JMeter.

### Option B: Command-Line Mode (For actual benchmarking)

```bash
jmeter -n -t CampusCare-API-Baseline.jmx -l results.jtl -e -o report
```

View results by opening `report/index.html` in a web browser.

## Step 4: Review Results

### In GUI Mode:
- Click on "Summary Report" listener
- Click on "View Results in Table" for detailed view

### In Command-Line Mode:
- Open `report/index.html` in browser
- Check `results.jtl` for raw data

## Expected Results

✅ **All requests should succeed (0% error rate)**

Typical metrics for local development:
- Login: ~100-500ms
- Create Complaint: ~50-300ms
- Get Complaints: ~50-200ms
- Throughput: ~20-100 requests/second

*Note: Actual values depend on your hardware and database performance*

## Troubleshooting

### "Connection Refused"
- Backend not running - start with `./mvnw spring-boot:run`

### "401 Unauthorized"
- Test user doesn't exist - run setup script
- Wrong credentials - check JMeter variables

### "400 Bad Request"
- Check backend logs for validation errors

### JMeter not found
- Install JMeter and add to PATH
- Or use full path: `/path/to/jmeter/bin/jmeter`

## Configuration Variables

Edit in JMeter Test Plan or override via command line:

| Variable | Default Value | Description |
|----------|---------------|-------------|
| BASE_URL | http://localhost:8080 | Backend URL |
| TEST_EMAIL | perftest@campuscare.test | Test user email |
| TEST_PASSWORD | TestPassword123 | Test user password |

### Override Example:
```bash
jmeter -n -t CampusCare-API-Baseline.jmx \
  -JBASE_URL=http://localhost:9090 \
  -JTEST_EMAIL=other@test.com \
  -l results.jtl
```

## Test Metrics Explained

- **# Samples**: Total number of requests
- **Average**: Mean response time (milliseconds)
- **Min**: Fastest response time
- **Max**: Slowest response time
- **Std. Dev.**: Response time variation
- **Error %**: Percentage of failed requests (should be 0%)
- **Throughput**: Requests per second
- **KB/sec**: Data transfer rate

## Clean Up

The test creates complaints in your database. To clean up:

```sql
-- Connect to your MySQL database
DELETE FROM complaint WHERE user_id = (SELECT id FROM user WHERE email = 'perftest@campuscare.test');
```

Or simply clear the test database if using H2 for development.

---

**Test Configuration:**
- 10 concurrent users
- 10 second ramp-up
- 20 iterations per user
- ~600 total requests
