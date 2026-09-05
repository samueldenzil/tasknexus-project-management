package com.denzil.project_management.auth.dto;

import java.util.UUID;

public record AuthResponse(
        UUID id,
        String name,
        String email
) {
}
