package com.fitlife.nutrition.mapper;

import com.fitlife.nutrition.dto.request.NutritionPlanItemRequest;
import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.MealDto;
import com.fitlife.nutrition.dto.response.NutritionPlanItemDto;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.entity.NutritionPlan;
import com.fitlife.nutrition.entity.NutritionPlanItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface NutritionPlanMapper {

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberName", source = "member.user.fullName")
    @Mapping(target = "aiSuggestionId", source = "aiSuggestion.id")
    @Mapping(target = "replacementPlanId", source = "replacementPlan.id")
    @Mapping(target = "meals", source = "items", qualifiedByName = "mapItemsToMeals")
    NutritionPlanResponse toResponse(NutritionPlan entity);

    NutritionPlanItemDto toItemDto(NutritionPlanItem entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "aiSuggestion", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "replacementPlan", ignore = true)
    @Mapping(target = "modifiedFromAi", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "items", ignore = true)
    NutritionPlan toEntity(NutritionPlanRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nutritionPlan", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NutritionPlanItem toItemEntity(NutritionPlanItemRequest request);

    @Named("mapItemsToMeals")
    default List<MealDto> mapItemsToMeals(List<NutritionPlanItem> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        List<NutritionPlanItem> orderedItems = items.stream()
                .sorted(Comparator
                        .comparing(
                                NutritionPlanItem::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        )
                        .thenComparing(
                                NutritionPlanItem::getId,
                                Comparator.nullsLast(Long::compareTo)
                        ))
                .toList();

        Map<String, List<NutritionPlanItemDto>> grouped = new LinkedHashMap<>();

        for (NutritionPlanItem item : orderedItems) {
            String mealName = item.getMealName() == null || item.getMealName().isBlank()
                    ? "Bữa ăn"
                    : item.getMealName().trim();

            grouped.computeIfAbsent(mealName, ignored -> new ArrayList<>())
                    .add(toItemDto(item));
        }

        return grouped.entrySet()
                .stream()
                .map(entry -> MealDto.builder()
                        .mealName(entry.getKey())
                        .foods(entry.getValue())
                        .build())
                .toList();
    }
}
