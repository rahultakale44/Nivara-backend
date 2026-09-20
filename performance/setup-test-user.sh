#!/bin/bash
# Setup script for CampusCare Performance Test User
# This creates a dedicated test account for JMeter performance testing

echo "Creating performance test user..."

curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Performance Test User",
    "email": "perftest@campuscare.test",
    "password": "TestPassword123"
  }'

echo ""
echo ""
echo "Test user created successfully!"
echo "Email: perftest@campuscare.test"
echo "Password: TestPassword123"
echo "Role: STUDENT (default)"
echo ""
echo "You can now run the JMeter test plan."
