package com.saleshub.service;

import com.saleshub.entity.BizQuarterlySnapshot;

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

    /** 获取可用的季度列表 */
    List<String> getAvailableQuarters();
    /** 获取指定季度的快照数据 */
    List<BizQuarterlySnapshot> getQuarterlySnapshots(String quarter);
    /** 手动生成指定季度的快照（补录历史） */
    int generateQuarterlySnapshot(String quarter);
}
