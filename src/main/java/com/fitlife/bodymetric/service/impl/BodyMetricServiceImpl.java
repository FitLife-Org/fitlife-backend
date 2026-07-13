package com.fitlife.bodymetric.service.impl;

import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.dto.request.BodyMetricUpdateRequest;
import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.bodymetric.mapper.BodyMetricMapper;
import com.fitlife.bodymetric.repository.BodyMetricRepository;
import com.fitlife.bodymetric.service.BodyMetricService;
import com.fitlife.common.response.PageResponse;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BodyMetricServiceImpl implements BodyMetricService {

    private final BodyMetricRepository bodyMetricRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final BodyMetricMapper bodyMetricMapper;

    // =========================
    // Admin / Staff
    // =========================

    @Override
    public BodyMetricResponse createByAdmin(BodyMetricCreateRequest request) {
        Member member = getActiveMemberById(request.getMemberId());

        BodyMetric bodyMetric = bodyMetricMapper.toEntity(request);
        bodyMetric.setMember(member);
        bodyMetric.setIsDeleted(false);

        if (bodyMetric.getHeightCm() == null) {
            bodyMetricRepository
                    .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(member.getId())
                    .map(BodyMetric::getHeightCm)
                    .ifPresent(bodyMetric::setHeightCm);
        }

        bodyMetric.calculateBmiIfPossible();

        BodyMetric savedBodyMetric = bodyMetricRepository.save(bodyMetric);
        return bodyMetricMapper.toResponse(savedBodyMetric);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BodyMetricResponse> getBodyMetricsForAdmin(
            Long memberId,
            String keyword,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        validateDateRange(from, to);

        String searchKeyword = normalizeKeyword(keyword);

        Page<BodyMetric> page = bodyMetricRepository.searchBodyMetricsByAdmin(
                memberId,
                searchKeyword,
                from,
                to,
                pageable
        );

        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse getBodyMetricDetailForAdmin(Long id) {
        BodyMetric bodyMetric = bodyMetricRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        return bodyMetricMapper.toResponse(bodyMetric);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BodyMetricResponse> getBodyMetricsByMemberForAdmin(
            Long memberId,
            Pageable pageable
    ) {
        getActiveMemberById(memberId);

        Page<BodyMetric> page = bodyMetricRepository
                .findByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(memberId, pageable);

        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse getLatestBodyMetricByMemberForAdmin(Long memberId) {
        getActiveMemberById(memberId);

        BodyMetric bodyMetric = bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        return bodyMetricMapper.toResponse(bodyMetric);
    }

    @Override
    public BodyMetricResponse updateByAdmin(Long id, BodyMetricUpdateRequest request) {
        BodyMetric bodyMetric = bodyMetricRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        bodyMetricMapper.updateEntity(bodyMetric, request);
        bodyMetric.calculateBmiIfPossible();

        BodyMetric updatedBodyMetric = bodyMetricRepository.save(bodyMetric);
        return bodyMetricMapper.toResponse(updatedBodyMetric);
    }

    @Override
    public void deleteByAdmin(Long id) {
        BodyMetric bodyMetric = bodyMetricRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        bodyMetric.setIsDeleted(true);
        bodyMetricRepository.save(bodyMetric);
    }

    // =========================
    // Member - My Body Metric
    // =========================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BodyMetricResponse> getMyBodyMetrics(Pageable pageable) {
        Member currentMember = getCurrentMember();

        Page<BodyMetric> page = bodyMetricRepository
                .findByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        currentMember.getId(),
                        pageable
                );

        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse getMyBodyMetricDetail(Long id) {
        Member currentMember = getCurrentMember();

        BodyMetric bodyMetric = bodyMetricRepository
                .findByIdAndMemberIdAndIsDeletedFalse(id, currentMember.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        return bodyMetricMapper.toResponse(bodyMetric);
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse getLatestMyBodyMetric() {
        Member currentMember = getCurrentMember();

        BodyMetric bodyMetric = bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(currentMember.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        return bodyMetricMapper.toResponse(bodyMetric);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BodyMetricResponse> getMyBodyMetricHistory(
            LocalDateTime from,
            LocalDateTime to
    ) {
        validateDateRangeRequired(from, to);

        Member currentMember = getCurrentMember();

        return bodyMetricRepository
                .findByMemberIdAndIsDeletedFalseAndRecordedAtBetweenOrderByRecordedAtAsc(
                        currentMember.getId(),
                        from,
                        to
                )
                .stream()
                .map(bodyMetricMapper::toResponse)
                .toList();
    }

    // =========================
    // Private helpers
    // =========================

    private Member getActiveMemberById(Long memberId) {
        if (memberId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        if (Boolean.TRUE.equals(member.getIsDeleted())) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return member;
    }

    private Member getCurrentMember() {
        String principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsernameOrEmail(principal, principal)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return memberRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateDateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateDateRangeRequired(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return keyword.trim();
    }

    private PageResponse<BodyMetricResponse> toPageResponse(Page<BodyMetric> page) {
        return PageResponse.<BodyMetricResponse>builder()
                .content(page.getContent()
                        .stream()
                        .map(bodyMetricMapper::toResponse)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}