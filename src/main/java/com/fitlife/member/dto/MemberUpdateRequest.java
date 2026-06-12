package com.fitlife.member.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateRequest {
    private String fullName;
    private String phone;

    @Min(value = 30, message = "CĂ¢n náº·ng pháº£i lá»›n hÆ¡n 30kg")
    private Double weight; // CĂ¢n náº·ng (kg)

    @Min(value = 100, message = "Chiá»u cao pháº£i lá»›n hÆ¡n 100cm")
    private Double height; // Chiá»u cao (cm)

    private String fitnessGoal; // Má»¥c tiĂªu: Giáº£m cĂ¢n, TÄƒng cÆ¡...
}