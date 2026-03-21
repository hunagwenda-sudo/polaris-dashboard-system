package com.saleshub.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.entity.SysPermission;
import com.saleshub.entity.SysRolePermission;
import com.saleshub.mapper.SysPermissionMapper;
import com.saleshub.mapper.SysRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限缓存服务：从 Redis 获取角色权限，缓存未命中时从 DB 加载
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String CACHE_PREFIX = "perm:role:";
    private static final long CACHE_TTL_MINUTES = 30;

    /**
     * 获取角色拥有的权限 code 集合
     */
    @SuppressWarnings("unchecked")
    public Set<String> getPermissions(String roleCode) {
        String key = CACHE_PREFIX + roleCode;
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null && !cached.isEmpty()) {
                return Set.of(cached.split(","));
            }
        } catch (Exception e) {
            log.warn("Redis read failed, fallback to DB: {}", e.getMessage());
        }

        // 从 DB 加载
        Set<String> perms = loadFromDb(roleCode);

        // 写入缓存（逗号分隔的纯字符串）
        try {
            stringRedisTemplate.opsForValue().set(key, String.join(",", perms), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis write failed: {}", e.getMessage());
        }
        return perms;
    }

    /**
     * 检查角色是否拥有指定权限
     */
    public boolean hasPermission(String roleCode, String permissionCode) {
        // admin 拥有所有权限
        if ("admin".equals(roleCode)) return true;
        return getPermissions(roleCode).contains(permissionCode);
    }

    /**
     * 清除指定角色的权限缓存（权限变更时调用）
     */
    public void evictCache(String roleCode) {
        try {
            stringRedisTemplate.delete(CACHE_PREFIX + roleCode);
        } catch (Exception e) {
            log.warn("Redis evict failed: {}", e.getMessage());
        }
    }

    /**
     * 清除所有角色权限缓存
     */
    public void evictAllCache() {
        try {
            var keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) stringRedisTemplate.delete(keys);
        } catch (Exception e) {
            log.warn("Redis evict all failed: {}", e.getMessage());
        }
    }

    private Set<String> loadFromDb(String roleCode) {
        List<SysRolePermission> rps = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleCode, roleCode)
        );
        if (rps.isEmpty()) return Collections.emptySet();

        List<Long> permIds = rps.stream().map(SysRolePermission::getPermissionId).toList();
        List<SysPermission> perms = permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, permIds)
        );
        return perms.stream().map(SysPermission::getCode).collect(Collectors.toSet());
    }
}
