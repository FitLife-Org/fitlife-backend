package com.fitlife.checkin.service;

import com.fitlife.checkin.dto.*;
import com.fitlife.common.response.PageResponse;

import java.time.LocalDate;
import java.util.List;

public interface CheckInService {

    // Member Self-Service Methods
    CheckInResponse memberCheckIn(MemberCheckInRequest request, String memberUsername);

    CheckInResponse memberCheckOut(MemberCheckOutRequest request, String memberUsername);

    CheckInResponse getMemberCurrentStatus(String memberUsername);

    PageResponse<CheckInResponse> getMemberHistory(
            String memberUsername,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    );

    // Staff/Admin Support Desk Methods
    CheckInLookupResponse lookupMember(String keyword);

    CheckInResponse staffCheckInMemberQr(StaffMemberQrCheckInRequest request, String staffUsername);

    CheckInResponse staffCheckInManual(StaffManualCheckInRequest request, String staffUsername);

    CheckInResponse staffCheckOutMember(Long id, String staffUsername);

    PageResponse<CheckInResponse> getMembersCurrentlyInside(int page, int size);

    PageResponse<CheckInResponse> getAllCheckInHistory(
            String keyword,
            Long memberId,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            int page,
            int size,
            String sort
    );

    CheckInResponse getDetail(Long id);

    CheckInResponse cancelCheckIn(Long id, CheckInCancelRequest request);

    void deleteCheckIn(Long id);

    CheckInTodayStatisticsResponse getTodayStatistics();

    // Admin QR Management Methods
    AdminCheckInQrResponse createGymQr(AdminCheckInQrRequest request, String adminUsername);

    List<AdminCheckInQrResponse> getAllGymQrs();

    AdminCheckInQrResponse getGymQrDetail(Long id);

    AdminCheckInQrResponse regenerateGymQrToken(Long id);

    AdminCheckInQrResponse toggleGymQrStatus(Long id, Boolean active);

    List<CheckInResponse> getTodayCheckIns();

    CheckInResponse getLatestCheckIn(String memberUsername);
}
