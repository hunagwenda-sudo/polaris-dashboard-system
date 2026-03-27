package com.saleshub.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.saleshub.common.Result;
import com.saleshub.dto.UserCreateRequest;
import com.saleshub.dto.UserUpdateRequest;
import com.saleshub.entity.SysUser;
import com.saleshub.security.JwtUserDetails;
import com.saleshub.service.AuditService;
import com.saleshub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuditService auditService;
    private final com.saleshub.service.UserPlatformService userPlatformService;
    private final com.saleshub.mapper.SysUserShopMapper userShopMapper;
    private final com.saleshub.mapper.SysPlatformShopMapper platformShopMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<IPage<SysUser>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(userService.listUsers(keyword, teamId, role, page, size));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<SysUser> create(@Valid @RequestBody UserCreateRequest request, Authentication auth) {
        log.info("创建用户: username={}, role={}", request.getUsername(), request.getRole());
        SysUser user = userService.createUser(request);
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "CREATE", "SysUser", user.getId(), "username=" + request.getUsername());
        return Result.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody UserUpdateRequest request, Authentication auth) {
        log.info("更新用户: id={}", id);
        SysUser user = userService.updateUser(id, request);
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "UPDATE", "SysUser", id, null);
        return Result.ok(user);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        log.info("切换用户状态: id={}, status={}", id, body.get("status"));
        userService.toggleStatus(id, body.get("status"));
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "UPDATE", "SysUser", id, "status=" + body.get("status"));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<?> delete(@PathVariable Long id, Authentication auth) {
        log.info("删除用户: id={}", id);
        userService.deleteUser(id);
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "DELETE", "SysUser", id, null);
        return Result.ok();
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<?> resetPassword(@PathVariable Long id, Authentication auth) {
        log.info("重置密码: userId={}", id);
        userService.resetPassword(id);
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "RESET_PWD", "SysUser", id, null);
        return Result.ok();
    }

    /** 获取用户的渠道分配 */
    @GetMapping("/{id}/platforms")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<?> getUserPlatforms(@PathVariable Long id) {
        return Result.ok(userPlatformService.listByUserId(id));
    }

    /** 设置用户的渠道分配（全量替换） */
    @PutMapping("/{id}/platforms")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<?> assignPlatforms(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        log.info("分配用户渠道: userId={}", id);
        @SuppressWarnings("unchecked")
        java.util.List<Number> accountIds = (java.util.List<Number>) body.get("accountIds");
        java.util.List<Long> ids = accountIds == null ? java.util.List.of()
            : accountIds.stream().map(Number::longValue).toList();
        userPlatformService.assign(id, ids);
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "UPDATE", "SysUserPlatform", id, "accountIds=" + ids);
        return Result.ok();
    }

    /** 获取用户的店铺分配（客服用） */
    @GetMapping("/{id}/shops")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<?> getUserShops(@PathVariable Long id) {
        return Result.ok(userShopMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.saleshub.entity.SysUserShop>()
                .eq(com.saleshub.entity.SysUserShop::getUserId, id)
        ));
    }

    /** 设置用户的店铺分配（全量替换） */
    @PutMapping("/{id}/shops")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<?> assignShops(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        log.info("分配用户店铺: userId={}", id);
        @SuppressWarnings("unchecked")
        java.util.List<Number> shopIds = (java.util.List<Number>) body.get("shopIds");
        java.util.List<Long> ids = shopIds == null ? java.util.List.of()
            : shopIds.stream().map(Number::longValue).toList();

        // 删除旧的
        userShopMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.saleshub.entity.SysUserShop>()
                .eq(com.saleshub.entity.SysUserShop::getUserId, id)
        );
        // 插入新的
        if (!ids.isEmpty()) {
            var shops = platformShopMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.saleshub.entity.SysPlatformShop>()
                    .in(com.saleshub.entity.SysPlatformShop::getId, ids)
            );
            var idToCode = shops.stream().collect(java.util.stream.Collectors.toMap(
                com.saleshub.entity.SysPlatformShop::getId, com.saleshub.entity.SysPlatformShop::getPlatformCode));
            for (Long shopId : ids) {
                var us = new com.saleshub.entity.SysUserShop();
                us.setUserId(id);
                us.setShopId(shopId);
                us.setPlatformCode(idToCode.getOrDefault(shopId, ""));
                userShopMapper.insert(us);
            }
        }
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "UPDATE", "SysUserShop", id, "shopIds=" + ids);
        return Result.ok();
    }
}
