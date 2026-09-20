package com.rahul.campuscare.security;

import com.rahul.campuscare.dto.RegisterRequest;
import com.rahul.campuscare.entity.Role;
import com.rahul.campuscare.entity.User;
import com.rahul.campuscare.repository.UserRepository;
import com.rahul.campuscare.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Security regression test to verify that public registration cannot create ADMIN accounts.
 * This addresses the critical security vulnerability where users could self-register as admins.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminRegistrationSecurityTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void publicRegistration_shouldAlwaysCreateStudentAccount() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Regular User");
        request.setEmail("user@example.com");
        request.setPassword("password123");

        // Act
        authService.register(request);

        // Assert
        User savedUser = userRepository.findByEmail("user@example.com").orElseThrow();
        assertThat(savedUser.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void publicRegistration_cannotCreateAdminAccount() {
        // Arrange
        // SECURITY TEST: Even if a malicious user tries to register as ADMIN,
        // the system should create a STUDENT account instead
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Malicious User");
        request.setEmail("malicious@example.com");
        request.setPassword("password123");
        // Note: The role field has been removed from RegisterRequest

        // Act
        authService.register(request);

        // Assert
        User savedUser = userRepository.findByEmail("malicious@example.com").orElseThrow();
        assertThat(savedUser.getRole()).isEqualTo(Role.STUDENT);
        assertThat(savedUser.getRole()).isNotEqualTo(Role.ADMIN);
    }

    @Test
    void existingAdminUsers_canStillLogin() {
        // Arrange
        // Create an existing ADMIN user (simulating one created before the security fix)
        User existingAdmin = User.builder()
                .fullName("Existing Admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("adminpass"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(existingAdmin);

        // Act
        User foundAdmin = userRepository.findByEmail("admin@example.com").orElseThrow();

        // Assert
        assertThat(foundAdmin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(passwordEncoder.matches("adminpass", foundAdmin.getPassword())).isTrue();
    }

    @Test
    void multipleStudentRegistrations_shouldAllHaveStudentRole() {
        // Arrange & Act
        for (int i = 1; i <= 3; i++) {
            RegisterRequest request = new RegisterRequest();
            request.setFullName("User " + i);
            request.setEmail("user" + i + "@example.com");
            request.setPassword("password" + i);
            authService.register(request);
        }

        // Assert
        long totalUsers = userRepository.count();
        assertThat(totalUsers).isEqualTo(3);

        for (int i = 1; i <= 3; i++) {
            User user = userRepository.findByEmail("user" + i + "@example.com").orElseThrow();
            assertThat(user.getRole()).isEqualTo(Role.STUDENT);
        }
    }

    @Test
    void studentAndAdminUsers_canCoexist() {
        // Arrange
        // Create an admin user directly (bypass public registration)
        User adminUser = User.builder()
                .fullName("Admin User")
                .email("admin@example.com")
                .password(passwordEncoder.encode("adminpass"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(adminUser);

        // Create a student via public registration
        RegisterRequest studentRequest = new RegisterRequest();
        studentRequest.setFullName("Student User");
        studentRequest.setEmail("student@example.com");
        studentRequest.setPassword("studentpass");
        authService.register(studentRequest);

        // Act
        User admin = userRepository.findByEmail("admin@example.com").orElseThrow();
        User student = userRepository.findByEmail("student@example.com").orElseThrow();

        // Assert
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(student.getRole()).isEqualTo(Role.STUDENT);
        assertThat(userRepository.count()).isEqualTo(2);
    }
}
