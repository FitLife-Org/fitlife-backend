package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.*;
import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFeedbackRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.response.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiSuggestionServiceImpl implements AiSuggestionService {

    private static final int DAILY_AI_LIMIT = 50;

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
    public AiSuggestionResponse createFullPlan(AiFullPlanRequest request) {
        Member currentMember = getCurrentMember();
        User currentUser = currentMember.getUser();

        checkDailyLimit(currentMember.getId());

        Optional<BodyMetric> latestBodyMetricOptional = bodyMetricRepository
                .findTopByMemberIdOrderByRecordedAtDesc(currentMember.getId());

        BodyMetric latestBodyMetric = latestBodyMetricOptional.orElse(null);

        AiInputSnapshot inputSnapshot = buildInputSnapshot(
                currentMember,
                currentUser,
                latestBodyMetric,
                request
        );

        String inputSnapshotJson = toJson(inputSnapshot);

        AiSuggestion aiSuggestion = AiSuggestion.builder()
                .member(currentMember)
                .latestBodyMetric(latestBodyMetric)
                .suggestionType(AiSuggestionType.FULL_PLAN)
                .goal(request.getGoal().name())
                .experienceLevel(request.getExperienceLevel())
                .activityLevel(request.getActivityLevel())
                .workoutDaysPerWeek(request.getWorkoutDaysPerWeek())
                .workoutDurationMinutes(request.getWorkoutDurationMinutes())
                .userNote(request.getUserNote())
                .inputSnapshot(inputSnapshotJson)
                .status(AiSuggestionStatus.PENDING)
                .warningMessage(buildInitialWarningMessage(currentMember, latestBodyMetric))
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .deleted(false)
                .build();

        AiSuggestion savedSuggestion = aiSuggestionRepository.save(aiSuggestion);

        try {
            String prompt = aiPromptBuilderService.buildFullPlanPrompt(inputSnapshot);
            String rawAiResponse = aiProviderService.generate(prompt);

            AiGeneratedPlanResponse planResponse = aiPlanParserService.parseGeneratedPlan(rawAiResponse);

            savedSuggestion.setAiResponse(toJson(planResponse));
            savedSuggestion.setSummary(planResponse.getSummary());
            savedSuggestion.setStatus(AiSuggestionStatus.SUCCESS);
            savedSuggestion.setErrorMessage(null);

            String warningMessage = buildWarningMessage(savedSuggestion.getWarningMessage(), planResponse);
            savedSuggestion.setWarningMessage(warningMessage);

            AiSuggestion updatedSuggestion = aiSuggestionRepository.save(savedSuggestion);

            aiPlanParserService.savePlanItems(updatedSuggestion, planResponse);

            return toSuggestionResponse(updatedSuggestion);
        } catch (Exception exception) {
            savedSuggestion.setStatus(AiSuggestionStatus.FAILED);
            savedSuggestion.setErrorMessage(exception.getMessage());
            aiSuggestionRepository.save(savedSuggestion);

            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiSuggestionResponse> getMySuggestions(Pageable pageable) {
        Member currentMember = getCurrentMember();

        Page<AiSuggestion> page = aiSuggestionRepository
                .findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(
                        currentMember.getId(),
                        pageable
                );

        return PageResponse.<AiSuggestionResponse>builder()
                .content(page.getContent()
                        .stream()
                        .map(this::toSuggestionResponse)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AiSuggestionDetailResponse getMySuggestionDetail(Long id) {
        Member currentMember = getCurrentMember();

        AiSuggestion aiSuggestion = aiSuggestionRepository
                .findByIdAndMemberIdAndDeletedFalse(id, currentMember.getId())
                .orElseThrow(() -> new AppException(ErrorCode.AI_SUGGESTION_NOT_FOUND));

        List<AiPlanItem> items = aiPlanItemRepository
                .findByAiSuggestionIdOrderBySortOrderAsc(aiSuggestion.getId());

        Optional<AiFeedback> feedbackOptional = aiFeedbackRepository
                .findByAiSuggestionIdAndMemberId(aiSuggestion.getId(), currentMember.getId());

        return toSuggestionDetailResponse(aiSuggestion, items, feedbackOptional.orElse(null));
    }

    @Override
    public AiFeedbackResponse submitFeedback(Long aiSuggestionId, AiFeedbackRequest request) {
        Member currentMember = getCurrentMember();

        AiSuggestion aiSuggestion = aiSuggestionRepository
                .findByIdAndMemberIdAndDeletedFalse(aiSuggestionId, currentMember.getId())
                .orElseThrow(() -> new AppException(ErrorCode.AI_SUGGESTION_NOT_FOUND));

        if (aiFeedbackRepository.existsByAiSuggestionIdAndMemberId(
                aiSuggestion.getId(),
                currentMember.getId()
        )) {
            throw new AppException(ErrorCode.AI_FEEDBACK_ALREADY_EXISTS);
        }

        AiFeedback feedback = AiFeedback.builder()
                .aiSuggestion(aiSuggestion)
                .member(currentMember)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        AiFeedback savedFeedback = aiFeedbackRepository.save(feedback);

        return toFeedbackResponse(savedFeedback);
    }

    private void checkDailyLimit(Long memberId) {
        LocalDate today = LocalDate.now();

        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();

        long count = aiSuggestionRepository.countByMemberIdAndCreatedAtBetweenAndDeletedFalse(
                memberId,
                from,
                to
        );

        if (count >= DAILY_AI_LIMIT) {
            throw new AppException(ErrorCode.AI_LIMIT_EXCEEDED);
        }
    }

    private AiInputSnapshot buildInputSnapshot(
            Member member,
            User user,
            BodyMetric latestBodyMetric,
            AiFullPlanRequest request
    ) {
        return AiInputSnapshot.builder()
                .member(AiInputMemberSnapshot.builder()
                        .memberId(member.getId())
                        .memberCode(member.getMemberCode())
                        .gender(member.getGender() == null ? null : member.getGender().name())
                        .dateOfBirth(member.getDateOfBirth())
                        .age(calculateAge(member.getDateOfBirth()))
                        .joinDate(member.getJoinDate())
                        .fitnessGoal(member.getFitnessGoal() == null ? null : member.getFitnessGoal().name())
                        .healthNote(member.getHealthNote())
                        .build())
                .user(AiInputUserSnapshot.builder()
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .build())
                .latestBodyMetric(buildBodyMetricSnapshot(latestBodyMetric))
                .request(AiInputRequestSnapshot.builder()
                        .goal(request.getGoal().name())
                        .experienceLevel(request.getExperienceLevel())
                        .activityLevel(request.getActivityLevel())
                        .workoutDaysPerWeek(request.getWorkoutDaysPerWeek())
                        .workoutDurationMinutes(request.getWorkoutDurationMinutes())
                        .userNote(request.getUserNote())
                        .build())
                .build();
    }

    private AiInputBodyMetricSnapshot buildBodyMetricSnapshot(BodyMetric bodyMetric) {
        if (bodyMetric == null) {
            return null;
        }

        return AiInputBodyMetricSnapshot.builder()
                .id(bodyMetric.getId())
                .heightCm(bodyMetric.getHeightCm())
                .weightKg(bodyMetric.getWeightKg())
                .bmi(bodyMetric.getBmi())
                .bodyFatPercent(bodyMetric.getBodyFatPercent())
                .muscleMassKg(bodyMetric.getMuscleMassKg())
                .note(bodyMetric.getNote())
                .recordedAt(bodyMetric.getRecordedAt())
                .build();
    }

    private String buildInitialWarningMessage(Member member, BodyMetric latestBodyMetric) {
        StringBuilder warning = new StringBuilder();

        if (latestBodyMetric == null) {
            warning.append("Member chưa có Body Metric mới nhất. Kết quả AI chỉ mang tính tham khảo. ");
        }

        if (member.getHealthNote() != null && !member.getHealthNote().isBlank()) {
            warning.append("Member có ghi chú sức khỏe, nên hỏi PT hoặc bác sĩ trước khi áp dụng. ");
        }

        return warning.isEmpty() ? null : warning.toString().trim();
    }

    private String buildWarningMessage(String currentWarning, AiGeneratedPlanResponse planResponse) {
        StringBuilder warning = new StringBuilder();

        if (currentWarning != null && !currentWarning.isBlank()) {
            warning.append(currentWarning).append(" ");
        }

        if (planResponse.getWarnings() != null && !planResponse.getWarnings().isEmpty()) {
            warning.append(String.join(" ", planResponse.getWarnings()));
        }

        return warning.isEmpty() ? null : warning.toString().trim();
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }

        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private Member getCurrentMember() {
        String principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        System.out.println("AUTH PRINCIPAL = " + principal);

        User user = userRepository.findByUsernameOrEmail(principal, principal)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return memberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }

    private Map<String, Object> jsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }

    private AiSuggestionResponse toSuggestionResponse(AiSuggestion aiSuggestion) {
        Member member = aiSuggestion.getMember();

        return AiSuggestionResponse.builder()
                .id(aiSuggestion.getId())
                .memberId(member.getId())
                .memberCode(member.getMemberCode())
                .memberName(member.getUser() == null ? null : member.getUser().getFullName())
                .suggestionType(aiSuggestion.getSuggestionType())
                .goal(aiSuggestion.getGoal())
                .experienceLevel(aiSuggestion.getExperienceLevel())
                .activityLevel(aiSuggestion.getActivityLevel())
                .workoutDaysPerWeek(aiSuggestion.getWorkoutDaysPerWeek())
                .workoutDurationMinutes(aiSuggestion.getWorkoutDurationMinutes())
                .summary(aiSuggestion.getSummary())
                .warningMessage(aiSuggestion.getWarningMessage())
                .status(aiSuggestion.getStatus())
                .createdAt(aiSuggestion.getCreatedAt())
                .updatedAt(aiSuggestion.getUpdatedAt())
                .build();
    }

    private AiSuggestionDetailResponse toSuggestionDetailResponse(
            AiSuggestion aiSuggestion,
            List<AiPlanItem> items,
            AiFeedback feedback
    ) {
        Member member = aiSuggestion.getMember();

        return AiSuggestionDetailResponse.builder()
                .id(aiSuggestion.getId())
                .memberId(member.getId())
                .memberCode(member.getMemberCode())
                .memberName(member.getUser() == null ? null : member.getUser().getFullName())
                .latestBodyMetricId(
                        aiSuggestion.getLatestBodyMetric() == null
                                ? null
                                : aiSuggestion.getLatestBodyMetric().getId()
                )
                .suggestionType(aiSuggestion.getSuggestionType())
                .goal(aiSuggestion.getGoal())
                .experienceLevel(aiSuggestion.getExperienceLevel())
                .activityLevel(aiSuggestion.getActivityLevel())
                .workoutDaysPerWeek(aiSuggestion.getWorkoutDaysPerWeek())
                .workoutDurationMinutes(aiSuggestion.getWorkoutDurationMinutes())
                .userNote(aiSuggestion.getUserNote())
                .inputSnapshot(jsonToMap(aiSuggestion.getInputSnapshot()))
                .aiResponse(jsonToMap(aiSuggestion.getAiResponse()))
                .summary(aiSuggestion.getSummary())
                .warningMessage(aiSuggestion.getWarningMessage())
                .status(aiSuggestion.getStatus())
                .errorMessage(aiSuggestion.getErrorMessage())
                .appliedWorkoutPlanId(aiSuggestion.getAppliedWorkoutPlanId())
                .appliedNutritionPlanId(aiSuggestion.getAppliedNutritionPlanId())
                .items(items.stream().map(this::toPlanItemResponse).toList())
                .feedback(feedback == null ? null : toFeedbackResponse(feedback))
                .createdAt(aiSuggestion.getCreatedAt())
                .updatedAt(aiSuggestion.getUpdatedAt())
                .build();
    }

    private AiPlanItemResponse toPlanItemResponse(AiPlanItem item) {
        return AiPlanItemResponse.builder()
                .id(item.getId())
                .aiSuggestionId(item.getAiSuggestion().getId())
                .itemType(item.getItemType())
                .title(item.getTitle())
                .description(item.getDescription())
                .dayNo(item.getDayNo())
                .dayOfWeek(item.getDayOfWeek())
                .exerciseName(item.getExerciseName())
                .sets(item.getSets())
                .reps(item.getReps())
                .durationMinutes(item.getDurationMinutes())
                .mealName(item.getMealName())
                .calories(item.getCalories())
                .proteinGrams(item.getProteinGrams())
                .carbsGrams(item.getCarbsGrams())
                .fatGrams(item.getFatGrams())
                .sortOrder(item.getSortOrder())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private AiFeedbackResponse toFeedbackResponse(AiFeedback feedback) {
        return AiFeedbackResponse.builder()
                .id(feedback.getId())
                .aiSuggestionId(feedback.getAiSuggestion().getId())
                .memberId(feedback.getMember().getId())
                .memberName(
                        feedback.getMember().getUser() == null
                                ? null
                                : feedback.getMember().getUser().getFullName()
                )
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public AiSuggestionDetailResponse analyzeBodyMetric(AiBodyAnalysisRequest request) {
        Member currentMember = getCurrentMember();
        User currentUser = currentMember.getUser();

        checkDailyLimit(currentMember.getId());

        BodyMetric latestBodyMetric = bodyMetricRepository
                .findTopByMemberIdOrderByRecordedAtDesc(currentMember.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        AiInputSnapshot snapshot = buildBodyAnalysisInputSnapshot(
                currentMember,
                currentUser,
                latestBodyMetric,
                request
        );

        AiSuggestion aiSuggestion = AiSuggestion.builder()
                .member(currentMember)
                .latestBodyMetric(latestBodyMetric)
                .suggestionType(AiSuggestionType.BODY_ANALYSIS)
                .status(AiSuggestionStatus.PENDING)
                .goal(currentMember.getFitnessGoal() == null ? null : currentMember.getFitnessGoal().name())
                .experienceLevel(null)
                .activityLevel(null)
                .workoutDaysPerWeek(null)
                .workoutDurationMinutes(null)
                .userNote(request.getUserNote())
                .inputSnapshot(toJson(snapshot))
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .deleted(false)
                .build();

        AiSuggestion savedSuggestion = aiSuggestionRepository.save(aiSuggestion);

        try {
            String prompt = aiPromptBuilderService.buildBodyAnalysisPrompt(snapshot);

            String rawAiResponse = aiProviderService.generate(prompt);

            AiGeneratedBodyAnalysisResponse analysisResponse =
                    aiPlanParserService.parseBodyAnalysis(rawAiResponse);

            savedSuggestion.setAiResponse(toJson(analysisResponse));
            savedSuggestion.setSummary(analysisResponse.getSummary());
            savedSuggestion.setWarningMessage(buildWarningMessage(analysisResponse.getWarnings()));
            savedSuggestion.setStatus(AiSuggestionStatus.SUCCESS);
            savedSuggestion.setErrorMessage(null);

            AiSuggestion updatedSuggestion = aiSuggestionRepository.save(savedSuggestion);

            aiPlanParserService.saveBodyAnalysisItems(updatedSuggestion, analysisResponse);

            List<AiPlanItem> items = aiPlanItemRepository
                    .findByAiSuggestionIdOrderBySortOrderAsc(updatedSuggestion.getId());

            return toSuggestionDetailResponse(updatedSuggestion, items, null);
        } catch (AppException exception) {
            savedSuggestion.setStatus(AiSuggestionStatus.FAILED);
            savedSuggestion.setErrorMessage(exception.getMessage());
            aiSuggestionRepository.save(savedSuggestion);
            throw exception;
        } catch (Exception exception) {
            savedSuggestion.setStatus(AiSuggestionStatus.FAILED);
            savedSuggestion.setErrorMessage(exception.getMessage());
            aiSuggestionRepository.save(savedSuggestion);
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }

    private AiInputSnapshot buildBodyAnalysisInputSnapshot(
            Member member,
            User user,
            BodyMetric latestBodyMetric,
            AiBodyAnalysisRequest request
    ) {
        return AiInputSnapshot.builder()
                .member(AiInputMemberSnapshot.builder()
                        .memberId(member.getId())
                        .memberCode(member.getMemberCode())
                        .gender(member.getGender() == null ? null : member.getGender().name())
                        .dateOfBirth(member.getDateOfBirth())
                        .age(calculateAge(member.getDateOfBirth()))
                        .joinDate(member.getJoinDate())
                        .fitnessGoal(member.getFitnessGoal() == null ? null : member.getFitnessGoal().name())
                        .healthNote(member.getHealthNote())
                        .build())
                .user(AiInputUserSnapshot.builder()
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .build())
                .latestBodyMetric(buildBodyMetricSnapshot(latestBodyMetric))
                .request(AiInputRequestSnapshot.builder()
                        .goal(member.getFitnessGoal() == null ? null : member.getFitnessGoal().name())
                        .experienceLevel(null)
                        .activityLevel(null)
                        .workoutDaysPerWeek(null)
                        .workoutDurationMinutes(null)
                        .userNote(request.getUserNote())
                        .build())
                .build();
    }

    private String buildWarningMessage(List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            return null;
        }

        return String.join(" ", warnings).trim();
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

        return PageResponse.<AiSuggestionResponse>builder()
                .content(page.getContent()
                        .stream()
                        .map(this::toSuggestionResponse)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}