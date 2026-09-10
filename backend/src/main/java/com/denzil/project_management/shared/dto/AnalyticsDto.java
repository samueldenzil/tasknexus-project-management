package com.denzil.project_management.shared.dto;

public record AnalyticsDto(
        long taskCount,
        long taskDifference,
        long assignedTaskCount,
        long assignedTaskDifference,
        long completedTaskCount,
        long completedTaskDifference,
        long incompleteTaskCount,
        long incompleteTaskDifference,
        long overdueTaskCount,
        long overdueTaskDifference
) {}
