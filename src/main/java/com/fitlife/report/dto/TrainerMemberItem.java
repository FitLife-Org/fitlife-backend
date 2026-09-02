package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TrainerMemberItem {
    private Long memberId;
    private String memberName;
    private String memberCode;
    private String email;
    private String phone;
    private String activePackageName;
}
