package com.denzil.project_management.workspace.dto;

import java.util.UUID;

public record WorkspaceDto(
        UUID id,
        String name,
        String imageUrl,
        String inviteCode
) {
}
