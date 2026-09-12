package com.denzil.project_management.project.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProjectRequest(
        @NotBlank
        String name
) {
}
