package com.saleshub.controller;

import com.saleshub.common.FileUploadUtil;
import com.saleshub.common.Result;
import com.saleshub.dto.LoginRequest;
import com.saleshub.dto.LoginResponse;
import com.saleshub.dto.ProfileUpdateRequest;
import com.saleshub.entity.SysUser;
import com.saleshub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录: username={}", request.getUsername());
        return Result.ok(authService.login(request));
    }

    @GetMapping("/me")
    public Result<SysUser> me(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(authService.getCurrentUser(userId));
    }

    @PutMapping("/profile")
    public Result<SysUser> updateProfile(Authentication auth, @RequestBody ProfileUpdateRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(authService.updateProfile(userId, request));
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(Authentication auth, @RequestParam("file") MultipartFile file) throws IOException {
        log.info("上传头像: userId={}, fileName={}", auth.getPrincipal(), file.getOriginalFilename());
        Path dir = Paths.get(uploadDir, "avatars").toAbsolutePath();
        String fileName = FileUploadUtil.save(file, dir);
        String url = "/uploads/avatars/" + fileName;

        Long userId = (Long) auth.getPrincipal();
        authService.updateProfile(userId, new ProfileUpdateRequest() {{ setAvatar(url); }});
        return Result.ok(Map.of("url", url));
    }
}
