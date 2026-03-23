package com.saleshub.controller;

import com.saleshub.common.FileUploadUtil;
import com.saleshub.common.Result;
import com.saleshub.entity.SysDict;
import com.saleshub.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** 按类型获取字典（所有登录用户可用） */
    @GetMapping("/{type}")
    public Result<List<SysDict>> listByType(@PathVariable String type, @RequestParam(required = false) Boolean all) {
        if (Boolean.TRUE.equals(all)) {
            return Result.ok(dictService.listAllByType(type));
        }
        return Result.ok(dictService.listByType(type));
    }

    /** 管理接口：仅管理员 */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysDict>> listAll() {
        return Result.ok(dictService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysDict> create(@RequestBody SysDict dict) {
        log.info("创建字典项: type={}, code={}", dict.getType(), dict.getCode());
        return Result.ok(dictService.create(dict));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysDict> update(@PathVariable Long id, @RequestBody SysDict dict) {
        log.info("更新字典项: id={}", id);
        return Result.ok(dictService.update(id, dict));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        log.info("删除字典项: id={}", id);
        dictService.delete(id);
        return Result.ok();
    }

    /** 上传渠道 icon */
    @PostMapping("/{id}/icon")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, String>> uploadIcon(@PathVariable Long id,
                                                   @RequestParam("file") MultipartFile file) throws Exception {
        Path dir = Paths.get(uploadDir, "icons");
        String fileName = FileUploadUtil.save(file, dir);
        String iconUrl = "/uploads/icons/" + fileName;
        dictService.update(id, new SysDict() {{ setIconUrl(iconUrl); }});
        return Result.ok(Map.of("iconUrl", iconUrl));
    }

    /** 删除渠道 icon */
    @DeleteMapping("/{id}/icon")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteIcon(@PathVariable Long id) {
        dictService.clearIcon(id);
        return Result.ok();
    }
}
