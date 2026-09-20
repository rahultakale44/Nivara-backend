package com.rahul.campuscare.security;

import com.rahul.campuscare.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinueWhenNoAuthorizationHeader() throws Exception {
        // Arrange - no Authorization header

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldContinueWhenAuthorizationHeaderDoesNotStartWithBearer() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Basic sometoken");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldNotSetAuthenticationWhenTokenIsInvalid() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer invalid-token");
        when(jwtService.isTokenValid(anyString())).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - authentication should not be set, letting Spring Security handle it
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldSetAuthenticationWhenTokenIsValid() throws Exception {
        // Arrange
        String validToken = "valid-jwt-token";
        request.addHeader("Authorization", "Bearer " + validToken);
        
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractEmail(validToken)).thenReturn("user@example.com");
        when(jwtService.extractRole(validToken)).thenReturn("STUDENT");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("user@example.com");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .hasSize(1);
    }

    @Test
    void doFilterInternal_shouldSetAdminRoleCorrectly() throws Exception {
        // Arrange
        String validToken = "admin-jwt-token";
        request.addHeader("Authorization", "Bearer " + validToken);
        
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractEmail(validToken)).thenReturn("admin@example.com");
        when(jwtService.extractRole(validToken)).thenReturn("ADMIN");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN");
    }
}
