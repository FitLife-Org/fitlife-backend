package com.fitlife.bodymetric.service.impl;

import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.dto.request.BodyMetricUpdateRequest;
import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.bodymetric.mapper.BodyMetricMapper;
import com.fitlife.bodymetric.repository.BodyMetricRepository;
import com.fitlife.bodymetric.service.BodyMetricService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BodyMetricServiceImpl
        implements BodyMetricService {

    private final BodyMetricRepository
            bodyMetricRepository;

    private final MemberRepository
            memberRepository;

    private final UserRepository
            userRepository;

    private final BodyMetricMapper
            bodyMetricMapper;

    // =====================================================
    // ADMIN / STAFF
    // =====================================================

    @Override
    public BodyMetricResponse createByAdmin(
            BodyMetricCreateRequest request
    ) {
        if (
                request == null ||
                        request.getMemberId() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        validateRecordedAt(
                request.getRecordedAt()
        );

        Member member =
                getActiveMemberById(
                        request.getMemberId()
                );

        User currentUser =
                getCurrentUser();

        BodyMetric bodyMetric =
                createBodyMetricEntity(
                        member,
                        currentUser,
                        request
                );

        BodyMetric savedBodyMetric =
                bodyMetricRepository.save(
                        bodyMetric
                );

        return bodyMetricMapper.toResponse(
                savedBodyMetric
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BodyMetricResponse>
    getBodyMetricsForAdmin(
            Long memberId,
            String keyword,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        validatePageable(pageable);

        validateDateRange(
                from,
                to
        );

        if (memberId != null) {
            getActiveMemberById(
                    memberId
            );
        }

        String normalizedKeyword =
                normalizeNullableText(
                        keyword
                );

        Page<BodyMetric> page =
                bodyMetricRepository
                        .searchBodyMetricsByAdmin(
                                memberId,
                                normalizedKeyword,
                                from,
                                to,
                                pageable
                        );

        return toPageResponse(
                page
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse
    getBodyMetricDetailForAdmin(
            Long id
    ) {
        BodyMetric bodyMetric =
                getBodyMetricById(
                        id
                );

        return bodyMetricMapper.toResponse(
                bodyMetric
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BodyMetricResponse>
    getBodyMetricsByMemberForAdmin(
            Long memberId,
            Pageable pageable
    ) {
        validatePageable(pageable);

        Member member =
                getActiveMemberById(
                        memberId
                );

        Page<BodyMetric> page =
                bodyMetricRepository
                        .findByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId(),
                                pageable
                        );

        return toPageResponse(
                page
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse
    getLatestBodyMetricByMemberForAdmin(
            Long memberId
    ) {
        Member member =
                getActiveMemberById(
                        memberId
                );

        BodyMetric latestBodyMetric =
                getLatestBodyMetricEntity(
                        member.getId()
                );

        return bodyMetricMapper.toResponse(
                latestBodyMetric
        );
    }

    @Override
    public BodyMetricResponse updateByAdmin(
            Long id,
            BodyMetricUpdateRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        validateRecordedAt(
                request.getRecordedAt()
        );

        BodyMetric bodyMetric =
                getBodyMetricById(
                        id
                );

        bodyMetricMapper.updateEntity(
                bodyMetric,
                request
        );

        bodyMetric.setNote(
                normalizeNullableText(
                        bodyMetric.getNote()
                )
        );

        validateRequiredMetricValues(
                bodyMetric
        );

        /*
         * BMI luôn được tính lại tại backend
         * sau khi cập nhật cân nặng hoặc chiều cao.
         */
        bodyMetric.calculateBmi();

        BodyMetric updatedBodyMetric =
                bodyMetricRepository.save(
                        bodyMetric
                );

        return bodyMetricMapper.toResponse(
                updatedBodyMetric
        );
    }

    @Override
    public void deleteByAdmin(
            Long id
    ) {
        BodyMetric bodyMetric =
                getBodyMetricById(
                        id
                );

        /*
         * Xóa mềm để không làm mất dữ liệu lịch sử
         * được sử dụng bởi biểu đồ và AI.
         */
        bodyMetric.setIsDeleted(
                true
        );

        bodyMetricRepository.save(
                bodyMetric
        );
    }

    // =====================================================
    // CURRENT MEMBER
    // =====================================================

    @Override
    public BodyMetricResponse createMyBodyMetric(
            BodyMetricCreateRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        validateRecordedAt(
                request.getRecordedAt()
        );

        Member currentMember =
                getCurrentMember();

        User currentUser =
                getCurrentUser();

        /*
         * Không tin memberId do client gửi lên.
         * Member hiện tại luôn được resolve từ access token.
         */
        BodyMetric bodyMetric =
                createBodyMetricEntity(
                        currentMember,
                        currentUser,
                        request
                );

        BodyMetric savedBodyMetric =
                bodyMetricRepository.save(
                        bodyMetric
                );

        return bodyMetricMapper.toResponse(
                savedBodyMetric
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BodyMetricResponse>
    getMyBodyMetrics(
            Pageable pageable
    ) {
        validatePageable(pageable);

        Member currentMember =
                getCurrentMember();

        Page<BodyMetric> page =
                bodyMetricRepository
                        .findByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                currentMember.getId(),
                                pageable
                        );

        return toPageResponse(
                page
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse
    getMyBodyMetricDetail(
            Long id
    ) {
        if (id == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Member currentMember =
                getCurrentMember();

        BodyMetric bodyMetric =
                bodyMetricRepository
                        .findByIdAndMemberIdAndIsDeletedFalse(
                                id,
                                currentMember.getId()
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode.BODY_METRIC_NOT_FOUND
                                )
                        );

        return bodyMetricMapper.toResponse(
                bodyMetric
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BodyMetricResponse
    getLatestMyBodyMetric() {
        Member currentMember =
                getCurrentMember();

        BodyMetric latestBodyMetric =
                getLatestBodyMetricEntity(
                        currentMember.getId()
                );

        return bodyMetricMapper.toResponse(
                latestBodyMetric
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BodyMetricResponse>
    getMyBodyMetricHistory(
            LocalDateTime from,
            LocalDateTime to
    ) {
        validateRequiredDateRange(
                from,
                to
        );

        Member currentMember =
                getCurrentMember();

        return bodyMetricRepository
                .findByMemberIdAndIsDeletedFalseAndRecordedAtBetweenOrderByRecordedAtAsc(
                        currentMember.getId(),
                        from,
                        to
                )
                .stream()
                .map(
                        bodyMetricMapper::toResponse
                )
                .toList();
    }

    // =====================================================
    // ENTITY CREATION
    // =====================================================

    private BodyMetric createBodyMetricEntity(
            Member member,
            User createdBy,
            BodyMetricCreateRequest request
    ) {
        BodyMetric bodyMetric =
                bodyMetricMapper.toEntity(
                        request
                );

        bodyMetric.setMember(
                member
        );

        bodyMetric.setCreatedBy(
                createdBy
        );

        bodyMetric.setIsDeleted(
                false
        );

        bodyMetric.setNote(
                normalizeNullableText(
                        request.getNote()
                )
        );

        if (
                bodyMetric.getRecordedAt() ==
                        null
        ) {
            bodyMetric.setRecordedAt(
                    LocalDateTime.now()
            );
        }

        resolveHeightFromLatestMetric(
                bodyMetric,
                member.getId()
        );

        validateRequiredMetricValues(
                bodyMetric
        );

        bodyMetric.calculateBmi();

        return bodyMetric;
    }

    /**
     * Nếu request không có chiều cao thì lấy chiều cao
     * từ lần đo gần nhất của Member.
     *
     * Nếu đây là lần đo đầu tiên thì chiều cao bắt buộc
     * phải được nhập.
     */
    private void resolveHeightFromLatestMetric(
            BodyMetric bodyMetric,
            Long memberId
    ) {
        if (
                bodyMetric.getHeightCm() !=
                        null
        ) {
            return;
        }

        bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        memberId
                )
                .map(
                        BodyMetric::getHeightCm
                )
                .ifPresent(
                        bodyMetric::setHeightCm
                );

        if (
                bodyMetric.getHeightCm() ==
                        null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateRequiredMetricValues(
            BodyMetric bodyMetric
    ) {
        if (
                bodyMetric.getWeightKg() ==
                        null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                bodyMetric.getHeightCm() ==
                        null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                bodyMetric.getRecordedAt() ==
                        null
        ) {
            bodyMetric.setRecordedAt(
                    LocalDateTime.now()
            );
        }

        validateRecordedAt(
                bodyMetric.getRecordedAt()
        );
    }

    // =====================================================
    // MEMBER / USER RESOLUTION
    // =====================================================

    private Member getActiveMemberById(
            Long memberId
    ) {
        if (memberId == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Member member =
                memberRepository
                        .findById(
                                memberId
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode.MEMBER_NOT_FOUND
                                )
                        );

        if (
                Boolean.TRUE.equals(
                        member.getIsDeleted()
                )
        ) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        return member;
    }

    private Member getCurrentMember() {
        User currentUser =
                getCurrentUser();

        return memberRepository
                .findByUserIdAndIsDeletedFalse(
                        currentUser.getId()
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.MEMBER_NOT_FOUND
                        )
                );
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        authentication.getName() == null ||
                        authentication.getName().isBlank()
        ) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        String principal =
                authentication
                        .getName()
                        .trim();

        return userRepository
                .findByUsernameOrEmail(
                        principal,
                        principal
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    // =====================================================
    // BODY METRIC RESOLUTION
    // =====================================================

    private BodyMetric getBodyMetricById(
            Long id
    ) {
        if (id == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return bodyMetricRepository
                .findByIdAndIsDeletedFalse(
                        id
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.BODY_METRIC_NOT_FOUND
                        )
                );
    }

    private BodyMetric getLatestBodyMetricEntity(
            Long memberId
    ) {
        return bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        memberId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.BODY_METRIC_NOT_FOUND
                        )
                );
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateRecordedAt(
            LocalDateTime recordedAt
    ) {
        if (
                recordedAt != null &&
                        recordedAt.isAfter(
                                LocalDateTime.now()
                        )
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateDateRange(
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (
                from != null &&
                        from.isAfter(
                                LocalDateTime.now()
                        )
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                to != null &&
                        to.isAfter(
                                LocalDateTime.now()
                        )
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                from != null &&
                        to != null &&
                        from.isAfter(to)
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateRequiredDateRange(
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (
                from == null ||
                        to == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        validateDateRange(
                from,
                to
        );
    }

    private void validatePageable(
            Pageable pageable
    ) {
        if (pageable == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                pageable.getPageNumber() < 0 ||
                        pageable.getPageSize() < 1 ||
                        pageable.getPageSize() > 100
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    // =====================================================
    // NORMALIZATION / RESPONSE
    // =====================================================

    private String normalizeNullableText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private PageResponse<BodyMetricResponse>
    toPageResponse(
            Page<BodyMetric> page
    ) {
        return PageResponse
                .<BodyMetricResponse>builder()
                .content(
                        page.getContent()
                                .stream()
                                .map(
                                        bodyMetricMapper::toResponse
                                )
                                .toList()
                )
                .page(
                        page.getNumber()
                )
                .size(
                        page.getSize()
                )
                .totalElements(
                        page.getTotalElements()
                )
                .totalPages(
                        page.getTotalPages()
                )
                .first(
                        page.isFirst()
                )
                .last(
                        page.isLast()
                )
                .empty(
                        page.isEmpty()
                )
                .build();
    }
}