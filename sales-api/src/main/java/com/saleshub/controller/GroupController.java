package com.saleshub.controller;

import com.saleshub.common.BusinessException;
import com.saleshub.common.Result;
import com.saleshub.dto.GroupRequest;
import com.saleshub.entity.SysGroup;
import com.saleshub.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(groupService.listGroups());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('group:manage')")
    public Result<SysGroup> create(@Valid @RequestBody GroupRequest request) {
        return Result.ok(groupService.createGroup(request));
    }

    @PutMapping("/{id}")
    public Result<SysGroup> update(@PathVariable Long id, @Valid @RequestBody GroupRequest request) {
        checkGroupManageOrLeader(id);
        return Result.ok(groupService.updateGroup(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('group:manage')")
    public Result<?> delete(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return Result.ok();
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<List<Map<String, Object>>> members(@PathVariable Long id) {
        return Result.ok(groupService.getMembers(id));
    }

    @GetMapping("/{id}/available-members")
    public Result<List<Map<String, Object>>> availableMembers(@PathVariable Long id) {
        checkGroupManageOrLeader(id);
        return Result.ok(groupService.getAvailableMembers(id));
    }

    @PostMapping("/{id}/members")
    public Result<?> addMember(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        checkGroupManageOrLeader(id);
        groupService.addMember(id, body.get("userId"));
        return Result.ok();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public Result<?> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        checkGroupManageOrLeader(id);
        groupService.removeMember(userId);
        return Result.ok();
    }

    /**
     * 检查当前用户是否有 group:manage 权限，或者是该小组的组长
     */
    private void checkGroupManageOrLeader(Long groupId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 有 group:manage 权限直接放行
        boolean hasManagePerm = auth.getAuthorities().stream()
                .anyMatch(a -> "group:manage".equals(a.getAuthority()));
        if (hasManagePerm) return;

        // 检查是否是该小组的组长
        Long userId = (Long) auth.getPrincipal();
        if (!groupService.isGroupLeader(groupId, userId)) {
            throw new BusinessException("无权操作该小组");
        }
    }
}
