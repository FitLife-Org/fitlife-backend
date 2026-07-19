package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiInputBodyMetricSnapshot;
import com.fitlife.ai.dto.internal.AiInputMemberSnapshot;
import com.fitlife.ai.dto.internal.AiInputRequestSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiInputUserSnapshot;
import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFeedbackRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.response.AiFeedbackResponse;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiPlanItemResponse;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.entity.AiFeedback;
import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.repository.AiFeedbackRepository;
import com.fitlife.ai.repository.AiPlanItemRepository;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.ai.service.AiPlanParserService;
import com.fitlife.ai.service.AiPromptBuilderService;
import com.fitlife.ai.service.AiProviderService;
import com.fitlife.ai.service.AiSuggestionService;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.bodymetric.repository.BodyMetricRepository;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.FitnessGoal;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiSuggestionServiceImpl implements AiSuggestionService {

    private static final int DAILY_AI_LIMIT = 5;
    private static final ZoneId FITLIFE_ZONE_ID =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private static final String FULL_PLAN_PROMPT_VERSION =
            "FULL_PLAN_V1";

    private static final String BODY_ANALYSIS_PROMPT_VERSION =
            "BODY_ANALYSIS_V1";

    private final AiSuggestionRepository aiSuggestionRepository;
    private final AiPlanItemRepository aiPlanItemRepository;
    private final AiFeedbackRepository aiFeedbackRepository;
    private final BodyMetricRepository bodyMetricRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final AiPromptBuilderService aiPromptBuilderService;
    private final AiProviderService aiProviderService;
    private final AiPlanParserService aiPlanParserService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(noRollbackFor = AppException.class)
    public AiSuggestionResponse createFullPlan(
            AiFullPlanRequest request
    ) {
        Member currentMember = getCurrentMember();
        User currentUser = currentMember.getUser();

        checkDailyLimit(currentMember.getId());

        BodyMetric latestBodyMetric = bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        currentMember.getId()
                )
                .orElse(null);

        AiInputSnapshot inputSnapshot = buildFullPlanInputSnapshot(
                currentMember,
                currentUser,
                latestBodyMetric,
                request
        );

        AiSuggestion suggestion = AiSuggestion.builder()
                .member(currentMember)
                .latestBodyMetric(latestBodyMetric)
                .suggestionType(AiSuggestionType.FULL_PLAN)
                .goal(request.getGoal().name())
                .experienceLevel(request.getExperienceLevel())
                .activityLevel(request.getActivityLevel())
                .workoutDaysPerWeek(
                        request.getWorkoutDaysPerWeek()
                )
                .workoutDurationMinutes(
                        request.getWorkoutDurationMinutes()
                )
                .userNote(normalizeText(request.getUserNote()))
                .preferredLanguage(resolveLanguage(
                        request.getPreferredLanguage()
                ))
                .inputSnapshot(toJson(inputSnapshot))
                .status(AiSuggestionStatus.PENDING)
                .promptVersion(FULL_PLAN_PROMPT_VERSION)
                .warningMessage(buildInitialWarningMessage(
                        currentMember,
                        latestBodyMetric
                ))
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .deleted(false)
                .build();

        AiSuggestion savedSuggestion =
                aiSuggestionRepository.save(suggestion);

        try {
            String prompt =
                    aiPromptBuilderService.buildFullPlanPrompt(
                            inputSnapshot
                    );

            String rawAiResponse =
                    aiProviderService.generate(prompt);

            AiGeneratedPlanResponse generatedPlan =
                    aiPlanParserService.parseGeneratedPlan(
                            rawAiResponse
                    );

            savedSuggestion.setAiResponse(toJson(generatedPlan));
            savedSuggestion.setSummary(
                    normalizeText(generatedPlan.getSummary())
            );
            savedSuggestion.setWarningMessage(
                    mergeWarnings(
                            savedSuggestion.getWarningMessage(),
                            joinWarnings(
                                    generatedPlan.getWarnings()
                            )
                    )
            );
            savedSuggestion.markSuccess();

            AiSuggestion updatedSuggestion =
                    aiSuggestionRepository.save(savedSuggestion);

            aiPlanParserService.savePlanItems(
                    updatedSuggestion,
                    generatedPlan
            );

            return toSuggestionResponse(updatedSuggestion);
        } catch (AppException exception) {
            markSuggestionFailed(savedSuggestion);
            throw exception;
        } catch (Exception exception) {
            markSuggestionFailed(savedSuggestion);
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiSuggestionResponse> getMySuggestions(
            Pageable pageable
    ) {
        Member currentMember = getCurrentMember();

        Page<AiSuggestion> page = aiSuggestionRepository
                .findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(
                        currentMember.getId(),
                        pageable
                );

        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiSuggestionResponse> getMySuggestionsByFilter(
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    ) {
        Member currentMember = getCurrentMember();
        Page<AiSuggestion> page;

        if (suggestionType != null && status != null) {
            page = aiSuggestionRepository
                    .findByMemberIdAndSuggestionTypeAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                            currentMember.getId(),
                            suggestionType,
                            status,
                            pageable
                    );
        } else if (suggestionType != null) {
            page = aiSuggestionRepository
                    .findByMemberIdAndSuggestionTypeAndDeletedFalseOrderByCreatedAtDesc(
                            currentMember.getId(),
                            suggestionType,
                            pageable
                    );
        } else if (status != null) {
            page = aiSuggestionRepository
                    .findByMemberIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                            currentMember.getId(),
                            status,
                            pageable
                    );
        } else {
            page = aiSuggestionRepository
                    .findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(
                            currentMember.getId(),
                            pageable
                    );
        }

        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public AiSuggestionDetailResponse getMySuggestionDetail(
            Long id
    ) {
        Member currentMember = getCurrentMember();

        AiSuggestion suggestion = aiSuggestionRepository
                .findByIdAndMemberIdAndDeletedFalse(
                        id,
                        currentMember.getId()
                )
                .orElseThrow(() -> new AppException(
                        ErrorCode.AI_SUGGESTION_NOT_FOUND
                ));

        List<AiPlanItem> items = aiPlanItemRepository
                .findByAiSuggestionIdOrderBySortOrderAscIdAsc(
                        suggestion.getId()
                );

        AiFeedback feedback = aiFeedbackRepository
                .findByAiSuggestionIdAndMemberId(
                        suggestion.getId(),
                        currentMember.getId()
                )
                .orElse(null);

        return toSuggestionDetailResponse(
                suggestion,
                items,
                feedback
        );
    }

    @Override
    @Transactional
    public AiFeedbackResponse submitFeedback(
            Long aiSuggestionId,
            AiFeedbackRequest request
    ) {
        Member currentMember = getCurrentMember();

        AiSuggestion suggestion = aiSuggestionRepository
                .findByIdAndMemberIdAndDeletedFalse(
                        aiSuggestionId,
                        currentMember.getId()
                )
                .orElseThrow(() -> new AppException(
                        ErrorCode.AI_SUGGESTION_NOT_FOUND
                ));

        if (suggestion.getStatus()
                != AiSuggestionStatus.SUCCESS
                && suggestion.getStatus()
                != AiSuggestionStatus.APPLIED) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_NOT_FOUND
            );
        }

        if (aiFeedbackRepository
                .existsByAiSuggestionIdAndMemberId(
                        suggestion.getId(),
                        currentMember.getId()
                )) {
            throw new AppException(
                    ErrorCode.AI_FEEDBACK_ALREADY_EXISTS
            );
        }

        AiFeedback feedback = AiFeedback.builder()
                .aiSuggestion(suggestion)
                .member(currentMember)
                .rating(request.getRating())
                .useful(request.getUseful())
                .comment(normalizeText(request.getComment()))
                .build();

        return toFeedbackResponse(
                aiFeedbackRepository.save(feedback)
        );
    }

    @Override
    @Transactional(noRollbackFor = AppException.class)
    public AiSuggestionDetailResponse analyzeBodyMetric(
            AiBodyAnalysisRequest request
    ) {
        Member currentMember = getCurrentMember();
        User currentUser = currentMember.getUser();

        checkDailyLimit(currentMember.getId());

        BodyMetric latestBodyMetric = bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        currentMember.getId()
                )
                .orElseThrow(() -> new AppException(
                        ErrorCode.BODY_METRIC_NOT_FOUND
                ));

        AiInputSnapshot inputSnapshot =
                buildBodyAnalysisInputSnapshot(
                        currentMember,
                        currentUser,
                        latestBodyMetric,
                        request
                );

        AiSuggestion suggestion = AiSuggestion.builder()
                .member(currentMember)
                .latestBodyMetric(latestBodyMetric)
                .suggestionType(AiSuggestionType.BODY_ANALYSIS)
                .goal(resolveMemberGoal(currentMember))
                .userNote(normalizeText(request.getUserNote()))
                .preferredLanguage(resolveLanguage(
                        request.getPreferredLanguage()
                ))
                .inputSnapshot(toJson(inputSnapshot))
                .status(AiSuggestionStatus.PENDING)
                .promptVersion(BODY_ANALYSIS_PROMPT_VERSION)
                .warningMessage(buildInitialWarningMessage(
                        currentMember,
                        latestBodyMetric
                ))
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .deleted(false)
                .build();

        AiSuggestion savedSuggestion =
                aiSuggestionRepository.save(suggestion);

        try {
            String prompt =
                    aiPromptBuilderService.buildBodyAnalysisPrompt(
                            inputSnapshot
                    );

            String rawAiResponse =
                    aiProviderService.generate(prompt);

            AiGeneratedBodyAnalysisResponse analysis =
                    aiPlanParserService.parseBodyAnalysis(
                            rawAiResponse
                    );

            savedSuggestion.setAiResponse(toJson(analysis));
            savedSuggestion.setSummary(
                    normalizeText(analysis.getSummary())
            );
            savedSuggestion.setWarningMessage(
                    mergeWarnings(
                            savedSuggestion.getWarningMessage(),
                            joinWarnings(analysis.getWarnings())
                    )
            );
            savedSuggestion.markSuccess();

            AiSuggestion updatedSuggestion =
                    aiSuggestionRepository.save(savedSuggestion);

            aiPlanParserService.saveBodyAnalysisItems(
                    updatedSuggestion,
                    analysis
            );

            List<AiPlanItem> items = aiPlanItemRepository
                    .findByAiSuggestionIdOrderBySortOrderAscIdAsc(
                            suggestion.getId()
                    );

            return toSuggestionDetailResponse(
                    updatedSuggestion,
                    items,
                    null
            );
        } catch (AppException exception) {
            markSuggestionFailed(savedSuggestion);
            throw exception;
        } catch (Exception exception) {
            markSuggestionFailed(savedSuggestion);
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    private PageResponse<AiSuggestionResponse> toPageResponse(
            Page<AiSuggestion> page
    ) {
        return PageResponse.<AiSuggestionResponse>builder()
                .content(
                        page.getContent()
                                .stream()
                                .map(this::toSuggestionResponse)
                                .toList()
                )
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private void checkDailyLimit(Long memberId) {
        LocalDate today = LocalDate.now(FITLIFE_ZONE_ID);
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to =
                today.plusDays(1).atStartOfDay();

        long usage = aiSuggestionRepository
                .countByMemberIdAndRequestedAtBetweenAndDeletedFalse(
                        memberId,
                        from,
                        to
                );

        if (usage >= DAILY_AI_LIMIT) {
            throw new AppException(
                    ErrorCode.AI_LIMIT_EXCEEDED
            );
        }
    }

    private AiInputSnapshot buildFullPlanInputSnapshot(
            Member member,
            User user,
            BodyMetric latestBodyMetric,
            AiFullPlanRequest request
    ) {
        return AiInputSnapshot.builder()
                .user(buildUserSnapshot(user))
                .member(buildMemberSnapshot(member))
                .latestBodyMetric(
                        buildBodyMetricSnapshot(latestBodyMetric)
                )
                .request(AiInputRequestSnapshot.builder()
                        .goal(request.getGoal())
                        .experienceLevel(
                                request.getExperienceLevel()
                        )
                        .activityLevel(
                                request.getActivityLevel()
                        )
                        .workoutDaysPerWeek(
                                request.getWorkoutDaysPerWeek()
                        )
                        .workoutDurationMinutes(
                                request.getWorkoutDurationMinutes()
                        )
                        .mealsPerDay(null)
                        .userNote(normalizeText(
                                request.getUserNote()
                        ))
                        .preferredLanguage(resolveLanguage(
                                request.getPreferredLanguage()
                        ))
                        .build())
                .build();
    }

    private AiInputSnapshot buildBodyAnalysisInputSnapshot(
            Member member,
            User user,
            BodyMetric latestBodyMetric,
            AiBodyAnalysisRequest request
    ) {
        return AiInputSnapshot.builder()
                .user(buildUserSnapshot(user))
                .member(buildMemberSnapshot(member))
                .latestBodyMetric(
                        buildBodyMetricSnapshot(latestBodyMetric)
                )
                .request(AiInputRequestSnapshot.builder()
                        .goal(member.getFitnessGoal())
                        .experienceLevel(null)
                        .activityLevel(null)
                        .workoutDaysPerWeek(null)
                        .workoutDurationMinutes(null)
                        .mealsPerDay(null)
                        .userNote(normalizeText(
                                request.getUserNote()
                        ))
                        .preferredLanguage(resolveLanguage(
                                request.getPreferredLanguage()
                        ))
                        .build())
                .build();
    }

    private AiInputUserSnapshot buildUserSnapshot(User user) {
        return AiInputUserSnapshot.builder()
                .fullName(user.getFullName())
                .build();
    }

    private AiInputMemberSnapshot buildMemberSnapshot(
            Member member
    ) {
        return AiInputMemberSnapshot.builder()
                .memberId(member.getId())
                .memberCode(member.getMemberCode())
                .gender(
                        member.getGender() == null
                                ? null
                                : member.getGender().name()
                )
                .dateOfBirth(member.getDateOfBirth())
                .age(calculateAge(member.getDateOfBirth()))
                .joinDate(member.getJoinDate())
                .fitnessGoal(
                        member.getFitnessGoal() == null
                                ? null
                                : member.getFitnessGoal().name()
                )
                .healthNote(
                        normalizeText(member.getHealthNote())
                )
                .build();
    }

    private AiInputBodyMetricSnapshot buildBodyMetricSnapshot(
            BodyMetric bodyMetric
    ) {
        if (bodyMetric == null) {
            return null;
        }

        return AiInputBodyMetricSnapshot.builder()
                .id(bodyMetric.getId())
                .heightCm(bodyMetric.getHeightCm())
                .weightKg(bodyMetric.getWeightKg())
                .bmi(bodyMetric.getBmi())
                .bodyFatPercent(
                        bodyMetric.getBodyFatPercent()
                )
                .muscleMassKg(bodyMetric.getMuscleMassKg())
                .note(normalizeText(bodyMetric.getNote()))
                .recordedAt(bodyMetric.getRecordedAt())
                .build();
    }

    private AiSuggestionResponse toSuggestionResponse(
            AiSuggestion suggestion
    ) {
        Member member = suggestion.getMember();

        return AiSuggestionResponse.builder()
                .id(suggestion.getId())
                .memberId(member.getId())
                .memberCode(member.getMemberCode())
                .memberName(resolveMemberName(member))
                .suggestionType(suggestion.getSuggestionType())
                .goal(suggestion.getGoal())
                .experienceLevel(
                        suggestion.getExperienceLevel()
                )
                .activityLevel(suggestion.getActivityLevel())
                .workoutDaysPerWeek(
                        suggestion.getWorkoutDaysPerWeek()
                )
                .workoutDurationMinutes(
                        suggestion.getWorkoutDurationMinutes()
                )
                .preferredLanguage(
                        suggestion.getPreferredLanguage()
                )
                .summary(suggestion.getSummary())
                .warningMessage(
                        suggestion.getWarningMessage()
                )
                .status(suggestion.getStatus())
                .provider(suggestion.getProvider())
                .modelName(suggestion.getModelName())
                .appliedWorkoutPlanId(
                        suggestion.getAppliedWorkoutPlanId()
                )
                .appliedNutritionPlanId(
                        suggestion.getAppliedNutritionPlanId()
                )
                .requestedAt(suggestion.getRequestedAt())
                .completedAt(suggestion.getCompletedAt())
                .createdAt(suggestion.getCreatedAt())
                .build();
    }

    private AiSuggestionDetailResponse toSuggestionDetailResponse(
            AiSuggestion suggestion,
            List<AiPlanItem> items,
            AiFeedback feedback
    ) {
        Member member = suggestion.getMember();

        return AiSuggestionDetailResponse.builder()
                .id(suggestion.getId())
                .memberId(member.getId())
                .memberCode(member.getMemberCode())
                .memberName(resolveMemberName(member))
                .latestBodyMetricId(
                        suggestion.getLatestBodyMetric() == null
                                ? null
                                : suggestion
                                .getLatestBodyMetric()
                                .getId()
                )
                .suggestionType(
                        suggestion.getSuggestionType()
                )
                .goal(suggestion.getGoal())
                .experienceLevel(
                        suggestion.getExperienceLevel()
                )
                .activityLevel(
                        suggestion.getActivityLevel()
                )
                .workoutDaysPerWeek(
                        suggestion.getWorkoutDaysPerWeek()
                )
                .workoutDurationMinutes(
                        suggestion.getWorkoutDurationMinutes()
                )
                .userNote(suggestion.getUserNote())
                .preferredLanguage(
                        suggestion.getPreferredLanguage()
                )
                .inputSnapshot(
                        jsonToMap(suggestion.getInputSnapshot())
                )
                .aiResponse(
                        jsonToMap(suggestion.getAiResponse())
                )
                .summary(suggestion.getSummary())
                .warningMessage(
                        suggestion.getWarningMessage()
                )
                .provider(suggestion.getProvider())
                .modelName(suggestion.getModelName())
                .promptVersion(suggestion.getPromptVersion())
                .status(suggestion.getStatus())
                .errorCode(suggestion.getErrorCode())
                .errorMessage(suggestion.getErrorMessage())
                .appliedWorkoutPlanId(
                        suggestion.getAppliedWorkoutPlanId()
                )
                .appliedNutritionPlanId(
                        suggestion.getAppliedNutritionPlanId()
                )
                .items(
                        items == null
                                ? List.of()
                                : items.stream()
                                .map(this::toPlanItemResponse)
                                .toList()
                )
                .feedback(
                        feedback == null
                                ? null
                                : toFeedbackResponse(feedback)
                )
                .requestedAt(suggestion.getRequestedAt())
                .completedAt(suggestion.getCompletedAt())
                .createdAt(suggestion.getCreatedAt())
                .updatedAt(suggestion.getUpdatedAt())
                .build();
    }

    private AiPlanItemResponse toPlanItemResponse(
            AiPlanItem item
    ) {
        return AiPlanItemResponse.builder()
                .id(item.getId())
                .aiSuggestionId(
                        item.getAiSuggestion().getId()
                )
                .itemType(item.getItemType())
                .title(item.getTitle())
                .description(item.getDescription())
                .dayNo(item.getDayNo())
                .dayOfWeek(item.getDayOfWeek())
                .exerciseName(item.getExerciseName())
                .sets(item.getSets())
                .reps(item.getReps())
                .restSeconds(item.getRestSeconds())
                .durationMinutes(item.getDurationMinutes())
                .mealName(item.getMealName())
                .portionText(item.getPortionText())
                .calories(item.getCalories())
                .proteinGrams(item.getProteinGrams())
                .carbsGrams(item.getCarbsGrams())
                .fatGrams(item.getFatGrams())
                .sortOrder(item.getSortOrder())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private AiFeedbackResponse toFeedbackResponse(
            AiFeedback feedback
    ) {
        return AiFeedbackResponse.builder()
                .id(feedback.getId())
                .aiSuggestionId(
                        feedback.getAiSuggestion().getId()
                )
                .memberId(feedback.getMember().getId())
                .memberName(
                        resolveMemberName(feedback.getMember())
                )
                .rating(feedback.getRating())
                .useful(feedback.getUseful())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }

    private Member getCurrentMember() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                authentication.getPrincipal()
        )) {
            throw new AppException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        String principal = authentication.getName();

        User user = userRepository
                .findByUsernameOrEmail(
                        principal,
                        principal
                )
                .orElseThrow(() -> new AppException(
                        ErrorCode.USER_NOT_FOUND
                ));

        return memberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.MEMBER_NOT_FOUND
                ));
    }

    private void markSuggestionFailed(
            AiSuggestion suggestion
    ) {
        suggestion.markFailed(
                "AI_REQUEST_FAILED",
                "Không thể xử lý yêu cầu AI vào lúc này."
        );
        aiSuggestionRepository.save(suggestion);
    }

    private String resolveMemberGoal(Member member) {
        return member.getFitnessGoal() == null
                ? FitnessGoal.IMPROVE_HEALTH.name()
                : member.getFitnessGoal().name();
    }

    private String resolveMemberName(Member member) {
        return member.getUser() == null
                ? null
                : member.getUser().getFullName();
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }

        return Period.between(
                dateOfBirth,
                LocalDate.now(FITLIFE_ZONE_ID)
        ).getYears();
    }

    private String resolveLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "vi";
        }

        return language.trim().toLowerCase();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String joinWarnings(List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            return null;
        }

        return warnings.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .reduce(
                        (first, second) ->
                                first + " " + second
                )
                .orElse(null);
    }

    private String mergeWarnings(
            String first,
            String second
    ) {
        String normalizedFirst = normalizeText(first);
        String normalizedSecond = normalizeText(second);

        if (normalizedFirst == null) {
            return normalizedSecond;
        }

        if (normalizedSecond == null) {
            return normalizedFirst;
        }

        return normalizedFirst + " " + normalizedSecond;
    }

    private String buildInitialWarningMessage(
            Member member,
            BodyMetric latestBodyMetric
    ) {
        StringBuilder warning = new StringBuilder();

        if (latestBodyMetric == null) {
            warning.append(
                    "Member chưa có Body Metric mới nhất. "
            );
            warning.append(
                    "Kết quả AI chỉ mang tính tham khảo."
            );
        }

        if (member.getHealthNote() != null
                && !member.getHealthNote().isBlank()) {
            if (!warning.isEmpty()) {
                warning.append(" ");
            }

            warning.append(
                    "Member có ghi chú sức khỏe, "
            );
            warning.append(
                    "nên hỏi huấn luyện viên hoặc bác sĩ "
            );
            warning.append(
                    "trước khi áp dụng."
            );
        }

        return normalizeText(warning.toString());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    private Map<String, Object> jsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<>() {
                    }
            );
        } catch (Exception exception) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }
}
