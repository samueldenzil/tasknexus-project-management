package com.denzil.project_management.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateProjectRequest(
        @NotBlank
        String name,

        @NotNull
        UUID workspaceId
) {
}
