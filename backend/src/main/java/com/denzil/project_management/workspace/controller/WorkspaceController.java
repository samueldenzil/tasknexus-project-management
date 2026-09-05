package com.denzil.project_management.workspace.controller;

import com.denzil.project_management.workspace.dto.CreateWorkspaceRequest;
import com.denzil.project_management.workspace.dto.WorkspaceDto;
import com.denzil.project_management.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceDto> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request, @AuthenticationPrincipal String userId) {
        WorkspaceDto workspaceDto = workspaceService.createWorkspace(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceDto);
    }
}
