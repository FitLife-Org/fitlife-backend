package com.fitlife.nutrition.mapper;

import com.fitlife.nutrition.dto.response.MealDto;
import com.fitlife.nutrition.dto.response.NutritionPlanItemDto;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.entity.NutritionPlan;
import com.fitlife.nutrition.entity.NutritionPlanItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface NutritionPlanMapper {

    @Mapping(target = "meals", source = "items", qualifiedByName = "mapItemsToMeals")
    NutritionPlanResponse toResponse(NutritionPlan entity);

    NutritionPlanItemDto toItemDto(NutritionPlanItem entity);

    @Named("mapItemsToMeals")
    default List<MealDto> mapItemsToMeals(List<NutritionPlanItem> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        // Tận dụng Java Stream (Map) để gom nhóm (groupingBy) theo mealName
        Map<String, List<NutritionPlanItem>> groupedByMeal = items.stream()
                .collect(Collectors.groupingBy(NutritionPlanItem::getMealName));

        List<MealDto> mealDtos = new ArrayList<>();
        for (Map.Entry<String, List<NutritionPlanItem>> entry : groupedByMeal.entrySet()) {
            List<NutritionPlanItemDto> dtos = entry.getValue().stream()
                    .map(this::toItemDto)
                    .collect(Collectors.toList());
            
            mealDtos.add(MealDto.builder()
                    .mealName(entry.getKey())
                    .foods(dtos)
                    .build());
        }

        return mealDtos;
    }
}
