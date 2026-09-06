package com.denzil.project_management.task.dto;

import java.util.List;
import java.util.UUID;

public record BulkUpdateRequest(
        List<TaskPositionDto> tasks,
        UUID workspaceId
) {
}
