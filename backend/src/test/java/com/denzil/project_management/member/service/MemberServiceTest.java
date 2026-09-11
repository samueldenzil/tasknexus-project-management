package com.denzil.project_management.member.service;

import com.denzil.project_management.member.dto.MemberDto;
import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.entity.MemberRole;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.shared.exception.BadRequestException;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.shared.exception.UnauthorizedAccessException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService")
class MemberServiceTest {

        @Mock
        private MemberRepository memberRepository;

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

        // -- deleteMember ------------------------------------------------------

        @Test
        @DisplayName("deleteMember: cannot delete a workspace admin")
        void deleteMember_targetIsAdmin_throwsBadRequestException() {
                when(memberRepository.findById(adminMember.getId())).thenReturn(Optional.of(adminMember));
                when(memberRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                                .thenReturn(Optional.of(adminMember));
                // NOTE: countByWorkspaceId is NOT stubbed here — the ADMIN check at step 5
                // throws BadRequestException before the count check at step 6 is ever reached.

                assertThatThrownBy(() -> memberService.deleteMember(adminMember.getId(), adminUserId.toString()))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("Cannot delete a workspace administrator");
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
}
