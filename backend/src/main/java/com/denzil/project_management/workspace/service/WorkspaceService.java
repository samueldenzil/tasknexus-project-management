package com.denzil.project_management.workspace.service;

import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.entity.MemberRole;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.shared.exception.BadRequestException;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.shared.exception.UnauthorizedAccessException;
import com.denzil.project_management.user.entity.User;
import com.denzil.project_management.user.repository.UserRepository;
import com.denzil.project_management.workspace.dto.CreateWorkspaceRequest;
import com.denzil.project_management.workspace.dto.UpdateWorkspaceRequest;
import com.denzil.project_management.workspace.dto.WorkspaceDto;
import com.denzil.project_management.workspace.entity.Workspace;
import com.denzil.project_management.workspace.repository.WorkspaceRepository;
import org.hibernate.jdbc.Work;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository, MemberRepository memberRepository, UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkspaceDto createWorkspace(CreateWorkspaceRequest request, String userId) {
        // 1. Fetch the user who is creating the workspace
        User owner = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Create the Workspace
        Workspace workspace = new Workspace();
        workspace.setName(request.name());
        // Generate a random 6-character invite code
        workspace.setInviteCode(UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase());
        workspace.setOwner(owner);

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        // 3. Create the Admin Membership
        Member member = new Member();
        member.setUser(owner);
        member.setWorkspace(savedWorkspace);
        member.setRole(MemberRole.ADMIN);

        memberRepository.save(member);

        // 4. Return the DTO
        return new WorkspaceDto(
                savedWorkspace.getId(),
                savedWorkspace.getName(),
                savedWorkspace.getImageUrl(),
                savedWorkspace.getInviteCode());
    }

    public List<WorkspaceDto> getWorkspaces(String userId) {
        // 1. Fetch the raw entities from the database using our new query
        List<Workspace> workspaces = workspaceRepository.findWorkspacesByUserId(UUID.fromString(userId));

        // 2. Map them to DTOs
        return workspaces.stream()
                .map(w -> new WorkspaceDto(
                        w.getId(),
                        w.getName(),
                        w.getImageUrl(),
                        w.getInviteCode()
                )).toList();
    }

    public WorkspaceDto getWorkspace(UUID workspaceId, String userId) {
        // 1. Authorization Check: Is this user a member of this workspace?
        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

        if (!isMember) {
            throw new ResourceNotFoundException("Workspace not found or access denied");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        // 3. Return the DTO
        return new WorkspaceDto(
                workspace.getId(),
                workspace.getName(),
                workspace.getImageUrl(),
                workspace.getInviteCode()
        );
    }

    public WorkspaceDto updateWorkspace(UUID workspaceId, UpdateWorkspaceRequest request, String userId) {
        Member member = memberRepository.findByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found or access denied"));

        if (!member.getRole().equals(MemberRole.ADMIN)) {
            throw new UnauthorizedAccessException("Only administrators can update workspace settings");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        workspace.setName(request.name());

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        return new WorkspaceDto(
                savedWorkspace.getId(),
                savedWorkspace.getName(),
                savedWorkspace.getImageUrl(),
                savedWorkspace.getInviteCode()
        );
    }

    public void deleteWorkspace(UUID workspaceId, String userId) {
        Member member = memberRepository.findByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found or access denied"));

        if (!member.getRole().equals(MemberRole.ADMIN)) {
            throw new UnauthorizedAccessException("Only administrators can update workspace settings");
        }

        workspaceRepository.deleteById(workspaceId);
    }

    public WorkspaceDto resetInviteCode(UUID workspaceId, String userId) {
        Member member = memberRepository.findByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found or access denied"));

        if (!member.getRole().equals(MemberRole.ADMIN)) {
            throw new UnauthorizedAccessException("Only administrators can update workspace settings");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        workspace.setInviteCode(UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase());

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        return new WorkspaceDto(
                savedWorkspace.getId(),
                savedWorkspace.getName(),
                savedWorkspace.getImageUrl(),
                savedWorkspace.getInviteCode()
        );
    }

    @Transactional
    public WorkspaceDto joinWorkspace(UUID workspaceId, String inviteCode, String userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isMember = memberRepository.existsByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId);

        if (isMember) {
            throw new BadRequestException("You are already a member of this workspace");
        }

        if (!workspace.getInviteCode().equals(inviteCode)) {
            throw new BadRequestException("Invalid Invite code");
        }

        Member member = new Member();
        member.setUser(user);
        member.setWorkspace(workspace);
        member.setRole(MemberRole.MEMBER);

        memberRepository.save(member);

        return new WorkspaceDto(
                workspace.getId(),
                workspace.getName(),
                workspace.getImageUrl(),
                workspace.getInviteCode()
        );
    }
}
