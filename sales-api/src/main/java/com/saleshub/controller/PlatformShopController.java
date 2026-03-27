package com.saleshub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.common.BusinessException;
import com.saleshub.common.Result;
import com.saleshub.entity.SysPlatformShop;
import com.saleshub.mapper.SysPlatformShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/platform-shops")
@RequiredArgsConstructor
public class PlatformShopController {

    private final SysPlatformShopMapper shopMapper;

    @GetMapping
    public Result<List<SysPlatformShop>> listAll() {
        return Result.ok(shopMapper.selectList(
            new LambdaQueryWrapper<SysPlatformShop>()
                .orderByAsc(SysPlatformShop::getPlatformCode)
                .orderByAsc(SysPlatformShop::getSort)
        ));
    }

    @GetMapping("/{platformCode}")
    public Result<List<SysPlatformShop>> listByPlatform(@PathVariable String platformCode) {
        return Result.ok(shopMapper.selectList(
            new LambdaQueryWrapper<SysPlatformShop>()
                .eq(SysPlatformShop::getPlatformCode, platformCode)
                .orderByAsc(SysPlatformShop::getSort)
        ));
    }

    @PostMapping
    public Result<SysPlatformShop> create(@RequestBody SysPlatformShop shop) {
        if (shop.getStatus() == null) shop.setStatus("active");
        if (shop.getSort() == null) shop.setSort(0);
        shop.setCreatedAt(LocalDateTime.now());
        shopMapper.insert(shop);
        return Result.ok(shop);
    }

    @PutMapping("/{id}")
    public Result<SysPlatformShop> update(@PathVariable Long id, @RequestBody SysPlatformShop shop) {
        SysPlatformShop existing = shopMapper.selectById(id);
        if (existing == null) throw new BusinessException("店铺不存在");
        if (shop.getShopName() != null) existing.setShopName(shop.getShopName());
        if (shop.getSort() != null) existing.setSort(shop.getSort());
        if (shop.getStatus() != null) existing.setStatus(shop.getStatus());
        shopMapper.updateById(existing);
        return Result.ok(existing);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        SysPlatformShop existing = shopMapper.selectById(id);
        if (existing == null) throw new BusinessException("店铺不存在");
        shopMapper.deleteById(id);
        return Result.ok();
    }
}
