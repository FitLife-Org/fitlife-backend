package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MemberSummaryResponse {
    private long totalMembers;
    private long activeMembers;
    private long inactiveMembers;
    private long newMembersThisMonth;
    private List<GenderDistributionItem> genderDistribution;
    private List<AgeGroupDistributionItem> ageGroupDistribution;
}
