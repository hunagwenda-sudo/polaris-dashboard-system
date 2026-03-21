package com.saleshub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saleshub.common.BusinessException;
import com.saleshub.dto.ServiceRecordSubmitRequest;
import com.saleshub.entity.BizServiceRecord;
import com.saleshub.entity.SysUser;
import com.saleshub.mapper.BizServiceRecordMapper;
import com.saleshub.mapper.SysUserMapper;
import com.saleshub.service.ServiceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceRecordServiceImpl implements ServiceRecordService {

    private final BizServiceRecordMapper recordMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public void submitRecords(Long userId, ServiceRecordSubmitRequest request) {
        if (request.getRecordDate() != null && request.getRecordDate().isAfter(LocalDate.now())) {
            throw new BusinessException("日期不能大于今天");
        }
        for (var item : request.getItems()) {
            Long count = recordMapper.selectCount(
                new LambdaQueryWrapper<BizServiceRecord>()
                    .eq(BizServiceRecord::getUserId, userId)
                    .eq(BizServiceRecord::getRecordDate, request.getRecordDate())
                    .eq(BizServiceRecord::getPlatform, item.getPlatform())
                    .eq(BizServiceRecord::getShift, item.getShift())
            );
            if (count > 0) {
                String shiftLabel = "morning".equals(item.getShift()) ? "早班" : "晚班";
                throw new BusinessException("日期 " + request.getRecordDate() + " 平台 " + item.getPlatform() + " " + shiftLabel + " 已提交过");
            }
            BizServiceRecord record = new BizServiceRecord();
            record.setUserId(userId);
            record.setRecordDate(request.getRecordDate());
            record.setPlatform(item.getPlatform());
            record.setShift(item.getShift());
            record.setReceptionCount(item.getReceptionCount() != null ? item.getReceptionCount() : 0);
            record.setReplyRate(item.getReplyRate());
            record.setPraiseRate(item.getPraiseRate());
            record.setCreatedAt(LocalDateTime.now());
            recordMapper.insert(record);
        }
    }

    @Override
    public IPage<BizServiceRecord> listRecords(Long userId, LocalDate startDate, LocalDate endDate, int page, int size) {
        var wrapper = new LambdaQueryWrapper<BizServiceRecord>();
        if (userId != null) wrapper.eq(BizServiceRecord::getUserId, userId);
        if (startDate != null) wrapper.ge(BizServiceRecord::getRecordDate, startDate);
        if (endDate != null) wrapper.le(BizServiceRecord::getRecordDate, endDate);
        wrapper.orderByDesc(BizServiceRecord::getRecordDate).orderByDesc(BizServiceRecord::getId);
        return recordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<BizServiceRecord> listAllRecords(String keyword, LocalDate startDate, LocalDate endDate, String sortField, String sortOrder, int page, int size) {
        var wrapper = new LambdaQueryWrapper<BizServiceRecord>();
        if (startDate != null) wrapper.ge(BizServiceRecord::getRecordDate, startDate);
        if (endDate != null) wrapper.le(BizServiceRecord::getRecordDate, endDate);

        if (StringUtils.hasText(keyword)) {
            List<SysUser> matchedUsers = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().like(SysUser::getName, keyword).select(SysUser::getId)
            );
            Set<Long> matchedIds = matchedUsers.stream().map(SysUser::getId).collect(Collectors.toSet());
            if (matchedIds.isEmpty()) return new Page<>(page, size);
            wrapper.in(BizServiceRecord::getUserId, matchedIds);
        }

        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if ("receptionCount".equals(sortField)) {
            wrapper.orderBy(true, asc, BizServiceRecord::getReceptionCount);
        } else if ("replyRate".equals(sortField)) {
            wrapper.orderBy(true, asc, BizServiceRecord::getReplyRate);
        } else if ("praiseRate".equals(sortField)) {
            wrapper.orderBy(true, asc, BizServiceRecord::getPraiseRate);
        } else if ("date".equals(sortField)) {
            wrapper.orderBy(true, asc, BizServiceRecord::getRecordDate);
        } else {
            wrapper.orderByDesc(BizServiceRecord::getRecordDate).orderByDesc(BizServiceRecord::getId);
        }

        return recordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Map<String, Object>> getUnfilledUsers(LocalDate date) {
        List<SysUser> allService = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "service")
                .eq(SysUser::getStatus, "active")
                .eq(SysUser::getRemindEnabled, 1)
                .select(SysUser::getId, SysUser::getName)
        );
        if (allService.isEmpty()) return List.of();

        Set<Long> allIds = allService.stream().map(SysUser::getId).collect(Collectors.toSet());
        List<BizServiceRecord> filled = recordMapper.selectList(
            new LambdaQueryWrapper<BizServiceRecord>()
                .eq(BizServiceRecord::getRecordDate, date)
                .in(BizServiceRecord::getUserId, allIds)
                .select(BizServiceRecord::getUserId)
        );
        Set<Long> filledIds = filled.stream().map(BizServiceRecord::getUserId).collect(Collectors.toSet());

        return allService.stream()
            .filter(u -> !filledIds.contains(u.getId()))
            .map(u -> {
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", u.getId());
                m.put("name", u.getName());
                return m;
            })
            .collect(Collectors.toList());
    }
}
