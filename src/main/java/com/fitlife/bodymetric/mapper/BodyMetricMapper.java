package com.fitlife.bodymetric.mapper;

import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.dto.request.BodyMetricUpdateRequest;
import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.bodymetric.entity.BodyMetric;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BodyMetricMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "bmi", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    BodyMetric toEntity(BodyMetricCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "bmi", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget BodyMetric bodyMetric, BodyMetricUpdateRequest request);

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberCode", source = "member.memberCode")
    @Mapping(target = "memberName", source = "member.user.fullName")
    BodyMetricResponse toResponse(BodyMetric bodyMetric);
}