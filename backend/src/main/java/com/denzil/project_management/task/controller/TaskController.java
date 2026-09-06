package com.denzil.project_management.task.controller;

import com.denzil.project_management.task.dto.CreateTaskRequest;
import com.denzil.project_management.task.dto.TaskDto;
import com.denzil.project_management.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @AuthenticationPrincipal String userId
    ) {
        TaskDto task = taskService.createTask(request, userId);
        return ResponseEntity.ok(task);
    }
}
