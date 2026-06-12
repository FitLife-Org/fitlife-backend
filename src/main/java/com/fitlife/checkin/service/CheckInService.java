package com.fitlife.checkin.service;

import com.fitlife.checkin.dto.CheckInResponse;

public interface CheckInService {
    CheckInResponse processCheckIn(Long memberId, String actorUsername);
}
