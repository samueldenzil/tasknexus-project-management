package com.denzil.project_management.member.repository;

import com.denzil.project_management.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);

    boolean existsByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);
}
