package com.denzil.project_management.member.dto;

import com.denzil.project_management.member.entity.MemberRole;

import java.util.UUID;

public record MemberDto(
        UUID id,
        UUID userId,
        String name,
        String email,
        MemberRole role
) {
}
