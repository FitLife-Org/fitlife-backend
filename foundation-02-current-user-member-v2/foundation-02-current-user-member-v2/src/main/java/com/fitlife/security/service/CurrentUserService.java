package com.fitlife.security.service;

import com.fitlife.user.entity.User;

/**
 * Điểm truy cập dùng chung tới tài khoản đang được xác thực.
 *
 * Các service nghiệp vụ không nên tự đọc SecurityContextHolder hoặc tự suy
 * luận principal là email/username. Mọi cách phân giải principal được gom tại
 * service này để tránh lặp code và tránh dùng nhầm users.id.
 */
public interface CurrentUserService {

    /**
     * Trả về User đang đăng nhập.
     *
     * @throws com.fitlife.common.exception.AppException khi request chưa được
     *         xác thực hoặc tài khoản trong token không còn tồn tại/hợp lệ.
     */
    User getCurrentUser();

    /**
     * Trả về khóa chính của bảng users cho tài khoản đang đăng nhập.
     */
    default Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
