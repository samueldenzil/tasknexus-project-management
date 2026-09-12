package com.denzil.project_management.auth.service;

import com.denzil.project_management.auth.dto.AuthResponse;
import com.denzil.project_management.auth.dto.LoginRequest;
import com.denzil.project_management.auth.dto.RegisterRequest;
import com.denzil.project_management.shared.exception.BadRequestException;
import com.denzil.project_management.shared.exception.ConflictException;
import com.denzil.project_management.user.entity.User;
import com.denzil.project_management.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setName("Alice");
        existingUser.setEmail("alice@example.com");
        existingUser.setPassword("hashed-password");
    }

    // -- register ----------------------------------------------------------

    @Test
    @DisplayName("register: happy path returns AuthResponse with saved user details")
    void register_happyPath_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password123");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        AuthResponse response = authService.register(request);

        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.email()).isEqualTo("alice@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: duplicate email throws ConflictException (409)")
    void register_duplicateEmail_throwsConflictException() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password123");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any());
    }

    // -- login -------------------------------------------------------------

    @Test
    @DisplayName("login: correct credentials returns AuthResponse")
    void login_correctCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("alice@example.com", "password123");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);

        AuthResponse response = authService.login(request);

        assertThat(response.email()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("login: unknown email throws BadRequestException with vague message")
    void login_unknownEmail_throwsBadRequestException() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("login: wrong password throws BadRequestException with same vague message (prevents user enumeration)")
    void login_wrongPassword_throwsBadRequestException() {
        LoginRequest request = new LoginRequest("alice@example.com", "wrong-password");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid credentials");
    }
}
