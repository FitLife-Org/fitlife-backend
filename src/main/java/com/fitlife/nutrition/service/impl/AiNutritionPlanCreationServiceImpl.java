package com.fitlife.nutrition.service.impl;

import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiPlanItemType;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.nutrition.entity.NutritionPlan;
import com.fitlife.nutrition.entity.NutritionPlanItem;
import com.fitlife.nutrition.enums.NutritionPlanSource;
import com.fitlife.nutrition.enums.NutritionPlanStatus;
import com.fitlife.nutrition.repository.NutritionPlanRepository;
import com.fitlife.nutrition.service.AiNutritionPlanCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiNutritionPlanCreationServiceImpl
        implements AiNutritionPlanCreationService {

    private static final int DEFAULT_DURATION_WEEKS = 4;
    private static final int DEFAULT_WATER_ML = 2000;

    private final NutritionPlanRepository
            nutritionPlanRepository;

    @Override
    @Transactional
    public NutritionPlan createFromAiSuggestion(
            AiSuggestion suggestion,
            Member member,
            List<AiPlanItem> items
    ) {
        validateInput(
                suggestion,
                member,
                items
        );

        if (nutritionPlanRepository
                .existsByAiSuggestionId(
                        suggestion.getId()
                )) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_ALREADY_APPLIED
            );
        }

        List<AiPlanItem> mealItems =
                items.stream()
                        .filter(item ->
                                item != null
                                        && item.getItemType()
                                        == AiPlanItemType.MEAL
                        )
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                this::resolveDayNo
                                        )
                                        .thenComparingInt(
                                                this::resolveSortOrder
                                        )
                                        .thenComparing(
                                                item ->
                                                        item.getId() == null
                                                                ? Long.MAX_VALUE
                                                                : item.getId()
                                        )
                        )
                        .toList();

        if (mealItems.isEmpty()) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_ITEMS_NOT_FOUND
            );
        }

        AiPlanItem nutritionSummary =
                items.stream()
                        .filter(item ->
                                item != null
                                        && item.getItemType()
                                        == AiPlanItemType.NUTRITION
                        )
                        .findFirst()
                        .orElse(null);

        NutritionPlan plan =
                buildPlan(
                        suggestion,
                        member,
                        nutritionSummary,
                        mealItems,
                        items
                );

        return nutritionPlanRepository
                .saveAndFlush(plan);
    }

    private NutritionPlan buildPlan(
            AiSuggestion suggestion,
            Member member,
            AiPlanItem nutritionSummary,
            List<AiPlanItem> mealItems,
            List<AiPlanItem> allItems
    ) {
        NutritionPlan plan =
                NutritionPlan.builder()
                        .member(member)
                        .aiSuggestion(suggestion)
                        .name(resolvePlanName(suggestion))
                        .description(
                                normalizeText(
                                        suggestion.getSummary()
                                )
                        )
                        .goal(resolveGoal(suggestion))
                        .source(
                                NutritionPlanSource.AI_GENERATED
                        )
                        .status(
                                NutritionPlanStatus.DRAFT
                        )
                        .durationWeeks(
                                DEFAULT_DURATION_WEEKS
                        )
                        .dailyCalories(
                                resolveCalories(
                                        nutritionSummary,
                                        mealItems
                                )
                        )
                        .proteinGrams(
                                resolveMacro(
                                        nutritionSummary,
                                        mealItems,
                                        MacroType.PROTEIN
                                )
                        )
                        .carbohydrateGrams(
                                resolveMacro(
                                        nutritionSummary,
                                        mealItems,
                                        MacroType.CARBS
                                )
                        )
                        .fatGrams(
                                resolveMacro(
                                        nutritionSummary,
                                        mealItems,
                                        MacroType.FAT
                                )
                        )
                        .mealsPerDay(
                                resolveMealsPerDay(
                                        mealItems
                                )
                        )
                        .waterMlPerDay(
                                DEFAULT_WATER_ML
                        )
                        .warningMessage(
                                resolveWarnings(
                                        suggestion,
                                        allItems
                                )
                        )
                        .modifiedFromAi(false)
                        .isDeleted(false)
                        .createdBy(resolveUserId(member))
                        .updatedBy(resolveUserId(member))
                        .items(new ArrayList<>())
                        .build();

        mealItems.stream()
                .map(this::buildPlanItem)
                .forEach(plan::addItem);

        return plan;
    }

    private NutritionPlanItem buildPlanItem(
            AiPlanItem item
    ) {
        return NutritionPlanItem.builder()
                .mealName(
                        firstNonBlank(
                                item.getMealName(),
                                "Bữa ăn"
                        )
                )
                .foodName(
                        firstNonBlank(
                                item.getTitle(),
                                item.getDescription(),
                                "Món ăn"
                        )
                )
                .portionText(
                        normalizeText(
                                item.getPortionText()
                        )
                )
                .calories(item.getCalories())
                .proteinGrams(
                        defaultZero(
                                item.getProteinGrams()
                        )
                )
                .carbohydrateGrams(
                        defaultZero(
                                item.getCarbsGrams()
                        )
                )
                .fatGrams(
                        defaultZero(
                                item.getFatGrams()
                        )
                )
                .preparation(
                        normalizeText(
                                item.getDescription()
                        )
                )
                .note(
                        item.getDayNo() == null
                                ? null
                                : "Ngày " + item.getDayNo()
                )
                .sortOrder(resolveSortOrder(item))
                .build();
    }

    private Integer resolveCalories(
            AiPlanItem summary,
            List<AiPlanItem> mealItems
    ) {
        if (summary != null
                && summary.getCalories() != null
                && summary.getCalories() > 0) {
            return summary.getCalories();
        }

        int calories =
                mealItems.stream()
                        .map(AiPlanItem::getCalories)
                        .filter(value -> value != null)
                        .mapToInt(Integer::intValue)
                        .sum();

        return calories > 0
                ? calories
                : null;
    }

    private BigDecimal resolveMacro(
            AiPlanItem summary,
            List<AiPlanItem> mealItems,
            MacroType macroType
    ) {
        BigDecimal summaryValue =
                readMacro(
                        summary,
                        macroType
                );

        if (summaryValue != null
                && summaryValue.signum() >= 0) {
            return summaryValue;
        }

        return mealItems.stream()
                .map(item ->
                        readMacro(
                                item,
                                macroType
                        )
                )
                .filter(value -> value != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private BigDecimal readMacro(
            AiPlanItem item,
            MacroType macroType
    ) {
        if (item == null) {
            return null;
        }

        return switch (macroType) {
            case PROTEIN ->
                    item.getProteinGrams();

            case CARBS ->
                    item.getCarbsGrams();

            case FAT ->
                    item.getFatGrams();
        };
    }

    private int resolveMealsPerDay(
            List<AiPlanItem> mealItems
    ) {
        long count =
                mealItems.stream()
                        .map(AiPlanItem::getMealName)
                        .map(this::normalizeText)
                        .filter(value -> value != null)
                        .distinct()
                        .count();

        if (count <= 0) {
            return 1;
        }

        return Math.toIntExact(
                Math.min(count, 10L)
        );
    }

    private String resolveWarnings(
            AiSuggestion suggestion,
            List<AiPlanItem> items
    ) {
        List<String> warnings =
                new ArrayList<>();

        String suggestionWarning =
                normalizeText(
                        suggestion.getWarningMessage()
                );

        if (suggestionWarning != null) {
            warnings.add(suggestionWarning);
        }

        items.stream()
                .filter(item ->
                        item != null
                                && item.getItemType()
                                == AiPlanItemType.WARNING
                )
                .map(item ->
                        firstNonBlank(
                                item.getDescription(),
                                item.getTitle()
                        )
                )
                .filter(value -> value != null)
                .forEach(warnings::add);

        return warnings.isEmpty()
                ? null
                : String.join(
                System.lineSeparator(),
                warnings
        );
    }

    private String resolvePlanName(
            AiSuggestion suggestion
    ) {
        String summary =
                normalizeText(
                        suggestion.getSummary()
                );

        if (summary != null
                && summary.length() <= 150) {
            return summary;
        }

        return "Kế hoạch dinh dưỡng AI - "
                + resolveGoal(suggestion);
    }

    private String resolveGoal(
            AiSuggestion suggestion
    ) {
        String goal =
                normalizeText(
                        suggestion.getGoal()
                );

        return goal == null
                ? "GENERAL"
                : goal;
    }

    private Long resolveUserId(
            Member member
    ) {
        if (member.getUser() == null) {
            return null;
        }

        return member.getUser().getId();
    }

    private int resolveDayNo(
            AiPlanItem item
    ) {
        Integer dayNo = item.getDayNo();

        if (dayNo == null || dayNo < 1) {
            return 1;
        }

        return dayNo;
    }

    private int resolveSortOrder(
            AiPlanItem item
    ) {
        return item.getSortOrder() == null
                ? 0
                : item.getSortOrder();
    }

    private BigDecimal defaultZero(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String firstNonBlank(
            String... values
    ) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            String normalized =
                    normalizeText(value);

            if (normalized != null) {
                return normalized;
            }
        }

        return null;
    }

    private String normalizeText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private void validateInput(
            AiSuggestion suggestion,
            Member member,
            List<AiPlanItem> items
    ) {
        if (suggestion == null
                || suggestion.getId() == null
                || member == null
                || member.getId() == null
                || items == null
                || items.isEmpty()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private enum MacroType {
        PROTEIN,
        CARBS,
        FAT
    }
}