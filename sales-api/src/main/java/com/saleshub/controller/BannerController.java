package com.saleshub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.common.BusinessException;
import com.saleshub.common.FileUploadUtil;
import com.saleshub.common.Result;
import com.saleshub.entity.SysDict;
import com.saleshub.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/banner")
@RequiredArgsConstructor
public class BannerController {

    private final SysDictMapper dictMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** 获取 banner 配置（所有登录用户） */
    @GetMapping
    public Result<Map<String, String>> get() {
        SysDict dict = dictMapper.selectOne(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "banner")
                .eq(SysDict::getCode, "bg_image")
        );
        String url = dict != null ? dict.getLabel() : "";
        return Result.ok(Map.of("imageUrl", url));
    }

    /** 上传 banner 背景图（仅管理员） */
    @PostMapping("/image")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("上传Banner背景图: fileName={}", file.getOriginalFilename());
        Path dir = Paths.get(uploadDir, "banner").toAbsolutePath();
        String fileName = FileUploadUtil.save(file, dir);
        String url = "/uploads/banner/" + fileName;

        // upsert
        SysDict dict = dictMapper.selectOne(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "banner")
                .eq(SysDict::getCode, "bg_image")
        );
        if (dict == null) {
            dict = new SysDict();
            dict.setType("banner");
            dict.setCode("bg_image");
            dict.setSort(0);
            dict.setStatus("active");
        }
        dict.setLabel(url);
        if (dict.getId() == null) dictMapper.insert(dict);
        else dictMapper.updateById(dict);

        return Result.ok(Map.of("imageUrl", url));
    }

    /** 清除 banner 背景图（仅管理员） */
    @DeleteMapping("/image")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> clearImage() {
        log.info("清除Banner背景图");
        SysDict dict = dictMapper.selectOne(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "banner")
                .eq(SysDict::getCode, "bg_image")
        );
        if (dict != null) {
            dict.setLabel("");
            dictMapper.updateById(dict);
        }
        return Result.ok();
    }

    /** 获取跑马灯自定义文字 */
    @GetMapping("/marquee")
    public Result<Map<String, String>> getMarquee() {
        SysDict dict = dictMapper.selectOne(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "banner")
                .eq(SysDict::getCode, "marquee_text")
        );
        String text = dict != null ? dict.getLabel() : "";
        return Result.ok(Map.of("text", text));
    }

    /** 保存跑马灯自定义文字（仅管理员） */
    @PutMapping("/marquee")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> saveMarquee(@RequestBody Map<String, String> body) {
        log.info("保存跑马灯文字");
        String text = body.getOrDefault("text", "").trim();
        SysDict dict = dictMapper.selectOne(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, "banner")
                .eq(SysDict::getCode, "marquee_text")
        );
        if (dict == null) {
            dict = new SysDict();
            dict.setType("banner");
            dict.setCode("marquee_text");
            dict.setSort(0);
            dict.setStatus("active");
        }
        dict.setLabel(text);
        if (dict.getId() == null) dictMapper.insert(dict);
        else dictMapper.updateById(dict);
        return Result.ok();
    }
}
