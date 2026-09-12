package com.denzil.project_management.task.dto;

import com.denzil.project_management.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskRequest(
        @NotBlank
        String name,

        @NotNull
        TaskStatus status,

        @NotNull
        UUID projectId,

        LocalDate dueDate,

        UUID assigneeId,

        String description
) {
}
