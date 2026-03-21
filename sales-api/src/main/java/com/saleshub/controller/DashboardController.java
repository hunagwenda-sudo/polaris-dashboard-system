package com.saleshub.controller;

import com.saleshub.common.Result;
import com.saleshub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
}
