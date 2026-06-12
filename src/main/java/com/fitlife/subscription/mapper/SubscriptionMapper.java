package com.fitlife.subscription.mapper;

import com.fitlife.subscription.dto.SubscriptionResponse;
import com.fitlife.subscription.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "gymPackage.id", target = "packageId")
    @Mapping(source = "gymPackage.name", target = "packageName")
    SubscriptionResponse toResponse(Subscription subscription);
}

