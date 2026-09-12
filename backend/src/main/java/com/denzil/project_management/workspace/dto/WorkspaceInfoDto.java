package com.denzil.project_management.workspace.dto;

import java.util.UUID;

public record WorkspaceInfoDto(
                UUID id,
                String name,
                String imageUrl) {
}
