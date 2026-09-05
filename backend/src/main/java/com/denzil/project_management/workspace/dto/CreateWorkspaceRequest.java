package com.denzil.project_management.workspace.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateWorkspaceRequest(
        @NotBlank(message = "Name is required")
        String name
) {
}
