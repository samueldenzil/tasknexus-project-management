package com.denzil.project_management.workspace.repository;

import com.denzil.project_management.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    @Query("SELECT m.workspace FROM Member m WHERE m.user.id = :userId")
    List<Workspace> findWorkspacesByUserId(@Param("userId") UUID userId);

    // Used by WorkspaceService to detect invite-code collisions before inserting.
    boolean existsByInviteCode(String inviteCode);
}
