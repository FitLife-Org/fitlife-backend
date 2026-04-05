package com.fitlife.core.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
     * @param file       File ảnh từ Client
     * @param folderName Tên thư mục con (Vd: "avatars", "packages")
     * @param publicId   Tên file cố định (Vd: "member_1") để ghi đè ảnh cũ
     */
    @Override
    public String uploadImage(MultipartFile file, String folderName, String publicId) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "fitlife/" + folderName, // Tổ chức thư mục
                            "public_id", publicId,             // Định danh file
                            "overwrite", true,                 // Ghi đè nếu đã tồn tại
                            "resource_type", "image"
                    ));
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tải ảnh lên đám mây: " + e.getMessage());
        }
    }

    /**
     * Xóa ảnh trên Cloudinary
     * @param publicId Full Path của ảnh (Vd: "fitlife/avatars/member_1")
     */
    @Override
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("Đã xóa ảnh cũ trên Cloudinary: " + publicId);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xóa ảnh trên đám mây: " + e.getMessage());
        }
    }
}