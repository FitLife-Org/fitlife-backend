package com.fitlife.member.controller;

import com.fitlife.member.dto.request.MyMemberUpdateRequest;
import com.fitlife.member.dto.response.MemberResponse;
import com.fitlife.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members/me")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<MemberResponse> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(memberService.getMyProfile(username));
    }

    @PutMapping
    public ResponseEntity<MemberResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody MyMemberUpdateRequest request
    ) {
        String username = authentication.getName();
        return ResponseEntity.ok(memberService.updateMyProfile(username, request));
    }
}