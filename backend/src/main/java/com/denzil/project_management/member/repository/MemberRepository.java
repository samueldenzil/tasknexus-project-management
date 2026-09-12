package com.denzil.project_management.member.repository;

import com.denzil.project_management.member.entity.Member;
import com.denzil.project_management.member.entity.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);

    boolean existsByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);

    List<Member> findAllByWorkspaceId(UUID workspaceId);

    long countByWorkspaceId(UUID workspaceId);

    long countByWorkspaceIdAndRole(UUID workspaceId, MemberRole role);

    List<Member> findAllByWorkspaceIdAndRole(UUID workspaceId, MemberRole role);

    void deleteAllByWorkspaceId(UUID workspaceId);
}
