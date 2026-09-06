package com.denzil.project_management.task.dto;

import com.denzil.project_management.task.entity.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

public record TaskDto(
        UUID id,
        String name,
        TaskStatus status,
        String description,
        LocalDate dueDate,
        Integer position,
        UUID workspaceId,
        UUID projectId,
        UUID assigneeId,
        UUID createdById
) {
}
