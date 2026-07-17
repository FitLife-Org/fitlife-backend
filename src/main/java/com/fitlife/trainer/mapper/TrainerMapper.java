package com.fitlife.trainer.mapper;

import com.fitlife.trainer.dto.request.TrainerCreateRequest;
import com.fitlife.trainer.dto.response.TrainerResponse;
import com.fitlife.trainer.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Trainer toEntity(TrainerCreateRequest request);

    @Mapping(target = "userId", source = "trainer.user.id")
    @Mapping(target = "username", source = "trainer.user.username")
    @Mapping(target = "fullName", source = "trainer.user.fullName")
    @Mapping(target = "email", source = "trainer.user.email")
    @Mapping(target = "phone", source = "trainer.user.phone")
    TrainerResponse toResponse(Trainer trainer);
    TrainerResponse toTrainerResponse(Trainer trainer);


}