package com.denzil.project_management.project.controller;

import com.denzil.project_management.project.dto.CreateProjectRequest;
import com.denzil.project_management.project.dto.ProjectDto;
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
}
