package com.fitlife.member;

import com.fitlife.member.dto.HealthMetricRequest;
import com.fitlife.member.entity.HealthMetric;
import com.fitlife.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthMetricServiceImpl implements HealthMetricService {

    private final HealthMetricRepository healthMetricRepository;
    private final MemberRepository memberRepository;

    @Transactional // Rất quan trọng vì ta sẽ update cả 2 bảng (Member và HealthMetric)
    @Override
    public HealthMetric addHealthMetric(String username, HealthMetricRequest request) {
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin hội viên"));

        // Lấy dữ liệu kiểu Double (Code sạch đẹp, không cần BigDecimal)
        Double weight = request.getWeight();
        Double height = request.getHeight();
        Double bmi = 0.0;

        // Logic tính BMI: BMI = weight (kg) / (height (m))^2
        if (height != null && height > 0 && weight != null && weight > 0) {
            double heightInMeters = height / 100.0;
            bmi = weight / (heightInMeters * heightInMeters);
            bmi = Math.round(bmi * 100.0) / 100.0; // Làm tròn 2 chữ số thập phân cho đẹp
        }

        // --- TECH LEAD BONUS: ĐỒNG BỘ DỮ LIỆU ---
        // Cập nhật luôn chỉ số mới nhất vào hồ sơ gốc của Member
        member.setHeight(height);
        member.setWeight(weight);
        memberRepository.save(member);

        // Lưu lịch sử vào bảng HealthMetric để vẽ biểu đồ Tracking
        HealthMetric metric = HealthMetric.builder()
                .member(member)
                .weight(weight)
                .height(height)
                .bmi(bmi)
                .recordedAt(LocalDateTime.now())
                .build();

        return healthMetricRepository.save(metric);
    }

    @Override
    public List<HealthMetric> getMemberHistory(String username) {
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Hội viên không tồn tại"));

        // Trả về danh sách lịch sử, sắp xếp mới nhất lên đầu để Frontend dễ vẽ biểu đồ
        return healthMetricRepository.findByMemberIdOrderByRecordedAtDesc(member.getId());
    }
}