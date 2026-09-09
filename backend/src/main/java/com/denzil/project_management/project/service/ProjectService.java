package com.denzil.project_management.project.service;

import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.project.dto.CreateProjectRequest;
import com.denzil.project_management.project.dto.ProjectDto;
import com.denzil.project_management.project.dto.UpdateProjectRequest;
import com.denzil.project_management.project.entity.Project;
import com.denzil.project_management.project.repository.ProjectRepository;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.workspace.entity.Workspace;
import com.denzil.project_management.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;

    public ProjectService(ProjectRepository projectRepository, MemberRepository memberRepository, WorkspaceRepository workspaceRepository) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public ProjectDto createProject(CreateProjectRequest request, String userId) {
        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), request.workspaceId());

        if (!isMember) {
            throw new ResourceNotFoundException("Workspace not found or access denied");
        }

        Workspace workspace = workspaceRepository.findById(request.workspaceId()).
                orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        Project project = new Project();
        project.setName(request.name());
        project.setWorkspace(workspace);

        Project savedProject = projectRepository.save(project);

        return new ProjectDto(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getImageUrl(),
                workspace.getId()
        );
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

        return new ProjectDto(updatedProject.getId(), updatedProject.getName(), updatedProject.getImageUrl(), workspaceId);
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

}
