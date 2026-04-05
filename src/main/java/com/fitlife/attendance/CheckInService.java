package com.fitlife.attendance;

public interface CheckInService {
        // Đảm bảo an toàn dữ liệu
    CheckInResponse processCheckIn(Long memberId, String actorUsername);
}
