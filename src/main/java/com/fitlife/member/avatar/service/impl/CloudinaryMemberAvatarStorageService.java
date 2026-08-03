package com.fitlife.member.avatar.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.avatar.service.MemberAvatarStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryMemberAvatarStorageService
        implements MemberAvatarStorageService {

    private static final long MAX_AVATAR_SIZE =
            5L * 1024L * 1024L;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    private static final String AVATAR_FOLDER =
            "fitlife/member-avatars";

    private final Cloudinary cloudinary;

    @Override
    public String uploadMemberAvatar(
            Long userId,
            MultipartFile file
    ) {
        validateFile(file);

        String publicId = buildPublicId(userId);

        try {
            Map<?, ?> result = cloudinary
                    .uploader()
                    .upload(
                            file.getBytes(),
                            ObjectUtils.asMap(
                                    "public_id", publicId,
                                    "resource_type", "image",
                                    "overwrite", true,
                                    "invalidate", true,
                                    "unique_filename", false,
                                    "use_filename", false,
                                    "transformation", "c_fill,g_face,h_512,w_512,q_auto,f_auto"
                            )
                    );

            Object secureUrl = result.get("secure_url");

            if (secureUrl == null || secureUrl.toString().isBlank()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            return secureUrl.toString();
        } catch (IOException exception) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateFile(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        String contentType = file.getContentType();

        if (
                contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(
                        contentType.toLowerCase(Locale.ROOT)
                )
        ) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String buildPublicId(
            Long userId
    ) {
        String userSegment = userId == null
                ? "unknown"
                : userId.toString();

        return AVATAR_FOLDER
                + "/user-"
                + userSegment
                + "-"
                + UUID.randomUUID();
    }
}
