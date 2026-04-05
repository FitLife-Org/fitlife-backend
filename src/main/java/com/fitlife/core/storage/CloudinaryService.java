package com.fitlife.core.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    String uploadImage(MultipartFile file, String folderName, String publicId) throws IOException;

    void deleteImage(String publicId);
}
