package com.denzil.project_management.project.controller;

import com.denzil.project_management.shared.dto.AnalyticsDto;
import com.denzil.project_management.project.dto.CreateProjectRequest;
import com.denzil.project_management.project.dto.ProjectDto;
import com.denzil.project_management.project.dto.UpdateProjectRequest;
import com.denzil.project_management.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody CreateProjectRequest request, @AuthenticationPrincipal String userId) {
        ProjectDto project = projectService.createProject(request, userId);
        return ResponseEntity.ok(project);
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getProjects(@RequestParam UUID workspaceId, @AuthenticationPrincipal String userId) {
        List<ProjectDto> projects = projectService.getProjects(workspaceId, userId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable UUID projectId, @AuthenticationPrincipal String userId) {
        ProjectDto project = projectService.getProject(projectId, userId);
        return ResponseEntity.ok(project);
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal String userId) {
        ProjectDto project = projectService.updateProject(projectId, request, userId);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID projectId, @AuthenticationPrincipal String userId) {
        projectService.deleteProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/analytics")
    public ResponseEntity<AnalyticsDto> getProjectAnalytics(@PathVariable UUID projectId, @AuthenticationPrincipal String userId) {
        AnalyticsDto analyticsDto = projectService.getProjectAnalytics(projectId, userId);
        return ResponseEntity.ok(analyticsDto);
    }
}
