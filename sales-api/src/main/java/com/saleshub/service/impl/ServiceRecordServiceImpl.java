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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRecordServiceImpl implements ServiceRecordService {

    private final BizServiceRecordMapper recordMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public void submitRecords(Long userId, ServiceRecordSubmitRequest request) {
        log.info("客服提交记录: userId={}, date={}, items={}", userId, request.getRecordDate(), request.getItems().size());
        if (request.getRecordDate() != null && request.getRecordDate().isAfter(LocalDate.now())) {
            throw new BusinessException("日期不能大于今天");
        }
        // 同一人同一天只能提交一次
        Long existing = recordMapper.selectCount(
            new LambdaQueryWrapper<BizServiceRecord>()
                .eq(BizServiceRecord::getUserId, userId)
                .eq(BizServiceRecord::getRecordDate, request.getRecordDate())
        );
        if (existing > 0) {
            throw new BusinessException(request.getRecordDate() + " 已提交过，如需修改请在客服业绩查看中编辑");
        }
        for (var item : request.getItems()) {
            BizServiceRecord record = new BizServiceRecord();
            record.setUserId(userId);
            record.setRecordDate(request.getRecordDate());
            record.setPlatform(item.getPlatform());
            record.setShopId(item.getShopId());
            record.setShopNote(item.getShopNote());
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

    @Override
    public void updateRecord(Long id, Map<String, Object> body, Long currentUserId, String currentRole) {
        BizServiceRecord record = recordMapper.selectById(id);
        if (record == null) throw new BusinessException("记录不存在");
        // 客服只能改自己的
        if ("service".equals(currentRole) && !record.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权修改他人记录");
        }
        if (body.containsKey("shift")) record.setShift((String) body.get("shift"));
        if (body.containsKey("receptionCount")) record.setReceptionCount(((Number) body.get("receptionCount")).intValue());
        if (body.containsKey("replyRate")) record.setReplyRate(new java.math.BigDecimal(body.get("replyRate").toString()));
        if (body.containsKey("praiseRate")) record.setPraiseRate(new java.math.BigDecimal(body.get("praiseRate").toString()));
        record.setUpdatedAt(LocalDateTime.now());
        recordMapper.updateById(record);
    }
}
