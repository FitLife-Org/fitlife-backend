package com.fitlife.auth.service;

import com.fitlife.auth.dto.UserCreationRequest;
import com.fitlife.auth.dto.UserResponse;

public interface UserService {
    UserResponse createUser(UserCreationRequest request);
}
