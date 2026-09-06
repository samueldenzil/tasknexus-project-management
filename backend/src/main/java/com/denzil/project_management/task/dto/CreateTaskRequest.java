package com.denzil.project_management.task.dto;

import com.denzil.project_management.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank
        String name,

        @NotNull
        UUID workspaceId,

        @NotNull
        UUID projectId,

        UUID assigneeId,

        TaskStatus status,

        LocalDate dueDate
) {
}
