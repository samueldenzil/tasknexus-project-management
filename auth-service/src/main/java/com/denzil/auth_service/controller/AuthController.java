package com.denzil.auth_service.controller;

import com.denzil.auth_service.dto.AuthResponse;
import com.denzil.auth_service.dto.LoginRequest;
import com.denzil.auth_service.dto.RegisterRequest;
import com.denzil.auth_service.security.JwtUtil;
import com.denzil.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);

        // Generate JWT token so the user is instantly logged in
        String token = jwtUtil.generateToken(authResponse.id());

        // Create the HTTP-Only cookie
        ResponseCookie cookie = ResponseCookie.from("tasknexus-session", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax") // Prevents cookie from being sent on cross-site POST/PATCH/DELETE requests
                                 // (CSRF mitigation)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. Verify credentials via Service
        AuthResponse authResponse = authService.login(request);

        // 2. Generate JWT token
        String token = jwtUtil.generateToken(authResponse.id());

        // 3. Create the HTTP-Only cookie
        ResponseCookie cookie = ResponseCookie.from("tasknexus-session", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax") // Prevents cookie from being sent on cross-site POST/PATCH/DELETE requests
                                 // (CSRF mitigation)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from("tasknexus-session", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax") // Keep consistent with login/register cookies
                .path("/")
                .maxAge(0) // This immediately deletes the cookie in the browser
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal String userId) {
        AuthResponse response = authService.getCurrentUser(userId);
        return ResponseEntity.ok(response);
    }
}
