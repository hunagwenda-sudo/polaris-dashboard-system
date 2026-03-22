package com.saleshub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FeishuNotifyService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.site-url:}")
    private String siteUrl;

    /**
     * 发送飞书群消息（支持多个 webhook 地址）
     * @param webhookUrls 飞书机器人 webhook 地址列表
     * @param title 消息标题
     * @param names 未填报人员名单
     * @param entryPath 录入页面路径，如 /data-entry 或 /service-entry
     */
    public void sendUnfilledReminder(List<String> webhookUrls, String title, List<String> names, String entryPath) {
        if (webhookUrls == null || webhookUrls.isEmpty()) return;
        if (names == null || names.isEmpty()) return;

        String nameList = String.join("、", names);
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n\n");
        sb.append("未填报人员（共").append(names.size()).append("人）：\n");
        sb.append(nameList).append("\n\n");
        sb.append("请尽快填报，谢谢！");

        if (siteUrl != null && !siteUrl.isBlank() && entryPath != null) {
            String link = siteUrl.endsWith("/") ? siteUrl.substring(0, siteUrl.length() - 1) : siteUrl;
            sb.append("\n\n👉 点击录入：").append(link).append(entryPath);
        }

        String content = sb.toString();

        Map<String, Object> body = Map.of(
            "msg_type", "text",
            "content", Map.of("text", content)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        for (String url : webhookUrls) {
            if (url == null || url.isBlank()) continue;
            try {
                restTemplate.postForEntity(url, request, String.class);
                log.info("飞书提醒发送成功: webhook={}, {} 人未填报", url.substring(0, Math.min(url.length(), 60)) + "...", names.size());
            } catch (Exception e) {
                log.error("飞书提醒发送失败: webhook={}, error={}", url.substring(0, Math.min(url.length(), 60)) + "...", e.getMessage());
            }
        }
    }
}
