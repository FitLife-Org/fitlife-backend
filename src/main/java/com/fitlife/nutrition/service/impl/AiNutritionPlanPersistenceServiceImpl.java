package com.fitlife.nutrition.service.impl;

import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiPlanItemType;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.repository.AiPlanItemRepository;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.nutrition.dto.request.NutritionPlanItemRequest;
import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.enums.NutritionPlanSource;
import com.fitlife.nutrition.service.AiNutritionPlanPersistenceService;
import com.fitlife.nutrition.service.NutritionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiNutritionPlanPersistenceServiceImpl implements AiNutritionPlanPersistenceService {

    private final AiSuggestionRepository aiSuggestionRepository;
    private final AiPlanItemRepository aiPlanItemRepository;
    private final NutritionPlanService nutritionPlanService;

    @Override
    @Transactional
    public NutritionPlanResponse persistAiSuggestion(Long suggestionId, Long memberId) {
        AiSuggestion suggestion = aiSuggestionRepository.findByIdAndMemberIdAndDeletedFalse(suggestionId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("AI Suggestion not found or does not belong to this member"));

        if (suggestion.getStatus() != AiSuggestionStatus.SUCCESS) {
            throw new IllegalArgumentException("AI Suggestion is not fully completed yet");
        }

        List<AiPlanItem> aiItems = aiPlanItemRepository.findByAiSuggestionIdOrderBySortOrderAscIdAsc(suggestionId);
        
        NutritionPlanRequest request = new NutritionPlanRequest();
        request.setName("Kế hoạch AI: " + (suggestion.getGoal() != null ? suggestion.getGoal() : "Chung"));
        request.setDescription(suggestion.getSummary());
        request.setGoal(suggestion.getGoal() != null ? suggestion.getGoal() : "GENERAL");
        request.setSource(NutritionPlanSource.AI_GENERATED);
        
        // Duration from AI (default to 4 if not specified)
        request.setDurationWeeks(4);
        
        // Single-pass calculation to save CPU and heap memory allocations
        int dailyCalories = 0;
        double dailyProtein = 0.0;
        double dailyCarbs = 0.0;
        double dailyFat = 0.0;

        for (AiPlanItem i : aiItems) {
            if (i.getItemType() == AiPlanItemType.NUTRITION && Integer.valueOf(1).equals(i.getDayNo())) {
                if (i.getCalories() != null) dailyCalories += i.getCalories();
                if (i.getProteinGrams() != null) dailyProtein += i.getProteinGrams().doubleValue();
                if (i.getCarbsGrams() != null) dailyCarbs += i.getCarbsGrams().doubleValue();
                if (i.getFatGrams() != null) dailyFat += i.getFatGrams().doubleValue();
            }
        }

        request.setDailyCalories(dailyCalories > 0 ? dailyCalories : 2000); // Default to 2000 if 0
        request.setProteinGrams(BigDecimal.valueOf(dailyProtein));
        request.setCarbohydrateGrams(BigDecimal.valueOf(dailyCarbs));
        request.setFatGrams(BigDecimal.valueOf(dailyFat));
        request.setMealsPerDay(3);
        request.setWaterMlPerDay(2000);
        
        request.setAiSuggestionId(suggestionId);
        request.setWarningMessage(suggestion.getWarningMessage());

        List<NutritionPlanItemRequest> itemRequests = aiItems.stream()
                .filter(item -> item.getItemType() == AiPlanItemType.NUTRITION)
                .map(item -> {
                    NutritionPlanItemRequest req = new NutritionPlanItemRequest();
                    req.setMealName(item.getMealName() != null ? item.getMealName() : "Bữa ăn");
                    req.setFoodName(item.getTitle() != null ? item.getTitle() : "Món ăn");
                    req.setPortionText(item.getPortionText());
                    req.setCalories(item.getCalories() != null ? item.getCalories() : 0);
                    req.setProteinGrams(item.getProteinGrams() != null ? item.getProteinGrams() : BigDecimal.ZERO);
                    req.setCarbohydrateGrams(item.getCarbsGrams() != null ? item.getCarbsGrams() : BigDecimal.ZERO);
                    req.setFatGrams(item.getFatGrams() != null ? item.getFatGrams() : BigDecimal.ZERO);
                    req.setPreparation(item.getDescription()); // description holds preparation
                    return req;
                }).collect(Collectors.toList());

        request.setItems(itemRequests);

        NutritionPlanResponse response = nutritionPlanService.createNutritionPlan(memberId, request);
        
        // Update applied ID
        suggestion.setAppliedNutritionPlanId(response.getId());
        aiSuggestionRepository.save(suggestion);
        
        return response;
    }
}
