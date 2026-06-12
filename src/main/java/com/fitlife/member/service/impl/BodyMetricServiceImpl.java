package com.fitlife.member.service.impl;

import com.fitlife.member.dto.BodyMetricRequest;
import com.fitlife.member.entity.BodyMetric;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.BodyMetricRepository;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.member.service.BodyMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BodyMetricServiceImpl implements BodyMetricService {

    private final BodyMetricRepository bodyMetricRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public BodyMetric addBodyMetric(String username, BodyMetricRequest request) {
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("KhÄ‚Â´ng tÄ‚Â¬m thĂ¡ÂºÂ¥y thÄ‚Â´ng tin hĂ¡Â»â„¢i viÄ‚Âªn"));

        // LĂ¡ÂºÂ¥y dĂ¡Â»Â¯ liĂ¡Â»â€¡u kiĂ¡Â»Æ’u Double (Code sĂ¡ÂºÂ¡ch Ă„â€˜Ă¡ÂºÂ¹p, khÄ‚Â´ng cĂ¡ÂºÂ§n BigDecimal)
        Double weight = request.getWeight();
        Double height = request.getHeight();
        Double bmi = 0.0;

        // Logic tÄ‚Â­nh BMI: BMI = weight (kg) / (height (m))^2
        if (height != null && height > 0 && weight != null && weight > 0) {
            double heightInMeters = height / 100.0;
            bmi = weight / (heightInMeters * heightInMeters);
            bmi = Math.round(bmi * 100.0) / 100.0; // LÄ‚Â m trÄ‚Â²n 2 chĂ¡Â»Â¯ sĂ¡Â»â€˜ thĂ¡ÂºÂ­p phÄ‚Â¢n cho Ă„â€˜Ă¡ÂºÂ¹p
        }

        // --- TECH LEAD BONUS: Ă„ÂĂ¡Â»â€™NG BĂ¡Â»Ëœ DĂ¡Â»Â® LIĂ¡Â»â€ U ---
        // CĂ¡ÂºÂ­p nhĂ¡ÂºÂ­t luÄ‚Â´n chĂ¡Â»â€° sĂ¡Â»â€˜ mĂ¡Â»â€ºi nhĂ¡ÂºÂ¥t vÄ‚Â o hĂ¡Â»â€œ sĂ†Â¡ gĂ¡Â»â€˜c cĂ¡Â»Â§a Member
        member.setHeight(BigDecimal.valueOf(height));
        member.setWeight(BigDecimal.valueOf(weight));
        memberRepository.save(member);

        // LĂ†Â°u lĂ¡Â»â€¹ch sĂ¡Â»Â­ vÄ‚Â o bĂ¡ÂºÂ£ng BodyMetric Ă„â€˜Ă¡Â»Æ’ vĂ¡ÂºÂ½ biĂ¡Â»Æ’u Ă„â€˜Ă¡Â»â€œ Tracking
        BodyMetric metric = BodyMetric.builder()
                .member(member)
                .weight(BigDecimal.valueOf(weight))
                .height(BigDecimal.valueOf(height))
                .bmi(BigDecimal.valueOf(bmi))
                .recordedAt(LocalDateTime.now())
                .build();

        return bodyMetricRepository.save(metric);
    }

    @Override
    public List<BodyMetric> getMemberHistory(String username) {
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("HĂ¡Â»â„¢i viÄ‚Âªn khÄ‚Â´ng tĂ¡Â»â€œn tĂ¡ÂºÂ¡i"));

        // TrĂ¡ÂºÂ£ vĂ¡Â»Â danh sÄ‚Â¡ch lĂ¡Â»â€¹ch sĂ¡Â»Â­, sĂ¡ÂºÂ¯p xĂ¡ÂºÂ¿p mĂ¡Â»â€ºi nhĂ¡ÂºÂ¥t lÄ‚Âªn Ă„â€˜Ă¡ÂºÂ§u Ă„â€˜Ă¡Â»Æ’ Frontend dĂ¡Â»â€¦ vĂ¡ÂºÂ½ biĂ¡Â»Æ’u Ă„â€˜Ă¡Â»â€œ
        return bodyMetricRepository.findByMemberIdOrderByRecordedAtDesc(member.getId());
    }
}