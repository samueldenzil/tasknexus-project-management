package com.denzil.project_management.task.controller;

import com.denzil.project_management.task.dto.BulkUpdateRequest;
import com.denzil.project_management.task.dto.CreateTaskRequest;
import com.denzil.project_management.task.dto.TaskDto;
import com.denzil.project_management.task.dto.UpdateTaskRequest;
import com.denzil.project_management.task.entity.TaskStatus;
import com.denzil.project_management.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate dueDate,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(taskService.getTasks(workspaceId, projectId, assigneeId, createdById, status, search, dueDate, userId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDto> getTask(@PathVariable UUID taskId, @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(taskService.getTask(taskId, userId));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request, userId));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId, @AuthenticationPrincipal String userId) {
        taskService.deleteTask(taskId, userId);
        return ResponseEntity.noContent().build();
    }
}
