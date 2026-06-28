package com.fitlife.member.controller;

import com.fitlife.common.response.PageResponse;
import com.fitlife.member.dto.AdminMemberCreateRequest;
import com.fitlife.member.dto.MemberDetailResponse;
import com.fitlife.member.dto.MemberResponse;
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
    public ResponseEntity<PageResponse<MemberResponse>> getAllMembers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status) {

        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(memberService.getAllMembersForAdmin(keyword, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDetailResponse> getMemberDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(memberService.getMemberDetailForAdmin(id));
    }


    @PostMapping
    public ResponseEntity<MemberResponse> createMemberByAdmin(@Valid @RequestBody AdminMemberCreateRequest request) {
        MemberResponse response = memberService.createMemberByAdmin(request);
        // Trả về HTTP Status 210 Created theo đúng chuẩn nghiệp vụ thiết kế API Restful
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/code/{memberCode}")
    public ResponseEntity<MemberDetailResponse> getMemberByCode(@PathVariable("memberCode") String memberCode) {
        return ResponseEntity.ok(memberService.getMemberByCodeForAdmin(memberCode));
    }


}