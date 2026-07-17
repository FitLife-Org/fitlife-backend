package com.fitlife.checkin.service.impl;

import com.fitlife.checkin.dto.*;
import com.fitlife.checkin.entity.CheckIn;
import com.fitlife.checkin.entity.CheckInQr;
import com.fitlife.checkin.enums.CheckInMethod;
import com.fitlife.checkin.enums.CheckInStatus;
import com.fitlife.checkin.mapper.CheckInMapper;
import com.fitlife.checkin.repository.CheckInQrRepository;
import com.fitlife.checkin.repository.CheckInRepository;
import com.fitlife.checkin.service.CheckInService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.user.entity.User;
import com.fitlife.user.enums.UserStatus;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRepository checkInRepository;
    private final CheckInQrRepository checkInQrRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final CheckInMapper checkInMapper;

    // =========================================================================
    // MEMBER SELF-SERVICE METHODS
    // =========================================================================

    @Override
    @Transactional
    public CheckInResponse memberCheckIn(MemberCheckInRequest request, String memberUsername) {
        CheckInQr qr = checkInQrRepository.findByTokenAndIsActiveTrue(request.getQrToken().trim())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_QR_DATA, "Mã QR phòng tập không tồn tại hoặc đã bị khóa"));

        User user = userRepository.findByUsernameOrEmail(memberUsername, memberUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Member member = memberRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND, "Member profile not found for user"));

        validateMemberEligibility(member);
        Subscription activeSub = validateAndGetActiveSubscription(member.getId());
        validateDailyCheckInUniqueness(member.getId());

        // Check if there is an active session
        Optional<CheckIn> activeCheckInOpt = checkInRepository.findFirstByMemberIdAndCheckOutTimeIsNullAndStatusAndDeletedFalseOrderByCheckInTimeDesc(
                member.getId(), CheckInStatus.SUCCESS
        );
        if (activeCheckInOpt.isPresent()) {
            throw new AppException(ErrorCode.ALREADY_CHECKED_IN_TODAY, "Bạn đang có phiên luyện tập chưa hoàn thành");
        }

        CheckIn checkIn = CheckIn.builder()
                .member(member)
                .subscription(activeSub)
                .checkInQr(qr)
                .checkInTime(LocalDateTime.now())
                .checkInMethod(CheckInMethod.MEMBER_SCAN_GYM_QR)
                .status(CheckInStatus.SUCCESS)
                .deleted(false)
                .build();

        CheckIn saved = checkInRepository.save(checkIn);
        return checkInMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CheckInResponse memberCheckOut(MemberCheckOutRequest request, String memberUsername) {
        checkInQrRepository.findByTokenAndIsActiveTrue(request.getQrToken().trim())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_QR_DATA, "Mã QR phòng tập không tồn tại hoặc đã bị khóa"));

        User user = userRepository.findByUsernameOrEmail(memberUsername, memberUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Member member = memberRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND, "Member profile not found for user"));

        CheckIn checkIn = checkInRepository.findFirstByMemberIdAndCheckOutTimeIsNullAndStatusAndDeletedFalseOrderByCheckInTimeDesc(
                member.getId(), CheckInStatus.SUCCESS
        ).orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND, "Không tìm thấy lượt check-in chưa hoàn thành để check-out"));

        // Accidental double scan protection (Block check-out within 5 minutes of check-in)
        long elapsedMinutes = Duration.between(checkIn.getCheckInTime(), LocalDateTime.now()).toMinutes();
        if (elapsedMinutes < 5) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Khoảng cách giữa check-in và check-out phải tối thiểu 5 phút để tránh quét nhầm");
        }

        checkIn.setCheckOutTime(LocalDateTime.now());
        checkIn.setCheckOutMethod(CheckInMethod.MEMBER_SCAN_GYM_QR);

        CheckIn saved = checkInRepository.save(checkIn);
        return checkInMapper.toResponse(saved);
    }

    @Override
    public CheckInResponse getMemberCurrentStatus(String memberUsername) {
        User user = userRepository.findByUsernameOrEmail(memberUsername, memberUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Member member = memberRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND, "Member profile not found for user"));

        Optional<CheckIn> activeCheckIn = checkInRepository.findFirstByMemberIdAndCheckOutTimeIsNullAndStatusAndDeletedFalseOrderByCheckInTimeDesc(
                member.getId(), CheckInStatus.SUCCESS
        );

        if (activeCheckIn.isPresent()) {
            return checkInMapper.toResponse(activeCheckIn.get());
        }

        return CheckInResponse.builder().isInside(false).build();
    }

    @Override
    public PageResponse<CheckInResponse> getMemberHistory(
            String memberUsername,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {
        return getMyCheckInHistory(memberUsername, fromDate, toDate, page, size);
    }

    // =========================================================================
    // STAFF/ADMIN SUPPORT DESK METHODS
    // =========================================================================

    @Override
    public CheckInLookupResponse lookupMember(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Keyword cannot be empty");
        }

        Optional<Member> memberOpt = memberRepository.findByMemberCodeAndIsDeletedFalse(keyword.trim());
        if (memberOpt.isEmpty()) {
            Page<Member> memberPage = memberRepository.searchMembers(keyword.trim(), null, PageRequest.of(0, 1));
            if (memberPage.hasContent()) {
                memberOpt = Optional.of(memberPage.getContent().get(0));
            }
        }

        if (memberOpt.isEmpty()) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Member member = memberOpt.get();
        User user = member.getUser();

        boolean canCheckIn = true;
        String checkInMessage = "Member can check in";

        if (user == null || user.getIsDeleted()) {
            canCheckIn = false;
            checkInMessage = "Member account does not exist";
        } else if (UserStatus.LOCKED.equals(user.getStatus())) {
            canCheckIn = false;
            checkInMessage = "Member account is locked";
        } else if (UserStatus.INACTIVE.equals(user.getStatus())) {
            canCheckIn = false;
            checkInMessage = "Member account is inactive";
        }

        CurrentSubscriptionResponse currentSubResponse = null;
        if (canCheckIn) {
            List<Subscription> activeSubs = checkInRepository.findActiveSubscriptionsByMemberId(member.getId());
            if (activeSubs.isEmpty()) {
                canCheckIn = false;
                checkInMessage = "Member has no active subscription";
            } else {
                LocalDate today = LocalDate.now();
                Optional<Subscription> validSubOpt = activeSubs.stream()
                        .filter(s -> !s.getStartDate().isAfter(today) && !s.getEndDate().isBefore(today))
                        .findFirst();

                Subscription displaySub = validSubOpt.orElse(activeSubs.get(0));
                currentSubResponse = CurrentSubscriptionResponse.builder()
                        .subscriptionId(displaySub.getId())
                        .packageName(displaySub.getGymPackage().getName())
                        .status(displaySub.getStatus().name())
                        .startDate(displaySub.getStartDate())
                        .endDate(displaySub.getEndDate())
                        .build();

                if (validSubOpt.isEmpty()) {
                    canCheckIn = false;
                    checkInMessage = "Subscription has expired or is not yet active";
                }
            }
        }

        if (canCheckIn) {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
            boolean alreadyCheckedIn = checkInRepository.existsByMemberIdAndCheckInTimeBetweenAndStatusAndDeletedFalse(
                    member.getId(), startOfDay, endOfDay, CheckInStatus.SUCCESS
            );

            if (alreadyCheckedIn) {
                Optional<CheckIn> activeCheckIn = checkInRepository.findFirstByMemberIdAndCheckOutTimeIsNullAndStatusAndDeletedFalseOrderByCheckInTimeDesc(
                        member.getId(), CheckInStatus.SUCCESS
                );
                if (activeCheckIn.isPresent()) {
                    checkInMessage = "Member is currently inside the gym (needs checkout)";
                } else {
                    canCheckIn = false;
                    checkInMessage = "Member already checked in today";
                }
            }
        }

        return CheckInLookupResponse.builder()
                .memberId(member.getId())
                .memberCode(member.getMemberCode())
                .fullName(user != null ? user.getFullName() : null)
                .email(user != null ? user.getEmail() : null)
                .phone(user != null ? user.getPhone() : null)
                .userStatus(user != null ? user.getStatus().name() : null)
                .currentSubscription(currentSubResponse)
                .canCheckIn(canCheckIn)
                .checkInMessage(checkInMessage)
                .build();
    }

    @Override
    @Transactional
    public CheckInResponse staffCheckInMemberQr(StaffMemberQrCheckInRequest request, String staffUsername) {
        String qrData = request.getQrData().trim();
        String memberCode;

        if (qrData.contains(":")) {
            String[] parts = qrData.split(":");
            memberCode = parts[parts.length - 1];
        } else {
            memberCode = qrData;
        }

        User staffUser = userRepository.findByUsernameOrEmail(staffUsername, staffUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Staff user not found"));

        Member member = memberRepository.findByMemberCodeAndIsDeletedFalse(memberCode)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        validateMemberEligibility(member);
        Subscription activeSub = validateAndGetActiveSubscription(member.getId());
        validateDailyCheckInUniqueness(member.getId());

        CheckIn checkIn = CheckIn.builder()
                .member(member)
                .subscription(activeSub)
                .checkInTime(LocalDateTime.now())
                .checkInMethod(CheckInMethod.STAFF_SCAN_MEMBER_QR)
                .status(CheckInStatus.SUCCESS)
                .checkedInBy(staffUser)
                .note(request.getReason())
                .deleted(false)
                .build();

        CheckIn saved = checkInRepository.save(checkIn);
        return checkInMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CheckInResponse staffCheckInManual(StaffManualCheckInRequest request, String staffUsername) {
        User staffUser = userRepository.findByUsernameOrEmail(staffUsername, staffUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Staff user not found"));

        Member member;
        if (request.getMemberId() != null) {
            member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        } else if (request.getMemberCode() != null && !request.getMemberCode().trim().isEmpty()) {
            member = memberRepository.findByMemberCodeAndIsDeletedFalse(request.getMemberCode().trim())
                    .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Either memberId or memberCode is required");
        }

        validateMemberEligibility(member);
        Subscription activeSub = validateAndGetActiveSubscription(member.getId());
        validateDailyCheckInUniqueness(member.getId());

        CheckIn checkIn = CheckIn.builder()
                .member(member)
                .subscription(activeSub)
                .checkInTime(LocalDateTime.now())
                .checkInMethod(CheckInMethod.STAFF_MANUAL)
                .status(CheckInStatus.SUCCESS)
                .checkedInBy(staffUser)
                .note(request.getReason())
                .deleted(false)
                .build();

        CheckIn saved = checkInRepository.save(checkIn);
        return checkInMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CheckInResponse staffCheckOutMember(Long id, String staffUsername) {
        CheckIn checkIn = checkInRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));

        if (checkIn.getCheckOutTime() != null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Lượt check-in này đã được check-out trước đó");
        }

        checkIn.setCheckOutTime(LocalDateTime.now());
        checkIn.setCheckOutMethod(CheckInMethod.STAFF_MANUAL);

        if (checkIn.getNote() != null) {
            checkIn.setNote(checkIn.getNote() + " | Check-out hộ bởi: " + staffUsername);
        } else {
            checkIn.setNote("Check-out hộ bởi: " + staffUsername);
        }

        CheckIn saved = checkInRepository.save(checkIn);
        return checkInMapper.toResponse(saved);
    }

    @Override
    public PageResponse<CheckInResponse> getMembersCurrentlyInside(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkInTime"));
        Page<CheckIn> pageResult = checkInRepository.findByCheckOutTimeIsNullAndStatusAndDeletedFalse(
                CheckInStatus.SUCCESS, pageable
        );
        return PageResponse.from(pageResult, checkInMapper::toResponse);
    }

    @Override
    public PageResponse<CheckInResponse> getAllCheckInHistory(
            String keyword,
            Long memberId,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            int page,
            int size,
            String sort
    ) {
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "checkInTime");
        if (sort != null && !sort.trim().isEmpty()) {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc"))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            sortOrder = Sort.by(direction, property);
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        CheckInStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = CheckInStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid status: " + status);
            }
        }

        Page<CheckIn> checkInPage = checkInRepository.searchCheckIns(
                keyword != null ? keyword.trim() : null,
                memberId,
                startDateTime,
                endDateTime,
                statusEnum,
                false,
                pageable
        );

        return PageResponse.from(checkInPage, checkInMapper::toResponse);
    }

    @Override
    public CheckInResponse getDetail(Long id) {
        CheckIn checkIn = checkInRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        return checkInMapper.toResponse(checkIn);
    }

    @Override
    @Transactional
    public CheckInResponse cancelCheckIn(Long id, CheckInCancelRequest request) {
        CheckIn checkIn = checkInRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));

        if (CheckInStatus.CANCELLED.equals(checkIn.getStatus())) {
            throw new AppException(ErrorCode.CHECKIN_ALREADY_CANCELLED);
        }

        checkIn.setStatus(CheckInStatus.CANCELLED);
        String cancelNote = "[CANCELLED] Reason: " + request.getReason();
        if (checkIn.getNote() != null && !checkIn.getNote().trim().isEmpty()) {
            checkIn.setNote(checkIn.getNote() + " | " + cancelNote);
        } else {
            checkIn.setNote(cancelNote);
        }

        CheckIn saved = checkInRepository.save(checkIn);
        return checkInMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCheckIn(Long id) {
        CheckIn checkIn = checkInRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        checkIn.setDeleted(true);
        checkInRepository.save(checkIn);
    }

    @Override
    public CheckInTodayStatisticsResponse getTodayStatistics() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        long manualCheckIns = checkInRepository.countByCheckInTimeBetweenAndCheckInMethodAndStatusAndDeletedFalse(
                startOfDay, endOfDay, CheckInMethod.STAFF_MANUAL, CheckInStatus.SUCCESS
        );

        long qrCheckIns = checkInRepository.countByCheckInTimeBetweenAndCheckInMethodAndStatusAndDeletedFalse(
                startOfDay, endOfDay, CheckInMethod.MEMBER_SCAN_GYM_QR, CheckInStatus.SUCCESS
        ) + checkInRepository.countByCheckInTimeBetweenAndCheckInMethodAndStatusAndDeletedFalse(
                startOfDay, endOfDay, CheckInMethod.STAFF_SCAN_MEMBER_QR, CheckInStatus.SUCCESS
        );

        long cancelledCheckIns = checkInRepository.countByCheckInTimeBetweenAndStatusAndDeletedFalse(
                startOfDay, endOfDay, CheckInStatus.CANCELLED
        );

        long totalCheckIns = manualCheckIns + qrCheckIns;

        return CheckInTodayStatisticsResponse.builder()
                .date(today)
                .totalCheckIns(totalCheckIns)
                .manualCheckIns(manualCheckIns)
                .qrCheckIns(qrCheckIns)
                .cancelledCheckIns(cancelledCheckIns)
                .build();
    }

    // =========================================================================
    // ADMIN QR MANAGEMENT METHODS
    // =========================================================================

    @Override
    @Transactional
    public AdminCheckInQrResponse createGymQr(AdminCheckInQrRequest request, String adminUsername) {
        User adminUser = userRepository.findByUsernameOrEmail(adminUsername, adminUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String token = UUID.randomUUID().toString();
        CheckInQr qr = CheckInQr.builder()
                .name(request.getName().trim())
                .location(request.getLocation() != null ? request.getLocation().trim() : null)
                .token(token)
                .isActive(request.getActive() != null ? request.getActive() : true)
                .createdBy(adminUser)
                .build();

        CheckInQr saved = checkInQrRepository.save(qr);
        return checkInMapper.toQrResponse(saved);
    }

    @Override
    public List<AdminCheckInQrResponse> getAllGymQrs() {
        List<CheckInQr> list = checkInQrRepository.findAll();
        return checkInMapper.toQrResponseList(list);
    }

    @Override
    public AdminCheckInQrResponse getGymQrDetail(Long id) {
        CheckInQr qr = checkInQrRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_QR_DATA, "Mã QR phòng tập không tồn tại"));
        return checkInMapper.toQrResponse(qr);
    }

    @Override
    @Transactional
    public AdminCheckInQrResponse regenerateGymQrToken(Long id) {
        CheckInQr qr = checkInQrRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_QR_DATA, "Mã QR phòng tập không tồn tại"));

        qr.setToken(UUID.randomUUID().toString());
        qr.setRegeneratedAt(LocalDateTime.now());

        CheckInQr saved = checkInQrRepository.save(qr);
        return checkInMapper.toQrResponse(saved);
    }

    @Override
    @Transactional
    public AdminCheckInQrResponse toggleGymQrStatus(Long id, Boolean active) {
        CheckInQr qr = checkInQrRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_QR_DATA, "Mã QR phòng tập không tồn tại"));

        qr.setIsActive(active);

        CheckInQr saved = checkInQrRepository.save(qr);
        return checkInMapper.toQrResponse(saved);
    }

    // =========================================================================
    // PRIVATE COMMON HELPER METHODS
    // =========================================================================

    private PageResponse<CheckInResponse> getMyCheckInHistory(
            String username,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Member member = memberRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND, "Member profile not found for user"));

        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(LocalTime.MAX) : LocalDateTime.of(2099, 12, 31, 23, 59);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkInTime"));
        Page<CheckIn> checkInPage = checkInRepository.findMyCheckIns(member.getId(), startDateTime, endDateTime, pageable);

        return PageResponse.from(checkInPage, checkInMapper::toResponse);
    }

    private void validateMemberEligibility(Member member) {
        User user = member.getUser();
        if (user == null || user.getIsDeleted()) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND, "Associated user account not found");
        }
        if (UserStatus.LOCKED.equals(user.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (UserStatus.INACTIVE.equals(user.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);
        }
    }

    private Subscription validateAndGetActiveSubscription(Long memberId) {
        List<Subscription> activeSubscriptions = checkInRepository.findActiveSubscriptionsByMemberId(memberId);
        if (activeSubscriptions.isEmpty()) {
            throw new AppException(ErrorCode.NO_ACTIVE_SUBSCRIPTION);
        }

        LocalDate today = LocalDate.now();
        Optional<Subscription> validSubscription = activeSubscriptions.stream()
                .filter(s -> !s.getStartDate().isAfter(today) && !s.getEndDate().isBefore(today))
                .findFirst();

        if (validSubscription.isEmpty()) {
            throw new AppException(ErrorCode.SUBSCRIPTION_EXPIRED);
        }

        return validSubscription.get();
    }

    private void validateDailyCheckInUniqueness(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        boolean alreadyCheckedIn = checkInRepository.existsByMemberIdAndCheckInTimeBetweenAndStatusAndDeletedFalse(
                memberId, startOfDay, endOfDay, CheckInStatus.SUCCESS
        );

        if (alreadyCheckedIn) {
            throw new AppException(ErrorCode.ALREADY_CHECKED_IN_TODAY);
        }
    }
}
