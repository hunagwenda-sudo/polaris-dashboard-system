package com.saleshub.controller;

import com.saleshub.common.Result;
import com.saleshub.dto.ServiceRecordSubmitRequest;
import com.saleshub.entity.SysPlatformShop;
import com.saleshub.mapper.SysPlatformShopMapper;
import com.saleshub.mapper.SysUserShopMapper;
import com.saleshub.service.ServiceRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/service-records")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ServiceRecordController {

    private final ServiceRecordService serviceRecordService;
    private final SysUserShopMapper userShopMapper;
    private final SysPlatformShopMapper platformShopMapper;

    @PostMapping
    @PreAuthorize("hasRole('SERVICE')")
    public Result<?> submit(@AuthenticationPrincipal Long userId,
                            @RequestBody ServiceRecordSubmitRequest request) {
        log.info("客服提交记录: userId={}, date={}", userId, request.getRecordDate());
        serviceRecordService.submitRecords(userId, request);
        return Result.ok();
    }

    /** 客服自己查看自己的记录 */
    @GetMapping("/mine")
    public Result<?> mine(@AuthenticationPrincipal Long userId,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(serviceRecordService.listRecords(userId, startDate, endDate, page, size));
    }

    /** 管理员/合伙人查看所有客服记录，客服只看自己 */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER', 'SERVICE')")
    public Result<?> list(Authentication auth,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                          @RequestParam(required = false) String sortField,
                          @RequestParam(required = false) String sortOrder,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "50") int size) {
        var details = (com.saleshub.security.JwtUserDetails) auth.getDetails();
        if ("service".equals(details.getRole())) {
            // 客服只能看自己的记录
            return Result.ok(serviceRecordService.listRecords(details.getUserId(), startDate, endDate, page, size));
        }
        return Result.ok(serviceRecordService.listAllRecords(keyword, startDate, endDate, sortField, sortOrder, page, size));
    }

    @GetMapping("/unfilled")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
    public Result<?> unfilled(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(serviceRecordService.getUnfilledUsers(date));
    }

    /** 修改客服记录（客服改自己的，管理员/合伙人改任何人的） */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTNER', 'SERVICE')")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        var details = (com.saleshub.security.JwtUserDetails) auth.getDetails();
        serviceRecordService.updateRecord(id, body, details.getUserId(), details.getRole());
        return Result.ok();
    }

    /** 客服获取自己分配的店铺 */
    @GetMapping("/my-shops")
    @PreAuthorize("hasRole('SERVICE')")
    public Result<?> myShops(@AuthenticationPrincipal Long userId) {
        var assignments = userShopMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.saleshub.entity.SysUserShop>()
                .eq(com.saleshub.entity.SysUserShop::getUserId, userId)
        );
        if (assignments.isEmpty()) return Result.ok(List.of());
        var shopIds = assignments.stream().map(a -> a.getShopId()).toList();
        var shops = platformShopMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPlatformShop>()
                .in(SysPlatformShop::getId, shopIds)
        );
        var shopMap = shops.stream().collect(Collectors.toMap(SysPlatformShop::getId, s -> s));
        var result = assignments.stream().map(a -> {
            var shop = shopMap.get(a.getShopId());
            return Map.<String, Object>of(
                "platformCode", a.getPlatformCode(),
                "shopId", a.getShopId(),
                "shopName", shop != null ? shop.getShopName() : ""
            );
        }).toList();
        return Result.ok(result);
    }
}
