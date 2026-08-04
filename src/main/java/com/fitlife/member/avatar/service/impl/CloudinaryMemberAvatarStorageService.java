package com.fitlife.member.avatar.service.impl;

import com.fitlife.common.file.dto.FileUploadResponse;
import com.fitlife.common.file.service.CloudinaryService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.avatar.service.MemberAvatarStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CloudinaryMemberAvatarStorageService
        implements MemberAvatarStorageService {

    private static final String AVATAR_FOLDER =
            "member-avatars";

    private final CloudinaryService cloudinaryService;

    @Override
    public String uploadMemberAvatar(
            Long userId,
            MultipartFile file
    ) {
        if (userId == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        /*
         * Public ID cố định theo user.
         * Upload lần sau sẽ ghi đè avatar cũ.
         *
         * CloudinaryService sẽ ghép thành:
         * fitlife/member-avatars/member-4
         */
        String publicId =
                "member-" + userId;

        FileUploadResponse response =
                cloudinaryService.uploadImage(
                        file,
                        AVATAR_FOLDER,
                        publicId
                );

        if (
                response == null
                        || response.getUrl() == null
                        || response.getUrl().isBlank()
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return response.getUrl();
    }
}