package com.saleshub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saleshub.common.Result;
import com.saleshub.entity.SysDict;
import com.saleshub.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final SysDictMapper dictMapper;
    private final ObjectMapper objectMapper;

    /** 获取 webhook 配置（返回 urls 数组） */
    @GetMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> get(@PathVariable String code) {
        SysDict dict = dictMapper.selectOne(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "webhook")
                .eq(SysDict::getCode, code)
        );
        List<String> urls = parseUrls(dict);
        return Result.ok(Map.of("urls", urls));
    }

    /** 保存 webhook 配置（接收 urls 数组，upsert） */
    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> save(@PathVariable String code, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> urls = (List<String>) body.getOrDefault("urls", Collections.emptyList());
        // 过滤空字符串
        List<String> cleaned = urls.stream().filter(u -> u != null && !u.isBlank()).toList();
        String json;
        try {
            json = objectMapper.writeValueAsString(cleaned);
        } catch (Exception e) {
            json = "[]";
        }

        SysDict dict = dictMapper.selectOne(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "webhook")
                .eq(SysDict::getCode, code)
        );
        if (dict == null) {
            dict = new SysDict();
            dict.setType("webhook");
            dict.setCode(code);
            dict.setLabel(json);
            dict.setSort(0);
            dict.setStatus("active");
            dictMapper.insert(dict);
        } else {
            dict.setLabel(json);
            dictMapper.updateById(dict);
        }
        return Result.ok();
    }

    /** 解析 label 字段为 URL 列表（兼容旧的单 URL 字符串格式） */
    private List<String> parseUrls(SysDict dict) {
        if (dict == null || dict.getLabel() == null || dict.getLabel().isBlank()) {
            return Collections.emptyList();
        }
        String label = dict.getLabel().trim();
        // 新格式：JSON 数组
        if (label.startsWith("[")) {
            try {
                return objectMapper.readValue(label, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("解析 webhook urls 失败: {}", e.getMessage());
                return Collections.emptyList();
            }
        }
        // 旧格式：单个 URL 字符串，兼容迁移
        return List.of(label);
    }
}
