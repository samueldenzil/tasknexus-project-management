package com.denzil.auth_service.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;

        // 1. Look for our specific cookie in the incoming request
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("tasknexus-session")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 2. If we found a token, validate it
        if (token != null) {
            try {
                String userId = jwtUtil.extractUserId(token);
                // 3. Tell Spring Security: "This user is officially authenticated!"
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null,
                        List.of());

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                // Expected: token is expired, malformed, or signed with the wrong key.
                // The request continues unauthenticated; Spring Security will block
                // protected endpoints automatically.
                log.debug("JWT validation failed: {}", e.getMessage());
            } catch (Exception e) {
                // Unexpected error (e.g. misconfigured secret, serialization bug).
                // Log at WARN so it surfaces in monitoring without crashing the filter chain.
                log.warn("Unexpected error during JWT validation", e);
            }
        }

        // 4. Pass the request to the next filter (or to the Controller)
        filterChain.doFilter(request, response);
    }
}
