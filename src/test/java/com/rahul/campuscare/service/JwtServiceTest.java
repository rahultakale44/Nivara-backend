package com.rahul.campuscare.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set the JWT secret for testing using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", "test-secret-key-for-unit-tests-minimum-32-characters");
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // Arrange
        String email = "test@example.com";
        String role = "STUDENT";

        // Act
        String token = jwtService.generateToken(email, role);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        // Arrange
        String email = "student@campuscare.com";
        String role = "STUDENT";
        String token = jwtService.generateToken(email, role);

        // Act
        String extractedEmail = jwtService.extractEmail(token);

        // Assert
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void extractRole_shouldReturnCorrectRole() {
        // Arrange
        String email = "admin@campuscare.com";
        String role = "ADMIN";
        String token = jwtService.generateToken(email, role);

        // Act
        String extractedRole = jwtService.extractRole(token);

        // Assert
        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        // Arrange
        String email = "valid@example.com";
        String role = "STUDENT";
        String token = jwtService.generateToken(email, role);

        // Act
        boolean isValid = jwtService.isTokenValid(token);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.isTokenValid(invalidToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalseForMalformedToken() {
        // Arrange
        String malformedToken = "notavalidjwt";

        // Act
        boolean isValid = jwtService.isTokenValid(malformedToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void generateToken_shouldHandleStudentRole() {
        // Arrange
        String email = "student@example.com";
        String role = "STUDENT";

        // Act
        String token = jwtService.generateToken(email, role);
        String extractedRole = jwtService.extractRole(token);

        // Assert
        assertThat(extractedRole).isEqualTo("STUDENT");
    }

    @Test
    void generateToken_shouldHandleAdminRole() {
        // Arrange
        String email = "admin@example.com";
        String role = "ADMIN";

        // Act
        String token = jwtService.generateToken(email, role);
        String extractedRole = jwtService.extractRole(token);

        // Assert
        assertThat(extractedRole).isEqualTo("ADMIN");
    }
}
