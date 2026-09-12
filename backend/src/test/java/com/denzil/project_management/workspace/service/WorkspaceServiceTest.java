package com.denzil.project_management.workspace.service;

import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.entity.MemberRole;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.project.repository.ProjectRepository;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.shared.exception.UnauthorizedAccessException;
import com.denzil.project_management.task.repository.TaskRepository;
import com.denzil.project_management.user.entity.User;
import com.denzil.project_management.user.repository.UserRepository;
import com.denzil.project_management.workspace.dto.WorkspaceDto;
import com.denzil.project_management.workspace.dto.WorkspaceInfoDto;
import com.denzil.project_management.workspace.entity.Workspace;
import com.denzil.project_management.workspace.repository.WorkspaceRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceService")
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    private UUID workspaceId;
    private UUID adminUserId;
    private UUID memberUserId;
    private Workspace workspace;
    private Member adminMember;
    private Member regularMember;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();
        memberUserId = UUID.randomUUID();

        workspace = new Workspace();
        workspace.setId(workspaceId);
        workspace.setName("My Workspace");
        workspace.setInviteCode("ABC12345");

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

    // -- getWorkspace ------------------------------------------------------

    @Test
    @DisplayName("getWorkspace: member receives workspace details in single DB query")
    void getWorkspace_validMember_returnsDto() {
        when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                .thenReturn(Optional.of(adminMember));

        WorkspaceDto dto = workspaceService.getWorkspace(workspaceId, adminUserId.toString());

        assertThat(dto.id()).isEqualTo(workspaceId);
        assertThat(dto.name()).isEqualTo("My Workspace");
        // Verify workspaceRepository was NOT called � workspace comes from member
        verify(workspaceRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getWorkspace: non-member throws ResourceNotFoundException (404)")
    void getWorkspace_nonMember_throwsResourceNotFoundException() {
        when(memberRepository.findByUserIdAndWorkspaceId(memberUserId, workspaceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.getWorkspace(workspaceId, memberUserId.toString()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workspace not found or access denied");
    }

    // -- getWorkspaceInfo --------------------------------------------------

    @Test
    @DisplayName("getWorkspaceInfo: returns workspace info when workspace exists")
    void getWorkspaceInfo_workspaceExists_returnsInfoDto() {
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        WorkspaceInfoDto info = workspaceService.getWorkspaceInfo(workspaceId);

        assertThat(info.id()).isEqualTo(workspaceId);
        assertThat(info.name()).isEqualTo("My Workspace");
    }

    @Test
    @DisplayName("getWorkspaceInfo: throws ResourceNotFoundException when workspace not found")
    void getWorkspaceInfo_notFound_throwsException() {
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.getWorkspaceInfo(workspaceId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workspace not found");
    }

    // -- deleteWorkspace ---------------------------------------------------

    @Test
    @DisplayName("deleteWorkspace: admin can delete the workspace")
    void deleteWorkspace_adminCaller_deletesWorkspace() {
        when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                .thenReturn(Optional.of(adminMember));

        workspaceService.deleteWorkspace(workspaceId, adminUserId.toString());

        verify(taskRepository).deleteAllByWorkspaceId(workspaceId);
        verify(projectRepository).deleteAllByWorkspaceId(workspaceId);
        verify(memberRepository).deleteAllByWorkspaceId(workspaceId);
        verify(workspaceRepository).deleteById(workspaceId);
    }

    @Test
    @DisplayName("deleteWorkspace: non-admin caller throws UnauthorizedAccessException (403)")
    void deleteWorkspace_nonAdminCaller_throwsUnauthorizedException() {
        when(memberRepository.findByUserIdAndWorkspaceId(memberUserId, workspaceId))
                .thenReturn(Optional.of(regularMember));

        assertThatThrownBy(() -> workspaceService.deleteWorkspace(workspaceId, memberUserId.toString()))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Only administrators can delete a workspace");

        verify(workspaceRepository, never()).deleteById(any());
    }

    // -- resetInviteCode ---------------------------------------------------

    @Test
    @DisplayName("resetInviteCode: generates a new 8-char code and saves")
    void resetInviteCode_adminCaller_generatesNewCode() {
        when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                .thenReturn(Optional.of(adminMember));
        Workspace saved = new Workspace();
        saved.setId(workspaceId);
        saved.setName("My Workspace");
        saved.setInviteCode("NEWCODE1");
        when(workspaceRepository.save(workspace)).thenReturn(saved);

        WorkspaceDto dto = workspaceService.resetInviteCode(workspaceId, adminUserId.toString());

        assertThat(dto.inviteCode()).isNotEqualTo("ABC12345");
        assertThat(dto.inviteCode()).hasSize(8);
        verify(workspaceRepository).save(workspace);
    }

    @Test
    @DisplayName("resetInviteCode: non-admin caller throws UnauthorizedAccessException (403)")
    void resetInviteCode_nonAdminCaller_throwsUnauthorizedException() {
        when(memberRepository.findByUserIdAndWorkspaceId(memberUserId, workspaceId))
                .thenReturn(Optional.of(regularMember));

        assertThatThrownBy(() -> workspaceService.resetInviteCode(workspaceId, memberUserId.toString()))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(workspaceRepository, never()).save(any());
    }
}
