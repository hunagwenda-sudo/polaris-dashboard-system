package com.saleshub.controller;

import com.saleshub.common.Result;
import com.saleshub.dto.ServiceRecordSubmitRequest;
import com.saleshub.service.ServiceRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/service-records")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ServiceRecordController {

    private final ServiceRecordService serviceRecordService;

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
}
