package com.fitlife.member.controller;

import com.fitlife.core.response.ApiResponse;
import com.fitlife.core.response.PageResponse;
import com.fitlife.member.dto.MemberCreationRequest;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // ==========================================
    // 1. LUỒNG DÀNH CHO HỘI VIÊN (USER)
    // ==========================================

    @PostMapping
    public ResponseEntity<ApiResponse<MemberProfileResponse>> createMember(@Valid @RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.createMember(request);
        // Tái sử dụng hàm static created() vừa viết, code cực kỳ Clean!
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result, "Member created successfully"));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal) throws IOException {
        String avatarUrl = memberService.updateAvatar(principal.getName(), file);
        return ResponseEntity.ok(ApiResponse.success(avatarUrl, "Cập nhật ảnh đại diện thành công"));
    }

    // ==========================================
    // 2. LUỒNG DÀNH CHO QUẢN TRỊ VIÊN (ADMIN)
    // ==========================================

    @GetMapping("/admin")
    // Dùng hasRole('ADMIN') -> Spring Security tự động hiểu là cần tìm chữ "ROLE_ADMIN" trong Token
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<MemberProfileResponse>>> getAllMembers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String keyword) {

        PageResponse<MemberProfileResponse> result = memberService.getAllMembers(page, size, sortBy, sortDir, keyword);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách hội viên thành công"));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> createMemberByAdmin(@Valid @RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.createMemberByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result, "Thêm hội viên và tạo tài khoản thành công"));
    }

    @PatchMapping("/admin/{id}/toggle-lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> toggleMemberLock(@PathVariable Long id) {
        memberService.toggleMemberLock(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái tài khoản thành công"));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMemberById(@PathVariable Long id) {
        MemberProfileResponse result = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy thông tin hội viên thành công"));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateMemberByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.updateMemberByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Cập nhật thông tin hội viên thành công"));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa hội viên khỏi hệ thống"));
    }
}