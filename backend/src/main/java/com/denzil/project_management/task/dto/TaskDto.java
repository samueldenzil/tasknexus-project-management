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
        ProjectSummaryDto project,
        AssigneeSummaryDto assignee,
        CreatedBySummaryDto createdBy) {
    public record ProjectSummaryDto(UUID id, String name, String imageUrl) {
    }

    public record AssigneeSummaryDto(UUID id, String name) {
    }

    public record CreatedBySummaryDto(UUID id, String name) {
    }
}
