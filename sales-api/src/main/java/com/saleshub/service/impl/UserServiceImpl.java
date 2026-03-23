package com.saleshub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saleshub.common.BusinessException;
import com.saleshub.dto.UserCreateRequest;
import com.saleshub.dto.UserUpdateRequest;
import com.saleshub.entity.SysUser;
import com.saleshub.mapper.SysUserMapper;
import com.saleshub.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /** 角色等级，数字越大权限越高 */
    private static final Map<String, Integer> ROLE_LEVEL = Map.of(
        "admin", 3, "partner", 2, "sales", 1
    );

    @Override
    public IPage<SysUser> listUsers(String keyword, Long teamId, String role, int page, int size) {
        var wrapper = new LambdaQueryWrapper<SysUser>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getName, keyword).or().like(SysUser::getUsername, keyword));
        }
        if (teamId != null) wrapper.eq(SysUser::getTeamId, teamId);
        if (StringUtils.hasText(role)) wrapper.eq(SysUser::getRole, role);
        wrapper.select(SysUser.class, f -> !"password".equals(f.getColumn()));
        wrapper.orderByDesc(SysUser::getCreatedAt);
        return userMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public SysUser createUser(UserCreateRequest request) {
        log.info("创建用户: username={}, role={}, teamId={}", request.getUsername(), request.getRole(), request.getTeamId());
        Long exists = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (exists > 0) throw new BusinessException("用户名已存在");

        // 不允许通过接口创建 admin
        if ("admin".equals(request.getRole())) throw new BusinessException("不允许创建管理员账号");

        // 默认密码：手机号后6位，无手机号则用 123456
        String phone = request.getPhone();
        String defaultPwd = (phone != null && phone.length() >= 6)
                ? phone.substring(phone.length() - 6) : "123456";

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(defaultPwd));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setTeamId(request.getTeamId());
        user.setLevel(request.getLevel() != null ? request.getLevel() : "K1");
        user.setBirthday(request.getBirthday());
        user.setHireDate(request.getHireDate());
        user.setRequiredPlatforms(request.getRequiredPlatforms());
        user.setPasswordChanged(false);
        user.setStatus("active");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public SysUser updateUser(Long id, UserUpdateRequest request) {
        log.info("更新用户: id={}", id);
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");

        // 角色变更限制：不允许改成 admin，也不允许改 admin 的角色
        if (request.getRole() != null && !request.getRole().equals(user.getRole())) {
            if ("admin".equals(request.getRole()) || "admin".equals(user.getRole())) {
                throw new BusinessException("不允许变更管理员角色");
            }
        }

        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getTeamId() != null) user.setTeamId(request.getTeamId());
        if (request.getLevel() != null) user.setLevel(request.getLevel());
        if (request.getTargetDgmv() != null) user.setTargetDgmv(request.getTargetDgmv());
        if (request.getBirthday() != null) user.setBirthday(request.getBirthday());
        if (request.getHireDate() != null) user.setHireDate(request.getHireDate());
        if (request.getRemindEnabled() != null) user.setRemindEnabled(request.getRemindEnabled());
        if (request.getRequiredPlatforms() != null) user.setRequiredPlatforms(request.getRequiredPlatforms());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public void toggleStatus(Long id, String status) {
        log.info("切换用户状态: id={}, status={}", id, status);
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        if ("admin".equals(user.getRole())) throw new BusinessException("不允许禁用管理员账号");
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("删除用户: id={}", id);
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        if ("admin".equals(user.getRole())) throw new BusinessException("不允许删除管理员账号");
        userMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long id) {
        log.info("重置密码: userId={}", id);
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        String phone = user.getPhone();
        String defaultPwd = (phone != null && phone.length() >= 6)
                ? phone.substring(phone.length() - 6) : "123456";
        user.setPassword(passwordEncoder.encode(defaultPwd));
        user.setPasswordChanged(false);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
