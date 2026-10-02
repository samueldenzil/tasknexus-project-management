package com.denzil.auth_service.config;

import com.denzil.auth_service.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.cors(AbstractHttpConfigurer::disable);

        // CSRF is disabled because our session cookies are set with SameSite=Lax, which
        // instructs browsers to never attach the cookie to cross-site non-safe requests
        // (POST, PATCH, DELETE, etc.). This is the mitigation against CSRF attacks.
        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/health").permitAll() // Anyone can check health
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll() // Anyone can login/register
                .anyRequest().authenticated()); // EVERYTHING else requires auth

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
