package com.fitlife.common.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /**
     * URL HTTPS của ảnh trên Cloudinary.
     */
    private String url;

    /**
     * Định danh đầy đủ của ảnh trên Cloudinary.
     *
     * Ví dụ:
     * fitlife/member-avatars/member-4
     */
    private String publicId;
}