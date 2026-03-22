package com.saleshub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.common.BusinessException;
import com.saleshub.dto.TeamRequest;
import com.saleshub.entity.BizDailyRecord;
import com.saleshub.entity.SysTeam;
import com.saleshub.entity.SysUser;
import com.saleshub.mapper.BizDailyRecordMapper;
import com.saleshub.mapper.SysTeamMapper;
import com.saleshub.mapper.SysUserMapper;
import com.saleshub.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final SysTeamMapper teamMapper;
    private final SysUserMapper userMapper;
    private final BizDailyRecordMapper recordMapper;

    @Override
    public List<Map<String, Object>> listTeamsWithStats() {
        List<SysTeam> teams = teamMapper.selectList(null);
        LocalDate quarterStart = getQuarterStart();

        // 一次性加载所有用户
        List<SysUser> allUsers = userMapper.selectList(null);
        Map<Long, List<SysUser>> teamUsers = allUsers.stream()
            .filter(u -> u.getTeamId() != null)
            .collect(java.util.stream.Collectors.groupingBy(SysUser::getTeamId));
        Map<Long, SysUser> userIdMap = allUsers.stream()
            .collect(java.util.stream.Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        // 一次性加载本季度所有记录
        List<BizDailyRecord> allRecords = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>().ge(BizDailyRecord::getRecordDate, quarterStart)
        );
        Map<Long, java.math.BigDecimal> userDgmvMap = allRecords.stream()
            .collect(java.util.stream.Collectors.groupingBy(BizDailyRecord::getUserId,
                java.util.stream.Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));

        List<Map<String, Object>> result = new ArrayList<>();
        for (SysTeam team : teams) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", team.getId());
            map.put("name", team.getName());
            map.put("leaderId", team.getLeaderId());
            List<SysUser> members = teamUsers.getOrDefault(team.getId(), List.of());
            map.put("memberCount", (long) members.size());

            // 团队目标 = 成员个人季度目标之和
            BigDecimal target = members.stream()
                .map(m -> m.getTargetDgmv() != null ? m.getTargetDgmv() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            map.put("targetDgmv", target);

            BigDecimal totalDgmv = members.stream()
                .map(m -> userDgmvMap.getOrDefault(m.getId(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            map.put("quarterDgmv", totalDgmv);
            BigDecimal rate = target.compareTo(BigDecimal.ZERO) > 0
                ? totalDgmv.multiply(BigDecimal.valueOf(100)).divide(target, 1, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            map.put("completionRate", rate);

            if (team.getLeaderId() != null) {
                SysUser leader = userIdMap.get(team.getLeaderId());
                map.put("leaderName", leader != null ? leader.getName() : null);
            }
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listPartners() {
        return userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "partner")
                .eq(SysUser::getStatus, "active")
        ).stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getName());
            return m;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public SysTeam createTeam(TeamRequest request) {
        // 校验负责人必须是合伙人
        if (request.getLeaderId() != null) {
            SysUser leader = userMapper.selectById(request.getLeaderId());
            if (leader == null || !"partner".equals(leader.getRole())) {
                throw new BusinessException("团队负责人必须是合伙人角色");
            }
        }
        SysTeam team = new SysTeam();
        team.setName(request.getName());
        team.setLeaderId(request.getLeaderId());
        team.setTargetDgmv(request.getTargetDgmv());
        teamMapper.insert(team);
        return team;
    }

    @Override
    public SysTeam updateTeam(Long id, TeamRequest request) {
        SysTeam team = teamMapper.selectById(id);
        if (team == null) throw new BusinessException("团队不存在");
        if (request.getName() != null) team.setName(request.getName());
        if (request.getLeaderId() != null) team.setLeaderId(request.getLeaderId());
        if (request.getTargetDgmv() != null) team.setTargetDgmv(request.getTargetDgmv());
        teamMapper.updateById(team);
        return team;
    }

    @Override
    public void addMember(Long teamId, Long userId) {
        SysTeam team = teamMapper.selectById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setTeamId(teamId);
        userMapper.updateById(user);
    }

    @Override
    public void deleteTeam(Long id) {
        SysTeam team = teamMapper.selectById(id);
        if (team == null) throw new BusinessException("团队不存在");
        // 检查是否还有成员
        Long memberCount = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getTeamId, id)
        );
        if (memberCount > 0) throw new BusinessException("团队下还有成员，无法删除");
        teamMapper.deleteById(id); // 逻辑删除
    }

    @Override
    public List<Map<String, Object>> getMembers(Long teamId) {
        List<SysUser> members = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getTeamId, teamId)
        );
        return members.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getName());
            m.put("role", u.getRole());
            m.put("level", u.getLevel());
            m.put("phone", u.getPhone());
            m.put("status", u.getStatus());
            m.put("targetDgmv", u.getTargetDgmv());
            return m;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean isTeamLeader(Long teamId, Long userId) {
        SysTeam team = teamMapper.selectById(teamId);
        return team != null && userId.equals(team.getLeaderId());
    }

    @Override
    public void removeMember(Long teamId, Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (!teamId.equals(user.getTeamId())) throw new BusinessException("该用户不在此团队");
        user.setTeamId(null);
        userMapper.updateById(user);
    }

    @Override
    public List<Map<String, Object>> getAvailableMembers(Long teamId) {
        List<SysUser> users = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .isNull(SysUser::getTeamId)
                .ne(SysUser::getRole, "admin")
        );
        return users.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getName());
            m.put("role", u.getRole());
            return m;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Map<String, Object> getTeamRecords(Long teamId, int page, int size) {
        List<SysUser> members = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getTeamId, teamId)
        );
        if (members.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("records", List.of());
            empty.put("total", 0L);
            empty.put("page", page);
            empty.put("size", size);
            return empty;
        }

        List<Long> memberIds = members.stream().map(SysUser::getId).collect(java.util.stream.Collectors.toList());
        Map<Long, String> nameMap = members.stream().collect(java.util.stream.Collectors.toMap(SysUser::getId, SysUser::getName, (a, b) -> a));

        // 只查最近7天
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        List<BizDailyRecord> allRecords = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>()
                .in(BizDailyRecord::getUserId, memberIds)
                .ge(BizDailyRecord::getRecordDate, weekAgo)
                .orderByDesc(BizDailyRecord::getRecordDate)
        );

        // 按 userId + recordDate 分组汇总
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (BizDailyRecord r : allRecords) {
            String key = r.getUserId() + "_" + r.getRecordDate();
            grouped.computeIfAbsent(key, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("userId", r.getUserId());
                m.put("name", nameMap.getOrDefault(r.getUserId(), String.valueOf(r.getUserId())));
                m.put("recordDate", r.getRecordDate());
                m.put("gmv", BigDecimal.ZERO);
                m.put("refund", BigDecimal.ZERO);
                m.put("dgmv", BigDecimal.ZERO);
                return m;
            });
            Map<String, Object> m = grouped.get(key);
            m.put("gmv", ((BigDecimal) m.get("gmv")).add(r.getGmv() != null ? r.getGmv() : BigDecimal.ZERO));
            m.put("refund", ((BigDecimal) m.get("refund")).add(r.getRefund() != null ? r.getRefund() : BigDecimal.ZERO));
            m.put("dgmv", ((BigDecimal) m.get("dgmv")).add(r.getDgmv() != null ? r.getDgmv() : BigDecimal.ZERO));
        }

        // 按日期倒序排列
        List<Map<String, Object>> sortedList = new ArrayList<>(grouped.values());
        sortedList.sort((a, b) -> ((LocalDate) b.get("recordDate")).compareTo((LocalDate) a.get("recordDate")));

        // 手动分页
        long total = sortedList.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, sortedList.size());
        List<Map<String, Object>> pageList = from < sortedList.size() ? sortedList.subList(from, to) : List.of();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("records", pageList);
        res.put("total", total);
        res.put("page", page);
        res.put("size", size);
        return res;
    }

    private LocalDate getQuarterStart() {
        LocalDate now = LocalDate.now();
        int month = ((now.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(now.getYear(), month, 1);
    }
}
