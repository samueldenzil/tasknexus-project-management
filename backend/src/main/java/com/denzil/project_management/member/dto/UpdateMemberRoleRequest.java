package com.denzil.project_management.member.dto;

import com.denzil.project_management.member.entity.MemberRole;

public record UpdateMemberRoleRequest(
        MemberRole role
) {
}
