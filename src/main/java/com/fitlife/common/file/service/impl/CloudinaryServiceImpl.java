package com.fitlife.common.file.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fitlife.common.file.dto.FileUploadResponse;
import com.fitlife.common.file.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl
        implements CloudinaryService {

    private static final long MAX_IMAGE_SIZE =
            10L * 1024L * 1024L;

    private static final Set<String>
            ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    private static final String ROOT_FOLDER =
            "fitlife";

    private final Cloudinary cloudinary;

    @Override
    public FileUploadResponse uploadImage(
            MultipartFile file,
            String folderName,
            String publicId
    ) {
        validateImage(file);
        validatePathSegment(
                folderName,
                "folderName"
        );
        validatePathSegment(
                publicId,
                "publicId"
        );

        String normalizedFolder =
                ROOT_FOLDER
                        + "/"
                        + folderName.trim();

        try {
            Map<?, ?> uploadResult =
                    cloudinary
                            .uploader()
                            .upload(
                                    file.getBytes(),
                                    ObjectUtils.asMap(
                                            "folder",
                                            normalizedFolder,

                                            "public_id",
                                            publicId.trim(),

                                            "overwrite",
                                            true,

                                            "invalidate",
                                            true,

                                            "unique_filename",
                                            false,

                                            "use_filename",
                                            false,

                                            "resource_type",
                                            "image",

                                            "transformation",
                                            "c_fill,g_auto,h_512,w_512,q_auto,f_auto"
                                    )
                            );

            String secureUrl =
                    getRequiredString(
                            uploadResult,
                            "secure_url"
                    );

            String uploadedPublicId =
                    getRequiredString(
                            uploadResult,
                            "public_id"
                    );

            log.info(
                    "Cloudinary image uploaded successfully. publicId={}",
                    uploadedPublicId
            );

            return FileUploadResponse
                    .builder()
                    .url(secureUrl)
                    .publicId(uploadedPublicId)
                    .build();

        } catch (IOException exception) {
            log.error(
                    "Unable to upload image to Cloudinary. folder={}, publicId={}",
                    normalizedFolder,
                    publicId,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to upload image to Cloudinary",
                    exception
            );
        }
    }

    @Override
    public void deleteImage(
            String fullPublicId
    ) {
        if (
                fullPublicId == null ||
                        fullPublicId.isBlank()
        ) {
            return;
        }

        try {
            Map<?, ?> result =
                    cloudinary
                            .uploader()
                            .destroy(
                                    fullPublicId.trim(),
                                    ObjectUtils.asMap(
                                            "resource_type",
                                            "image",
                                            "invalidate",
                                            true
                                    )
                            );

            log.info(
                    "Cloudinary image delete result. publicId={}, result={}",
                    fullPublicId,
                    result.get("result")
            );

        } catch (IOException exception) {
            log.error(
                    "Unable to delete Cloudinary image. publicId={}",
                    fullPublicId,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to delete image from Cloudinary",
                    exception
            );
        }
    }

    private void validateImage(
            MultipartFile file
    ) {
        if (
                file == null ||
                        file.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "Image file is required"
            );
        }

        if (
                file.getSize() >
                        MAX_IMAGE_SIZE
        ) {
            throw new IllegalArgumentException(
                    "Image size must not exceed 10 MB"
            );
        }

        String contentType =
                file.getContentType();

        if (
                contentType == null ||
                        !ALLOWED_CONTENT_TYPES.contains(
                                contentType.toLowerCase(
                                        Locale.ROOT
                                )
                        )
        ) {
            throw new IllegalArgumentException(
                    "Only JPG, PNG and WEBP images are supported"
            );
        }
    }

    private void validatePathSegment(
            String value,
            String fieldName
    ) {
        if (
                value == null ||
                        value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        if (
                !value.matches(
                        "^[a-zA-Z0-9_-]+$"
                )
        ) {
            throw new IllegalArgumentException(
                    fieldName
                            + " may only contain letters, numbers, underscores and hyphens"
            );
        }
    }

    private String getRequiredString(
            Map<?, ?> result,
            String key
    ) {
        Object value =
                result.get(key);

        if (
                value == null ||
                        value.toString().isBlank()
        ) {
            throw new IllegalStateException(
                    "Cloudinary response does not contain "
                            + key
            );
        }

        return value.toString();
    }
}