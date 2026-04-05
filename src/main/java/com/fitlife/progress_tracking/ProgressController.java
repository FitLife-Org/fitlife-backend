package com.fitlife.progress_tracking;

import com.fitlife.core.response.ApiResponse;
import com.fitlife.identity.entity.User;
import com.fitlife.identity.UserRepository;
import com.fitlife.progress_tracking.dto.MemberProgressSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressFacadeService progressFacadeService;
    private final UserRepository userRepository;

    @GetMapping("/my-summary")
    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
    public ResponseEntity<ApiResponse<MemberProgressSummaryResponse>> getPersonalDashboard(Authentication auth) {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Tài khoản không hợp lệ"));

        if (user.getMember() == null) {
            throw new RuntimeException("Bạn chưa thiết lập hồ sơ hội viên!");
        }

        MemberProgressSummaryResponse report = progressFacadeService.getMyProgress(user.getMember().getId());

        return ResponseEntity.ok(ApiResponse.<MemberProgressSummaryResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy báo cáo cá nhân thành công")
                .data(report)
                .build());
    }
}