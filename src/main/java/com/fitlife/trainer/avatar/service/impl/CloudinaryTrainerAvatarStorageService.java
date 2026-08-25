package com.fitlife.trainer.avatar.service.impl;

import com.fitlife.common.file.dto.FileUploadResponse;
import com.fitlife.common.file.service.CloudinaryService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.trainer.avatar.service.TrainerAvatarStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CloudinaryTrainerAvatarStorageService implements TrainerAvatarStorageService {

    private static final String AVATAR_FOLDER = "trainer-avatars";

    private final CloudinaryService cloudinaryService;

    @Override
    public String uploadTrainerAvatar(Long userId, MultipartFile file) {
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        String publicId = "trainer-" + userId;

        FileUploadResponse response = cloudinaryService.uploadImage(
                file,
                AVATAR_FOLDER,
                publicId
        );

        if (response == null || response.getUrl() == null || response.getUrl().isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return response.getUrl();
    }
}
