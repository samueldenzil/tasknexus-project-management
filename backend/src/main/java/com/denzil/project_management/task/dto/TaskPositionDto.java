package com.denzil.project_management.task.dto;

import com.denzil.project_management.task.entity.TaskStatus;

import java.util.UUID;

public record TaskPositionDto(
        UUID taskId,
        TaskStatus status,
        Integer position
) {
}
