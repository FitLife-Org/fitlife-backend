package com.fitlife.trainer.avatar.service;

import org.springframework.web.multipart.MultipartFile;

public interface TrainerAvatarStorageService {
    String uploadTrainerAvatar(Long userId, MultipartFile file);
}
