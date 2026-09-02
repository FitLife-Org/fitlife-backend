package com.fitlife.common.file.controller;

import com.fitlife.common.file.dto.FileUploadResponse;
import com.fitlife.common.file.service.CloudinaryService;
import com.fitlife.common.response.ApiResponse;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/uploads")
public class FileUploadController {

    private final CloudinaryService
            cloudinaryService;

    /**
     * Endpoint upload ảnh dùng chung.
     *
     * Ví dụ:
     * POST /uploads/images?folder=equipment
     */
    @PostMapping(
            value = "/images",
            consumes =
                    MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'STAFF', 'TRAINER', 'MEMBER')"
    )
    public ApiResponse<FileUploadResponse>
    uploadImage(
            @RequestPart("file")
            MultipartFile file,

            @RequestParam(
                    defaultValue = "general"
            )
            @Pattern(
                    regexp = "^[a-zA-Z0-9_-]+$",
                    message =
                            "Folder name is invalid"
            )
            String folder
    ) {
        String publicId =
                UUID.randomUUID()
                        .toString();

        FileUploadResponse response =
                cloudinaryService.uploadImage(
                        file,
                        folder,
                        publicId
                );

        return ApiResponse.success(
                "Upload image successfully",
                response
        );
    }
}