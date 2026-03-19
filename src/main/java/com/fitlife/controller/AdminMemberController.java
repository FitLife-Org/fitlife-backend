package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.MemberCreationRequest;
import com.fitlife.dto.MemberResponse;
import com.fitlife.dto.PageResponse;
import com.fitlife.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/members") // Chuẩn đường dẫn Frontend đang gọi
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')") // Bảo mật 2 lớp
public class AdminMemberController {

    private final MemberService memberService;

    // 1. LẤY DANH SÁCH HỘI VIÊN (ADMIN)
    @GetMapping
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

    // 2. API KHÓA / MỞ KHÓA TÀI KHOẢN (TOGGLE LOCK)
    @PatchMapping("/{id}/toggle-lock")
    public ResponseEntity<ApiResponse<String>> toggleMemberLock(@PathVariable Long id) {
        memberService.toggleMemberLock(id);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật trạng thái tài khoản thành công")
                .data(null)
                .build());
    }

    // 3. XEM CHI TIẾT HỘI VIÊN (READ DETAIL)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(@PathVariable Long id) {
        MemberResponse result = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.<MemberResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy thông tin hội viên thành công")
                .data(result)
                .build());
    }

    // 4. ADMIN CẬP NHẬT THÔNG TIN HỘI VIÊN (UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMemberByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody MemberCreationRequest request) { // Dùng tạm CreationRequest hoặc tạo UpdateRequest mới
        MemberResponse result = memberService.updateMemberByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.<MemberResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật thông tin hội viên thành công")
                .data(result)
                .build());
    }

    // 5. XÓA CỨNG HỘI VIÊN (HARD DELETE) - Khuyến cáo không nên dùng ở Frontend
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Đã xóa vĩnh viễn hội viên khỏi hệ thống")
                .data(null)
                .build());
    }
}