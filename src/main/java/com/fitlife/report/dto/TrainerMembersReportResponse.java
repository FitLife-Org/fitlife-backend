package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TrainerMembersReportResponse {
    private long totalAssignedMembers;
    private long activeMembersCount;
    private long workoutPlansCreatedCount;
    private long nutritionPlansCreatedCount;
    private List<TrainerMemberItem> membersList;
}
