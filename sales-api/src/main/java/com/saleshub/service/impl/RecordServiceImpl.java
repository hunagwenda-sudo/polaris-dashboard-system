package com.saleshub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saleshub.common.BusinessException;
import com.saleshub.dto.RecordSubmitRequest;
import com.saleshub.entity.BizDailyRecord;
import com.saleshub.entity.SysUser;
import com.saleshub.mapper.BizDailyRecordMapper;
import com.saleshub.mapper.SysUserMapper;
import com.saleshub.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final BizDailyRecordMapper recordMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public void submitRecords(Long userId, RecordSubmitRequest request) {
        log.info("提交业绩: userId={}, date={}, items={}", userId, request.getRecordDate(), request.getItems().size());
        if (request.getRecordDate() != null && !request.getRecordDate().isBefore(LocalDate.now())) {
            throw new BusinessException("只能录入昨天及之前的业绩");
        }
        doSubmit(userId, request);
    }

    @Override
    @Transactional
    public void submitRecordsNoDateCheck(Long userId, RecordSubmitRequest request) {
        log.info("补录业绩(无日期校验): userId={}, date={}", userId, request.getRecordDate());
        doSubmit(userId, request);
    }

    private void doSubmit(Long userId, RecordSubmitRequest request) {
        for (var item : request.getItems()) {
            // 按 userId + recordDate + platform + accountId 去重
            Long accountId = item.getAccountId();
            String note = item.getAccountNote() == null ? "" : item.getAccountNote().trim();
            var dupWrapper = new LambdaQueryWrapper<BizDailyRecord>()
                    .eq(BizDailyRecord::getUserId, userId)
                    .eq(BizDailyRecord::getRecordDate, request.getRecordDate())
                    .eq(BizDailyRecord::getPlatform, item.getPlatform());
            if (accountId != null) {
                dupWrapper.eq(BizDailyRecord::getAccountId, accountId);
            } else {
                dupWrapper.eq(BizDailyRecord::getAccountNote, note);
            }
            Long existCount = recordMapper.selectCount(dupWrapper);
            if (existCount > 0) {
                String label = item.getPlatform() + (note.isEmpty() ? "" : "(" + note + ")");
                throw new BusinessException("渠道 " + label + " 在 " + request.getRecordDate() + " 已提交过");
            }
            BizDailyRecord record = new BizDailyRecord();
            record.setUserId(userId);
            record.setRecordDate(request.getRecordDate());
            record.setPlatform(item.getPlatform());
            record.setAccountId(accountId);
            record.setAccountNote(note);
            record.setGmv(item.getGmv());
            record.setRefund(item.getRefund());
            record.setDgmv(item.getGmv().subtract(item.getRefund()));
            record.setCreatedAt(LocalDateTime.now());
            recordMapper.insert(record);
        }
    }

    @Override
    public boolean hasSubmitted(Long userId, LocalDate date) {
        return recordMapper.selectCount(
            new LambdaQueryWrapper<BizDailyRecord>()
                .eq(BizDailyRecord::getUserId, userId)
                .eq(BizDailyRecord::getRecordDate, date)
        ) > 0;
    }

    /** 公共过滤逻辑：按 userId/日期范围/姓名关键字构建 wrapper，返回 false 表示无结果（keyword 无匹配） */
    private boolean applyFilters(LambdaQueryWrapper<BizDailyRecord> wrapper,
                                  Long userId, String keyword, LocalDate startDate, LocalDate endDate) {
        if (userId != null) wrapper.eq(BizDailyRecord::getUserId, userId);
        if (startDate != null) wrapper.ge(BizDailyRecord::getRecordDate, startDate);
        if (endDate != null) wrapper.le(BizDailyRecord::getRecordDate, endDate);
        if (StringUtils.hasText(keyword)) {
            List<SysUser> matched = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().like(SysUser::getName, keyword).select(SysUser::getId)
            );
            Set<Long> matchedIds = matched.stream().map(SysUser::getId).collect(Collectors.toSet());
            if (matchedIds.isEmpty()) return false;
            wrapper.in(BizDailyRecord::getUserId, matchedIds);
        }
        return true;
    }

    @Override
    public IPage<BizDailyRecord> listRecords(Long userId, String keyword, LocalDate startDate, LocalDate endDate, String sortField, String sortOrder, int page, int size) {
        var wrapper = new LambdaQueryWrapper<BizDailyRecord>();
        if (!applyFilters(wrapper, userId, keyword, startDate, endDate)) {
            return new Page<>(page, size);
        }
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        switch (sortField == null ? "" : sortField) {
            case "gmv"    -> wrapper.orderBy(true, asc, BizDailyRecord::getGmv);
            case "refund" -> wrapper.orderBy(true, asc, BizDailyRecord::getRefund);
            case "dgmv"   -> wrapper.orderBy(true, asc, BizDailyRecord::getDgmv);
            case "date"   -> wrapper.orderBy(true, asc, BizDailyRecord::getRecordDate);
            default       -> wrapper.orderByDesc(BizDailyRecord::getRecordDate).orderByDesc(BizDailyRecord::getId);
        }
        IPage<BizDailyRecord> result = recordMapper.selectPage(new Page<>(page, size), wrapper);
        // 批量填充 userName，避免前端额外请求
        if (!result.getRecords().isEmpty()) {
            Set<Long> userIds = result.getRecords().stream().map(BizDailyRecord::getUserId).collect(Collectors.toSet());
            Map<Long, String> nameMap = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds).select(SysUser::getId, SysUser::getName)
            ).stream().collect(Collectors.toMap(SysUser::getId, u -> u.getName() != null ? u.getName() : ""));
            result.getRecords().forEach(r -> r.setUserName(nameMap.get(r.getUserId())));
        }
        return result;
    }

    @Override
    public BigDecimal sumDgmv(Long userId, String keyword, LocalDate startDate, LocalDate endDate) {
        var wrapper = new LambdaQueryWrapper<BizDailyRecord>();
        if (!applyFilters(wrapper, userId, keyword, startDate, endDate)) return BigDecimal.ZERO;
        BigDecimal result = recordMapper.sumDgmv(wrapper);
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public void updateRecord(Long id, java.math.BigDecimal gmv, java.math.BigDecimal refund, String accountNote) {
        log.info("更新业绩记录: id={}, gmv={}, refund={}", id, gmv, refund);
        BizDailyRecord record = recordMapper.selectById(id);
        if (record == null) throw new BusinessException("记录不存在");
        record.setGmv(gmv);
        record.setRefund(refund);
        record.setDgmv(gmv.subtract(refund));
        if (accountNote != null) record.setAccountNote(accountNote.trim());
        recordMapper.updateById(record);
    }

    @Override
    public void deleteRecord(Long id) {
        log.info("删除业绩记录: id={}", id);
        BizDailyRecord record = recordMapper.selectById(id);
        if (record == null) throw new BusinessException("记录不存在");
        recordMapper.deleteById(id); // 逻辑删除
    }

    @Override
    public List<Map<String, Object>> getUnfilledUsers(LocalDate date) {
        // 查出所有需要填报的用户（sales + partner，排除 admin 和 service，且 remind_enabled=1）
        List<SysUser> allUsers = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getRole, "sales", "partner")
                .eq(SysUser::getStatus, "active")
                .eq(SysUser::getRemindEnabled, 1)
                .select(SysUser::getId, SysUser::getName, SysUser::getTeamId)
        );
        if (allUsers.isEmpty()) return List.of();

        // 查出当天已填报的 userId 集合
        Set<Long> allIds = allUsers.stream().map(SysUser::getId).collect(Collectors.toSet());
        List<BizDailyRecord> filled = recordMapper.selectList(
            new LambdaQueryWrapper<BizDailyRecord>()
                .eq(BizDailyRecord::getRecordDate, date)
                .in(BizDailyRecord::getUserId, allIds)
                .select(BizDailyRecord::getUserId)
        );
        Set<Long> filledIds = filled.stream().map(BizDailyRecord::getUserId).collect(Collectors.toSet());

        return allUsers.stream()
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
