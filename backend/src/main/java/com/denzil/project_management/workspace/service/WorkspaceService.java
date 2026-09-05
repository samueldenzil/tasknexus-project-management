package com.denzil.project_management.workspace.service;

import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.entity.MemberRole;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.user.entity.User;
import com.denzil.project_management.user.repository.UserRepository;
import com.denzil.project_management.workspace.dto.CreateWorkspaceRequest;
import com.denzil.project_management.workspace.dto.WorkspaceDto;
import com.denzil.project_management.workspace.entity.Workspace;
import com.denzil.project_management.workspace.repository.WorkspaceRepository;
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
                .orElseThrow(() -> new RuntimeException("User not found"));

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
}
