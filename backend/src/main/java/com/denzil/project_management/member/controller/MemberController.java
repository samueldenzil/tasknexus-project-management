package com.denzil.project_management.member.controller;

import com.denzil.project_management.member.dto.MemberDto;
import com.denzil.project_management.member.dto.UpdateMemberRoleRequest;
import com.denzil.project_management.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<MemberDto>> getMembers(
            @RequestParam UUID workspaceId,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.ok(
                memberService.getMembers(workspaceId, userId)
        );
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable UUID memberId,
            @AuthenticationPrincipal String userId
    ) {
        memberService.deleteMember(memberId, userId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<MemberDto> updateMemberRole(
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @AuthenticationPrincipal String userId
    ) {
        MemberDto member = memberService.updateMemberRole(
                memberId,
                request.role(),
                userId
        );

        return ResponseEntity.ok(member);
    }
}
