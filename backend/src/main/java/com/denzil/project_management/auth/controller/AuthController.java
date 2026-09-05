package com.denzil.project_management.auth.controller;

import com.denzil.project_management.auth.dto.AuthResponse;
import com.denzil.project_management.auth.dto.LoginRequest;
import com.denzil.project_management.auth.dto.RegisterRequest;
import com.denzil.project_management.auth.service.AuthService;
import com.denzil.project_management.shared.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. Verify credentials via Service
        AuthResponse authResponse = authService.login(request);

        // 2. Generate JWT token
        String token = jwtUtil.generateToken(authResponse.id());

        // 3. Create the HTTP-Only cookie
        ResponseCookie cookie = ResponseCookie.from("jira-clone-session", token)
                .httpOnly(true)
                .secure(false)  // Set to true in production (HTTPS only)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok("You are authenticated! Your database ID is: " + userId);
    }
}
