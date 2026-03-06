package com.fitlife.service;

import com.fitlife.dto.CheckInRequest;
import com.fitlife.dto.CheckInResponse;
import com.fitlife.entity.CheckInHistory;
import com.fitlife.entity.Member;
import com.fitlife.entity.Subscription;
import com.fitlife.repository.CheckInHistoryRepository;
import com.fitlife.repository.MemberRepository;
import com.fitlife.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CheckInHistoryRepository checkInHistoryRepository;

    public CheckInResponse processCheckIn(CheckInRequest request) {
        // 1. Tìm ai đang quẹt thẻ
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found. Fake ID card!"));

        LocalDateTime now = LocalDateTime.now(); // Lấy mốc thời gian quẹt thẻ chính xác đến từng giây
        LocalDate today = LocalDate.now();

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