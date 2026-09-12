package com.denzil.project_management.project.service;

import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.entity.MemberRole;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.project.dto.*;
import com.denzil.project_management.project.entity.Project;
import com.denzil.project_management.project.repository.ProjectRepository;
import com.denzil.project_management.shared.dto.AnalyticsDto;
import com.denzil.project_management.shared.dto.TaskAnalyticsProjection;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.shared.exception.UnauthorizedAccessException;
import com.denzil.project_management.task.repository.TaskRepository;
import com.denzil.project_management.workspace.entity.Workspace;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        private final TaskRepository taskRepository;

        public ProjectService(ProjectRepository projectRepository, MemberRepository memberRepository,
                        TaskRepository taskRepository) {
                this.projectRepository = projectRepository;
                this.memberRepository = memberRepository;
                this.taskRepository = taskRepository;
        }

        public ProjectDto createProject(CreateProjectRequest request, String userId) {
                Member member = memberRepository.findByUserIdAndWorkspaceId(
                                UUID.fromString(userId), request.workspaceId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Workspace not found or access denied"));

                Workspace workspace = member.getWorkspace();

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

        @Transactional
        public ProjectDto updateProject(UUID projectId, UpdateProjectRequest request, String userId) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

                UUID workspaceId = project.getWorkspace().getId();

                Member member = memberRepository.findByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Workspace not found or access denied"));

                if (!MemberRole.ADMIN.equals(member.getRole())) {
                        throw new UnauthorizedAccessException("Only administrators can update projects");
                }

                project.setName(request.name());

                Project updatedProject = projectRepository.save(project);

                return new ProjectDto(updatedProject.getId(), updatedProject.getName(), updatedProject.getImageUrl(),
                                workspaceId);
        }

        @Transactional
        public void deleteProject(UUID projectId, String userId) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

                UUID workspaceId = project.getWorkspace().getId();

                Member member = memberRepository.findByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Workspace not found or access denied"));

                if (!MemberRole.ADMIN.equals(member.getRole())) {
                        throw new UnauthorizedAccessException("Only administrators can delete projects");
                }

                taskRepository.deleteAllByProjectId(projectId);
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
                ZoneId utc = ZoneId.of("UTC");
                Instant startOfThisMonth = YearMonth.now(utc).atDay(1).atStartOfDay(utc).toInstant();
                Instant startOfNextMonth = YearMonth.now(utc).plusMonths(1).atDay(1).atStartOfDay(utc).toInstant();
                Instant startOfLastMonth = YearMonth.now(utc).minusMonths(1).atDay(1).atStartOfDay(utc).toInstant();
                LocalDate today = LocalDate.now(utc);

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
