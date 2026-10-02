package com.denzil.auth_service.service;

import com.denzil.auth_service.dto.AuthResponse;
import com.denzil.auth_service.dto.LoginRequest;
import com.denzil.auth_service.dto.RegisterRequest;
import com.denzil.auth_service.entity.User;
import com.denzil.auth_service.exception.BadRequestException;
import com.denzil.auth_service.exception.ConflictException;
import com.denzil.auth_service.exception.ResourceNotFoundException;
import com.denzil.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new ConflictException("Email already in use");
        });

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);

        return new AuthResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        return new AuthResponse(user.getId(), user.getName(), user.getEmail());
    }

    public AuthResponse getCurrentUser(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new AuthResponse(user.getId(), user.getName(), user.getEmail());
    }
}
