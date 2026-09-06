package com.denzil.project_management.task.repository;

import com.denzil.project_management.task.entity.Task;
import com.denzil.project_management.task.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
