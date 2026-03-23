package com.saleshub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.entity.*;
import com.saleshub.mapper.*;
import com.saleshub.mapper.BizQuarterlySnapshotMapper;
import com.saleshub.service.DashboardService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BizDailyRecordMapper recordMapper;
    private final SysUserMapper userMapper;
    private final SysTeamMapper teamMapper;
    private final SysDictMapper dictMapper;
    private final BizQuarterlySnapshotMapper snapshotMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "dashboard:";
    private static final long CACHE_TTL_SECONDS = 60; // 1 分钟缓存
    private static final int WEEKLY_TOP_N = 7; // 英雄榜显示人数

    private <T> T readCache(String key, TypeReference<T> typeRef) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) return objectMapper.readValue(json, typeRef);
        } catch (Exception e) { log.warn("Redis read failed: {}", e.getMessage()); }
        return null;
    }

    private void writeCache(String key, Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) { log.warn("Redis write failed: {}", e.getMessage()); }
    }

    /** 批量加载用户 map，避免 N+1 */
    private Map<Long, SysUser> loadUserMap(Collection<Long> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        return userMapper.selectList(
            new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds)
        ).stream().collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }

    @Override
    public Map<String, Object> getCompanyStats(Long userId) {
        String key = CACHE_PREFIX + "stats:" + userId;
        Map<String, Object> cached = readCache(key, new TypeReference<>() {});
        if (cached != null) return cached;

        Map<String, Object> result = computeRoleStats(userId);
        writeCache(key, result);
        return result;
    }

    private Map<String, Object> computeRoleStats(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return Collections.emptyMap();

        String role = user.getRole();
        LocalDate now = LocalDate.now();
        LocalDate quarterStart = getQuarterStart(now);
        LocalDate quarterEnd = getQuarterEnd(now);

        BigDecimal targetDgmv;
        BigDecimal totalDgmv;
        String scope;

        if ("admin".equals(role)) {
            // 管理员：所有团队目标之和，所有人的 DGMV
            List<SysTeam> teams = teamMapper.selectList(null);
            targetDgmv = teams.stream()
                .map(t -> t.getTargetDgmv() != null ? t.getTargetDgmv() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            List<BizDailyRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<BizDailyRecord>()
                    .ge(BizDailyRecord::getRecordDate, quarterStart)
                    .le(BizDailyRecord::getRecordDate, now)
            );
            totalDgmv = records.stream().map(BizDailyRecord::getDgmv).reduce(BigDecimal.ZERO, BigDecimal::add);
            scope = "company";
        } else if ("partner".equals(role)) {
            // 合伙人：自己团队的目标，团队成员的 DGMV
            Long teamId = user.getTeamId();
            if (teamId != null) {
                SysTeam team = teamMapper.selectById(teamId);
                targetDgmv = team != null && team.getTargetDgmv() != null ? team.getTargetDgmv() : BigDecimal.ZERO;
                // 查团队所有销售/合伙人成员（排除 service）
                List<SysUser> members = userMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getTeamId, teamId)
                        .in(SysUser::getRole, "sales", "partner")
                );
                List<Long> memberIds = members.stream().map(SysUser::getId).toList();
                if (!memberIds.isEmpty()) {
                    List<BizDailyRecord> records = recordMapper.selectList(
                        new LambdaQueryWrapper<BizDailyRecord>()
                            .in(BizDailyRecord::getUserId, memberIds)
                            .ge(BizDailyRecord::getRecordDate, quarterStart)
                            .le(BizDailyRecord::getRecordDate, now)
                    );
                    totalDgmv = records.stream().map(BizDailyRecord::getDgmv).reduce(BigDecimal.ZERO, BigDecimal::add);
                } else {
                    totalDgmv = BigDecimal.ZERO;
                }
            } else {
                targetDgmv = BigDecimal.ZERO;
                totalDgmv = BigDecimal.ZERO;
            }
            scope = "team";
        } else {
            // 运营：个人目标，个人 DGMV
            targetDgmv = user.getTargetDgmv() != null ? user.getTargetDgmv() : BigDecimal.ZERO;
            List<BizDailyRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<BizDailyRecord>()
                    .eq(BizDailyRecord::getUserId, userId)
                    .ge(BizDailyRecord::getRecordDate, quarterStart)
                    .le(BizDailyRecord::getRecordDate, now)
            );
            totalDgmv = records.stream().map(BizDailyRecord::getDgmv).reduce(BigDecimal.ZERO, BigDecimal::add);
            scope = "personal";
        }

        BigDecimal gap = targetDgmv.subtract(totalDgmv);
        if (gap.compareTo(BigDecimal.ZERO) < 0) gap = BigDecimal.ZERO;
        long quarterTotalDays = ChronoUnit.DAYS.between(quarterStart, quarterEnd) + 1;
        long elapsedDays = ChronoUnit.DAYS.between(quarterStart, now);
        long daysLeft = ChronoUnit.DAYS.between(now, quarterEnd);
        BigDecimal dailyNeeded = daysLeft > 0 && gap.compareTo(BigDecimal.ZERO) > 0
            ? gap.divide(BigDecimal.valueOf(daysLeft), 0, RoundingMode.CEILING)
            : BigDecimal.ZERO;
        BigDecimal rate = targetDgmv.compareTo(BigDecimal.ZERO) > 0
            ? totalDgmv.multiply(BigDecimal.valueOf(100)).divide(targetDgmv, 1, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("targetDgmv", targetDgmv);
        result.put("totalDgmv", totalDgmv);
        result.put("gap", gap);
        result.put("daysLeft", daysLeft);
        result.put("elapsedDays", elapsedDays);
        result.put("quarterTotalDays", quarterTotalDays);
        result.put("dailyNeeded", dailyNeeded);
        result.put("completionRate", rate);
        result.put("scope", scope);
        return result;
    }

    @Override
    public List<Map<String, Object>> getDailyLeaderboard(String role) {
        boolean includePartner = "partner".equals(role) || "admin".equals(role);
        String key = CACHE_PREFIX + "daily_lb:" + (includePartner ? "full" : "sales");
        List<Map<String, Object>> cached = readCache(key, new TypeReference<>() {});
        if (cached != null) return cached;

        List<Map<String, Object>> result = computeDailyLeaderboard(includePartner);
        writeCache(key, result);
        return result;
    }

    private List<Map<String, Object>> computeDailyLeaderboard(boolean includePartner) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<BizDailyRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>().eq(BizDailyRecord::getRecordDate, yesterday)
        );
        Map<Long, BigDecimal> userDgmv = records.stream()
            .collect(Collectors.groupingBy(BizDailyRecord::getUserId,
                Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));

        // 加载用户列表，根据 includePartner 决定是否包含合伙人
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>();
        if (includePartner) {
            query.in(SysUser::getRole, "sales", "partner");
        } else {
            query.eq(SysUser::getRole, "sales");
        }
        List<SysUser> users = userMapper.selectList(query);
        java.text.Collator collator = java.text.Collator.getInstance(java.util.Locale.CHINA);
        users.sort((a, b) -> collator.compare(a.getName() != null ? a.getName() : "", b.getName() != null ? b.getName() : ""));

        return users.stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", u.getId());
            map.put("name", u.getName() != null ? u.getName() : "未知");
            map.put("dgmv", userDgmv.getOrDefault(u.getId(), BigDecimal.ZERO));
            return map;
        }).toList();
    }

    @Override
    public List<Map<String, Object>> getWeeklyLeaderboard(String role) {
        boolean includePartner = "partner".equals(role) || "admin".equals(role);
        String key = CACHE_PREFIX + "weekly_lb:" + (includePartner ? "full" : "sales");
        List<Map<String, Object>> cached = readCache(key, new TypeReference<>() {});
        if (cached != null) return cached;

        List<Map<String, Object>> result = computeWeeklyLeaderboard(includePartner);
        writeCache(key, result);
        return result;
    }

    private List<Map<String, Object>> computeWeeklyLeaderboard(boolean includePartner) {
        LocalDate now = LocalDate.now();
        // 自然周：周一到周日
        LocalDate weekStart = now.with(java.time.DayOfWeek.MONDAY);
        List<BizDailyRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>()
                .ge(BizDailyRecord::getRecordDate, weekStart)
                .le(BizDailyRecord::getRecordDate, now)
        );
        Map<Long, BigDecimal> userDgmv = records.stream()
            .collect(Collectors.groupingBy(BizDailyRecord::getUserId,
                Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));

        // 根据 includePartner 决定是否排除合伙人
        if (!includePartner) {
            List<Long> partnerIds = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "partner")
            ).stream().map(SysUser::getId).toList();
            partnerIds.forEach(userDgmv::remove);
        }

        List<Map.Entry<Long, BigDecimal>> top7 = userDgmv.entrySet().stream()
            .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
            .limit(WEEKLY_TOP_N).toList();

        Map<Long, SysUser> userMap = loadUserMap(top7.stream().map(Map.Entry::getKey).toList());

        // Load thresholds once for all users
        List<Map.Entry<String, BigDecimal>> thresholds = loadSortedThresholds();
        List<String> levels = extractLevelNames(thresholds);

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 0;
        for (var e : top7) {
            rank++;
            SysUser user = userMap.get(e.getKey());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", e.getKey());
            map.put("name", user != null ? user.getName() : "未知");
            map.put("role", user != null ? user.getRole() : "sales");
            map.put("level", user != null ? user.getLevel() : "K1");
            // 第6名开始金额用 ** 隐藏
            map.put("dgmv", rank <= 5 ? e.getValue() : "**");
            map.put("estimatedLevel", estimateLevelFromThresholds(e.getValue(), thresholds, levels));
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getTeamBattle() {
        String key = CACHE_PREFIX + "team_battle";
        List<Map<String, Object>> cached = readCache(key, new TypeReference<>() {});
        if (cached != null) return cached;

        List<Map<String, Object>> result = computeTeamBattle();
        writeCache(key, result);
        return result;
    }

    private List<Map<String, Object>> computeTeamBattle() {
        List<SysTeam> teams = teamMapper.selectList(null);
        LocalDate quarterStart = getQuarterStart(LocalDate.now());

        // 一次性加载所有销售/合伙人用户（排除 admin 和 service）
        List<SysUser> allUsers = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>().in(SysUser::getRole, "sales", "partner")
        );
        Map<Long, List<SysUser>> teamUsers = allUsers.stream()
            .filter(u -> u.getTeamId() != null)
            .collect(Collectors.groupingBy(SysUser::getTeamId));

        // 一次性加载本季度所有记录
        List<BizDailyRecord> allRecords = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>().ge(BizDailyRecord::getRecordDate, quarterStart)
        );
        Map<Long, BigDecimal> userDgmvMap = allRecords.stream()
            .collect(Collectors.groupingBy(BizDailyRecord::getUserId,
                Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));

        Map<Long, SysUser> userIdMap = allUsers.stream().collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));

        List<Map<String, Object>> result = new ArrayList<>();
        for (SysTeam team : teams) {
            List<SysUser> members = teamUsers.getOrDefault(team.getId(), List.of());
            BigDecimal totalDgmv = members.stream()
                .map(m -> userDgmvMap.getOrDefault(m.getId(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("teamId", team.getId());
            map.put("teamName", team.getName());
            map.put("quarterDgmv", totalDgmv);
            map.put("targetDgmv", team.getTargetDgmv());
            map.put("memberCount", members.size());
            result.add(map);
        }
        result.sort((a, b) -> ((BigDecimal) b.get("quarterDgmv")).compareTo((BigDecimal) a.get("quarterDgmv")));
        return result;
    }

    @Override
    public Map<String, Object> getChannelBreakdown() {
        String key = CACHE_PREFIX + "channel";
        Map<String, Object> cached = readCache(key, new TypeReference<>() {});
        if (cached != null) return cached;

        Map<String, Object> result = computeChannelBreakdown();
        writeCache(key, result);
        return result;
    }

    private Map<String, Object> computeChannelBreakdown() {
        LocalDate quarterStart = getQuarterStart(LocalDate.now());
        List<BizDailyRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>().ge(BizDailyRecord::getRecordDate, quarterStart)
        );
        Map<String, BigDecimal> platformDgmv = records.stream()
            .collect(Collectors.groupingBy(BizDailyRecord::getPlatform,
                Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));

        LocalDate weekAgo = LocalDate.now().minusDays(7);
        List<BizDailyRecord> weekRecords = records.stream()
            .filter(r -> !r.getRecordDate().isBefore(weekAgo)).toList();
        Map<String, Map<LocalDate, BigDecimal>> trend = new LinkedHashMap<>();
        for (var r : weekRecords) {
            trend.computeIfAbsent(r.getPlatform(), k -> new TreeMap<>())
                .merge(r.getRecordDate(), r.getDgmv(), BigDecimal::add);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("breakdown", platformDgmv);
        result.put("trend", trend);
        return result;
    }

    @Override
    public Map<String, Object> getAnnouncement() {
        String key = CACHE_PREFIX + "announcement";
        Map<String, Object> cached = readCache(key, new TypeReference<>() {});
        if (cached != null) return cached;

        Map<String, Object> result = computeAnnouncement();
        writeCache(key, result);
        return result;
    }

    private Map<String, Object> computeAnnouncement() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 只有 sales 角色参与滚动条排名和晋升预估
        Set<Long> salesUserIds = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "sales").eq(SysUser::getStatus, "active").select(SysUser::getId)
        ).stream().map(SysUser::getId).collect(Collectors.toSet());

        // 昨日最佳（仅 sales）
        List<BizDailyRecord> yesterdayRecords = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>().eq(BizDailyRecord::getRecordDate, yesterday)
        );
        Map<Long, BigDecimal> yesterdayDgmv = yesterdayRecords.stream()
            .filter(r -> salesUserIds.contains(r.getUserId()))
            .collect(Collectors.groupingBy(BizDailyRecord::getUserId,
                Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));

        Map<String, Object> result = new LinkedHashMap<>();
        if (!yesterdayDgmv.isEmpty()) {
            var best = yesterdayDgmv.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
            if (best != null) {
                SysUser user = userMapper.selectById(best.getKey());
                result.put("bestUser", user != null ? user.getName() : "未知");
                result.put("bestDgmv", best.getValue());
            }
        }
        result.put("totalYesterday", yesterdayDgmv.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));

        // 本周周榜第一（仅 sales）
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        List<BizDailyRecord> weekRecords = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>()
                .ge(BizDailyRecord::getRecordDate, weekStart)
                .le(BizDailyRecord::getRecordDate, today)
        );
        Map<Long, BigDecimal> weekDgmv = weekRecords.stream()
            .filter(r -> salesUserIds.contains(r.getUserId()))
            .collect(Collectors.groupingBy(BizDailyRecord::getUserId,
                Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));
        if (!weekDgmv.isEmpty()) {
            var weekBest = weekDgmv.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
            if (weekBest != null) {
                SysUser user = userMapper.selectById(weekBest.getKey());
                result.put("weekTopUser", user != null ? user.getName() : "未知");
            }
        }

        // 最接近晋升的人（本季度 DGMV，找进度最高但未满100%的人）
        LocalDate quarterStart = getQuarterStart(today);
        List<BizDailyRecord> quarterRecords = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>().ge(BizDailyRecord::getRecordDate, quarterStart)
        );
        Map<Long, BigDecimal> quarterDgmv = quarterRecords.stream()
            .collect(Collectors.groupingBy(BizDailyRecord::getUserId,
                Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));

        List<Map.Entry<String, BigDecimal>> thresholds = loadSortedThresholds();
        List<String> levels = extractLevelNames(thresholds);

        if (!quarterDgmv.isEmpty() && !thresholds.isEmpty()) {
            // 找预估职级刚上涨的人（对比 sys_user.estimated_level，变高才播报并更新）
            for (var entry : quarterDgmv.entrySet()) {
                if (!salesUserIds.contains(entry.getKey())) continue;
                String newEstimated = estimateLevelFromThresholds(entry.getValue(), thresholds, levels);
                SysUser user = userMapper.selectById(entry.getKey());
                if (user == null) continue;
                String oldEstimated = user.getEstimatedLevel();
                int newIdx = levels.indexOf(newEstimated);
                int oldIdx = oldEstimated != null ? levels.indexOf(oldEstimated) : -1;
                if (newIdx > oldIdx) {
                    // 预估职级上涨了，更新字段并播报
                    user.setEstimatedLevel(newEstimated);
                    user.setUpdatedAt(LocalDateTime.now());
                    userMapper.updateById(user);
                    result.put("nearLevelUser", user.getName());
                    result.put("nearLevelName", newEstimated);
                    break; // 只播报一个
                } else if (newIdx != oldIdx && oldEstimated != null) {
                    // 预估下降了（退款等），静默更新字段
                    user.setEstimatedLevel(newEstimated);
                    user.setUpdatedAt(LocalDateTime.now());
                    userMapper.updateById(user);
                }
            }
        }

        return result;
    }

    @Override
    public Map<String, Object> getPersonalLevel(Long userId) {
        SysUser user = userMapper.selectById(userId);
        String lastLevel = user != null && user.getLevel() != null ? user.getLevel() : "K1";

        LocalDate quarterStart = getQuarterStart(LocalDate.now());
        List<BizDailyRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>()
                .eq(BizDailyRecord::getUserId, userId)
                .ge(BizDailyRecord::getRecordDate, quarterStart)
        );
        BigDecimal totalDgmv = records.stream().map(BizDailyRecord::getDgmv).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 动态读取职级阈值
        List<Map.Entry<String, BigDecimal>> thresholds = loadSortedThresholds();
        List<String> levels = extractLevelNames(thresholds);

        // 每季度从 K1 重新开始，estimatedLevel 根据本季 DGMV 实时计算
        String estimatedLevel = estimateLevelFromThresholds(totalDgmv, thresholds, levels);
        int estimatedIdx = levels.indexOf(estimatedLevel);
        if (estimatedIdx < 0) estimatedIdx = 0;

        boolean isMax = estimatedIdx >= levels.size() - 1;
        String nextLevel = isMax ? estimatedLevel : levels.get(estimatedIdx + 1);

        // 当前段的起点阈值
        BigDecimal currentThreshold = estimatedIdx > 0 ? thresholds.get(estimatedIdx - 1).getValue() : BigDecimal.ZERO;
        // 下一级别的阈值
        BigDecimal nextThreshold = isMax ? currentThreshold : thresholds.get(estimatedIdx).getValue();

        BigDecimal range = nextThreshold.subtract(currentThreshold);
        BigDecimal progress = totalDgmv.subtract(currentThreshold);
        BigDecimal rate = range.compareTo(BigDecimal.ZERO) > 0
            ? progress.multiply(BigDecimal.valueOf(100)).divide(range, 1, RoundingMode.HALF_UP)
            : BigDecimal.valueOf(100);
        if (rate.compareTo(BigDecimal.ZERO) < 0) rate = BigDecimal.ZERO;
        if (rate.compareTo(BigDecimal.valueOf(100)) > 0) rate = BigDecimal.valueOf(100);

        BigDecimal gap = nextThreshold.subtract(totalDgmv);
        if (gap.compareTo(BigDecimal.ZERO) < 0) gap = BigDecimal.ZERO;

        // 季度剩余天数 & 日均需完成
        LocalDate quarterEnd = getQuarterEnd(LocalDate.now());
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), quarterEnd);
        BigDecimal dailyNeeded = daysLeft > 0 && gap.compareTo(BigDecimal.ZERO) > 0
            ? gap.divide(BigDecimal.valueOf(daysLeft), 0, RoundingMode.CEILING)
            : BigDecimal.ZERO;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lastLevel", lastLevel);           // 上季职级（sys_user.level）
        result.put("currentLevel", estimatedLevel);    // 本季实时职级（从K1算起）
        result.put("nextLevel", nextLevel);
        result.put("totalDgmv", totalDgmv);
        result.put("currentThreshold", currentThreshold);
        result.put("nextThreshold", nextThreshold);
        result.put("gap", gap);
        result.put("daysLeft", daysLeft);
        result.put("dailyNeeded", dailyNeeded);
        result.put("completionRate", rate);
        result.put("estimatedLevel", estimatedLevel);
        // 返回完整职级列表（从K1开始）
        result.put("allLevels", levels);
        return result;
    }

    @Override
    public void evictCache() {
        try {
            var keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
        } catch (Exception e) { log.warn("Cache eviction failed: {}", e.getMessage()); }
    }

    @Override
    public List<String> getAvailableQuarters() {
        List<BizQuarterlySnapshot> all = snapshotMapper.selectList(
            new LambdaQueryWrapper<BizQuarterlySnapshot>()
                .select(BizQuarterlySnapshot::getQuarter)
                .groupBy(BizQuarterlySnapshot::getQuarter)
                .orderByDesc(BizQuarterlySnapshot::getQuarter)
        );
        return all.stream().map(BizQuarterlySnapshot::getQuarter).distinct().toList();
    }

    @Override
    public List<BizQuarterlySnapshot> getQuarterlySnapshots(String quarter) {
        return snapshotMapper.selectList(
            new LambdaQueryWrapper<BizQuarterlySnapshot>()
                .eq(BizQuarterlySnapshot::getQuarter, quarter)
                .orderByDesc(BizQuarterlySnapshot::getTotalDgmv)
        );
    }

    @Override
    public int generateQuarterlySnapshot(String quarter) {
        // 解析季度 → 日期范围
        // quarter 格式: 2026-Q1
        String[] parts = quarter.split("-Q");
        if (parts.length != 2) throw new com.saleshub.common.BusinessException("季度格式错误，应为 yyyy-Qn");
        int year = Integer.parseInt(parts[0]);
        int q = Integer.parseInt(parts[1]);
        if (q < 1 || q > 4) throw new com.saleshub.common.BusinessException("季度范围 1-4");
        int startMonth = (q - 1) * 3 + 1;
        LocalDate qStart = LocalDate.of(year, startMonth, 1);
        LocalDate qEnd = qStart.plusMonths(3).minusDays(1);

        // 不能为未来季度生成
        if (qStart.isAfter(LocalDate.now())) throw new com.saleshub.common.BusinessException("不能为未来季度生成快照");

        // 检查是否已有快照
        Long existing = snapshotMapper.selectCount(
            new LambdaQueryWrapper<BizQuarterlySnapshot>().eq(BizQuarterlySnapshot::getQuarter, quarter)
        );
        if (existing > 0) throw new com.saleshub.common.BusinessException("该季度快照已存在，共 " + existing + " 条");

        // 所有运营（含已离职的，因为历史数据需要）
        List<SysUser> salesUsers = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "sales")
        );
        if (salesUsers.isEmpty()) return 0;

        // 团队名称
        List<SysTeam> teams = teamMapper.selectList(null);
        Map<Long, String> teamNameMap = teams.stream()
            .collect(Collectors.toMap(SysTeam::getId, SysTeam::getName, (a, b) -> a));

        // 该季度业绩
        Set<Long> userIds = salesUsers.stream().map(SysUser::getId).collect(Collectors.toSet());
        List<BizDailyRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>()
                .in(BizDailyRecord::getUserId, userIds)
                .ge(BizDailyRecord::getRecordDate, qStart)
                .le(BizDailyRecord::getRecordDate, qEnd)
        );
        Map<Long, BigDecimal> dgmvMap = records.stream()
            .collect(Collectors.groupingBy(BizDailyRecord::getUserId,
                Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));

        List<Map.Entry<String, BigDecimal>> thresholds = loadSortedThresholds();
        List<String> levelNames = extractLevelNames(thresholds);

        int count = 0;
        for (SysUser user : salesUsers) {
            BigDecimal totalDgmv = dgmvMap.getOrDefault(user.getId(), BigDecimal.ZERO);
            String estimated = estimateLevelFromThresholds(totalDgmv, thresholds, levelNames);

            BizQuarterlySnapshot snapshot = new BizQuarterlySnapshot();
            snapshot.setUserId(user.getId());
            snapshot.setUserName(user.getName());
            snapshot.setTeamId(user.getTeamId());
            snapshot.setTeamName(user.getTeamId() != null ? teamNameMap.get(user.getTeamId()) : null);
            snapshot.setQuarter(quarter);
            snapshot.setLevel(user.getLevel()); // 当前职级（补录时可能已被重置）
            snapshot.setEstimatedLevel(estimated);
            snapshot.setTotalDgmv(totalDgmv);
            snapshot.setCreatedAt(java.time.LocalDateTime.now());
            snapshotMapper.insert(snapshot);
            count++;
        }
        return count;
    }

    // --- helpers ---
    /** 从字典表读取职级阈值，返回按 sort 排序的列表 [(fromLevel, toLevel, threshold)] */
    private List<Map.Entry<String, BigDecimal>> loadSortedThresholds() {
        List<SysDict> dicts = dictMapper.selectList(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "level_threshold")
                .eq(SysDict::getStatus, "active")
                .orderByAsc(SysDict::getSort)
        );
        List<Map.Entry<String, BigDecimal>> result = new ArrayList<>();
        for (SysDict d : dicts) {
            try { result.add(Map.entry(d.getCode(), new BigDecimal(d.getLabel()))); }
            catch (Exception ignored) {}
        }
        if (result.isEmpty()) {
            result.add(Map.entry("K1_K2", BigDecimal.valueOf(50000)));
        }
        return result;
    }

    /** 从阈值列表提取所有职级名称（有序） */
    private List<String> extractLevelNames(List<Map.Entry<String, BigDecimal>> thresholds) {
        List<String> levels = new ArrayList<>();
        for (var entry : thresholds) {
            String[] parts = entry.getKey().split("_");
            if (parts.length == 2) {
                if (levels.isEmpty()) levels.add(parts[0]);
                levels.add(parts[1]);
            }
        }
        return levels;
    }

    private LocalDate getQuarterStart(LocalDate date) {
        int month = ((date.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), month, 1);
    }

    private LocalDate getQuarterEnd(LocalDate date) {
        int month = ((date.getMonthValue() - 1) / 3) * 3 + 3;
        return LocalDate.of(date.getYear(), month, 1).plusMonths(1).minusDays(1);
    }

    private String estimateLevelFromThresholds(BigDecimal dgmv, List<Map.Entry<String, BigDecimal>> thresholds, List<String> levels) {
        for (int i = thresholds.size() - 1; i >= 0; i--) {
            if (dgmv.compareTo(thresholds.get(i).getValue()) >= 0) {
                return levels.get(i + 1);
            }
        }
        return levels.isEmpty() ? "K1" : levels.get(0);
    }
}
