package com.fitlife.common.file.controller;

import com.fitlife.common.file.dto.FileUploadResponse;
import com.fitlife.common.file.service.CloudinaryService;
import com.fitlife.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/uploads")
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @Value("${cloudinary.cloud-name:demo}")
    private String cloudinaryCloudName;

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    @PostMapping
    public ApiResponse<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error(40001, "File trống");
        }

        // 1. Try uploading to Cloudinary if it is configured
        if (!"demo".equalsIgnoreCase(cloudinaryCloudName)) {
            try {
                String folderName = "equipment";
                String publicId = UUID.randomUUID().toString();
                String url = cloudinaryService.uploadImage(file, folderName, publicId);
                return ApiResponse.success("Tải ảnh lên thành công (Cloudinary)", new FileUploadResponse(url, publicId));
            } catch (Exception e) {
                log.warn("Cloudinary upload failed, falling back to local storage", e);
            }
        }

        // 2. Fallback to Local Storage
        try {
            // Create target directory if it doesn't exist
            Files.createDirectories(this.fileStorageLocation);

            // Generate clean filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // Copy file to target location
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Construct local URL dynamically
            String fileDownloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();
            
            return ApiResponse.success("Tải ảnh lên thành công (Local)", new FileUploadResponse(fileDownloadUrl, fileName));
        } catch (IOException ex) {
            log.error("Could not store file", ex);
            return ApiResponse.error(50001, "Không thể lưu file trên máy chủ: " + ex.getMessage());
        }
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                String contentType = "image/jpeg";
                try {
                    contentType = Files.probeContentType(filePath);
                } catch (IOException e) {
                    log.warn("Could not determine file type", e);
                }
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
