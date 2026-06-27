package com.fitlife.user.service;

import com.fitlife.user.dto.request.AdminUserSearchRequest;
import com.fitlife.user.dto.response.AdminUserDetailResponse;
import com.fitlife.user.dto.response.AdminUserResponse;
import com.fitlife.user.dto.response.PageResponse;

public interface UserService {

    AdminUserResponse getCurrentUser();

    PageResponse<AdminUserResponse> getAdminUsers(AdminUserSearchRequest request);

    AdminUserDetailResponse getAdminUserDetail(Long id);
}