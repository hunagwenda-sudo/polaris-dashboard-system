package com.saleshub.controller;

import com.saleshub.common.Result;
import com.saleshub.entity.SysPlatformAccount;
import com.saleshub.service.PlatformAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/platform-accounts")
@RequiredArgsConstructor
public class PlatformAccountController {

    private final PlatformAccountService accountService;

    @GetMapping
    public Result<List<SysPlatformAccount>> listAll() {
        return Result.ok(accountService.listAll());
    }

    @GetMapping("/{platformCode}")
    public Result<List<SysPlatformAccount>> listByPlatform(@PathVariable String platformCode) {
        return Result.ok(accountService.listByPlatform(platformCode));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PARTNER')")
    public Result<SysPlatformAccount> create(@RequestBody SysPlatformAccount account) {
        log.info("创建平台账号: platform={}, name={}", account.getPlatformCode(), account.getAccountName());
        return Result.ok(accountService.create(account));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PARTNER')")
    public Result<SysPlatformAccount> update(@PathVariable Long id, @RequestBody SysPlatformAccount account) {
        log.info("更新平台账号: id={}", id);
        return Result.ok(accountService.update(id, account));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PARTNER')")
    public Result<?> delete(@PathVariable Long id) {
        log.info("删除平台账号: id={}", id);
        accountService.delete(id);
        return Result.ok();
    }
}
