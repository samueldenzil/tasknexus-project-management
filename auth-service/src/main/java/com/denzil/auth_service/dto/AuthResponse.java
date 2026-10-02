package com.denzil.auth_service.dto;

import java.util.UUID;

public record AuthResponse(
        UUID id,
        String name,
        String email
) {
}
