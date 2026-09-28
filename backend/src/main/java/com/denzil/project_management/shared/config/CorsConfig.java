package com.denzil.project_management.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // @Value("${app.frontend-url}")
    // private String frontendUrl;

    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    // registry.addMapping("/api/**") // Apply to all API routes
    // .allowedOrigins(frontendUrl)
    // .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    // .allowedHeaders("*")
    // .allowCredentials(true);
    // }

    @Override
    public void addFormatters(org.springframework.format.FormatterRegistry registry) {
        registry.addConverter(String.class, java.time.LocalDate.class, source -> {
            if (source == null || source.isBlank()) {
                return null;
            }
            String text = source.trim();
            if (text.contains("T")) {
                try {
                    return java.time.OffsetDateTime.parse(text).toLocalDate();
                } catch (Exception ignored) {
                }
                try {
                    return java.time.LocalDate.parse(text.substring(0, 10));
                } catch (Exception ignored) {
                }
            }
            return java.time.LocalDate.parse(text);
        });
    }
}
