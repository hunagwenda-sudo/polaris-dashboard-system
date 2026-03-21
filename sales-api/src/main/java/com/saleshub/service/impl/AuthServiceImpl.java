package com.saleshub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.common.BusinessException;
import com.saleshub.dto.LoginRequest;
import com.saleshub.dto.LoginResponse;
import com.saleshub.dto.ProfileUpdateRequest;
import com.saleshub.entity.SysUser;
import com.saleshub.mapper.SysUserMapper;
import com.saleshub.security.JwtUtil;
import com.saleshub.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if ("inactive".equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(),
                user.getName(), user.getRole(), user.getLevel(), user.getTeamId(), user.getGroupId(), user.getAvatar(),
                user.getPasswordChanged() != null ? user.getPasswordChanged() : true);
    }

    @Override
    public SysUser getCurrentUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(null);
        return user;
    }

    @Override
    public SysUser updateProfile(Long userId, ProfileUpdateRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());

        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            // 首次登录强制修改：跳过旧密码校验
            if (Boolean.TRUE.equals(request.getForceChange()) && Boolean.FALSE.equals(user.getPasswordChanged())) {
                // OK, skip old password check
            } else if (request.getOldPassword() == null || !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new BusinessException("原密码错误");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setPasswordChanged(true);
        }

        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }
}
