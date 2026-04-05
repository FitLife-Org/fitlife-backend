package com.fitlife.attendance;

import com.fitlife.attendance.entity.CheckInHistory;
import com.fitlife.member.entity.Member;
import com.fitlife.subscription.Subscription;
import com.fitlife.member.MemberRepository;
import com.fitlife.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CheckInHistoryRepository checkInHistoryRepository;

    @Transactional
    @Override
    public CheckInResponse processCheckIn(Long memberId, String actorUsername) {
        // 1. Tìm ai đang quẹt thẻ
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found. Fake ID card!"));

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        // 1.5 KIỂM TRA SPAM: Hôm nay đã vào cửa thành công chưa?
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Optional<CheckInHistory> alreadyCheckedIn = checkInHistoryRepository
                .findSuccessfulCheckInToday(member, startOfDay, endOfDay);

        if (alreadyCheckedIn.isPresent()) {
            return CheckInResponse.builder()
                    .memberId(member.getId())
                    .memberName(member.getFullName())
                    .checkInTime(now)
                    .status("ALREADY_CHECKED_IN")
                    .message("Bạn đã điểm danh thành công trong hôm nay rồi. Chúc buổi tập vui vẻ!")
                    .build();
        }

        // 2. Mặc định là Cấm Cửa
        String accessStatus = "ACCESS_DENIED";
        String message = "No active subscription found.";

        // 3. Tìm gói tập đang ACTIVE của người này
        Optional<Subscription> activeSub = subscriptionRepository.findFirstByMemberAndStatus(member, "ACTIVE");

        if (activeSub.isPresent()) {
            Subscription sub = activeSub.get();
            // ÁP DỤNG DEFENSIVE PROGRAMMING: Kiểm tra ngày tháng cực kỳ gắt gao!
            if (today.isBefore(sub.getStartDate())) {
                message = "Subscription has not started yet. Starts on: " + sub.getStartDate();
            } else if (today.isAfter(sub.getEndDate())) {
                message = "Subscription expired on: " + sub.getEndDate() + ". Please renew!";
            } else {
                // Vượt qua mọi trạm gác -> MỞ CỬA!
                accessStatus = "ACCESS_GRANTED";
                message = "Welcome to FitLife, " + member.getFullName() + "!";
            }
        }

        // 4. BẤT BIẾN: Dù thành công hay thất bại, ĐỀU PHẢI GHI LOG LỊCH SỬ!
        CheckInHistory history = CheckInHistory.builder()
                .member(member)
                .checkInTime(now)
                .status(accessStatus) // Lưu lại kết quả
                // .checkedInBy(actorUsername) // Bỏ comment dòng này nếu trong Entity em đã thêm cột này nhé
                .build();
        checkInHistoryRepository.save(history);

        // 5. Trả kết quả về cho Cửa Từ (Frontend/Turnstile)
        return CheckInResponse.builder()
                .memberId(member.getId())
                .memberName(member.getFullName())
                .checkInTime(now)
                .status(accessStatus)
                .message(message)
                .build();
    }
}