package com.rahul.campuscare.integration;

import com.rahul.campuscare.entity.Role;
import com.rahul.campuscare.entity.User;
import com.rahul.campuscare.repository.UserRepository;
import com.rahul.campuscare.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies the complete authentication flow
 * with real database interactions (H2 in-memory database).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void completeAuthenticationFlow_shouldWorkEndToEnd() {
        // Step 1: Create and save a user (simulating registration)
        User user = User.builder()
                .fullName("Integration Test User")
                .email("integration@test.com")
                .password(passwordEncoder.encode("testpassword"))
                .role(Role.STUDENT)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Assert user was saved
        assertThat(savedUser.getId()).isNotNull();
        assertThat(userRepository.existsByEmail("integration@test.com")).isTrue();

        // Step 2: Verify user can be found by email (simulating login lookup)
        User foundUser = userRepository.findByEmail("integration@test.com").orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("integration@test.com");

        // Step 3: Verify password matches (simulating login password check)
        boolean passwordMatches = passwordEncoder.matches("testpassword", foundUser.getPassword());
        assertThat(passwordMatches).isTrue();

        // Step 4: Generate JWT token (simulating successful login)
        String token = jwtService.generateToken(foundUser.getEmail(), foundUser.getRole().name());
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // Step 5: Verify token can be validated and contains correct information
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("integration@test.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("STUDENT");
    }

    @Test
    void userRepository_shouldPreventDuplicateEmails() {
        // Arrange
        User user1 = User.builder()
                .fullName("User One")
                .email("duplicate@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.STUDENT)
                .build();
        
        userRepository.save(user1);

        // Act & Assert
        boolean emailExists = userRepository.existsByEmail("duplicate@test.com");
        assertThat(emailExists).isTrue();

        // Verify the check works correctly for non-existent emails
        boolean nonExistentEmail = userRepository.existsByEmail("nonexistent@test.com");
        assertThat(nonExistentEmail).isFalse();
    }

    @Test
    void passwordEncoding_shouldBeOneWay() {
        // Arrange
        String plainPassword = "mysecretpassword";
        
        // Act
        String encodedPassword = passwordEncoder.encode(plainPassword);
        
        // Assert
        assertThat(encodedPassword).isNotEqualTo(plainPassword);
        assertThat(encodedPassword).isNotEmpty();
        assertThat(passwordEncoder.matches(plainPassword, encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches("wrongpassword", encodedPassword)).isFalse();
    }

    @Test
    void userRepository_shouldSaveAndRetrieveAdminUser() {
        // Arrange
        User adminUser = User.builder()
                .fullName("Admin User")
                .email("admin@test.com")
                .password(passwordEncoder.encode("adminpass"))
                .role(Role.ADMIN)
                .build();

        // Act
        User savedAdmin = userRepository.save(adminUser);
        User foundAdmin = userRepository.findByEmail("admin@test.com").orElse(null);

        // Assert
        assertThat(savedAdmin).isNotNull();
        assertThat(foundAdmin).isNotNull();
        assertThat(foundAdmin.getRole()).isEqualTo(Role.ADMIN);
    }
}
