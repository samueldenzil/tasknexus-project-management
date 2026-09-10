package com.denzil.project_management.project.service;

import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.project.dto.*;
import com.denzil.project_management.project.entity.Project;
import com.denzil.project_management.project.repository.ProjectRepository;
import com.denzil.project_management.shared.dto.AnalyticsDto;
import com.denzil.project_management.shared.dto.TaskAnalyticsProjection;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.task.repository.TaskRepository;
import com.denzil.project_management.workspace.entity.Workspace;
import com.denzil.project_management.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TaskRepository taskRepository;

    public ProjectService(ProjectRepository projectRepository, MemberRepository memberRepository,
            WorkspaceRepository workspaceRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.workspaceRepository = workspaceRepository;
        this.taskRepository = taskRepository;
    }

    public ProjectDto createProject(CreateProjectRequest request, String userId) {
        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId),
                request.workspaceId());

        if (!isMember) {
            throw new ResourceNotFoundException("Workspace not found or access denied");
        }

        Workspace workspace = workspaceRepository.findById(request.workspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        Project project = new Project();
        project.setName(request.name());
        project.setWorkspace(workspace);

        Project savedProject = projectRepository.save(project);

        return new ProjectDto(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getImageUrl(),
                workspace.getId());
    }

    public List<ProjectDto> getProjects(UUID workspaceId, String userId) {
        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

        if (!isMember) {
            throw new ResourceNotFoundException("Workspace not found or access denied");
        }

        List<Project> projects = projectRepository.findByWorkspaceId(workspaceId);

        return projects.stream()
                .map(p -> new ProjectDto(p.getId(), p.getName(), p.getImageUrl(), workspaceId))
                .toList();
    }

    public ProjectDto getProject(UUID projectId, String userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        UUID workspaceId = project.getWorkspace().getId();

        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

        if (!isMember) {
            throw new ResourceNotFoundException("Access denied");
        }

        return new ProjectDto(project.getId(), project.getName(), project.getImageUrl(), workspaceId);
    }

    public ProjectDto updateProject(UUID projectId, UpdateProjectRequest request, String userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        UUID workspaceId = project.getWorkspace().getId();

        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

        if (!isMember) {
            throw new ResourceNotFoundException("Access denied");
        }

        project.setName(request.name());

        Project updatedProject = projectRepository.save(project);

        return new ProjectDto(updatedProject.getId(), updatedProject.getName(), updatedProject.getImageUrl(),
                workspaceId);
    }

    public void deleteProject(UUID projectId, String userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        UUID workspaceId = project.getWorkspace().getId();

        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

        if (!isMember) {
            throw new ResourceNotFoundException("Access denied");
        }

        projectRepository.delete(project);
    }

    public AnalyticsDto getProjectAnalytics(UUID projectId, String userId) {
        // 1. Authorization check
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        UUID workspaceId = project.getWorkspace().getId();

        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

        if (!isMember) {
            throw new ResourceNotFoundException("Access denied");
        }

        // 2. Date boundaries
        Instant startOfThisMonth = YearMonth.now().atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfNextMonth = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        Instant startOfLastMonth = YearMonth.now().minusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        LocalDate today = LocalDate.now();

        // 3. Let the Database do the math!
        TaskAnalyticsProjection thisMonth = taskRepository.getProjectAnalytics(
                projectId, startOfThisMonth, startOfNextMonth, today);

        TaskAnalyticsProjection lastMonth = taskRepository.getProjectAnalytics(
                projectId, startOfLastMonth, startOfThisMonth, today);

        // 4. Map to DTO
        return new AnalyticsDto(
                thisMonth.getTotalCount(),
                thisMonth.getTotalCount() - lastMonth.getTotalCount(),

                thisMonth.getAssignedCount(),
                thisMonth.getAssignedCount() - lastMonth.getAssignedCount(),

                thisMonth.getCompletedCount(),
                thisMonth.getCompletedCount() - lastMonth.getCompletedCount(),

                thisMonth.getIncompleteCount(),
                thisMonth.getIncompleteCount() - lastMonth.getIncompleteCount(),

                thisMonth.getOverdueCount(),
                thisMonth.getOverdueCount() - lastMonth.getOverdueCount());
    }
}
