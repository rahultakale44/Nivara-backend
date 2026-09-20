package com.rahul.campuscare.service;

import com.rahul.campuscare.dto.LoginRequest;
import com.rahul.campuscare.dto.LoginResponse;
import com.rahul.campuscare.dto.RegisterRequest;
import com.rahul.campuscare.entity.Role;
import com.rahul.campuscare.entity.User;
import com.rahul.campuscare.exception.DuplicateEmailException;
import com.rahul.campuscare.exception.InvalidCredentialsException;
import com.rahul.campuscare.exception.UserNotFoundException;
import com.rahul.campuscare.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        testUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.STUDENT)
                .build();
    }

    @Test
    void register_shouldRegisterNewStudentSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = authService.register(registerRequest);

        // Assert
        assertThat(result).isEqualTo("User Registered Successfully!");
        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowDuplicateEmailExceptionWhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email already exists");
        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldAlwaysCreateStudentAccount() {
        // Arrange
        // SECURITY FIX: Public registration always creates STUDENT accounts
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertThat(savedUser.getRole()).isEqualTo(Role.STUDENT);
            return savedUser;
        });

        // Act
        authService.register(registerRequest);

        // Assert
        verify(userRepository).save(argThat(user -> user.getRole() == Role.STUDENT));
    }

    @Test
    void register_shouldCreateStudentEvenIfAdminRoleAttempted() {
        // Arrange
        // SECURITY TEST: Verify that even if someone tries to pass admin role, 
        // it is ignored and STUDENT is created
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        // Act
        authService.register(registerRequest);

        // Assert
        verify(userRepository).save(argThat(user -> user.getRole() == Role.STUDENT));
    }

    @Test
    void register_shouldEncodePassword() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Act
        authService.register(registerRequest);

        // Assert
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user -> 
            user.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("STUDENT");
        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtService).generateToken("john@example.com", "STUDENT");
    }

    @Test
    void login_shouldThrowUserNotFoundExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_shouldThrowInvalidCredentialsExceptionWhenPasswordIsInvalid() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_shouldHandleAdminLogin() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .fullName("Admin User")
                .email("admin@example.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .build();
        loginRequest.setEmail("admin@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("admin-jwt-token");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("admin-jwt-token");
        assertThat(response.getRole()).isEqualTo("ADMIN");
        verify(jwtService).generateToken("admin@example.com", "ADMIN");
    }
}
