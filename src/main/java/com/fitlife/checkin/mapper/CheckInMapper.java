package com.fitlife.checkin.mapper;

import com.fitlife.checkin.dto.CheckInResponse;
import com.fitlife.checkin.entity.CheckIn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CheckInMapper {

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberCode", source = "member.memberCode")
    @Mapping(target = "memberName", source = "member.user.fullName")
    @Mapping(target = "subscriptionId", source = "subscription.id")
    @Mapping(target = "packageName", source = "subscription.gymPackage.name")
    @Mapping(target = "checkedInBy", source = "checkedInBy.id")
    @Mapping(target = "checkedInByName", source = "checkedInBy.fullName")
    @Mapping(target = "isInside", expression = "java(checkIn.getCheckOutTime() == null && com.fitlife.checkin.enums.CheckInStatus.SUCCESS.equals(checkIn.getStatus()))")
    CheckInResponse toResponse(CheckIn checkIn);

    List<CheckInResponse> toResponseList(List<CheckIn> checkIns);
}
