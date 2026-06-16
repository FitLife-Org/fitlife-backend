package com.fitlife.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(name = "MemberProfileResponse", description = "ThĂ´ng tin há»“ sÆ¡ há»™i viĂªn")
public class MemberProfileResponse {
    @Schema(description = "ID há»™i viĂªn", example = "100")
    private Long id;
    @Schema(description = "ID tĂ i khoáº£n liĂªn káº¿t", example = "10")
    private Long userId;
    @Schema(description = "Há» vĂ  tĂªn", example = "Nguyen Van A")
    private String fullName;
    @Schema(description = "Sá»‘ Ä‘iá»‡n thoáº¡i", example = "0912345678")
    private String phone;
    @Schema(description = "Äá»‹a chá»‰ email", example = "member01@fitlife.local")
    private String email;
    @Schema(description = "Tráº¡ng thĂ¡i há»“ sÆ¡", example = "ACTIVE")
    private String status;
    @Schema(description = "ÄÆ°á»ng dáº«n avatar", example = "https://cdn.fitlife.local/avatar/100.jpg")
    private String avatarUrl;
    @Schema(description = "Chiá»u cao (cm)", example = "175")
    private Double height;
    @Schema(description = "CĂ¢n náº·ng (kg)", example = "70")
    private Double weight;
    @Schema(description = "Chá»‰ sá»‘ BMI", example = "22.86")
    private Double bmi;
    @Schema(description = "Má»¥c tiĂªu fitness", example = "Giáº£m cĂ¢n")
    private String fitnessGoal;
}