//package com.fitlife.auth.mapper;
//
//import org.mapstruct.Mapper;
//
//@Mapper(componentModel = "spring")
//public interface AuthMapper {
//
//    default LoginResponse toLoginResponse(String token, String username, String role) {
//        return LoginResponse.builder()
//                .token(token)
//                .username(username)
//                .role(role)
//                .build();
//    }
//}
//
//
