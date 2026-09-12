package com.denzil.project_management.task.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record BulkUpdateRequest(
                // At least one task must be provided; cap at 500 to prevent abuse.
                @NotNull(message = "Tasks list is required") @Size(min = 1, max = 500, message = "Tasks list must contain between 1 and 500 items") List<@NotNull @Valid TaskPositionDto> tasks,

                @NotNull(message = "Workspace ID is required") UUID workspaceId) {
}
