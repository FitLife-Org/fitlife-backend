package com.fitlife.checkin.service;

import com.fitlife.checkin.dto.*;
import com.fitlife.common.dto.PageResponse;

import java.time.LocalDate;

public interface CheckInService {

    CheckInLookupResponse lookupMember(String keyword);

    CheckInResponse checkInManual(CheckInManualRequest request, String actorUsername);

    CheckInResponse checkInQr(CheckInQrRequest request, String actorUsername);

    PageResponse<CheckInResponse> getCheckInList(
            String keyword,
            Long memberId,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            int page,
            int size,
            String sort
    );

    CheckInResponse getCheckInDetail(Long id);

    CheckInResponse cancelCheckIn(Long id, CheckInCancelRequest request);

    void deleteCheckIn(Long id);

    PageResponse<CheckInResponse> getMyCheckInHistory(
            String username,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    );

    CheckInTodayStatisticsResponse getTodayStatistics();
}
