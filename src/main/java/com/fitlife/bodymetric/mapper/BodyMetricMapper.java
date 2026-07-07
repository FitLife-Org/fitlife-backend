package com.fitlife.bodymetric.mapper;

import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.dto.request.BodyMetricUpdateRequest;
import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.bodymetric.dto.response.BodyMetricSummaryResponse;
import com.fitlife.bodymetric.entity.BodyMetric;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BodyMetricMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "bmi", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BodyMetric toEntity(BodyMetricCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "bmi", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget BodyMetric bodyMetric, BodyMetricUpdateRequest request);

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberCode", source = "member.memberCode")
    @Mapping(target = "fullName", source = "member.user.fullName")
    @Mapping(target = "email", source = "member.user.email")
    @Mapping(target = "phone", source = "member.user.phone")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    BodyMetricResponse toResponse(BodyMetric bodyMetric);

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberCode", source = "member.memberCode")
    @Mapping(target = "fullName", source = "member.user.fullName")
    BodyMetricSummaryResponse toSummaryResponse(BodyMetric bodyMetric);
}