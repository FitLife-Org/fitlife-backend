package com.fitlife.workout.service;

import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.member.entity.Member;
import com.fitlife.workout.entity.WorkoutPlan;

import java.util.List;

public interface AiWorkoutPlanCreationService {

    WorkoutPlan createFromAiSuggestion(
            AiSuggestion suggestion,
            Member member,
            List<AiPlanItem> items
    );
}