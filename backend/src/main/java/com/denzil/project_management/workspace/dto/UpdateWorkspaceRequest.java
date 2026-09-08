package com.denzil.project_management.workspace.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateWorkspaceRequest(
        @NotBlank
        String name
) {
}
