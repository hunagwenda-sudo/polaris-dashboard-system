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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuditService auditService;

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
        SysUser user = userService.createUser(request);
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "CREATE", "SysUser", user.getId(), "username=" + request.getUsername());
        return Result.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody UserUpdateRequest request, Authentication auth) {
        SysUser user = userService.updateUser(id, request);
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "UPDATE", "SysUser", id, null);
        return Result.ok(user);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        userService.toggleStatus(id, body.get("status"));
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "UPDATE", "SysUser", id, "status=" + body.get("status"));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<?> delete(@PathVariable Long id, Authentication auth) {
        userService.deleteUser(id);
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "DELETE", "SysUser", id, null);
        return Result.ok();
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<?> resetPassword(@PathVariable Long id, Authentication auth) {
        userService.resetPassword(id);
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(details.getUserId(), details.getUsername(), "RESET_PWD", "SysUser", id, null);
        return Result.ok();
    }
}
