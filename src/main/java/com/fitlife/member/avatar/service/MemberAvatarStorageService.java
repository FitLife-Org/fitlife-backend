package com.fitlife.member.avatar.service;

import org.springframework.web.multipart.MultipartFile;

public interface MemberAvatarStorageService {

    String uploadMemberAvatar(
            Long userId,
            MultipartFile file
    );
}
