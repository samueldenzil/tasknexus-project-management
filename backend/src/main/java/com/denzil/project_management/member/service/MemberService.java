package com.denzil.project_management.member.service;

import com.denzil.project_management.member.dto.MemberDto;
import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.entity.MemberRole;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.shared.exception.BadRequestException;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.shared.exception.UnauthorizedAccessException;
import com.denzil.project_management.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberDto> getMembers(UUID workspaceId, String currentUserId) {
        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(currentUserId), workspaceId);

        if (!isMember) {
            throw new ResourceNotFoundException("Workspace not found or access denied");
        }

        List<Member> members = memberRepository.findAllByWorkspaceId(workspaceId);

        return members.stream()
                .map(m -> {
                    User user = m.getUser();
                    return new MemberDto(m.getId(), user.getId(), user.getName(), user.getEmail(), m.getRole());
                })
                .toList();
    }

    @Transactional
    public void deleteMember(UUID memberId, String currentUserId) {
        UUID userId = UUID.fromString(currentUserId);

        // 1. Find the member to delete
        Member memberToDelete = memberRepository
                .findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        // 2. Get the workspace from the member being deleted
        UUID workspaceId = memberToDelete.getWorkspace().getId();

        // 3. Find the current user's membership in the same workspace
        Member currentUser = memberRepository
                .findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found or access denied"));

        // 4. Verify current user has permission (Must be ADMIN, OR they must be
        // deleting themselves)
        boolean isSelf = memberToDelete.getUser().getId().equals(userId);

        if (!MemberRole.ADMIN.equals(currentUser.getRole()) && !isSelf) {
            throw new UnauthorizedAccessException(
                    "Only workspace administrators can remove members, or you can remove yourself to leave.");
        }

        // 5. Prevent deleting an ADMIN
        if (MemberRole.ADMIN.equals(memberToDelete.getRole())) {
            throw new BadRequestException("Cannot delete a workspace administrator");
        }

        // 6. Prevent deleting the last member
        if (memberRepository.countByWorkspaceId(workspaceId) <= 1) {
            throw new BadRequestException("Cannot delete the last member of the workspace");
        }

        // 7. Delete member
        memberRepository.delete(memberToDelete);
    }

    @Transactional
    public MemberDto updateMemberRole(UUID memberId, MemberRole newRole, String currentUserId) {
        // 1. Find the member to update
        Member memberToUpdate = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        // 2. Get the workspace from the member being updated
        UUID workspaceId = memberToUpdate.getWorkspace().getId();

        // 3. Find the current user's membership in the same workspace
        Member currentUser = memberRepository
                .findByUserIdAndWorkspaceId(UUID.fromString(currentUserId), workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found or access denied"));

        // 4. Verify current user has permission
        if (!MemberRole.ADMIN.equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("Only workspace administrators can update members");
        }

        // 5. Update the member's role
        memberToUpdate.setRole(newRole);

        // 6. Save the updated member
        Member updatedMember = memberRepository.save(memberToUpdate);

        return new MemberDto(
                updatedMember.getId(),
                updatedMember.getUser().getId(),
                updatedMember.getUser().getName(),
                updatedMember.getUser().getEmail(),
                updatedMember.getRole());
    }
}
