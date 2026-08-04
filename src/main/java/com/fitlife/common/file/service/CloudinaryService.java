package com.fitlife.common.file.service;

import com.fitlife.common.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    FileUploadResponse uploadImage(
            MultipartFile file,
            String folderName,
            String publicId
    );

    void deleteImage(String fullPublicId);
}