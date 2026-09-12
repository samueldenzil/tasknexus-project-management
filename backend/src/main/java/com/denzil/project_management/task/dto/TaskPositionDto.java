package com.denzil.project_management.task.dto;

import com.denzil.project_management.task.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TaskPositionDto(
                @NotNull(message = "Task ID is required") UUID taskId,

                @NotNull(message = "Status is required") TaskStatus status,

                @NotNull(message = "Position is required") Integer position) {
}
