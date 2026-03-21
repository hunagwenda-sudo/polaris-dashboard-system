package com.saleshub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.common.Result;
import com.saleshub.entity.SysDict;
import com.saleshub.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final SysDictMapper dictMapper;

    /** 获取 webhook 配置 */
    @GetMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, String>> get(@PathVariable String code) {
        SysDict dict = dictMapper.selectOne(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "webhook")
                .eq(SysDict::getCode, code)
        );
        String url = dict != null ? dict.getLabel() : "";
        return Result.ok(Map.of("url", url));
    }

    /** 保存 webhook 配置（upsert） */
    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> save(@PathVariable String code, @RequestBody Map<String, String> body) {
        String url = body.getOrDefault("url", "");
        SysDict dict = dictMapper.selectOne(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "webhook")
                .eq(SysDict::getCode, code)
        );
        if (dict == null) {
            dict = new SysDict();
            dict.setType("webhook");
            dict.setCode(code);
            dict.setLabel(url);
            dict.setSort(0);
            dict.setStatus("active");
            dictMapper.insert(dict);
        } else {
            dict.setLabel(url);
            dictMapper.updateById(dict);
        }
        return Result.ok();
    }
}
