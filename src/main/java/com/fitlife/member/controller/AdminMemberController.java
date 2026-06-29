package com.fitlife.member.controller;

import com.fitlife.common.response.PageResponse;
import com.fitlife.member.dto.request.AdminMemberStatusUpdateRequest;
import com.fitlife.member.dto.request.MemberCreateRequest;
import com.fitlife.member.dto.request.MemberUpdateRequest;
import com.fitlife.member.dto.response.MemberResponse;
import com.fitlife.member.dto.response.MemberSummaryResponse;
import com.fitlife.member.enums.MemberStatus;
import com.fitlife.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<PageResponse<MemberSummaryResponse>> getAllMembers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) MemberStatus status
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(memberService.getAllMembersForAdmin(keyword, status, pageable));
    }

    @PostMapping
    public ResponseEntity<MemberResponse> createMemberByAdmin(@Valid @RequestBody MemberCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(memberService.createMemberByAdmin(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMemberDetail(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberDetailForAdmin(id));
    }

    @GetMapping("/code/{memberCode}")
    public ResponseEntity<MemberResponse> getMemberByCode(@PathVariable String memberCode) {
        return ResponseEntity.ok(memberService.getMemberByCodeForAdmin(memberCode));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMemberByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody MemberUpdateRequest request
    ) {
        return ResponseEntity.ok(memberService.updateMemberByAdmin(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MemberResponse> updateMemberStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminMemberStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(memberService.updateMemberStatusByAdmin(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemberByAdmin(@PathVariable Long id) {
        memberService.deleteMemberByAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restoreMemberByAdmin(@PathVariable Long id) {
        memberService.restoreMemberByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}