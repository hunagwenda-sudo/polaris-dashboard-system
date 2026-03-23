package com.saleshub.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.saleshub.common.Result;
import com.saleshub.dto.RecordSubmitRequest;
import com.saleshub.entity.BizDailyRecord;
import com.saleshub.security.JwtUserDetails;
import com.saleshub.service.AuditService;
import com.saleshub.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;
    private final AuditService auditService;
    private final com.saleshub.service.DashboardService dashboardService;
    private final com.saleshub.service.UserPlatformService userPlatformService;
    private final com.saleshub.mapper.SysPlatformAccountMapper platformAccountMapper;

    @GetMapping("/check")
    @PreAuthorize("hasAuthority('record:create')")
    public Result<Boolean> check(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = (Long) auth.getPrincipal();
        boolean submitted = recordService.hasSubmitted(userId, date);
        return Result.ok(submitted);
    }

    /** 获取当前用户被分配的渠道+账号（用于数据录入页面） */
    @GetMapping("/my-platforms")
    @PreAuthorize("hasAuthority('record:create')")
    public Result<?> myPlatforms(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(buildPlatformList(userId));
    }

    /** 管理员查看指定用户的渠道分配（用于业绩补录） */
    @GetMapping("/user-platforms/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> userPlatforms(@PathVariable Long userId) {
        return Result.ok(buildPlatformList(userId));
    }

    private List<Map<String, Object>> buildPlatformList(Long userId) {
        var assignments = userPlatformService.listByUserId(userId);
        if (assignments.isEmpty()) return List.of();
        var accountIds = assignments.stream().map(a -> a.getAccountId()).toList();
        var accounts = platformAccountMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.saleshub.entity.SysPlatformAccount>()
                .in(com.saleshub.entity.SysPlatformAccount::getId, accountIds)
        );
        var accountMap = accounts.stream().collect(java.util.stream.Collectors.toMap(
            com.saleshub.entity.SysPlatformAccount::getId, a -> a));
        return assignments.stream().map(a -> {
            var acc = accountMap.get(a.getAccountId());
            return Map.<String, Object>of(
                "platformCode", a.getPlatformCode(),
                "accountId", a.getAccountId(),
                "accountName", acc != null ? acc.getAccountName() : ""
            );
        }).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('record:create')")
    public Result<?> submit(Authentication auth, @Valid @RequestBody RecordSubmitRequest request) {
        Long userId = (Long) auth.getPrincipal();
        recordService.submitRecords(userId, request);
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log(userId, details.getUsername(), "CREATE", "BizDailyRecord", null,
            "date=" + request.getRecordDate() + ", items=" + request.getItems().size());
        dashboardService.evictCache();
        return Result.ok();
    }

    /** 管理员业绩补录：可为指定员工补录任意日期的业绩 */
    @PostMapping("/backfill")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> backfill(Authentication auth, @Valid @RequestBody RecordSubmitRequest request) {
        Long targetUserId = request.getUserId();
        if (targetUserId == null) throw new com.saleshub.common.BusinessException("请选择员工");
        if (request.getRecordDate() == null) throw new com.saleshub.common.BusinessException("请选择日期");
        if (request.getRecordDate().isAfter(LocalDate.now().minusDays(1))) {
            throw new com.saleshub.common.BusinessException("只能补录昨天及之前的业绩");
        }
        recordService.submitRecordsNoDateCheck(targetUserId, request);
        var details = (JwtUserDetails) auth.getDetails();
        auditService.log((Long) auth.getPrincipal(), details.getUsername(), "BACKFILL", "BizDailyRecord", null,
            "targetUser=" + targetUserId + ", date=" + request.getRecordDate() + ", items=" + request.getItems().size());
        dashboardService.evictCache();
        return Result.ok();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('record:view')")
    public Result<IPage<BizDailyRecord>> list(
            Authentication auth,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        // 运营只能查自己的记录
        var details = (JwtUserDetails) auth.getDetails();
        if ("sales".equals(details.getRole())) {
            userId = details.getUserId();
            keyword = null;
        }
        return Result.ok(recordService.listRecords(userId, keyword, startDate, endDate, sortField, sortOrder, page, size));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('record:view')")
    public Result<Map<String, Object>> summary(
            Authentication auth,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        var details = (JwtUserDetails) auth.getDetails();
        if ("sales".equals(details.getRole())) {
            userId = details.getUserId();
            keyword = null;
        }
        BigDecimal total = recordService.sumDgmv(userId, keyword, startDate, endDate);
        return Result.ok(Map.of("totalDgmv", total));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('record:view_all')")
    public Result<?> delete(@PathVariable Long id) {
        recordService.deleteRecord(id);
        dashboardService.evictCache();
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object gmvObj = body.get("gmv");
        Object refundObj = body.get("refund");
        if (gmvObj == null || refundObj == null) throw new com.saleshub.common.BusinessException("gmv 和 refund 不能为空");
        java.math.BigDecimal gmv = new java.math.BigDecimal(gmvObj.toString());
        java.math.BigDecimal refund = new java.math.BigDecimal(refundObj.toString());
        if (gmv.compareTo(java.math.BigDecimal.ZERO) < 0) throw new com.saleshub.common.BusinessException("GMV 不能为负");
        if (refund.compareTo(java.math.BigDecimal.ZERO) < 0) throw new com.saleshub.common.BusinessException("退款不能为负");
        String accountNote = body.containsKey("accountNote") ? (String) body.get("accountNote") : null;
        recordService.updateRecord(id, gmv, refund, accountNote);
        dashboardService.evictCache();
        return Result.ok();
    }

    @GetMapping("/unfilled")
    @PreAuthorize("hasAuthority('record:view_all')")
    public Result<?> unfilled(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(recordService.getUnfilledUsers(date));
    }
}
