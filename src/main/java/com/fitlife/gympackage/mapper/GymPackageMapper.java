package com.fitlife.gympackage.mapper;

import com.fitlife.gympackage.dto.GymPackageRequest;
import com.fitlife.gympackage.dto.GymPackageResponse;
import com.fitlife.gympackage.entity.GymPackage;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GymPackageMapper {

    GymPackageResponse toResponse(GymPackage gymPackage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "packageType", ignore = true)
    @Mapping(target = "benefits", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    GymPackage toEntity(GymPackageRequest request);

    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "packageType", ignore = true)
    @Mapping(target = "benefits", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(GymPackageRequest request, @MappingTarget GymPackage gymPackage);
}

