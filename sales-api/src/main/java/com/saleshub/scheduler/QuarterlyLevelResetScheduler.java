package com.saleshub.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.entity.BizDailyRecord;
import com.saleshub.entity.BizQuarterlySnapshot;
import com.saleshub.entity.SysTeam;
import com.saleshub.entity.SysUser;
import com.saleshub.entity.SysDict;
import com.saleshub.mapper.BizDailyRecordMapper;
import com.saleshub.mapper.BizQuarterlySnapshotMapper;
import com.saleshub.mapper.SysTeamMapper;
import com.saleshub.mapper.SysUserMapper;
import com.saleshub.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuarterlyLevelResetScheduler {

    private final SysUserMapper userMapper;
    private final SysTeamMapper teamMapper;
    private final BizDailyRecordMapper recordMapper;
    private final BizQuarterlySnapshotMapper snapshotMapper;
    private final SysDictMapper dictMapper;

    /** 每季度第一天凌晨 0:05 —— 先保存上季度快照，再清空所有运营的职级 */
    @Scheduled(cron = "0 5 0 1 1,4,7,10 ?", zone = "Asia/Shanghai")
    public void resetSalesLevel() {
        log.info("季度首日：保存上季度快照并重置运营职级");

        // 上一个季度的时间范围
        LocalDate today = LocalDate.now();
        LocalDate prevQuarterEnd = today.minusDays(1); // 昨天 = 上季度最后一天
        LocalDate prevQuarterStart = getQuarterStart(prevQuarterEnd);
        String quarterLabel = formatQuarterLabel(prevQuarterStart);

        // 所有在职运营
        List<SysUser> salesUsers = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "sales")
                .eq(SysUser::getStatus, "active")
        );

        if (!salesUsers.isEmpty()) {
            // 团队名称 map
            List<SysTeam> teams = teamMapper.selectList(null);
            Map<Long, String> teamNameMap = teams.stream()
                .collect(Collectors.toMap(SysTeam::getId, SysTeam::getName, (a, b) -> a));

            // 上季度所有业绩记录
            Set<Long> userIds = salesUsers.stream().map(SysUser::getId).collect(Collectors.toSet());
            List<BizDailyRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<BizDailyRecord>()
                    .in(BizDailyRecord::getUserId, userIds)
                    .ge(BizDailyRecord::getRecordDate, prevQuarterStart)
                    .le(BizDailyRecord::getRecordDate, prevQuarterEnd)
            );
            Map<Long, BigDecimal> dgmvMap = records.stream()
                .collect(Collectors.groupingBy(BizDailyRecord::getUserId,
                    Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));

            // 职级阈值
            List<Map.Entry<String, BigDecimal>> thresholds = loadSortedThresholds();
            List<String> levels = extractLevelNames(thresholds);

            // 保存快照
            int snapshotCount = 0;
            for (SysUser user : salesUsers) {
                BigDecimal totalDgmv = dgmvMap.getOrDefault(user.getId(), BigDecimal.ZERO);
                String estimatedLevel = estimateLevel(totalDgmv, thresholds, levels);

                // 检查是否已存在（防止重复执行）
                Long existing = snapshotMapper.selectCount(
                    new LambdaQueryWrapper<BizQuarterlySnapshot>()
                        .eq(BizQuarterlySnapshot::getUserId, user.getId())
                        .eq(BizQuarterlySnapshot::getQuarter, quarterLabel)
                );
                if (existing > 0) continue;

                BizQuarterlySnapshot snapshot = new BizQuarterlySnapshot();
                snapshot.setUserId(user.getId());
                snapshot.setUserName(user.getName());
                snapshot.setTeamId(user.getTeamId());
                snapshot.setTeamName(user.getTeamId() != null ? teamNameMap.get(user.getTeamId()) : null);
                snapshot.setQuarter(quarterLabel);
                snapshot.setLevel(user.getLevel());
                snapshot.setEstimatedLevel(estimatedLevel);
                snapshot.setTotalDgmv(totalDgmv);
                snapshot.setCreatedAt(LocalDateTime.now());
                snapshotMapper.insert(snapshot);
                snapshotCount++;
            }
            log.info("已保存 {} 条季度快照 ({})", snapshotCount, quarterLabel);
        }

        // 重置职级
        int resetCount = 0;
        for (SysUser user : salesUsers) {
            user.setLevel(null);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
            resetCount++;
        }
        log.info("已重置 {} 名运营的职级", resetCount);
    }

    // --- helpers ---

    private LocalDate getQuarterStart(LocalDate date) {
        int month = ((date.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), month, 1);
    }

    /** 格式化季度标识，如 2026-Q1 */
    private String formatQuarterLabel(LocalDate dateInQuarter) {
        int q = (dateInQuarter.getMonthValue() - 1) / 3 + 1;
        return dateInQuarter.getYear() + "-Q" + q;
    }

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
        if (result.isEmpty()) result.add(Map.entry("K1_K2", BigDecimal.valueOf(50000)));
        return result;
    }

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

    private String estimateLevel(BigDecimal dgmv, List<Map.Entry<String, BigDecimal>> thresholds, List<String> levels) {
        for (int i = thresholds.size() - 1; i >= 0; i--) {
            if (dgmv.compareTo(thresholds.get(i).getValue()) >= 0) {
                return levels.get(i + 1);
            }
        }
        return levels.isEmpty() ? "K1" : levels.get(0);
    }
}
