package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.MemberCreationRequest;
import com.fitlife.dto.MemberResponse;
import com.fitlife.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> createMember(@Valid @RequestBody MemberCreationRequest request) {
        MemberResponse result = memberService.createMember(request);

        ApiResponse<MemberResponse> response = ApiResponse.<MemberResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Member created successfully")
                .data(result)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal) throws IOException {

        String avatarUrl = memberService.updateAvatar(principal.getName(), file);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(200)
                .message("Cập nhật ảnh đại diện thành công")
                .data(avatarUrl)
                .build());
    }
}