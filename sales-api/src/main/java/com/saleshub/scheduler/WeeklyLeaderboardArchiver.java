package com.saleshub.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.entity.*;
import com.saleshub.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每周一凌晨 00:05 自动存档上周的英雄榜
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyLeaderboardArchiver {

    private final BizDailyRecordMapper recordMapper;
    private final SysUserMapper userMapper;
    private final BizWeeklyLeaderboardMapper leaderboardMapper;

    @Scheduled(cron = "0 5 0 * * MON")
    public void archiveLastWeek() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonday = today.minusWeeks(1);
        LocalDate lastSunday = lastMonday.plusDays(6);
        doArchive(lastMonday, lastSunday);
    }

    /**
     * 手动存档指定周（供 API 调用）
     */
    public int doArchive(LocalDate weekStart, LocalDate weekEnd) {
        String weekLabel = weekStart.getYear() + "-W"
            + String.format("%02d", weekStart.get(WeekFields.ISO.weekOfWeekBasedYear()));

        // 检查是否已存档
        Long existing = leaderboardMapper.selectCount(
            new LambdaQueryWrapper<BizWeeklyLeaderboard>().eq(BizWeeklyLeaderboard::getWeekLabel, weekLabel)
        );
        if (existing > 0) {
            log.info("周榜 {} 已存档，跳过", weekLabel);
            return 0;
        }

        log.info("开始存档周榜: {} ({} ~ {})", weekLabel, weekStart, weekEnd);

        // 查该周所有业绩
        List<BizDailyRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>()
                .ge(BizDailyRecord::getRecordDate, weekStart)
                .le(BizDailyRecord::getRecordDate, weekEnd)
        );
        Map<Long, BigDecimal> userDgmv = records.stream()
            .collect(Collectors.groupingBy(BizDailyRecord::getUserId,
                Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));

        // 排除 admin 和 service
        List<SysUser> bizUsers = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>().in(SysUser::getRole, "sales", "partner")
        );
        Set<Long> bizUserIds = bizUsers.stream().map(SysUser::getId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = bizUsers.stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        // 只保留业务用户，按 DGMV 降序排名
        List<Map.Entry<Long, BigDecimal>> sorted = userDgmv.entrySet().stream()
            .filter(e -> bizUserIds.contains(e.getKey()))
            .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
            .toList();

        int count = 0;
        for (int i = 0; i < sorted.size(); i++) {
            var entry = sorted.get(i);
            SysUser user = userMap.get(entry.getKey());
            if (user == null) continue;

            BizWeeklyLeaderboard row = new BizWeeklyLeaderboard();
            row.setWeekStart(weekStart);
            row.setWeekEnd(weekEnd);
            row.setWeekLabel(weekLabel);
            row.setRankNum(i + 1);
            row.setUserId(entry.getKey());
            row.setUserName(user.getName());
            row.setUserRole(user.getRole());
            row.setUserLevel(user.getLevel());
            row.setEstimatedLevel(user.getEstimatedLevel());
            row.setDgmv(entry.getValue());
            row.setCreatedAt(LocalDateTime.now());
            leaderboardMapper.insert(row);
            count++;
        }
        log.info("周榜 {} 存档完成，共 {} 条", weekLabel, count);
        return count;
    }
}
