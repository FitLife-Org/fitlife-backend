package com.fitlife.member;

import com.fitlife.core.response.ApiResponse;
import com.fitlife.core.response.PageResponse;
import com.fitlife.identity.entity.User;
import com.fitlife.member.dto.MemberCreationRequest;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.identity.UserRepository; // Lưu ý: Tương lai nên chuyển việc gọi DB này xuống tầng Service
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

    // Tiêm Interface thay vì Impl để đúng chuẩn thiết kế SOLID
    private final MemberService memberService;
    private final UserRepository userRepository;

    // ==========================================
    // 1. LUỒNG DÀNH CHO HỘI VIÊN (USER)
    // ==========================================

    @PostMapping
    public ResponseEntity<ApiResponse<MemberProfileResponse>> createMember(@Valid @RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<MemberProfileResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Member created successfully")
                .data(result)
                .build());
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


    // ==========================================
    // 2. LUỒNG DÀNH CHO QUẢN TRỊ VIÊN (ADMIN)
    // Bổ sung tiền tố /admin vào đường dẫn để tách biệt API
    // ==========================================

    @GetMapping("/admin")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<MemberProfileResponse>>> getAllMembers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<MemberProfileResponse> result = memberService.getAllMembers(page, size, sortBy, sortDir, keyword);
        return ResponseEntity.ok(ApiResponse.<PageResponse<MemberProfileResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách hội viên thành công")
                .data(result)
                .build());
    }

    @PostMapping("/admin")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> createMemberByAdmin(@RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.createMemberByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<MemberProfileResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Thêm hội viên và tạo tài khoản thành công")
                .data(result)
                .build());
    }

    @PatchMapping("/admin/{id}/toggle-lock")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> toggleMemberLock(@PathVariable Long id) {
        memberService.toggleMemberLock(id);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật trạng thái tài khoản thành công")
                .data(null)
                .build());
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMemberById(@PathVariable Long id) {
        MemberProfileResponse result = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.<MemberProfileResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy thông tin hội viên thành công")
                .data(result)
                .build());
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateMemberByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.updateMemberByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.<MemberProfileResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật thông tin hội viên thành công")
                .data(result)
                .build());
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Đã xóa vĩnh viễn hội viên khỏi hệ thống")
                .data(null)
                .build());
    }
}