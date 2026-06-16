package com.fitlife.common.file.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fitlife.common.file.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * @param file       File áº£nh tá»« Client
     * @param folderName TĂªn thÆ° má»¥c con (Vd: "avatars", "packages")
     * @param publicId   TĂªn file cá»‘ Ä‘á»‹nh (Vd: "member_1") Ä‘á»ƒ ghi Ä‘Ă¨ áº£nh cÅ©
     */
    @Override
    public String uploadImage(MultipartFile file, String folderName, String publicId) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "fitlife/" + folderName, // Tá»• chá»©c thÆ° má»¥c
                            "public_id", publicId,             // Äá»‹nh danh file
                            "overwrite", true,                 // Ghi Ä‘Ă¨ náº¿u Ä‘Ă£ tá»“n táº¡i
                            "resource_type", "image"
                    ));
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Lá»—i khi táº£i áº£nh lĂªn Ä‘Ă¡m mĂ¢y: " + e.getMessage());
        }
    }

    /**
     * XĂ³a áº£nh trĂªn Cloudinary
     * @param publicId Full Path cá»§a áº£nh (Vd: "fitlife/avatars/member_1")
     */
    @Override
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("ÄĂ£ xĂ³a áº£nh cÅ© trĂªn Cloudinary: " + publicId);
        } catch (IOException e) {
            throw new RuntimeException("Lá»—i khi xĂ³a áº£nh trĂªn Ä‘Ă¡m mĂ¢y: " + e.getMessage());
        }
    }
}