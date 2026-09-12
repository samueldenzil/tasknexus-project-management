package com.denzil.project_management.member.service;

import com.denzil.project_management.member.dto.MemberDto;
import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.entity.MemberRole;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.shared.exception.BadRequestException;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService")
class MemberServiceTest {

        @Mock
        private MemberRepository memberRepository;

        @Mock
        private TaskRepository taskRepository;

        @InjectMocks
        private MemberService memberService;

        private UUID workspaceId;
        private UUID adminUserId;
        private UUID memberUserId;
        private Member adminMember;
        private Member regularMember;
        private User adminUser;
        private User regularUser;
        private Workspace workspace;

        @BeforeEach
        void setUp() {
                workspaceId = UUID.randomUUID();
                adminUserId = UUID.randomUUID();
                memberUserId = UUID.randomUUID();

                workspace = new Workspace();
                workspace.setId(workspaceId);

                adminUser = new User();
                adminUser.setId(adminUserId);
                adminUser.setName("Admin");
                adminUser.setEmail("admin@example.com");

                regularUser = new User();
                regularUser.setId(memberUserId);
                regularUser.setName("Bob");
                regularUser.setEmail("bob@example.com");

                adminMember = new Member();
                adminMember.setId(UUID.randomUUID());
                adminMember.setUser(adminUser);
                adminMember.setWorkspace(workspace);
                adminMember.setRole(MemberRole.ADMIN);

                regularMember = new Member();
                regularMember.setId(UUID.randomUUID());
                regularMember.setUser(regularUser);
                regularMember.setWorkspace(workspace);
                regularMember.setRole(MemberRole.MEMBER);
        }

        // -- updateMemberRole --------------------------------------------------

        @Test
        @DisplayName("updateMemberRole: admin can promote a member")
        void updateMemberRole_adminCaller_updatesRole() {
                when(memberRepository.findById(regularMember.getId())).thenReturn(Optional.of(regularMember));
                when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                                .thenReturn(Optional.of(adminMember));
                Member updatedMember = new Member();
                updatedMember.setId(regularMember.getId());
                updatedMember.setUser(regularUser);
                updatedMember.setWorkspace(workspace);
                updatedMember.setRole(MemberRole.ADMIN);
                when(memberRepository.save(regularMember)).thenReturn(updatedMember);

                MemberDto result = memberService.updateMemberRole(regularMember.getId(), MemberRole.ADMIN,
                                adminUserId.toString());

                assertThat(result.role()).isEqualTo(MemberRole.ADMIN);
                verify(memberRepository).save(regularMember);
        }

        @Test
        @DisplayName("updateMemberRole: non-admin caller throws UnauthorizedAccessException (403)")
        void updateMemberRole_nonAdminCaller_throwsUnauthorizedException() {
                when(memberRepository.findById(regularMember.getId())).thenReturn(Optional.of(regularMember));
                // Caller is also a regular MEMBER, not an ADMIN
                Member callerMember = new Member();
                callerMember.setUser(regularUser);
                callerMember.setWorkspace(workspace);
                callerMember.setRole(MemberRole.MEMBER);
                when(memberRepository.findByUserIdAndWorkspaceId(memberUserId, workspaceId))
                                .thenReturn(Optional.of(callerMember));

                assertThatThrownBy(() -> memberService.updateMemberRole(regularMember.getId(), MemberRole.ADMIN,
                                memberUserId.toString()))
                                .isInstanceOf(UnauthorizedAccessException.class)
                                .hasMessage("Only workspace administrators can update members");

                verify(memberRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateMemberRole: unknown member ID throws ResourceNotFoundException (404)")
        void updateMemberRole_memberNotFound_throwsResourceNotFoundException() {
                UUID unknownId = UUID.randomUUID();
                when(memberRepository.findById(unknownId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> memberService.updateMemberRole(unknownId, MemberRole.ADMIN,
                                adminUserId.toString()))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessage("Member not found");
        }

        @Test
        @DisplayName("updateMemberRole: cannot demote the only administrator of the workspace")
        void updateMemberRole_demoteLastAdmin_throwsBadRequestException() {
                when(memberRepository.findById(adminMember.getId())).thenReturn(Optional.of(adminMember));
                when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                                .thenReturn(Optional.of(adminMember));
                when(memberRepository.countByWorkspaceIdAndRole(workspaceId, MemberRole.ADMIN)).thenReturn(1L);

                assertThatThrownBy(() -> memberService.updateMemberRole(adminMember.getId(), MemberRole.MEMBER,
                                adminUserId.toString()))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("Cannot demote the only administrator of the workspace");

                verify(memberRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateMemberRole: admin can demote an admin when another administrator exists")
        void updateMemberRole_demoteAdmin_multipleAdmins_success() {
                when(memberRepository.findById(adminMember.getId())).thenReturn(Optional.of(adminMember));
                when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                                .thenReturn(Optional.of(adminMember));
                when(memberRepository.countByWorkspaceIdAndRole(workspaceId, MemberRole.ADMIN)).thenReturn(2L);

                Member demotedMember = new Member();
                demotedMember.setId(adminMember.getId());
                demotedMember.setUser(adminUser);
                demotedMember.setWorkspace(workspace);
                demotedMember.setRole(MemberRole.MEMBER);
                when(memberRepository.save(adminMember)).thenReturn(demotedMember);

                MemberDto result = memberService.updateMemberRole(adminMember.getId(), MemberRole.MEMBER,
                                adminUserId.toString());

                assertThat(result.role()).isEqualTo(MemberRole.MEMBER);
                verify(memberRepository).save(adminMember);
        }

        // -- deleteMember ------------------------------------------------------

        @Test
        @DisplayName("deleteMember: cannot delete the only administrator of the workspace")
        void deleteMember_targetIsOnlyAdmin_throwsBadRequestException() {
                when(memberRepository.findById(adminMember.getId())).thenReturn(Optional.of(adminMember));
                when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                                .thenReturn(Optional.of(adminMember));
                when(memberRepository.countByWorkspaceIdAndRole(workspaceId, MemberRole.ADMIN)).thenReturn(1L);

                assertThatThrownBy(() -> memberService.deleteMember(adminMember.getId(), adminUserId.toString()))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("Cannot delete the only administrator of the workspace");
        }

        @Test
        @DisplayName("deleteMember: admin can be deleted if another administrator exists")
        void deleteMember_targetIsAdmin_multipleAdmins_deletesAdmin() {
                User secondAdminUser = new User();
                secondAdminUser.setId(UUID.randomUUID());
                Member secondAdmin = new Member();
                secondAdmin.setId(UUID.randomUUID());
                secondAdmin.setUser(secondAdminUser);
                secondAdmin.setWorkspace(workspace);
                secondAdmin.setRole(MemberRole.ADMIN);

                when(memberRepository.findById(adminMember.getId())).thenReturn(Optional.of(adminMember));
                when(memberRepository.findByUserIdAndWorkspaceId(secondAdminUser.getId(), workspaceId))
                                .thenReturn(Optional.of(secondAdmin));
                when(memberRepository.countByWorkspaceIdAndRole(workspaceId, MemberRole.ADMIN)).thenReturn(2L);
                when(memberRepository.countByWorkspaceId(workspaceId)).thenReturn(2L);
                when(memberRepository.findAllByWorkspaceIdAndRole(workspaceId, MemberRole.ADMIN))
                                .thenReturn(List.of(adminMember, secondAdmin));

                memberService.deleteMember(adminMember.getId(), secondAdminUser.getId().toString());

                verify(taskRepository).reassignCreatedBy(adminMember, secondAdmin);
                verify(taskRepository).unassignMember(adminMember);
                verify(memberRepository).delete(adminMember);
        }

        @Test
        @DisplayName("deleteMember: cannot delete the last member of a workspace")
        void deleteMember_lastMember_throwsBadRequestException() {
                when(memberRepository.findById(regularMember.getId())).thenReturn(Optional.of(regularMember));
                when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                                .thenReturn(Optional.of(adminMember));
                when(memberRepository.countByWorkspaceId(workspaceId)).thenReturn(1L);

                assertThatThrownBy(() -> memberService.deleteMember(regularMember.getId(), adminUserId.toString()))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("Cannot delete the last member of the workspace");
        }

        @Test
        @DisplayName("deleteMember: admin can delete a regular member and task references are cleaned up")
        void deleteMember_adminCaller_deletesMemberAndCleansUpTasks() {
                when(memberRepository.findById(regularMember.getId())).thenReturn(Optional.of(regularMember));
                when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                                .thenReturn(Optional.of(adminMember));
                when(memberRepository.countByWorkspaceId(workspaceId)).thenReturn(2L);
                when(memberRepository.findAllByWorkspaceIdAndRole(workspaceId, MemberRole.ADMIN))
                                .thenReturn(List.of(adminMember));

                memberService.deleteMember(regularMember.getId(), adminUserId.toString());

                verify(taskRepository).reassignCreatedBy(regularMember, adminMember);
                verify(taskRepository).unassignMember(regularMember);
                verify(memberRepository).delete(regularMember);
        }
}
