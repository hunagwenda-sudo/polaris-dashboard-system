package com.saleshub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.common.BusinessException;
import com.saleshub.dto.GroupRequest;
import com.saleshub.entity.BizDailyRecord;
import com.saleshub.entity.SysGroup;
import com.saleshub.entity.SysUser;
import com.saleshub.mapper.BizDailyRecordMapper;
import com.saleshub.mapper.SysGroupMapper;
import com.saleshub.mapper.SysUserMapper;
import com.saleshub.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final SysGroupMapper groupMapper;
    private final SysUserMapper userMapper;
    private final BizDailyRecordMapper recordMapper;

    @Override
    public List<Map<String, Object>> listGroups() {
        List<SysGroup> groups = groupMapper.selectList(null);

        List<SysUser> allUsers = userMapper.selectList(null);
        Map<Long, SysUser> userIdMap = allUsers.stream()
            .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));
        Map<Long, List<SysUser>> groupUsers = allUsers.stream()
            .filter(u -> u.getGroupId() != null)
            .collect(Collectors.groupingBy(SysUser::getGroupId));

        // 季度业绩
        LocalDate quarterStart = getQuarterStart();
        List<BizDailyRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>().ge(BizDailyRecord::getRecordDate, quarterStart)
        );
        Map<Long, BigDecimal> userDgmvMap = records.stream()
            .collect(Collectors.groupingBy(BizDailyRecord::getUserId,
                Collectors.reducing(BigDecimal.ZERO, BizDailyRecord::getDgmv, BigDecimal::add)));

        List<Map<String, Object>> result = new ArrayList<>();
        for (SysGroup g : groups) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", g.getId());
            map.put("name", g.getName());
            map.put("leaderId", g.getLeaderId());
            map.put("targetDgmv", g.getTargetDgmv());

            List<SysUser> members = groupUsers.getOrDefault(g.getId(), List.of());
            map.put("memberCount", (long) members.size());

            BigDecimal totalDgmv = members.stream()
                .map(m -> userDgmvMap.getOrDefault(m.getId(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            map.put("quarterDgmv", totalDgmv);

            BigDecimal target = g.getTargetDgmv() != null ? g.getTargetDgmv() : BigDecimal.ZERO;
            BigDecimal rate = target.compareTo(BigDecimal.ZERO) > 0
                ? totalDgmv.multiply(BigDecimal.valueOf(100)).divide(target, 1, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            map.put("completionRate", rate);

            if (g.getLeaderId() != null) {
                SysUser leader = userIdMap.get(g.getLeaderId());
                map.put("leaderName", leader != null ? leader.getName() : null);
            }
            result.add(map);
        }
        return result;
    }

    @Override
    public SysGroup createGroup(GroupRequest request) {
        if (request.getLeaderId() == null) {
            throw new BusinessException("请选择组长");
        }
        // 检查组长是否已在其他小组
        SysUser leader = userMapper.selectById(request.getLeaderId());
        if (leader == null) throw new BusinessException("组长用户不存在");
        if (leader.getGroupId() != null) throw new BusinessException("该用户已在其他小组中，不能担任组长");

        SysGroup group = new SysGroup();
        group.setName(request.getName());
        group.setLeaderId(request.getLeaderId());
        group.setTargetDgmv(request.getTargetDgmv());
        groupMapper.insert(group);

        // 自动将组长加入该小组
        leader.setGroupId(group.getId());
        userMapper.updateById(leader);

        return group;
    }

    @Override
    public SysGroup updateGroup(Long id, GroupRequest request) {
        SysGroup group = groupMapper.selectById(id);
        if (group == null) throw new BusinessException("小组不存在");
        if (request.getName() != null) group.setName(request.getName());
        if (request.getLeaderId() != null) group.setLeaderId(request.getLeaderId());
        if (request.getTargetDgmv() != null) group.setTargetDgmv(request.getTargetDgmv());
        groupMapper.updateById(group);
        return group;
    }

    @Override
    public void deleteGroup(Long id) {
        SysGroup group = groupMapper.selectById(id);
        if (group == null) throw new BusinessException("小组不存在");
        // 清空该小组所有成员的 groupId
        List<SysUser> members = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getGroupId, id)
        );
        for (SysUser m : members) {
            m.setGroupId(null);
            userMapper.updateById(m);
        }
        groupMapper.deleteById(id);
    }

    @Override
    public void addMember(Long groupId, Long userId) {
        SysGroup group = groupMapper.selectById(groupId);
        if (group == null) throw new BusinessException("小组不存在");
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (user.getGroupId() != null) throw new BusinessException("该用户已在其他小组中");
        user.setGroupId(groupId);
        userMapper.updateById(user);
    }

    @Override
    public void removeMember(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setGroupId(null);
        userMapper.updateById(user);
    }

    @Override
    public List<Map<String, Object>> getMembers(Long groupId) {
        List<SysUser> members = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getGroupId, groupId)
                .select(SysUser::getId, SysUser::getName, SysUser::getRole)
        );
        return members.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getName());
            m.put("role", u.getRole());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAvailableMembers(Long groupId) {
        // 返回不在任何小组的非管理员用户
        List<SysUser> users = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .isNull(SysUser::getGroupId)
                .ne(SysUser::getRole, "admin")
                .select(SysUser::getId, SysUser::getName, SysUser::getRole)
        );
        return users.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getName());
            m.put("role", u.getRole());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isGroupLeader(Long groupId, Long userId) {
        SysGroup group = groupMapper.selectById(groupId);
        return group != null && userId.equals(group.getLeaderId());
    }

    private LocalDate getQuarterStart() {
        LocalDate now = LocalDate.now();
        int month = ((now.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(now.getYear(), month, 1);
    }
}
