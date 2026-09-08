package com.denzil.project_management.workspace.controller;

import java.util.List;
import java.util.UUID;

import com.denzil.project_management.workspace.dto.JoinWorkspaceRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.denzil.project_management.workspace.dto.CreateWorkspaceRequest;
import com.denzil.project_management.workspace.dto.UpdateWorkspaceRequest;
import com.denzil.project_management.workspace.dto.WorkspaceDto;
import com.denzil.project_management.workspace.service.WorkspaceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceDto> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request,
                                                        @AuthenticationPrincipal String userId) {
        WorkspaceDto workspaceDto = workspaceService.createWorkspace(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceDto);
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceDto>> getWorkspaces(@AuthenticationPrincipal String userId) {
        List<WorkspaceDto> workspaces = workspaceService.getWorkspaces(userId);
        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDto> getWorkspace(@PathVariable UUID workspaceId,
                                                     @AuthenticationPrincipal String userId) {
        WorkspaceDto workspace = workspaceService.getWorkspace(workspaceId, userId);
        return ResponseEntity.ok(workspace);
    }

    @PatchMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDto> updateWorkspace(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request,
            @AuthenticationPrincipal String userId) {
        WorkspaceDto workspace = workspaceService.updateWorkspace(workspaceId, request, userId);
        return ResponseEntity.ok(workspace);
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal String userId) {
        workspaceService.deleteWorkspace(workspaceId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{workspaceId}/reset-invite-code")
    public ResponseEntity<WorkspaceDto> resetInviteCode(@PathVariable UUID workspaceId, @AuthenticationPrincipal String userId) {
        WorkspaceDto workspace = workspaceService.resetInviteCode(workspaceId, userId);
        return ResponseEntity.ok(workspace);
    }

    @PostMapping("/{workspaceId}/join")
    public ResponseEntity<WorkspaceDto> joinWorkspace(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody JoinWorkspaceRequest request,
            @AuthenticationPrincipal String userId) {
        WorkspaceDto workspace = workspaceService.joinWorkspace(workspaceId, request.inviteCode(), userId);
        return ResponseEntity.ok(workspace);
    }
}
