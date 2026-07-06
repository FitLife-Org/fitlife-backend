package com.fitlife.bodymetric.service.impl;

import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.dto.request.BodyMetricUpdateRequest;
import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.bodymetric.mapper.BodyMetricMapper;
import com.fitlife.bodymetric.repository.BodyMetricRepository;
import com.fitlife.bodymetric.service.BodyMetricService;
import com.fitlife.common.dto.PageResponse;
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
public
class BodyMetricServiceImpl implements BodyMetricService {

    private final BodyMetricRepository bodyMetricRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final BodyMetricMapper bodyMetricMapper;

    @Override
    public BodyMetricResponse createMyBodyMetric(BodyMetricCreateRequest request) {
        Member currentMember = getCurrentMember();

        BodyMetric bodyMetric = bodyMetricMapper.toEntity(request);
        bodyMetric.setMember(currentMember);
        bodyMetric.calculateBmiIfPossible();

        BodyMetric savedBodyMetric = bodyMetricRepository.save(bodyMetric);

        return bodyMetricMapper.toResponse(savedBodyMetric);
    }

    @Override
    public BodyMetricResponse createForMember(Long memberId, BodyMetricCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        BodyMetric bodyMetric = bodyMetricMapper.toEntity(request);
        bodyMetric.setMember(member);
        bodyMetric.calculateBmiIfPossible();

        BodyMetric savedBodyMetric = bodyMetricRepository.save(bodyMetric);

        return bodyMetricMapper.toResponse(savedBodyMetric);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BodyMetricResponse> getMyBodyMetrics(Pageable pageable) {
        Member currentMember = getCurrentMember();

        Page<BodyMetric> page = bodyMetricRepository
                .findByMemberIdOrderByRecordedAtDesc(currentMember.getId(), pageable);

        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BodyMetricResponse> getBodyMetricsByMember(Long memberId, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Page<BodyMetric> page = bodyMetricRepository
                .findByMemberIdOrderByRecordedAtDesc(memberId, pageable);

        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse getMyBodyMetricDetail(Long id) {
        Member currentMember = getCurrentMember();

        BodyMetric bodyMetric = bodyMetricRepository.findByIdAndMemberId(id, currentMember.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        return bodyMetricMapper.toResponse(bodyMetric);
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse getLatestMyBodyMetric() {
        Member currentMember = getCurrentMember();

        BodyMetric bodyMetric = bodyMetricRepository
                .findTopByMemberIdOrderByRecordedAtDesc(currentMember.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        return bodyMetricMapper.toResponse(bodyMetric);
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse getLatestBodyMetricByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }

        BodyMetric bodyMetric = bodyMetricRepository
                .findTopByMemberIdOrderByRecordedAtDesc(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        return bodyMetricMapper.toResponse(bodyMetric);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BodyMetricResponse> getMyBodyMetricHistory(LocalDateTime from, LocalDateTime to) {
        Member currentMember = getCurrentMember();

        if (from == null || to == null || from.isAfter(to)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // ĐÃ FIX: Sửa lại tên hàm tìm kiếm theo khoảng thời gian chính xác của JPA
        return bodyMetricRepository
                .findByMemberIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                        currentMember.getId(),
                        from,
                        to
                )
                .stream()
                .map(bodyMetricMapper::toResponse)
                .toList();
    }

    @Override
    public BodyMetricResponse updateMyBodyMetric(Long id, BodyMetricUpdateRequest request) {
        Member currentMember = getCurrentMember();

        BodyMetric bodyMetric = bodyMetricRepository.findByIdAndMemberId(id, currentMember.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        bodyMetricMapper.updateEntity(bodyMetric, request);
        bodyMetric.calculateBmiIfPossible();

        BodyMetric updatedBodyMetric = bodyMetricRepository.save(bodyMetric);

        return bodyMetricMapper.toResponse(updatedBodyMetric);
    }

    @Override
    public void deleteMyBodyMetric(Long id) {
        Member currentMember = getCurrentMember();

        BodyMetric bodyMetric = bodyMetricRepository.findByIdAndMemberId(id, currentMember.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        bodyMetricRepository.delete(bodyMetric);
    }

    @Override
    public BodyMetricResponse createByAdmin(BodyMetricCreateRequest request) {
        Member member;

        // ĐÃ CẬP NHẬT: Tìm kiếm thông minh bằng Code hoặc ID
        if (request.getMemberCode() != null && !request.getMemberCode().trim().isEmpty()) {
            member = memberRepository.findByMemberCodeAndIsDeletedFalse(request.getMemberCode().trim())
                    .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        }
        else if (request.getMemberId() != null) {
            member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        }
        else {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        BodyMetric bodyMetric = bodyMetricMapper.toEntity(request);
        bodyMetric.setMember(member);
        bodyMetric.calculateBmiIfPossible();

        BodyMetric savedBodyMetric = bodyMetricRepository.save(bodyMetric);

        return bodyMetricMapper.toResponse(savedBodyMetric);
    }

    private Member getCurrentMember() {
        String principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsernameOrEmail(principal, principal)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return memberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BodyMetricResponse> getBodyMetricsForAdmin(String keyword, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        String searchKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        Page<BodyMetric> page = bodyMetricRepository.searchBodyMetricsByAdmin(searchKeyword, from, to, pageable);
        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse getBodyMetricDetailForAdmin(Long id) {
        BodyMetric bodyMetric = bodyMetricRepository.findById(id) // hoặc .findByIdWithMember(id) nếu làm bước 1
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));
        return bodyMetricMapper.toResponse(bodyMetric);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<BodyMetricResponse> getBodyMetricsByMemberForAdmin(Long memberId, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }
        Page<BodyMetric> page = bodyMetricRepository
                .findByMemberIdOrderByRecordedAtDesc(memberId, pageable);

        return toPageResponse(page);
    }


    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse getLatestBodyMetricByMemberForAdmin(Long memberId) {

        if (!memberRepository.existsById(memberId)) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }

        BodyMetric bodyMetric = bodyMetricRepository
                .findTopByMemberIdOrderByRecordedAtDesc(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));


        return bodyMetricMapper.toResponse(bodyMetric);
    }
}