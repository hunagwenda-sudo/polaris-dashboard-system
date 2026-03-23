package com.saleshub.controller;

import com.saleshub.common.Result;
import com.saleshub.entity.SysPlatformAccount;
import com.saleshub.service.PlatformAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return Result.ok(accountService.create(account));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PARTNER')")
    public Result<SysPlatformAccount> update(@PathVariable Long id, @RequestBody SysPlatformAccount account) {
        return Result.ok(accountService.update(id, account));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PARTNER')")
    public Result<?> delete(@PathVariable Long id) {
        accountService.delete(id);
        return Result.ok();
    }
}
