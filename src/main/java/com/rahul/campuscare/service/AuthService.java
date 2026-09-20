package com.rahul.campuscare.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rahul.campuscare.dto.LoginRequest;
import com.rahul.campuscare.dto.LoginResponse;
import com.rahul.campuscare.dto.RegisterRequest;
import com.rahul.campuscare.entity.Role;
import com.rahul.campuscare.entity.User;
import com.rahul.campuscare.exception.DuplicateEmailException;
import com.rahul.campuscare.exception.InvalidCredentialsException;
import com.rahul.campuscare.exception.UserNotFoundException;
import com.rahul.campuscare.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        // SECURITY FIX: Public registration always creates STUDENT accounts
        // Admin accounts must be created through a separate administrative process
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .build();

        userRepository.save(user);

        return "User Registered Successfully!";
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponse(
                token,
                user.getRole().name()
        );
    }
}