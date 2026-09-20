# CampusCare Backend Startup Script
# Sets environment variables and starts the Spring Boot application

$env:JWT_SECRET = "campuscare-dev-secret-key-for-local-development-only-minimum-32-chars"
$env:DB_URL = "jdbc:mysql://localhost:3306/campuscare_db"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "rahul@44"

Write-Host "Starting CampusCare Backend..."
Write-Host "Database: $env:DB_URL"
Write-Host "User: $env:DB_USERNAME"
Write-Host ""

./mvnw spring-boot:run
