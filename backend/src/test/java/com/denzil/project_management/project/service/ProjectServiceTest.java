package com.denzil.project_management.project.service;

import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.entity.MemberRole;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.project.dto.CreateProjectRequest;
import com.denzil.project_management.project.dto.ProjectDto;
import com.denzil.project_management.project.dto.UpdateProjectRequest;
import com.denzil.project_management.project.entity.Project;
import com.denzil.project_management.project.repository.ProjectRepository;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.shared.exception.UnauthorizedAccessException;
import com.denzil.project_management.task.repository.TaskRepository;
import com.denzil.project_management.user.entity.User;
import com.denzil.project_management.workspace.entity.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService")
class ProjectServiceTest {

        @Mock
        private ProjectRepository projectRepository;

        @Mock
        private MemberRepository memberRepository;

        @Mock
        private TaskRepository taskRepository;

        @InjectMocks
        private ProjectService projectService;

        private UUID workspaceId;
        private UUID projectId;
        private UUID adminUserId;
        private UUID memberUserId;
        private Workspace workspace;
        private Project project;
        private Member adminMember;
        private Member regularMember;

        @BeforeEach
        void setUp() {
                workspaceId = UUID.randomUUID();
                projectId = UUID.randomUUID();
                adminUserId = UUID.randomUUID();
                memberUserId = UUID.randomUUID();

                workspace = new Workspace();
                workspace.setId(workspaceId);
                workspace.setName("Test Workspace");

                project = new Project();
                project.setId(projectId);
                project.setName("Initial Project");
                project.setWorkspace(workspace);

                User adminUser = new User();
                adminUser.setId(adminUserId);

                adminMember = new Member();
                adminMember.setId(UUID.randomUUID());
                adminMember.setUser(adminUser);
                adminMember.setWorkspace(workspace);
                adminMember.setRole(MemberRole.ADMIN);

                User regularUser = new User();
                regularUser.setId(memberUserId);

                regularMember = new Member();
                regularMember.setId(UUID.randomUUID());
                regularMember.setUser(regularUser);
                regularMember.setWorkspace(workspace);
                regularMember.setRole(MemberRole.MEMBER);
        }

        // -- updateProject -----------------------------------------------------

        @Test
        @DisplayName("updateProject: admin can update project")
        void updateProject_adminCaller_updatesProject() {
                when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
                when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                                .thenReturn(Optional.of(adminMember));
                when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

                UpdateProjectRequest request = new UpdateProjectRequest("Updated Name");
                ProjectDto result = projectService.updateProject(projectId, request, adminUserId.toString());

                assertThat(result.name()).isEqualTo("Updated Name");
                verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("updateProject: non-admin caller throws UnauthorizedAccessException (403)")
        void updateProject_nonAdminCaller_throwsUnauthorizedAccessException() {
                when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
                when(memberRepository.findByUserIdAndWorkspaceId(memberUserId, workspaceId))
                                .thenReturn(Optional.of(regularMember));

                UpdateProjectRequest request = new UpdateProjectRequest("Updated Name");

                assertThatThrownBy(() -> projectService.updateProject(projectId, request, memberUserId.toString()))
                                .isInstanceOf(UnauthorizedAccessException.class)
                                .hasMessage("Only administrators can update projects");

                verify(projectRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateProject: non-member caller throws ResourceNotFoundException (404)")
        void updateProject_nonMemberCaller_throwsResourceNotFoundException() {
                when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
                when(memberRepository.findByUserIdAndWorkspaceId(memberUserId, workspaceId))
                                .thenReturn(Optional.empty());

                UpdateProjectRequest request = new UpdateProjectRequest("Updated Name");

                assertThatThrownBy(() -> projectService.updateProject(projectId, request, memberUserId.toString()))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessage("Workspace not found or access denied");

                verify(projectRepository, never()).save(any());
        }

        // -- deleteProject -----------------------------------------------------

        @Test
        @DisplayName("deleteProject: admin can delete project and associated tasks")
        void deleteProject_adminCaller_deletesProjectAndAssociatedTasks() {
                when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
                when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                                .thenReturn(Optional.of(adminMember));

                projectService.deleteProject(projectId, adminUserId.toString());

                verify(taskRepository).deleteAllByProjectId(projectId);
                verify(projectRepository).delete(project);
        }

        @Test
        @DisplayName("deleteProject: non-admin caller throws UnauthorizedAccessException (403)")
        void deleteProject_nonAdminCaller_throwsUnauthorizedAccessException() {
                when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
                when(memberRepository.findByUserIdAndWorkspaceId(memberUserId, workspaceId))
                                .thenReturn(Optional.of(regularMember));

                assertThatThrownBy(() -> projectService.deleteProject(projectId, memberUserId.toString()))
                                .isInstanceOf(UnauthorizedAccessException.class)
                                .hasMessage("Only administrators can delete projects");

                verify(taskRepository, never()).deleteAllByProjectId(any());
                verify(projectRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deleteProject: non-member caller throws ResourceNotFoundException (404)")
        void deleteProject_nonMemberCaller_throwsResourceNotFoundException() {
                when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
                when(memberRepository.findByUserIdAndWorkspaceId(memberUserId, workspaceId))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> projectService.deleteProject(projectId, memberUserId.toString()))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessage("Workspace not found or access denied");

                verify(taskRepository, never()).deleteAllByProjectId(any());
                verify(projectRepository, never()).delete(any());
        }
}
