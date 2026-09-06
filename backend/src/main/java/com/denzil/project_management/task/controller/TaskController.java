package com.denzil.project_management.task.controller;

import com.denzil.project_management.task.dto.BulkUpdateRequest;
import com.denzil.project_management.task.dto.CreateTaskRequest;
import com.denzil.project_management.task.dto.TaskDto;
import com.denzil.project_management.task.entity.TaskStatus;
import com.denzil.project_management.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal String userId) {
        TaskDto task = taskService.createTask(request, userId);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<Void> bulkUpdateTasks(@RequestBody BulkUpdateRequest request, @AuthenticationPrincipal String userId) {
        taskService.bulkUpdateTasks(request, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            @RequestParam UUID workspaceId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) UUID createdById,
            @RequestParam(required = false) TaskStatus status,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(taskService.getTasks(workspaceId, projectId, assigneeId, createdById, status, userId));
    }
}
