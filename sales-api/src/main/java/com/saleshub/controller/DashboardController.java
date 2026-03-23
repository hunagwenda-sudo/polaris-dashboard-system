package com.saleshub.controller;

import com.saleshub.common.Result;
import com.saleshub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('dashboard:view')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/personal")
    public Result<Map<String, Object>> personal(@AuthenticationPrincipal Long userId) {
        return Result.ok(dashboardService.getCompanyStats(userId));
    }

    @GetMapping("/personal-level")
    public Result<Map<String, Object>> personalLevel(@AuthenticationPrincipal Long userId) {
        return Result.ok(dashboardService.getPersonalLevel(userId));
    }

    @GetMapping("/leaderboard/daily")
    public Result<List<Map<String, Object>>> dailyLeaderboard(@RequestParam(defaultValue = "sales") String role) {
        return Result.ok(dashboardService.getDailyLeaderboard(role));
    }

    @GetMapping("/leaderboard/weekly")
    public Result<List<Map<String, Object>>> weeklyLeaderboard(@RequestParam(defaultValue = "sales") String role) {
        return Result.ok(dashboardService.getWeeklyLeaderboard(role));
    }

    @GetMapping("/team-battle")
    public Result<List<Map<String, Object>>> teamBattle() {
        return Result.ok(dashboardService.getTeamBattle());
    }

    @GetMapping("/channel")
    public Result<Map<String, Object>> channel() {
        return Result.ok(dashboardService.getChannelBreakdown());
    }

    @GetMapping("/announcement")
    public Result<Map<String, Object>> announcement() {
        return Result.ok(dashboardService.getAnnouncement());
    }

    /** 获取可用的季度列表 */
    @GetMapping("/quarterly/quarters")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<String>> availableQuarters() {
        return Result.ok(dashboardService.getAvailableQuarters());
    }

    /** 获取指定季度的快照数据 */
    @GetMapping("/quarterly/snapshots")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> quarterlySnapshots(@RequestParam String quarter) {
        return Result.ok(dashboardService.getQuarterlySnapshots(quarter));
    }

    /** 手动生成指定季度的快照（补录历史） */
    @PostMapping("/quarterly/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> generateSnapshot(@RequestBody Map<String, String> body) {
        log.info("手动生成季度快照: quarter={}", body.get("quarter"));
        String quarter = body.get("quarter");
        if (quarter == null || quarter.isBlank()) throw new com.saleshub.common.BusinessException("请指定季度");
        int count = dashboardService.generateQuarterlySnapshot(quarter);
        return Result.ok(Map.of("count", count));
    }
}
