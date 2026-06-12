package com.fitlife.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "AiWorkoutRequest", description = "Payload yĂªu cáº§u AI táº¡o lá»‹ch táº­p cĂ¡ nhĂ¢n hĂ³a")
public class AiWorkoutRequest {


    @Schema(description = "Má»¥c tiĂªu táº­p luyá»‡n", example = "TÄƒng cÆ¡ giáº£m má»¡")
    @NotBlank(message = "Má»¥c tiĂªu khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng (VD: TÄƒng cÆ¡ giáº£m má»¡)")
    private String goal;

    @Schema(description = "TrĂ¬nh Ä‘á»™ hiá»‡n táº¡i", example = "Beginner")
    @NotBlank(message = "TrĂ¬nh Ä‘á»™ khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng (VD: Beginner, Intermediate)")
    private String fitnessLevel;

    @Schema(description = "Sá»‘ buá»•i táº­p má»—i tuáº§n", example = "5")
    @Min(1)
    @Max(7)
    private int daysPerWeek;

    @Schema(description = "Cháº¥n thÆ°Æ¡ng hoáº·c háº¡n cháº¿ cáº§n lÆ°u Ă½", example = "Äau lÆ°ng dÆ°á»›i")
    private String injuries;
    @Schema(description = "Thiáº¿t bá»‹ sáºµn cĂ³ Ä‘á»ƒ táº­p", example = "Dumbbell, Barbell")
    private String equipment;
    @Schema(description = "Sá»Ÿ thĂ­ch dinh dÆ°á»¡ng", example = "High protein")
    private String dietPreference;
}