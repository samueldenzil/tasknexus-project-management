package com.denzil.project_management.member.entity;

import com.denzil.project_management.shared.entity.BaseEntity;
import com.denzil.project_management.user.entity.User;
import com.denzil.project_management.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_user_workspace", columnNames = { "user_id", "workspace_id" })
})
public class Member extends BaseEntity {

    // A Member links One User to One Workspace
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;
}
