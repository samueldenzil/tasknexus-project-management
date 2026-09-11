package com.denzil.project_management.workspace.service;

import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.entity.MemberRole;
import com.denzil.project_management.member.repository.MemberRepository;
import com.denzil.project_management.shared.dto.AnalyticsDto;
import com.denzil.project_management.shared.dto.TaskAnalyticsProjection;
import com.denzil.project_management.shared.exception.BadRequestException;
import com.denzil.project_management.shared.exception.ResourceNotFoundException;
import com.denzil.project_management.shared.exception.UnauthorizedAccessException;
import com.denzil.project_management.task.repository.TaskRepository;
import com.denzil.project_management.user.entity.User;
import com.denzil.project_management.user.repository.UserRepository;
import com.denzil.project_management.workspace.dto.CreateWorkspaceRequest;
import com.denzil.project_management.workspace.dto.UpdateWorkspaceRequest;
import com.denzil.project_management.workspace.dto.WorkspaceDto;
import com.denzil.project_management.workspace.entity.Workspace;
import com.denzil.project_management.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.security.SecureRandom;
import java.util.UUID;

@Service
public class WorkspaceService {

        // Unambiguous uppercase alphanumeric characters — excludes visually similar
        // pairs (0/O, 1/I) to reduce transcription errors when codes are shared
        // manually
        private static final String INVITE_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        private static final int INVITE_CODE_LENGTH = 8;
        private static final SecureRandom SECURE_RANDOM = new SecureRandom();

        private final WorkspaceRepository workspaceRepository;
        private final MemberRepository memberRepository;
        private final UserRepository userRepository;
        private final TaskRepository taskRepository;

        public WorkspaceService(WorkspaceRepository workspaceRepository, MemberRepository memberRepository,
                        UserRepository userRepository, TaskRepository taskRepository) {
                this.workspaceRepository = workspaceRepository;
                this.memberRepository = memberRepository;
                this.userRepository = userRepository;
                this.taskRepository = taskRepository;
        }

        /**
         * Generates a cryptographically random invite code of
         * {@value INVITE_CODE_LENGTH}
         * characters drawn from {@value INVITE_CODE_ALPHABET}.
         * Provides ~34^8 ≈ 1.8 trillion possible codes.
         */
        private static String generateInviteCode() {
                StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
                for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                        sb.append(INVITE_CODE_ALPHABET.charAt(SECURE_RANDOM.nextInt(INVITE_CODE_ALPHABET.length())));
                }
                return sb.toString();
        }

        @Transactional
        public WorkspaceDto createWorkspace(CreateWorkspaceRequest request, String userId) {
                // 1. Fetch the user who is creating the workspace
                User owner = userRepository.findById(UUID.fromString(userId))
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                // 2. Create the Workspace
                Workspace workspace = new Workspace();
                workspace.setName(request.name());
                workspace.setInviteCode(generateInviteCode());
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
                                                w.getInviteCode()))
                                .toList();
        }

        public WorkspaceDto getWorkspace(UUID workspaceId, String userId) {
                Member member = memberRepository.findByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Workspace not found or access denied"));

                Workspace workspace = member.getWorkspace();

                return new WorkspaceDto(
                                workspace.getId(),
                                workspace.getName(),
                                workspace.getImageUrl(),
                                workspace.getInviteCode());
        }

        public WorkspaceDto updateWorkspace(UUID workspaceId, UpdateWorkspaceRequest request, String userId) {
                Member member = memberRepository.findByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Workspace not found or access denied"));

                if (!member.getRole().equals(MemberRole.ADMIN)) {
                        throw new UnauthorizedAccessException("Only administrators can update workspace settings");
                }

                // Workspace is already loaded via the member — no second query needed
                Workspace workspace = member.getWorkspace();
                workspace.setName(request.name());

                Workspace savedWorkspace = workspaceRepository.save(workspace);

                return new WorkspaceDto(
                                savedWorkspace.getId(),
                                savedWorkspace.getName(),
                                savedWorkspace.getImageUrl(),
                                savedWorkspace.getInviteCode());
        }

        public void deleteWorkspace(UUID workspaceId, String userId) {
                Member member = memberRepository.findByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Workspace not found or access denied"));

                if (!member.getRole().equals(MemberRole.ADMIN)) {
                        throw new UnauthorizedAccessException("Only administrators can delete a workspace");
                }

                workspaceRepository.deleteById(workspaceId);
        }

        public WorkspaceDto resetInviteCode(UUID workspaceId, String userId) {
                Member member = memberRepository.findByUserIdAndWorkspaceId(UUID.fromString(userId), workspaceId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Workspace not found or access denied"));

                if (!member.getRole().equals(MemberRole.ADMIN)) {
                        throw new UnauthorizedAccessException("Only administrators can update workspace settings");
                }

                // Workspace is already loaded via the member — no second query needed
                Workspace workspace = member.getWorkspace();
                workspace.setInviteCode(generateInviteCode());

                Workspace savedWorkspace = workspaceRepository.save(workspace);

                return new WorkspaceDto(
                                savedWorkspace.getId(),
                                savedWorkspace.getName(),
                                savedWorkspace.getImageUrl(),
                                savedWorkspace.getInviteCode());
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
                                workspace.getInviteCode());
        }

        public AnalyticsDto getWorkspaceAnalytics(UUID workspaceId, String userId) {
                // 1. Authorization check
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
                TaskAnalyticsProjection thisMonth = taskRepository.getWorkspaceAnalytics(
                                workspaceId, startOfThisMonth, startOfNextMonth, today);

                TaskAnalyticsProjection lastMonth = taskRepository.getWorkspaceAnalytics(
                                workspaceId, startOfLastMonth, startOfThisMonth, today);

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
