package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.DashboardResponse;
import com.fitlife.dto.MemberCreationRequest;
import com.fitlife.dto.MemberResponse;
import com.fitlife.dto.PageResponse; // Thêm thư viện này cho Phân trang
import com.fitlife.entity.User;
import com.fitlife.repository.UserRepository;
import com.fitlife.service.DashboardService;
import com.fitlife.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    // API create member
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

    // API update avatar
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

    // API get dashboard personal
    @GetMapping("me/dashboard")
    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getPersonalDashboard(Authentication auth) {

        // Find user by username from authentication
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Tài khoản không hợp lệ"));

        if (user.getMember() == null) {
            throw new RuntimeException("Bạn chưa thiết lập hồ sơ hội viên!");
        }

        DashboardResponse report = dashboardService.getMemberDashboard(user.getMember().getId());

        return ResponseEntity.ok(ApiResponse.<DashboardResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy báo cáo cá nhân thành công")
                .data(report)
                .build());
    }

    // API get all members (Pagination, Sorting, Filtering) - ADMIN/STAFF
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<MemberResponse>>> getAllMembers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<MemberResponse> result = memberService.getAllMembers(page, size, sortBy, sortDir, keyword);

        return ResponseEntity.ok(ApiResponse.<PageResponse<MemberResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách hội viên thành công")
                .data(result)
                .build());
    }
}