package com.denzil.project_management.shared.dto;

public interface TaskAnalyticsProjection {
    long getTotalCount();

    long getAssignedCount();

    long getCompletedCount();

    long getIncompleteCount();

    long getOverdueCount();
}

