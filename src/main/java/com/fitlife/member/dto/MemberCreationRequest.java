package com.fitlife.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "MemberCreationRequest", description = "Payload táº¡o hoáº·c cáº­p nháº­t há»“ sÆ¡ há»™i viĂªn")
public class MemberCreationRequest {
    @Schema(description = "ID tĂ i khoáº£n ngÆ°á»i dĂ¹ng liĂªn káº¿t", example = "10")
    private Long userId;
    @Schema(description = "Há» vĂ  tĂªn há»™i viĂªn", example = "Nguyen Van A")
    private String fullName;
    @Schema(description = "Sá»‘ Ä‘iá»‡n thoáº¡i", example = "0912345678")
    private String phone;
    @Schema(description = "Äá»‹a chá»‰ email", example = "member01@fitlife.local")
    private String email;
}