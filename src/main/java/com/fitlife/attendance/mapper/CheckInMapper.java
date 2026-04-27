package com.fitlife.attendance.mapper;

import com.fitlife.attendance.dto.CheckInResponse;
import com.fitlife.member.entity.Member;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface CheckInMapper {

    default CheckInResponse toResponse(Member member, LocalDateTime checkInTime, String status, String message) {
        return CheckInResponse.builder()
                .memberId(member.getId())
                .memberName(member.getFullName())
                .checkInTime(checkInTime)
                .status(status)
                .message(message)
                .build();
    }
}


