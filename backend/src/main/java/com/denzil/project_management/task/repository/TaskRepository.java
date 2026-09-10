package com.denzil.project_management.task.repository;

import com.denzil.project_management.shared.dto.TaskAnalyticsProjection;
import com.denzil.project_management.task.entity.Task;
import com.denzil.project_management.task.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("SELECT t from Task t WHERE t.workspace.id = :workspaceId " +
            "AND (cast(:projectId as uuid) IS NULL OR t.project.id = :projectId) " +
            "AND (cast(:assigneeId as uuid) IS NULL OR t.assignee.id = :assigneeId) " +
            "AND (cast(:createdById as uuid) IS NULL OR t.createdBy.id = :createdById) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "ORDER BY t.position ASC")
    List<Task> findFilteredTasks(
            @Param("workspaceId") UUID workspaceId,
            @Param("projectId") UUID projectId,
            @Param("assigneeId") UUID assigneeId,
            @Param("createdById") UUID createdById,
            @Param("status") TaskStatus status);

    @Query("""
             SELECT
                 COUNT(t.id) as totalCount,
                 COALESCE(SUM(CASE WHEN t.assignee IS NOT NULL THEN 1 ELSE 0 END), 0) as assignedCount,
                 COALESCE(SUM(CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END), 0) as completedCount,
                 COALESCE(SUM(CASE WHEN t.status != 'DONE' THEN 1 ELSE 0 END), 0) as incompleteCount,
                 COALESCE(SUM(CASE WHEN t.status != 'DONE' AND t.dueDate < :today THEN 1 ELSE 0 END), 0) as overdueCount
             FROM Task t
             WHERE t.project.id = :projectId\s
               AND t.createdAt >= :startDate\s
               AND t.createdAt < :endDate
            """)
    TaskAnalyticsProjection getProjectAnalytics(
            @Param("projectId") UUID projectId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("today") LocalDate today
    );

    @Query("""
             SELECT
                 COUNT(t.id) as totalCount,
                 COALESCE(SUM(CASE WHEN t.assignee IS NOT NULL THEN 1 ELSE 0 END), 0) as assignedCount,
                 COALESCE(SUM(CASE WHEN t.status = 'DONE' THEN 1 ELSE 0 END), 0) as completedCount,
                 COALESCE(SUM(CASE WHEN t.status != 'DONE' THEN 1 ELSE 0 END), 0) as incompleteCount,
                 COALESCE(SUM(CASE WHEN t.status != 'DONE' AND t.dueDate < :today THEN 1 ELSE 0 END), 0) as overdueCount
             FROM Task t
             WHERE t.workspace.id = :workspaceId 
               AND t.createdAt >= :startDate 
               AND t.createdAt < :endDate
            """)
    TaskAnalyticsProjection getWorkspaceAnalytics(
            @Param("workspaceId") UUID workspaceId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("today") LocalDate today
    );
}
