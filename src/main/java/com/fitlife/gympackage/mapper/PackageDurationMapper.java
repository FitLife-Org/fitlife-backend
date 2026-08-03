package com.fitlife.gympackage.mapper;

import com.fitlife.gympackage.dto.PackageDurationCreateRequest;
import com.fitlife.gympackage.dto.PackageDurationResponse;
import com.fitlife.gympackage.dto.PackageDurationUpdateRequest;
import com.fitlife.gympackage.entity.PackageDuration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PackageDurationMapper {

    @Mapping(target = "gymPackageId", source = "gymPackage.id")
    @Mapping(target = "gymPackageName", source = "gymPackage.name")
    PackageDurationResponse toResponse(PackageDuration entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PackageDuration toEntity(PackageDurationCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(PackageDurationUpdateRequest request, @MappingTarget PackageDuration entity);
}
