package com.denzil.project_management.project.dto;

import java.util.UUID;

public record ProjectDto(
        UUID id,
        String name,
        String imageUrl,
        UUID workspaceId
) {
}
