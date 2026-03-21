package com.saleshub.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    Map<String, Object> getCompanyStats(Long userId);
    Map<String, Object> getPersonalLevel(Long userId);
    List<Map<String, Object>> getDailyLeaderboard(String role);
    List<Map<String, Object>> getWeeklyLeaderboard(String role);
    List<Map<String, Object>> getTeamBattle();
    Map<String, Object> getChannelBreakdown();
    Map<String, Object> getAnnouncement();
    void evictCache();
}
