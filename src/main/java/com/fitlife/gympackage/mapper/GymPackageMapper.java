package com.fitlife.gympackage.mapper;

import com.fitlife.gympackage.dto.GymPackageCreateRequest;
import com.fitlife.gympackage.dto.GymPackageResponse;
import com.fitlife.gympackage.dto.GymPackageUpdateRequest;
import com.fitlife.gympackage.entity.GymPackage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GymPackageMapper {

    GymPackageResponse toResponse(GymPackage entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    GymPackage toEntity(GymPackageCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(GymPackageUpdateRequest request, @MappingTarget GymPackage entity);
}
