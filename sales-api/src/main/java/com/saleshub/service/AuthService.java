package com.saleshub.service;

import com.saleshub.dto.LoginRequest;
import com.saleshub.dto.LoginResponse;
import com.saleshub.dto.ProfileUpdateRequest;
import com.saleshub.entity.SysUser;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    SysUser getCurrentUser(Long userId);
    SysUser updateProfile(Long userId, ProfileUpdateRequest request);
}
