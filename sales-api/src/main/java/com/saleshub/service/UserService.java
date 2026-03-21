package com.saleshub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.saleshub.dto.UserCreateRequest;
import com.saleshub.dto.UserUpdateRequest;
import com.saleshub.entity.SysUser;

public interface UserService {
    IPage<SysUser> listUsers(String keyword, Long teamId, String role, int page, int size);
    SysUser createUser(UserCreateRequest request);
    SysUser updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    void toggleStatus(Long id, String status);
    void resetPassword(Long id);
}
