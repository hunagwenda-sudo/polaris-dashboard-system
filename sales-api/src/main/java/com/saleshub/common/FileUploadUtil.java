package com.saleshub.common;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class FileUploadUtil {

    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB
    private static final String ALLOWED_EXT_PATTERN = "\\.(jpg|jpeg|png|gif|webp)";

    public static String save(MultipartFile file, Path dir) throws IOException {
        if (file == null || file.isEmpty()) throw new BusinessException("请选择文件");

        // Content-Type check
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只允许上传图片文件");
        }

        // Size check (application layer, independent of container limit)
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("文件大小不能超过 2MB");
        }

        // Extension check — use only the last segment to prevent double-extension attacks (e.g. evil.php.jpg)
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf(".")).toLowerCase()
                : ".jpg";
        if (!ext.matches(ALLOWED_EXT_PATTERN)) {
            throw new BusinessException("只允许 jpg/png/gif/webp 格式");
        }

        // UUID filename — no path traversal possible
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        Files.createDirectories(dir);
        Path dest = dir.resolve(fileName);
        try (var in = file.getInputStream()) {
            Files.copy(in, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }
}
