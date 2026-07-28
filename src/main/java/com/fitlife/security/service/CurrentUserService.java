package com.fitlife.security.service;

import com.fitlife.user.entity.User;

public interface CurrentUserService {

    User getCurrentUser();

    default Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}