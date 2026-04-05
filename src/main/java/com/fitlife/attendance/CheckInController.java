package com.fitlife.attendance;

import com.fitlife.core.response.ApiResponse;
import com.fitlife.identity.entity.User;
import com.fitlife.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;
    private final UserRepository userRepository;

    /**
     * 1. Staff/Admin: Staff scan card/qr of member
     */
    @PostMapping("/{memberId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<CheckInResponse>> staffProcessCheckIn(
            @PathVariable Long memberId,
            Authentication authentication) {

        // Get information about the Staff performing the operation
        User staffUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Tài khoản nhân viên không hợp lệ"));

        // ANTI-FRAUD LOGIC: Staff/Admins are not allowed to check-in for themselves
        if (staffUser.getMember() != null && staffUser.getMember().getId().equals(memberId)) {
            throw new RuntimeException("LỖI GIAN LẬN: Nhân viên hoặc Quản lý không thể tự check-in cho chính mình tại quầy!");
        }

        CheckInResponse result = checkInService.processCheckIn(memberId, staffUser.getUsername());

        return ResponseEntity.ok(ApiResponse.<CheckInResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Check-in xử lý thành công bởi nhân viên")
                .data(result)
                .build());
    }

    /**
     * 2. Stream Self-Service: Members automatically open the App to scan the code at the door
     */
    @PostMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_MEMBER', 'MEMBER')")
    public ResponseEntity<ApiResponse<CheckInResponse>> memberSelfCheckIn(Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Tài khoản không hợp lệ"));

        if (user.getMember() == null) {
            throw new RuntimeException("Tài khoản này chưa có hồ sơ hội viên!");
        }

        CheckInResponse result = checkInService.processCheckIn(user.getMember().getId(), user.getUsername());

        return ResponseEntity.ok(ApiResponse.<CheckInResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Hội viên tự check-in thành công")
                .data(result)
                .build());
    }
}