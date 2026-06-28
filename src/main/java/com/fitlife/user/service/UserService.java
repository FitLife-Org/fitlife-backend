package com.fitlife.user.service;

import com.fitlife.user.dto.request.*;
import com.fitlife.user.dto.response.AdminUserDetailResponse;
import com.fitlife.user.dto.response.AdminUserResponse;
import com.fitlife.user.dto.response.PageResponse;

public interface UserService {

    AdminUserResponse getCurrentUser();

    PageResponse<AdminUserResponse> getAdminUsers(AdminUserSearchRequest request);

    AdminUserDetailResponse getAdminUserDetail(Long id);

    AdminUserDetailResponse createInternalUser(AdminCreateInternalUserRequest request);

    AdminUserDetailResponse updateUser(Long id, AdminUpdateUserRequest request);

    AdminUserDetailResponse updateUserStatus(Long id, AdminUpdateUserStatusRequest request);

    AdminUserDetailResponse updateUserRoles(Long id, AdminUpdateUserRolesRequest request);
}