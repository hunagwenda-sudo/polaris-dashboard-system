package com.saleshub.controller;

import com.saleshub.common.BusinessException;
import com.saleshub.common.Result;
import com.saleshub.dto.TeamRequest;
import com.saleshub.entity.SysTeam;
import com.saleshub.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(teamService.listTeamsWithStats());
    }

    /** 获取所有合伙人（用于选择团队负责人），仅管理员 */
    @GetMapping("/partners")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> partners() {
        return Result.ok(teamService.listPartners());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysTeam> create(@Valid @RequestBody TeamRequest request) {
        log.info("创建团队: name={}, leaderId={}", request.getName(), request.getLeaderId());
        return Result.ok(teamService.createTeam(request));
    }

    @PutMapping("/{id}")
    public Result<SysTeam> update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        log.info("更新团队: id={}", id);
        checkAdminOrTeamLeader(id);
        return Result.ok(teamService.updateTeam(id, request));
    }

    @PostMapping("/{id}/members")
    public Result<?> addMember(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        log.info("团队添加成员: teamId={}, userId={}", id, body.get("userId"));
        checkAdminOrTeamLeader(id);
        teamService.addMember(id, body.get("userId"));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        log.info("删除团队: id={}", id);
        teamService.deleteTeam(id);
        return Result.ok();
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<List<Map<String, Object>>> members(@PathVariable Long id) {
        return Result.ok(teamService.getMembers(id));
    }

    @GetMapping("/{id}/available-members")
    public Result<List<Map<String, Object>>> availableMembers(@PathVariable Long id) {
        checkAdminOrTeamLeader(id);
        return Result.ok(teamService.getAvailableMembers(id));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public Result<?> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        log.info("团队移除成员: teamId={}, userId={}", id, userId);
        checkAdminOrTeamLeader(id);
        teamService.removeMember(id, userId);
        return Result.ok();
    }

    @GetMapping("/{id}/records")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<Map<String, Object>> records(@PathVariable Long id,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.ok(teamService.getTeamRecords(id, page, size));
    }

    private void checkAdminOrTeamLeader(Long teamId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) return;
        Long userId = (Long) auth.getPrincipal();
        if (!teamService.isTeamLeader(teamId, userId)) {
            throw new BusinessException("无权操作该团队");
        }
    }
}
